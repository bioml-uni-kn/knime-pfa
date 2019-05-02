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
package de.unikn.knime.pfa.data.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.InvalidSettingsException;

import com.opendatagroup.hadrian.data.PFAArray;

import de.unikn.knime.pfa.data.KnimeAvroConverterRegistry;
import de.unikn.knime.pfa.data.PFADataTypeConverter;

/**
 * Converter class for List variables to convert from KNIME Data Type to Avro
 * and vice versa.
 * 
 * @author Mete Can Akar
 *
 */

public class ListPFAConverter implements PFADataTypeConverter {
    @Override
    public Type getAvroType() {
        return Schema.Type.ARRAY;
    }

    @Override
    public Class<? extends DataValue> getDataValueClass() {
        return ListDataValue.class;
    }

    @Override
    public DataType getDataType(final Schema schema) throws InvalidSettingsException {
        return ListCell
                .getCollectionType(KnimeAvroConverterRegistry.getInstance().getDataType(schema.getElementType()));
    }

    @Override
    public Schema getAvroSchema(final DataType dt) throws InvalidSettingsException {
        Schema s = KnimeAvroConverterRegistry.getInstance().getSchema(dt.getCollectionElementType());
        return Schema.createUnion(Arrays.asList(Schema.createArray(s), Schema.create(Schema.Type.NULL)));
    }

    @Override
    public Function<DataCell, Object> createCellToPFAMapper(final DataType dt, final Schema target)
            throws InvalidSettingsException {
        Function<DataCell, Object> elementMapper = KnimeAvroConverterRegistry.getInstance()
                .createCellToPFAMapper(dt.getCollectionElementType(), target.getElementType());
        return (cell) -> {
            if (cell.isMissing()) {
                return null;
            }
            ListDataValue list = (ListDataValue) cell;
            PFAArray<Object> arr = PFAArray.empty(list.size());
            for (int i = 0; i < list.size(); i++) {
                arr.add(elementMapper.apply(list.get(i)));
            }
            return arr;
        };
    }

    @Override
    public Function<Object, DataCell> createPFAToCellMapper(final Schema schema) throws InvalidSettingsException {
        // Function for converting individual elements of the list
        final Function<Object, DataCell> elementMapper = KnimeAvroConverterRegistry.getInstance()
                .createPFAToCellMapper(schema.getElementType());
        // Function for the whole list
        return (o) -> {
            if (o == null) {
                return DataType.getMissingCell();
            }
            ArrayList<DataCell> cells = new ArrayList<>();
            PFAArray<?> arr = (PFAArray<?>) o;
            for (int i = 0; i < arr.size(); i++) {
                cells.add(elementMapper.apply(arr.get(i)));
            }
            return CollectionCellFactory.createListCell(cells);
        };
    }
}
