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
package de.unikn.pfa.node.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.json.JsonException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;

import de.unikn.pfa.node.port.PFAPortObject;

/**
 * Reads the PFA model function and outputs it as a {@link PFAPortObject}.
 * 
 * @author Mete Can Akar
 */
public class PFAReaderNodeModel extends NodeModel {

    /**
     * the settings key which is used to retrieve and store the settings (from
     * the dialog or from a settings file) (package visibility to be usable from
     * the dialog).
     */
    static final String CONFIGNAME_PFA = "PfaFilePath";

    /**
     * Creates a SettingsModelString object for the PFA file.
     * 
     * @return A SettingsStringModel for storing the PFA file path
     */
    public static SettingsModelString createPfaPathModel() {
        return new SettingsModelString(CONFIGNAME_PFA, null);
    }

    // Holds the setting for the PFA file
    private final SettingsModelString m_pfa = createPfaPathModel();

    /**
     * Constructor for the node model.
     */
    protected PFAReaderNodeModel() {
        super(new PortType[] {}, new PortType[] {PFAPortObject.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        String file = m_pfa.getStringValue();
        PFAPortObject o = null;

        CheckUtils.checkSourceFile(file);
        URL url = FileUtil.toURL(file);
        String res = FileUtil.resolveToPath(url).toString();
        String extension = FilenameUtils.getExtension(res);
        FileInputStream fis = new FileInputStream(res);
        
        if (extension.equals("json")) {
            o = PFAPortObject.loadFromJson(fis);
        } else if (extension.equals("yaml")) {
            o = PFAPortObject.loadFromYaml(fis);
        } else {
            try {
                o = PFAPortObject.loadFromJson(fis);
            } catch (JsonException e1) {
                try {
                    o = PFAPortObject.loadFromYaml(fis);
                } catch (IOException e2) {
                    throw new InvalidSettingsException("The given file cannot be loaded neither as JSON nor YAML.");
                }
            }
        }

        return new PortObject[] {o};
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
        String file = m_pfa.getStringValue();
        if (StringUtils.isEmpty(file)) {
            throw new InvalidSettingsException("No PFA file selected");
        }

        String warning = CheckUtils.checkSourceFile(file);
        if (warning != null) {
            setWarningMessage(warning);
        }
        return new PortObjectSpec[] {null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_pfa.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pfa.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pfa.validateSettings(settings);
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
