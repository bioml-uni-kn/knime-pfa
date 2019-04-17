/*
 * ------------------------------------------------------------------------
 *  Copyright by University of Konstanz, Germany
 *  Website: https://www.bison.uni-konstanz.de
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 * 
 * History
 *   Dec 11, 2018 (fillbrunn): created
 */
package org.knime.ext.pfa.node.predictor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.MutableInteger;
import org.knime.ext.pfa.data.KnimeAvroConverterRegistry;
import org.knime.ext.pfa.node.port.PFAPortObject;
import org.knime.ext.pfa.node.port.PFAPortObjectSpec;

import com.opendatagroup.hadrian.ast.Method;
import com.opendatagroup.hadrian.data.PFAMap;
import com.opendatagroup.hadrian.data.PFARecord;
import com.opendatagroup.hadrian.jvmcompiler.PFAEmitEngine;
import com.opendatagroup.hadrian.jvmcompiler.PFAEngine;

import scala.runtime.BoxedUnit;

/**
 * PFA Predictor node model for a node executing PFA scoring engines.
 * 
 * @author Mete Can Akar, Alexander Fillbrunn
 */
public class PFAPredictorNodeModel extends NodeModel {

    private NodeLogger m_logger = NodeLogger.getLogger(PFAPredictorNodeModel.class);
    
    private static final String CFG_OUTPUT_COLUMN = "colNameStringModel";
    private static final String CFG_INPUT_COLUMN = "inputColumn";

    /**
     * Creates a new settings object holding the output column name.
     * @return SettingsModelString object for the column name setting.
     */
    public static SettingsModelString createColumnNameModel() {
        return new SettingsModelString(CFG_OUTPUT_COLUMN, "Prediction");
    }

    /**
     * Creates a new settings object holding the input column name.
     * @return SettingsModelColumnName for the input column
     */
    public static SettingsModelString createInputColumnModel() {
        return new SettingsModelString(CFG_INPUT_COLUMN, null);
    }
    
    private final SettingsModelString m_colName = createColumnNameModel();
    private final SettingsModelString m_inputCol = createInputColumnModel();
    
    /**
     * Constructor for the node model.
     */
    protected PFAPredictorNodeModel() {
        super(new PortType[] {PFAPortObject.TYPE, BufferedDataTable.TYPE}, new PortType[] {BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        PFAPortObject pfa = (PFAPortObject) inData[0];
        BufferedDataTable table = (BufferedDataTable) inData[1];

        PFAEngine<Object, Object> engine = pfa.createEngine();

        BufferedDataTable result;
        if (engine.outputType().schema().getType() == Type.MAP) {
            // When the result is a map, results need to be cached and the table created after all key are known
            result = runEngineMap(table, engine, exec);
        } else {
            // Otherwise we can create the table on-the-fly
            result = runEngine(table, engine, exec);
        }
        
        return new PortObject[] {result};
    }
    
    private BufferedDataTable runEngineMap(final BufferedDataTable table, final PFAEngine<Object, Object> engine,
            final ExecutionContext exec) throws InvalidSettingsException, CanceledExecutionException {
        
        // This set holds all encountered keys of the output maps
        final Set<String> keys = new LinkedHashSet<String>();
        // Mapper for converting the input cells to Avro types
        final Function<DataRow, Object> inputMapper = createInputMapper(table.getDataTableSpec(), engine.inputClass(),
                engine.inputType().schema());
        
        Schema valueSchema = engine.outputType().schema().getValueType();
        if (valueSchema.getType() == Type.MAP || valueSchema.getType() == Type.RECORD) {
            throw new InvalidSettingsException("Nested Map and Record types are currently not supported.");
        }
        
        DataType knimeValueType = KnimeAvroConverterRegistry.getInstance().getDataType(valueSchema);
        Function<Object, DataCell> valueTypeMapper = KnimeAvroConverterRegistry.getInstance()
                                                        .createPFAToCellMapper(valueSchema);
        
        // We first run the scoring engine and save the results because we need to know the keys of the output maps
        final Map<String, PFAMap<?>> resultCache = new LinkedHashMap<>();
        
        // Emit engines do not produce one output per input, so we have to hook into the emit callback
        if (engine.method() == Method.EMIT()) {
            MutableInteger outCount = new MutableInteger(-1);
            PFAEmitEngine<Object, Object> emitEngine = (PFAEmitEngine<Object, Object>) engine;
            emitEngine.emit_$eq(new EmitCallback() {
                @Override
                public BoxedUnit apply(final Object res) {
                    PFAMap<?> map = (PFAMap<?>)res;
                    addKeysToSet(map, keys);
                    resultCache.put("Row" + outCount.inc(), map);
                    return null;
                }
            });
        }
        
        exec.setMessage(() -> "Running initializing PFA code.");
        engine.begin();
        exec.setMessage(() -> "Running prediction PFA code.");
        int count = 0;

        for (DataRow row : table) {
            exec.checkCanceled();
            exec.setProgress((double) count++ / table.size());
            Object pfaInput = inputMapper.apply(row);
            PFAMap<?> result = (PFAMap<?>)engine.action(pfaInput);
            addKeysToSet(result, keys);
            
            if (engine.method() != Method.EMIT()) {
                resultCache.put(row.getKey().getString(), result);
            }
        }

        exec.setMessage(() -> "Running post-predictions PFA code.");
        engine.end();
        
        // Now that the results are cached, we can create a BufferedDataTable from it
        DataTableSpec outputSpec = createMapOutputSpec(keys, knimeValueType);
        BufferedDataContainer output = exec.createDataContainer(outputSpec);
        
        for (Entry<String, PFAMap<?>> map : resultCache.entrySet()) {
            List<DataCell> cells = keys.stream().map(k -> {
                Object v = map.getValue().get(k);
                return (v == null) ? DataType.getMissingCell() : valueTypeMapper.apply(v);
            }).collect(Collectors.toList());
            output.addRowToTable(new DefaultRow(new RowKey(map.getKey()), cells));
        }
        output.close();
        return output.getTable();
    }
    
    private void addKeysToSet(final PFAMap<?> map, final Set<String> set) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            set.add(entry.getKey());
        }
    }
    
    private DataTableSpec createMapOutputSpec(final Set<String> keys, final DataType valueType) {
        DataTableSpecCreator creator = new DataTableSpecCreator();
        for (String k : keys) {
            creator.addColumns(new DataColumnSpecCreator(k, valueType).createSpec());
        }
        return creator.createSpec();
    }
    
    private BufferedDataTable runEngine(final BufferedDataTable table, final PFAEngine<Object, Object> engine,
            final ExecutionContext exec) throws InvalidSettingsException, CanceledExecutionException {
        
        // Mapper for converting the input cells to Avro types
        Function<DataRow, Object> inputMapper = createInputMapper(table.getDataTableSpec(), engine.inputClass(),
                engine.inputType().schema());
        
        // Create the output spec from the engine's info on the output type
        DataTableSpec outputSpec = KnimeAvroConverterRegistry.getInstance()
                .dataTableSpecFromSchema(engine.outputType().schema(), m_colName.getStringValue());
        // Container for the predictions
        BufferedDataContainer output = exec.createDataContainer(outputSpec);
        
        // Mapper for converting the output to a KNIME table row
        BiFunction<RowKey, Object, DataRow> outputMapper = createOutputMapper(engine);

        if (engine.method() == Method.EMIT()) {
            MutableInteger outCount = new MutableInteger(-1);
            PFAEmitEngine<Object, Object> emitEngine = (PFAEmitEngine<Object, Object>) engine;
            emitEngine.emit_$eq(new EmitCallback() {
                @Override
                public BoxedUnit apply(final Object res) {
                    output.addRowToTable(outputMapper.apply(new RowKey("Row" + outCount.inc()), res));
                    return null;
                }
            });
        }

        exec.setMessage(() -> "Running initializing PFA code.");
        engine.begin();
        exec.setMessage(() -> "Running prediction PFA code.");
        int count = 0;

        for (DataRow row : table) {
            exec.checkCanceled();
            exec.setProgress((double) count++ / table.size());
            Object pfaInput = inputMapper.apply(row);
            Object result = engine.action(pfaInput);
            if (engine.method() != Method.EMIT()) {
                output.addRowToTable(outputMapper.apply(row.getKey(), result));
            }
        }

        exec.setMessage(() -> "Running post-predictions PFA code.");
        engine.end();

        output.close();
        return output.getTable();
    }

    /**
     * Creates the output mapper.
     * 
     * @param engine
     *            PFAEngine
     * @return DefaultRow
     * @throws InvalidSettingsException
     */
    private BiFunction<RowKey, Object, DataRow> createOutputMapper(final PFAEngine<?, ?> engine)
            throws InvalidSettingsException {
        if (engine.outputType().schema().getType().equals(Schema.Type.RECORD)) {
            final HashMap<String, Function<Object, DataCell>> fieldMappers = new HashMap<>();
            for (Field f : engine.outputType().schema().getFields()) {
                fieldMappers.put(f.name(), KnimeAvroConverterRegistry.getInstance().createPFAToCellMapper(f.schema()));
            }
            return (key, o) -> {
                PFARecord r = (PFARecord) o;
                String[] fieldNames = r.fieldNames();
                DataCell[] cells = new DataCell[r.numFields()];
                for (int i = 0; i < cells.length; i++) {
                    cells[i] = fieldMappers.get(fieldNames[i]).apply(r.get(i));
                }
                return new DefaultRow(key, cells);
            };
        } else {
            final Function<Object, DataCell> cellMapper = KnimeAvroConverterRegistry.getInstance()
                    .createPFAToCellMapper(engine.outputType().schema());
            return (key, o) -> new DefaultRow(key, cellMapper.apply(o));
        }
    }

    /**
     * Create the input mapper.
     * 
     * @param spec the DataTableSpec describing the input
     * @param inputClass the input class of the PFA document
     * @param schema the Avro Schema describing the PFA document's input
     * @return A function mapping data rows to objects for consumption of the PFA scoring engine
     * @throws InvalidSettingsException when the input mappers cannot be created with the registered converters
     */
    private Function<DataRow, Object> createInputMapper(final DataTableSpec spec, final Class<?> inputClass,
            final Schema schema) throws InvalidSettingsException {
        if (schema.getType().equals(Schema.Type.RECORD)) {
            // Find registered column mappers for each column
            final HashMap<String, Function<DataCell, Object>> columnMappers = new HashMap<>();
            for (int i = 0; i < spec.getNumColumns(); i++) {
                DataColumnSpec cs = spec.getColumnSpec(i);
                Schema colSchema = schema.getField(cs.getName()).schema();
                Function<DataCell, Object> mapper = KnimeAvroConverterRegistry.getInstance()
                        .createCellToPFAMapper(cs.getType(), colSchema);
                columnMappers.put(cs.getName(), mapper);
            }
            // Function that creates a PFA record from a row
            return row -> {
                PFARecord rec;
                try {
                    rec = (PFARecord) inputClass.newInstance();
                } catch (Exception e) {
                    m_logger.error(e);
                    return null;
                }
                for (String fn : rec.fieldNames()) {
                    rec.put(fn, columnMappers.get(fn).apply(row.getCell(spec.findColumnIndex(fn))));
                }
                return rec;
            };
        } else if (schema.getType().equals(Schema.Type.MAP)) {
            // We turn each row into a map where the column names are the keys and the cell contents the values
            Map<String, Function<DataCell, Object>> colMappers = new HashMap<>();
            for (String col : spec.getColumnNames()) {
                colMappers.put(col, KnimeAvroConverterRegistry.getInstance()
                        .createCellToPFAMapper(spec.getColumnSpec(col).getType(), schema.getValueType()));
            }
            // Function that creates a PFA map from a row
            return row -> {
                PFAMap<Object> map = PFAMap.empty();
                for (String col : spec.getColumnNames()) {
                    map.put(col, colMappers.get(col).apply(row.getCell(spec.findColumnIndex(col))));
                }
                return map;
            };
        } else {
            // For primitive types we need to find the column to process
            String columnName = m_inputCol.getStringValue();
            if (columnName == null) {
                int idx = -1;
                Class<? extends DataValue> fit = KnimeAvroConverterRegistry.getInstance().getDataType(schema)
                        .getPreferredValueClass();
                for (int i = 0; i < spec.getNumColumns(); i++) {
                    DataType dt = spec.getColumnSpec(i).getType();
                    if (dt.isCompatible(fit)) {
                        idx = i;
                        break;
                    }
                }
                if (idx == -1) {
                    throw new InvalidSettingsException(
                            "No fitting column for type " + schema.getType().getName() + " found in input table");
                }
                setWarningMessage("The PFA model requires a single value as input. Using first matching column: \""
                        + spec.getColumnNames()[idx] + "\"");
                columnName = spec.getColumnNames()[idx];
                m_inputCol.setStringValue(columnName);
            }
            
            final int index = spec.findColumnIndex(columnName);
            final Function<DataCell, Object> mapper = KnimeAvroConverterRegistry.getInstance()
                    .createCellToPFAMapper(spec.getColumnSpec(index).getType(), schema);
            return row -> mapper.apply(row.getCell(index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // No-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        PFAPortObjectSpec pfaSpec = (PFAPortObjectSpec) inSpecs[0];
        DataTableSpec dtSpec = (DataTableSpec) inSpecs[1];

        if (!KnimeAvroConverterRegistry.getInstance().isApplicable(pfaSpec.getInputSchema(), dtSpec)) {
            throw new InvalidSettingsException("The PFA input schema is not compatible with the data table.");
        }

        DataTableSpec outSpec = KnimeAvroConverterRegistry.getInstance()
                                .dataTableSpecFromSchema(pfaSpec.getOutputSchema(), m_colName.getStringValue());
        
        return new PortObjectSpec[] {outSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colName.saveSettingsTo(settings);
        m_inputCol.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colName.loadSettingsFrom(settings);
        m_inputCol.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colName.validateSettings(settings);
        m_inputCol.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // No-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // No-op
    }

}
