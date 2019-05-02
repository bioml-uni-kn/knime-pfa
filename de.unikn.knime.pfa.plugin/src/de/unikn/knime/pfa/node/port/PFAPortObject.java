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
package de.unikn.knime.pfa.node.port;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.swing.JComponent;

import org.codehaus.jackson.map.ObjectMapper;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.yaml.snakeyaml.Yaml;

import com.opendatagroup.antinous.pfainterface.PFAEngineFactory;
import com.opendatagroup.hadrian.jvmcompiler.PFAEngine;

/**
 * PortObject storing PFA documents as JSON.
 * 
 * @author Mete Can Akar
 */
public class PFAPortObject implements PortObject {

    /** Convenience accessor for the port type. */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PFAPortObject.class);

    private JsonObject m_pfaDocument;
    private PFAPortObjectSpec m_spec;

    /**
     * Creates a PFAPortObject instance.
     * 
     * @param pfaDocument the JsonObject representing the PFA document
     */
    public PFAPortObject(final JsonObject pfaDocument) {
        // Document to be instantiated
        m_pfaDocument = pfaDocument;
        // spec of the document
        m_spec = PFAPortObjectSpec.fromJson(pfaDocument);
    }

    /**
     * Creates a new PFA scoring engine from the document in this port object.
     * 
     * @return a <code>PFAEngine</code> instance
     */
    public PFAEngine<Object, Object> createEngine() {
        PFAEngineFactory factory = new PFAEngineFactory();
        return factory.engineFromJson(m_pfaDocument.toString());
    }

    @Override
    public String getSummary() {
        StringBuffer sb = new StringBuffer();
        sb.append("PFA Document mapping ");
        sb.append(m_spec.getInputSchema().getType().toString());
        sb.append(" to ");
        sb.append(m_spec.getOutputSchema().getType().toString());
        return sb.toString();
    }

    /**
     * Get the PFA document as string.
     * 
     * @return The pfa document.
     */
    public String getJson() {
        return m_pfaDocument.toString();
    }

    /**
     * Get the PFA document as a JsonObject.
     * 
     * @return The PFA object.
     */
    public JsonObject getJsonObject() {
        return m_pfaDocument;
    }

    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    @Override
    public JComponent[] getViews() {
        return new JComponent[] {new PFAPortObjectView(this)}; // null
    }

    /**
     * Writes the PFA document to the output stream.
     * 
     * @param out the stream to save the document into
     * @throws IOException when the document cannot be saved.
     */
    public void saveTo(final OutputStream out) throws IOException {
        out.write(m_pfaDocument.toString().getBytes());
    }

    /**
     * Loads the PFAPortObject from an input stream.
     * 
     * @param in the InputStream to load the JSON from
     * @return a PFAPortObject that is initialized from the JSON in the given stream
     * @throws IOException when the document cannot be loaded
     * @throws JsonException when the document's JSON cannot be parsed
     */
    public static PFAPortObject loadFromJson(final InputStream in) throws IOException, JsonException {
        return new PFAPortObject(Json.createReader(in).readObject());
    }
    
    /**
     * Reads a YAML file from an input stream.
     * @param in the InputStream to load the YAML from
     * @return a PFAPortObject that is initialized from the YAML in the given stream
     * @throws IOException when the document cannot be loaded
     */
    public static PFAPortObject loadFromYaml(final InputStream in) throws IOException {
        // Since we store the PFA as JSON internally, we use SnakeYAML here to convert the YAML to JSON
        Yaml yaml = new Yaml();
        Object json = yaml.load(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectMapper().writeValue(bos, json);
        return loadFromJson(new ByteArrayInputStream(bos.toByteArray()));
    }
}
