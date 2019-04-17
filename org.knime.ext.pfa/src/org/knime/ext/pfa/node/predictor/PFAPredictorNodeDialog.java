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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.avro.Schema.Type;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.pfa.data.KnimeAvroConverterRegistry;
import org.knime.ext.pfa.node.port.PFAPortObjectSpec;

/**
 * PFA predictor node dialog.
 * 
 * @author Alexander Fillbrunn
 */
public class PFAPredictorNodeDialog extends NodeDialogPane {
    /**
     * The model that stores the value for the column name.
     */
    private SettingsModelString m_colNameStringModel;
    private SettingsModelString m_inputColModel;
    private JPanel m_main;
    private DialogComponentColumnNameSelection m_inputDialogComp;
    private DialogComponentString m_outputComp;
    /**
     * Creates a new instance of {@link PFAPredictorNodeDialog}.
     */
    protected PFAPredictorNodeDialog() {
        super();
        m_colNameStringModel = PFAPredictorNodeModel.createColumnNameModel();
        m_inputColModel = PFAPredictorNodeModel.createInputColumnModel();
        
        m_main = new JPanel();
        m_main.setLayout(new BoxLayout(m_main, BoxLayout.Y_AXIS));
        addTab("General", m_main);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        PFAPortObjectSpec spec = (PFAPortObjectSpec)specs[0];
        Type inType = spec.getInputSchema().getType();
        Type outType = spec.getOutputSchema().getType();
        
        m_main.removeAll();
        boolean hasSettings = false;
        
        // We only show the input column selection if the input is a single value
        if (!(inType.equals(Type.RECORD) || inType.equals(Type.MAP))) {
            DataType dt;
            try {
                dt = KnimeAvroConverterRegistry.getInstance().getDataType(spec.getInputSchema());
            } catch (InvalidSettingsException e) {
                throw new NotConfigurableException("Cannot find converter for input type " + inType.toString());
            }
            m_inputDialogComp = new DialogComponentColumnNameSelection(
                    m_inputColModel, "Input column", 1, true, false, dt.getPreferredValueClass());
            m_inputDialogComp.loadSettingsFrom(settings, specs);
            m_main.add(m_inputDialogComp.getComponentPanel());
            hasSettings = true;
        }
        // The output column name can only be chosen if it is a single value
        if (!(outType.equals(Type.RECORD) || outType.equals(Type.MAP))) {
            m_outputComp = new DialogComponentString(m_colNameStringModel, "Output Column Name:");
            m_outputComp.loadSettingsFrom(settings, specs);
            m_main.add(m_outputComp.getComponentPanel());
            hasSettings = true;
        }
        if (!hasSettings) {
            m_main.add(new JLabel("Input and output settings are read from the PFA scoring engine"));
        }
    }
    
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_colNameStringModel.saveSettingsTo(settings);
        m_inputColModel.saveSettingsTo(settings);
    }
}
