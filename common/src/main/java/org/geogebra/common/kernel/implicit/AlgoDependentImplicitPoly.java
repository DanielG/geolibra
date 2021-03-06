package org.geogebra.common.kernel.implicit;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.arithmetic.Equation;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.FunctionNVar;
import org.geogebra.common.kernel.arithmetic.FunctionalNVar;
import org.geogebra.common.kernel.arithmetic.Inspecting;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.arithmetic.Polynomial;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Feature;

/**
 * Dependent implicit polynomial (or line / conic)
 */
public class AlgoDependentImplicitPoly extends AlgoElement
		implements AlgoDependentImplicit {

	private ExpressionValue[][] coeff; // input
	private GeoElementND geoElement; // output (will be a implicitPoly, line or
									// conic)
	// private FunctionNVar[] dependentFromFunctions;
	private Set<FunctionNVar> dependentFromFunctions;

	/**
	 * Creates new implicit polynomial from equation. This algo may also return
	 * line or conic.
	 * 
	 * @param c
	 *            construction
	 * @param label
	 *            label
	 * @param equ
	 *            equation
	 */
	public AlgoDependentImplicitPoly(Construction c, Equation equ,
			boolean simplify) {
		super(c, false);
		Polynomial lhs = equ.getNormalForm();
		coeff = lhs.getCoeff();
		for (int i = 0; i < coeff.length; i++) {
			for (int j = 0; j < coeff[i].length; j++) {
				if (coeff[i][j] != null) {
					// find constant parts of input and evaluate them right now
					if (simplify && !coeff[i][j]
							.inspect(Inspecting.dynamicGeosFinder)) {
						coeff[i][j] = coeff[i][j]
								.evaluate(StringTemplate.defaultTemplate);
					}

					// check that coefficient is a number: this may throw an
					// exception
					ExpressionValue eval = coeff[i][j]
							.evaluate(StringTemplate.defaultTemplate);
					((NumberValue) eval).getDouble();
				}
			}
		}
		c.addToConstructionList(this, false);
		if (equ.isForcedLine()) {
			geoElement = new GeoLine(c);
		} else if (equ.isForcedConic()) {
			geoElement = new GeoConic(c);
		} else if (equ.isForcedImplicitPoly()) {
			geoElement = kernel.newImplicitPoly(cons);
		} else {
			switch (equ.degree()) {
			// linear equation -> LINE
			case 1:
				geoElement = new GeoLine(c);
				break;
			// quadratic equation -> CONIC
			case 2:
				geoElement = new GeoConic(c);
				break;
			default:
				geoElement = kernel.newImplicitPoly(c);
			}
		}
		geoElement.setDefinition(equ.wrap());
		setInputOutput(); // for AlgoElement

		compute(true);

	}

	@Override
	public void compute() {
		compute(false);
	}

	/**
	 * Replace output element with new one; needed if changes e.g. from line to
	 * conic
	 * 
	 * @param newElem
	 *            replacement element
	 */
	protected void replaceGeoElement(GeoElementND newElem) {
		String label = geoElement.getLabel(StringTemplate.defaultTemplate);
		geoElement.doRemove();
		geoElement = newElem;
		setInputOutput();
		geoElement.setLabel(label);
	}

	/**
	 * @return equation
	 */
	public Equation getEquation() {
		return (Equation) geoElement.getDefinition().unwrap();
	}

	private void compute(boolean first) {
		Equation equation = (Equation) geoElement.getDefinition().unwrap();
		if (!first) {

			if (equation.isFunctionDependent()) {
				// boolean functionChanged=false;
				Set<FunctionNVar> functions = new HashSet<FunctionNVar>();
				addAllFunctionalDescendents(this, functions,
						new TreeSet<AlgoElement>());

				// for (int i=0;i<dependentFromFunctions.length;i++){
				// if (dependentFromFunctions[i]!=null){
				// if (!(input[i] instanceof FunctionalNVar)){
				// functionChanged=true;
				// break;
				// }else{
				// if
				// (((FunctionalNVar)input[i]).getFunction()!=dependentFromFunctions[i]){
				// functionChanged=true;
				// break;
				// }
				// }
				// }
				// }
				if (!functions.equals(dependentFromFunctions)) {
					equation.initEquation();
					coeff = equation.getNormalForm().getCoeff();
					dependentFromFunctions = functions;
				}
			} else if (equation.hasVariableDegree()) {
				App.printStacktrace("");
				equation.initEquation();
				coeff = equation.getNormalForm().getCoeff();
			}
			if (!equation.isPolynomial()
					&& !kernel.getApplication().has(Feature.IMPLICIT_CURVES)) {
				geoElement.setUndefined();
				return;
			}
		}
		ExpressionNode def = geoElement.getDefinition();
		// use the forced behavior here
		int degree = equation.isForcedImplicitPoly() ? 3
				: (equation.isForcedConic() ? 2 : equation.degree());
		switch (degree) {
		// linear equation -> LINE
		case 1:
			if (geoElement instanceof GeoLine) {
				setLine();
			} else {
				if (geoElement.hasChildren())
					geoElement.setUndefined();
				else {
					replaceGeoElement(new GeoLine(getConstruction()));
					setLine();
				}
			}
			geoElement.setDefinition(def);
			break;
		// quadratic equation -> CONIC
		case 2:
			if (geoElement instanceof GeoConic) {
				setConic();
			} else {
				if (geoElement.hasChildren())
					geoElement.setUndefined();
				else {
					replaceGeoElement(new GeoConic(getConstruction()));
					setConic();
				}
			}
			geoElement.setDefinition(def);
			break;
		default:
			if (geoElement instanceof GeoImplicit) {
				((GeoImplicit) geoElement).setDefined();
				((GeoImplicit) geoElement).fromEquation(equation, null);
				((GeoImplicit) geoElement).setCoeff(coeff);
				geoElement.setDefinition(def);
			} else {
				if (geoElement.hasChildren())
					geoElement.setUndefined();
				else {
					replaceGeoElement(kernel.newImplicitPoly(getConstruction()));
					((GeoImplicit) geoElement).setDefined();
					((GeoImplicit) geoElement).fromEquation(equation, null);
					((GeoImplicit) geoElement).setCoeff(coeff);
				}
			}
		}
	}

	private void setLine() {
		ExpressionValue[] expr = new ExpressionValue[3];
		expr[2] = expr[1] = expr[0] = null;
		if (coeff.length > 0) {
			if (coeff[0].length > 0) {
				expr[2] = coeff[0][0];
				if (coeff[0].length > 1) {
					expr[1] = coeff[0][1];
				}
			}
			if (coeff.length > 1) {
				if (coeff[1].length > 0) {
					expr[0] = coeff[1][0];
				}
			}
		}
		double[] dCoeff = new double[expr.length];
		for (int i = 0; i < expr.length; i++) {
			if (expr[i] != null) {
				dCoeff[i] = expr[i].evaluateDouble();
			} else {
				dCoeff[i] = 0;
			}
		}
		((GeoLine) geoElement).setCoords(dCoeff[0], dCoeff[1], dCoeff[2]);
	}

	private void setConic() {
		ExpressionValue[] expr = new ExpressionValue[6];
		for (int i = 0; i < 6; i++) {
			expr[i] = null;
		}
		if (coeff.length > 0) {
			if (coeff[0].length > 0) {
				expr[5] = coeff[0][0];
				if (coeff[0].length > 1) {
					expr[4] = coeff[0][1];
					if (coeff[0].length > 2) {
						expr[2] = coeff[0][2];
					}
				}
			}
			if (coeff.length > 1) {
				if (coeff[1].length > 0) {
					expr[3] = coeff[1][0];
					if (coeff[1].length > 1) {
						expr[1] = coeff[1][1];
					}
				}
				if (coeff.length > 2) {
					if (coeff[2].length > 0) {
						expr[0] = coeff[2][0];
					}
				}
			}
		}
		double[] dCoeff = new double[expr.length];
		for (int i = 0; i < expr.length; i++) {
			if (expr[i] != null) {
				dCoeff[i] = expr[i].evaluateDouble();
			} else {
				dCoeff[i] = 0;
			}
		}
		((GeoConic) geoElement).setDefined();
		((GeoConic) geoElement).setCoeffs(dCoeff);
	}

	/**
	 * Adds all functions from inputs of algo and its ancestors to destination
	 * set
	 * 
	 * @param algo
	 *            algo whose input functions need adding
	 * @param set
	 *            destination set
	 * @param algos
	 *            set of algorithms that were already processed
	 */
	protected void addAllFunctionalDescendents(AlgoElement algo,
			Set<FunctionNVar> set, Set<AlgoElement> algos) {
		GeoElement[] in = algo.getInput();
		for (int i = 0; i < in.length; i++) {
			AlgoElement p = in[i].getParentAlgorithm();
			if (p != null && !algos.contains(p)) {
				algos.add(p);
				addAllFunctionalDescendents(p, set, algos);
			}
			if (in[i] instanceof FunctionalNVar) {
				set.add(((FunctionalNVar) in[i]).getFunction());
			}
		}
	}

	@Override
	protected void setInputOutput() {
		if (input == null) {
			input = geoElement.getDefinition().getGeoElementVariables();
			dependentFromFunctions = new HashSet<FunctionNVar>();
			addAllFunctionalDescendents(this, dependentFromFunctions,
					new TreeSet<AlgoElement>());
		}
		if (getOutputLength() == 0)
			setOutputLength(1);
		setOutput(0, geoElement.toGeoElement());
		setDependencies(); // done by AlgoElement
	}

	@Override
	public Algos getClassName() {
		return Algos.Expression;
	}

	/**
	 * @return resulting poly, conic or line
	 */
	public GeoElement getGeo() {
		return geoElement.toGeoElement();
		// if (type==GeoElement.GEO_CLASS_IMPLICIT_POLY)
		// return (GeoImplicitPoly)geoElement;
		// else
		// return null;
	}

	@Override
	public final String toString(StringTemplate tpl) {
		return geoElement.getDefinition().toString(tpl);
	}

	@Override
	protected String toExpString(StringTemplate tpl) {
		return geoElement.getLabel(tpl) + ": "
				+ geoElement.getDefinition().toString(tpl);
	}

	// TODO Consider locusequability

}
