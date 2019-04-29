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

import java.util.Arrays;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;

/**
 * A converter interface for converting KNIME Data Types to Avro and vice versa.
 * 
 * @author Mete Can Akar
 *
 */
public interface PFADataTypeConverter {
    /**
     * Default method for setting the one way flag.
     * This is necessary if no exact corresponding type exists in KNIME.
     * Example: AVRO floats are converted to KNIME doubles, since KNIME does not have float
     * 
     * @return returns false
     */
    default boolean isOneWayPFAToKnime() {
        return false;
    }

    /**
     * Returns the Avro Type that this converter can handle.
     * 
     * @return an avro Type.
     */
    Schema.Type getAvroType();

    /**
     * Returns the KNIME data value class that this converter can handle.
     * 
     * @return data value class.
     */
    Class<? extends DataValue> getDataValueClass();

    /**
     * Default method for the interface to get the Avro Schema.
     * 
     * @param dt the KNIME data type to get the schema for
     * @return An Avro schema for the given data type
     * @throws InvalidSettingsException when no matching converter is registered
     */
    default Schema getAvroSchema(DataType dt) throws InvalidSettingsException {
        return Schema.createUnion(Arrays.asList(Schema.create(getAvroType()), Schema.create(Schema.Type.NULL)));
    }

    /**
     * Returns the KNIME data type of the schema.
     * 
     * @param schema the schema to get the KNIME data type for
     * @return The KNIME data type corresponding to the schema
     * @throws InvalidSettingsException
     *             when no matching converter is registered
     */
    DataType getDataType(Schema schema) throws InvalidSettingsException;

    /**
     * Creates a mapping function that turns KNIME cells into Avro objects of the target type.
     * 
     * @param dt The KNIME data type of the cells to convert
     * @param target The target schema
     * @return A function mapping data cells to Avro objects of the given target
     * @throws InvalidSettingsException When no matching converter is registered
     */
    Function<DataCell, Object> createCellToPFAMapper(DataType dt, Schema target) throws InvalidSettingsException;

    /**
     * Creates a mapping function that turns Avro objects into KNIME cells.
     * 
     * @param schema The schema of the input for the function
     * @return A function mapping objects to KNIME cells
     * @throws InvalidSettingsException when no matching converter is registered
     */
    Function<Object, DataCell> createPFAToCellMapper(Schema schema) throws InvalidSettingsException;
}
