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

import java.awt.BorderLayout;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * View for displaying the JSON representation of a {@link PFAPortObject}.
 * 
 * @author Mete Can Akar
 *
 */
public class PFAPortObjectView extends JComponent {

    private static final long serialVersionUID = 1L;
    private static final String COMPONENT_NAME = "PFA Document";

    /**
     * Constructor of the PFAPortObjectView.
     * 
     * @param obj the {@link PFAPortObject} whose content is displayed by the view
     */
    public PFAPortObjectView(final PFAPortObject obj) {
        setLayout(new BorderLayout());

        JTextArea json = new JTextArea();
        json.setEditable(false);
        setName(COMPONENT_NAME);

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName("JavaScript");
        if (scriptEngine == null) {
            json.setText(obj.getJson());
        } else {
            scriptEngine.put("jsonString", obj.getJson());
            try {
                scriptEngine.eval("result = JSON.stringify(JSON.parse(jsonString), null, 2)");
                json.setText((String) scriptEngine.get("result"));
            } catch (ScriptException e) {
                json.setText("The JSON could not be parsed");
            }
    
            JScrollPane scroller = new JScrollPane(json);
            add(scroller, BorderLayout.CENTER);
        }
    }
}
