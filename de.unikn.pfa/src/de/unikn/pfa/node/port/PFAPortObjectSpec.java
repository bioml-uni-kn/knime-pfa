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
package de.unikn.pfa.node.port;

import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.swing.JComponent;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 * Specifications for the PFAPortObject that are passed along node connections.
 * 
 * @author Mete Can Akar
 *
 */

public class PFAPortObjectSpec implements PortObjectSpec {

    private static final String INPUT_KEY = "input";
    private static final String OUTPUT_KEY = "output";

    /**
     * Key for the output schema.
     */
    protected static final String OUTPUT_SCHEMA_KEY = "outputSchema";
    /**
     * Key for the input schema.
     */
    protected static final String INPUT_SCHEMA_KEY = "inputSchema";

    /**
     * Creates a new instance of {@link PFAPortObjectSpec}.
     * 
     * @param inputSchema The input schema describing the input data of the PFA document.
     * @param outputSchema The output schema describing the output of the PFA document.
     */
    public PFAPortObjectSpec(final Schema inputSchema, final Schema outputSchema) {
        m_inputSchema = inputSchema;
        m_outputSchema = outputSchema;
    }

    private Schema m_inputSchema;
    private Schema m_outputSchema;

    /**
     * Get the input schema.
     * 
     * @return The input schema describing the input data of the PFA document.
     */
    public Schema getInputSchema() {
        return m_inputSchema;
    }

    /**
     * Get the output schema.
     * 
     * @return The output schema describing the output of the PFA document.
     */
    public Schema getOutputSchema() {
        return m_outputSchema;
    }

    @Override
    public JComponent[] getViews() {
        return null;
    }

    /**
     * Saves the PFAPortObjectSpec to an output stream.
     * 
     * @param out Stream that is used to persist objects of {@link PFAPortObjectSpec}
     * @throws IOException when the document cannot be saved.
     */
    public void saveTo(final PortObjectSpecZipOutputStream out) throws IOException {
        ZipEntry input = new ZipEntry(INPUT_SCHEMA_KEY);
        out.putNextEntry(input);
        out.write(m_inputSchema.toString().getBytes());
        ZipEntry output = new ZipEntry(OUTPUT_SCHEMA_KEY);
        out.putNextEntry(output);
        out.write(m_outputSchema.toString().getBytes());
    }

    /**
     * Loads the PFAPortObjectSpec from an input stream.
     * 
     * @param in The input stream to read from
     * @return A {@link PFAPortObjectSpec} describing input and output of the PFA document
     * @throws IOException when the document cannot be loaded from the stream
     */
    public static PFAPortObjectSpec loadFrom(final PortObjectSpecZipInputStream in) throws IOException {
        NonClosableInputStream noCloseIn = new NonClosableInputStream(in);
        ZipEntry entry = in.getNextEntry();
        assert entry.getName().equals(INPUT_SCHEMA_KEY);
        Parser p = new Schema.Parser();
        Schema input = p.parse(noCloseIn);
        entry = in.getNextEntry();
        assert entry.getName().equals(OUTPUT_SCHEMA_KEY);
        Schema output = p.parse(noCloseIn);
        return new PFAPortObjectSpec(input, output);
    }

    /**
     * Loads the {@link PFAPortObjectSpec} from a JsonObject.
     * 
     * @param pfaDocument PFA document whose spec will be loaded.
     * @return A {@link PFAPortObjectSpec} describing input and output of the PFA document
     */
    public static PFAPortObjectSpec fromJson(final JsonObject pfaDocument) {
        JsonValue input = pfaDocument.get(INPUT_KEY);
        JsonValue output = pfaDocument.get(OUTPUT_KEY);
        Parser p = new Schema.Parser();
        Schema inputSchema = p.parse(input.toString());
        Schema outputSchema = p.parse(output.toString());
        return new PFAPortObjectSpec(inputSchema, outputSchema);
    }
}
