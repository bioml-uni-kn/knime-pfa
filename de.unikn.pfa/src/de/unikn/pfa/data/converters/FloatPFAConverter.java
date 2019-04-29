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
package de.unikn.pfa.data.converters;

import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;

import de.unikn.pfa.data.PFADataTypeConverter;

/**
 * Converter class for Float variables to convert from KNIME Data Type to Avro
 * and vice versa.
 * 
 * @author Mete Can Akar
 *
 */
public class FloatPFAConverter implements PFADataTypeConverter {

    @Override
    public boolean isOneWayPFAToKnime() {
        return true;
    }

    @Override
    public Type getAvroType() {
        return Schema.Type.FLOAT;
    }

    @Override
    public Class<? extends DataValue> getDataValueClass() {
        return DoubleValue.class;
    }

    @Override
    public DataType getDataType(final Schema schema) {
        return DoubleCell.TYPE;
    }

    @Override
    public Function<DataCell, Object> createCellToPFAMapper(final DataType dt, final Schema target) {
        return (cell) -> {
            if (cell.isMissing()) {
                return null;
            }
            return (float) ((DoubleValue) cell).getDoubleValue();
        };
    }

    @Override
    public Function<Object, DataCell> createPFAToCellMapper(final Schema schema) {
        return (o) -> {
            if (o == null) {
                return DataType.getMissingCell();
            }
            return new DoubleCell(((Float) o).doubleValue());
        };
    }
}
