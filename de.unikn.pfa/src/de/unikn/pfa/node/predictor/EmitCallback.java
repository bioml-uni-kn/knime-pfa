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
package de.unikn.pfa.node.predictor;

import scala.Function1;
import scala.runtime.BoxedUnit;

/**
 * Callback function for the Emit-Method of a scoring engine.
 * The only method that needs to be implemented for our use cases is <code>apply</code>,
 * so this class masks all the others with default implementations.
 * 
 * @author Mete Can Akar
 *
 */
public abstract class EmitCallback implements Function1<Object, BoxedUnit> {

    @Override
    public abstract BoxedUnit apply(Object arg0);
    
    @Override
    public <A> Function1<Object, A> andThen(final Function1<BoxedUnit, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcDD$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcDF$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcDI$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcDJ$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcFD$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcFF$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcFI$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcFJ$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcID$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcIF$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcII$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcIJ$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcJD$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcJF$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcJI$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcJJ$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcVD$sp(final Function1<BoxedUnit, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcVF$sp(final Function1<BoxedUnit, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcVI$sp(final Function1<BoxedUnit, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcVJ$sp(final Function1<BoxedUnit, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcZD$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcZF$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcZI$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<Object, A> andThen$mcZJ$sp(final Function1<Object, A> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public double apply$mcDD$sp(final double arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public double apply$mcDF$sp(final float arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public double apply$mcDI$sp(final int arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public double apply$mcDJ$sp(final long arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public float apply$mcFD$sp(final double arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public float apply$mcFF$sp(final float arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public float apply$mcFI$sp(final int arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public float apply$mcFJ$sp(final long arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public int apply$mcID$sp(final double arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public int apply$mcIF$sp(final float arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public int apply$mcII$sp(final int arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public int apply$mcIJ$sp(final long arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public long apply$mcJD$sp(final double arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public long apply$mcJF$sp(final float arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public long apply$mcJI$sp(final int arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public long apply$mcJJ$sp(final long arg0) {
        // Intentionally left blank.
        return 0;
    }

    @Override
    public void apply$mcVD$sp(final double arg0) {
        // Intentionally left blank.
    }

    @Override
    public void apply$mcVF$sp(final float arg0) {
        // Intentionally left blank.
    }

    @Override
    public void apply$mcVI$sp(final int arg0) {
        // Intentionally left blank.
    }

    @Override
    public void apply$mcVJ$sp(final long arg0) {
        // Intentionally left blank.
    }

    @Override
    public boolean apply$mcZD$sp(final double arg0) {
        // Intentionally left blank.
        return false;
    }

    @Override
    public boolean apply$mcZF$sp(final float arg0) {
        // Intentionally left blank.
        return false;
    }

    @Override
    public boolean apply$mcZI$sp(final int arg0) {
        // Intentionally left blank.
        return false;
    }

    @Override
    public boolean apply$mcZJ$sp(final long arg0) {
        // Intentionally left blank.
        return false;
    }

    @Override
    public <A> Function1<A, BoxedUnit> compose(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcDD$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcDF$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcDI$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcDJ$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcFD$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcFF$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcFI$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcFJ$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcID$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcIF$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcII$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcIJ$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcJD$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcJF$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcJI$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcJJ$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, BoxedUnit> compose$mcVD$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, BoxedUnit> compose$mcVF$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, BoxedUnit> compose$mcVI$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, BoxedUnit> compose$mcVJ$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcZD$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcZF$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcZI$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

    @Override
    public <A> Function1<A, Object> compose$mcZJ$sp(final Function1<A, Object> arg0) {
        // Intentionally left blank.
        return null;
    }

}
