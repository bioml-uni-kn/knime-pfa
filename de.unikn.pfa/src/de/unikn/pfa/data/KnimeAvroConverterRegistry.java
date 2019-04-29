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
package de.unikn.pfa.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;

import de.unikn.pfa.data.converters.BooleanPFAConverter;
import de.unikn.pfa.data.converters.DoublePFAConverter;
import de.unikn.pfa.data.converters.FloatPFAConverter;
import de.unikn.pfa.data.converters.IntPFAConverter;
import de.unikn.pfa.data.converters.ListPFAConverter;
import de.unikn.pfa.data.converters.LongPFAConverter;
import de.unikn.pfa.data.converters.StringPFAConverter;

/**
 * Registry for converters between Avro and KNIME.
 * 
 * @author Alexander Fillbrunn
 *
 */
public final class KnimeAvroConverterRegistry {

    // This map can access converters by KNIME data value
    private Map<Class<? extends DataValue>, PFADataTypeConverter> m_knimeToAvro;
    // This map can access converters by AVRO data type
    private Map<Schema.Type, PFADataTypeConverter> m_avroToKnime;

    // Singleton instance
    private static KnimeAvroConverterRegistry registryInstance = new KnimeAvroConverterRegistry();

    private KnimeAvroConverterRegistry() {
        m_knimeToAvro = new HashMap<>();
        m_avroToKnime = new HashMap<>();

        // Register any new data converters here
        registerConverter(BooleanPFAConverter.class);
        registerConverter(DoublePFAConverter.class);
        registerConverter(FloatPFAConverter.class);
        registerConverter(IntPFAConverter.class);
        registerConverter(ListPFAConverter.class);
        registerConverter(LongPFAConverter.class);
        registerConverter(StringPFAConverter.class);
    }

    /**
     * Gets the singleton instance of this registry.
     * 
     * @return the <code>KnimeAvroConverterRegistry</code>
     */
    public static KnimeAvroConverterRegistry getInstance() {
        return registryInstance;
    }

    private void registerConverter(final Class<? extends PFADataTypeConverter> convClass) {
        try {
            PFADataTypeConverter conv = convClass.newInstance();
            if (!conv.isOneWayPFAToKnime()) {
                m_knimeToAvro.put(conv.getDataValueClass(), conv);
            }
            m_avroToKnime.put(conv.getAvroType(), conv);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("All converter classes " + "should be instantiable with a default constructor", e);
        }
    }

    private Schema unwrapUnion(final Schema schema) throws InvalidSettingsException {
        if (schema.getType().equals(Schema.Type.UNION)) {
            List<Schema> types = schema.getTypes();
            // Union types are used for nullable types in PFA. Other use cases
            // are currently not supported.
            if (types.size() > 2) {
                throw new InvalidSettingsException(
                        "Currently only unions of null and one other data type are supported");
            } else if (types.size() == 1) {
                // Does not make much sense, but doesn't hurt to support it...
                return types.get(0);
            } else {
                // We have to check that the NULL type is included
                boolean hasNull = false;
                Schema inner = null;
                for (Schema s : types) {
                    if (s.getType().equals(Schema.Type.NULL)) {
                        hasNull = true;
                    } else {
                        inner = s;
                    }
                }
                if (inner == null) {
                    throw new InvalidSettingsException("Unions only consisting of null are not supported");
                }
                if (!hasNull) {
                    throw new InvalidSettingsException(
                            "Currently only unions of null and one other data type are supported");
                }
                return inner;
            }
        }
        return schema;
    }

    /**
     * Creates a KNIME data type from an Avro schema.
     * 
     * @param schema the Avro schema
     * @return the KNIME data type corresponding to the schema
     * @throws InvalidSettingsException when no matching converter is registered
     */
    public DataType getDataType(final Schema schema) throws InvalidSettingsException {
        Schema innerSchema = unwrapUnion(schema);
        PFADataTypeConverter conv = m_avroToKnime.get(innerSchema.getType());
        if (conv == null) {
            throw new InvalidSettingsException(
                    "An converter for the Avro type " + innerSchema.getType().toString() + " is not registered");
        }
        return conv.getDataType(innerSchema);
    }
    
    /**
     * Creates a KNIME data value from an Avro schema.
     * 
     * @param schema the Avro schema of the type to get the KNIME data value for
     * @return the KNIME data value corresponding to the schema
     * @throws InvalidSettingsException when no matching converter is registered
     */
    public Class<? extends DataValue> getValueType(final Schema schema) throws InvalidSettingsException {
        Schema innerSchema = unwrapUnion(schema);
        PFADataTypeConverter conv = m_avroToKnime.get(innerSchema.getType());
        if (conv == null) {
            throw new InvalidSettingsException(
                    "An converter for the Avro type " + innerSchema.getType().toString() + " is not registered");
        }
        return conv.getDataValueClass();
    }

    /**
     * Creates an Avro schema from a KNIME data type.
     * 
     * @param dt the KNIME data type
     * @return the Avro schema corresponding to the KNIME data type
     * @throws InvalidSettingsException when no matching converter is registered
     */
    public Schema getSchema(final DataType dt) throws InvalidSettingsException {
        Class<? extends DataValue> val = dt.getPreferredValueClass();
        PFADataTypeConverter conv = m_knimeToAvro.get(val);
        if (conv == null) {
            throw new InvalidSettingsException(
                    "An converter for the KNIME data type " + dt.getName() + " is not registered");
        }
        return conv.getAvroSchema(dt);
    }

    /**
     * Creates a mapping function that turns Avro objects into KNIME cells.
     * 
     * @param schema the schema of the input for the function
     * @return a function mapping objects to KNIME cells
     * @throws InvalidSettingsException
     *             when no matching converter is registered
     */
    public Function<Object, DataCell> createPFAToCellMapper(final Schema schema) throws InvalidSettingsException {
        Schema innerSchema = unwrapUnion(schema);
        PFADataTypeConverter conv = m_avroToKnime.get(innerSchema.getType());
        if (conv == null) {
            throw new InvalidSettingsException(
                    "An converter for the Avro type " + innerSchema.getType().toString() + " is not registered");
        }
        return conv.createPFAToCellMapper(innerSchema);
    }

    /**
     * Creates a mapping function that turns KNIME cells into Avro objects of
     * the target type.
     * 
     * @param dt The data type of the cells to convert
     * @param target The target schema
     * @return a function mapping data cells to Avro objects of the given target type
     * @throws InvalidSettingsException when no matching converter is registered
     */
    public Function<DataCell, Object> createCellToPFAMapper(final DataType dt, final Schema target)
            throws InvalidSettingsException {
        PFADataTypeConverter conv = m_avroToKnime.get(target.getType());
        if (conv == null) {
            throw new InvalidSettingsException(
                    "An converter for the Avro data type " + target.getType().getName() + " is not registered");
        }
        return conv.createCellToPFAMapper(dt, target);
    }

    /**
     * Creates an Avro schema for a record from a KNIME data table spec.
     * 
     * @param spec the KNIME table spec to convert
     * @return an Avro schema with type RECORD matching the data table spec
     * @throws InvalidSettingsException when no matching converter is registered for a column type in the spec
     */
    public Schema schemaFromDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        List<Field> fields = new ArrayList<Field>();
        for (int i = 0; i < spec.getNumColumns(); i++) {
            DataColumnSpec colSpec = spec.getColumnSpec(i);
            fields.add(new Field(colSpec.getName(), getSchema(colSpec.getType()), null, null));
        }
        return Schema.createRecord(fields);
    }

    /**
     * Creates a data table spec from an Avro schema. If the schema of the PFA
     * document is a Record, then set the column names according to the column
     * names from the record of the PFA document. If the schema is not a record,
     * then set the column name by the user input from the dialog of the node.
     * 
     * @param schema the schema to get a KNIME data table spec from
     * @param columnName  the name of the output column if the output is a scalar
     * @return a DataTableSpec corresponding to the Avro schema or null if none can be created
     * @throws InvalidSettingsException when no matching converter is registered
     *                                  for a column type in the schema
     */
    public DataTableSpec dataTableSpecFromSchema(final Schema schema, final String columnName)
            throws InvalidSettingsException {
        DataTableSpecCreator specCreator = new DataTableSpecCreator();
        if (schema.getType() == Schema.Type.RECORD) {
            for (Field field : schema.getFields()) {
                DataColumnSpecCreator colSpecCreator = new DataColumnSpecCreator(field.name(),
                        getDataType(field.schema()));
                specCreator.addColumns(colSpecCreator.createSpec());
            }
        } else if (schema.getType() == Schema.Type.MAP) {
            return null;
        } else {
            specCreator.addColumns(new DataColumnSpecCreator(columnName, getDataType(schema)).createSpec());
        }

        return specCreator.createSpec();
    }

    /**
     * Checks whether a PFA document with the given input schema can be applied
     * on the given data table spec.
     * 
     * @param inputSchema the input schema of the PFA document
     * @param spec the data table spec to check against
     * @return true if a PFA document with the given schema can be applied on a
     *         table with the given spec, else false
     * @throws InvalidSettingsException when a type in the schema cannot be converted
     */
    public boolean isApplicable(final Schema inputSchema, final DataTableSpec spec) throws InvalidSettingsException {
        if (inputSchema.getType().equals(Schema.Type.RECORD)) {
            for (Field f : inputSchema.getFields()) {
                DataColumnSpec cs = spec.getColumnSpec(f.name());
                if (cs == null) {
                    return false;
                }
                PFADataTypeConverter conv = m_avroToKnime.get(f.schema().getType());
                if (conv == null) {
                    throw new IllegalArgumentException(
                            "An converter for the Avro type " + f.schema().getType().toString() + " is not registered");
                }

                if (!cs.getType().isCompatible(conv.getDataValueClass())) {
                    return false;
                }
            }
            return true;
        } else if (inputSchema.getType().equals(Schema.Type.MAP)) {
            Schema elementType = inputSchema.getValueType();
            for (int i = 0; i < spec.getNumColumns(); i++) {
                DataColumnSpec cs = spec.getColumnSpec(i);
                if (!cs.getType().isCompatible(getValueType(elementType))) {
                    return false;
                }
            }
            return true;
        } else {
            PFADataTypeConverter conv = m_avroToKnime.get(inputSchema.getType());
            if (conv == null) {
                throw new IllegalArgumentException("An converter for the Avro data type "
                        + inputSchema.getType().toString() + " is not registered");
            }
            return specContainsType(spec, conv.getDataValueClass());
        }
    }

    private boolean specContainsType(final DataTableSpec spec, final Class<? extends DataValue> valueClass) {
        for (int i = 0; i < spec.getNumColumns(); i++) {
            DataColumnSpec cs = spec.getColumnSpec(i);
            if (cs.getType().isCompatible(valueClass)) {
                return true;
            }
        }
        return false;
    }
}
