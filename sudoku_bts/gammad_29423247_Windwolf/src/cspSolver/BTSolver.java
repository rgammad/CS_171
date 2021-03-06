package cspSolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import sudoku.Converter;
import sudoku.SudokuFile;

/**
 * Backtracking solver.
 *
 */
public class BTSolver implements Runnable {

	// ===============================================================================
	// Properties
	// ===============================================================================

	private ConstraintNetwork network;
	private static Trail trail = Trail.getTrail();
	private boolean hasSolution = false;
	private SudokuFile sudokuGrid;

	private int numAssignments;
	private int numBacktracks;
	public long startTime;
	public long endTime;

	public boolean statusTime = false;
	public long timeout;
	public long prepStart;
	public long prepDone;

	public enum VariableSelectionHeuristic {
		None, MinimumRemainingValue, Degree, MRVDH
	};

	public enum ValueSelectionHeuristic {
		None, LeastConstrainingValue
	};

	public enum ConsistencyCheck {
		None, ForwardChecking, ArcConsistency
	};

	private VariableSelectionHeuristic varHeuristics;
	private ValueSelectionHeuristic valHeuristics;
	private ConsistencyCheck cChecks;
	// ===============================================================================
	// Constructors
	// ===============================================================================

	public BTSolver(SudokuFile sf) {
		this.network = Converter.SudokuFileToConstraintNetwork(sf);
		this.sudokuGrid = sf;
		numAssignments = 0;
		numBacktracks = 0;
	}

	// ===============================================================================
	// Modifiers
	// ===============================================================================

	public void setVariableSelectionHeuristic(VariableSelectionHeuristic vsh) {
		this.varHeuristics = vsh;
	}

	public void setValueSelectionHeuristic(ValueSelectionHeuristic vsh) {
		this.valHeuristics = vsh;
	}

	public void setConsistencyChecks(ConsistencyCheck cc) {
		this.cChecks = cc;
	}
	// ===============================================================================
	// Accessors
	// ===============================================================================

	/**
	 * @return true if a solution has been found, false otherwise.
	 */
	public boolean hasSolution() {
		return hasSolution;
	}

	/**
	 * @return solution if a solution has been found, otherwise returns the
	 *         unsolved puzzle.
	 */
	public SudokuFile getSolution() {
		return sudokuGrid;
	}

	public void printSolverStats() {
		System.out.println("Time taken:" + (endTime - startTime) + " ms");
		System.out.println("Number of assignments: " + numAssignments);
		System.out.println("Number of backtracks: " + numBacktracks);
	}

	/**
	 * 
	 * @return time required for the solver to attain in seconds
	 */
	public long getTimeTaken() {
		return endTime - startTime;
	}

	public int getNumAssignments() {
		return numAssignments;
	}

	public int getNumBacktracks() {
		return numBacktracks;
	}

	public ConstraintNetwork getNetwork() {
		return network;
	}

	// ===============================================================================
	// Helper Methods
	// ===============================================================================

	/**
	 * Checks whether the changes from the last time this method was called are
	 * consistent.
	 * 
	 * @return true if consistent, false otherwise
	 */
	private boolean checkConsistency() {
		boolean isConsistent = false;
		switch (cChecks) {
		case None:
			isConsistent = assignmentsCheck();
			break;
		case ForwardChecking:
			isConsistent = forwardChecking();
			break;
		case ArcConsistency:
			isConsistent = arcConsistency();
			break;
		default:
			isConsistent = assignmentsCheck();
			break;
		}
		return isConsistent;
	}

	/**
	 * default consistency check. Ensures no two variables are assigned to the
	 * same value.
	 * 
	 * @return true if consistent, false otherwise.
	 */
	private boolean assignmentsCheck() {
		for (Variable v : network.getVariables()) {
			if (v.isAssigned()) {
				for (Variable vOther : network.getNeighborsOfVariable(v)) {
					if (v.getAssignment() == vOther.getAssignment()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean forwardChecking() {
		for (Variable v : network.getVariables()) {
			if (v.isAssigned()) {
				for (Variable vOther : network.getNeighborsOfVariable(v)) {
					vOther.removeValueFromDomain(v.getAssignment());
					if (vOther.getDomain().size() == 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * TODO: Implement Maintaining Arc Consistency.
	 */
	private boolean arcConsistency() {
		return false;
	}

	/**
	 * Selects the next variable to check.
	 * 
	 * @return next variable to check. null if there are no more variables to
	 *         check.
	 */
	private Variable selectNextVariable() {
		Variable next = null;
		switch (varHeuristics) {
		case None:
			next = getfirstUnassignedVariable();
			break;
		case MinimumRemainingValue:
			next = getMRV();
			break;
		case Degree:
			next = getDegree();
			break;
		case MRVDH:
			next = getMRVDH();
			break;
		default:
			next = getfirstUnassignedVariable();
			break;
		}
		return next;
	}

	/**
	 * default next variable selection heuristic. Selects the first unassigned
	 * variable.
	 * 
	 * @return first unassigned variable. null if no variables are unassigned.
	 */
	private Variable getfirstUnassignedVariable() {
		for (Variable v : network.getVariables()) {
			if (!v.isAssigned()) {
				return v;
			}
		}
		return null;
	}

	/**
	 * TODO: Implement MRV heuristic
	 * 
	 * @return variable with minimum remaining values that isn't assigned, null
	 *         if all variables are assigned.
	 */
	private Variable getMRV() {
		List<Variable> vList = new ArrayList<Variable>();
		for (Variable v : network.getVariables()) {
			if (!v.isAssigned()) {
				vList.add(v);
			}
		}
		if (!vList.isEmpty()) {
			int minSize = vList.get(0).getDomain().size();
			Variable minV = vList.get(0);
			for (int i = 1; i < vList.size(); i++) {
				if (minSize > vList.get(i).getDomain().size()) {
					minSize = vList.get(i).getDomain().size();
					minV = vList.get(i);
				}
			}
			return minV;
		}
		return null;
	}

	/**
	 * TODO: Implement Degree heuristic
	 * 
	 * @return variable constrained by the most unassigned variables, null if
	 *         all variables are assigned.
	 */
	private Variable getDegree() {
		Map<Variable, Integer> varMap = new HashMap<Variable, Integer>();
		for (Variable v : network.getVariables()) {
			if (!v.isAssigned()) {
				int count = 0;
				for (Variable vOther : network.getNeighborsOfVariable(v)) {
					if (!vOther.isAssigned()) {
						count++;
					}
				}
				varMap.put(v, count);
			}
		}
		if (!varMap.isEmpty()) {
			int maxValue = Collections.max(varMap.values());
			Variable retVar = null;
			for (Map.Entry<Variable, Integer> var : varMap.entrySet()) {
				if (var.getValue() == maxValue) {
					retVar = var.getKey();
				}
			}
			return retVar;
		}
		return null;
	}

	private Variable getMRVDH() {
		List<Variable> vList = new ArrayList<Variable>();
		List<Variable> dhList = new ArrayList<Variable>();
		List<Integer> constraintCount = new ArrayList<Integer>();
		for (Variable v : network.getVariables()) {
			if (!v.isAssigned()) {
				int count = 0;

				for (Variable vOther : network.getNeighborsOfVariable(v)) {
					if (!vOther.isAssigned()) {
						count++;
					}
				}
				constraintCount.add(count);
				vList.add(v);
			}
		}
		if (!vList.isEmpty()) {
			int minSize = vList.get(0).getDomain().size();
			Variable minV = vList.get(0);
			for (int i = 1; i < vList.size(); i++) {
				if (minSize > vList.get(i).getDomain().size()) {
					minSize = vList.get(i).getDomain().size();
					minV = vList.get(i);
				} else if (minSize == vList.get(i).getDomain().size()) {
					dhList.add(vList.get(i));
				}
			}
			int maxDeg = constraintCount.get(0);
			Variable retV = dhList.get(0);
			for (Variable v : dhList) {
				int temp = constraintCount.get(vList.indexOf(v));
				if (maxDeg < temp) {
					retV = v;
					maxDeg = temp;
				}
			}
			return retV;
		}
		return null;
	}

	/**
	 * Value Selection Heuristics. Orders the values in the domain of the
	 * variable passed as a parameter and returns them as a list.
	 * 
	 * @return List of values in the domain of a variable in a specified order.
	 */
	public List<Integer> getNextValues(Variable v) {
		List<Integer> orderedValues;
		switch (valHeuristics) {
		case None:
			orderedValues = getValuesInOrder(v);
			break;
		case LeastConstrainingValue:
			orderedValues = getValuesLCVOrder(v);
			break;
		default:
			orderedValues = getValuesInOrder(v);
			break;
		}
		return orderedValues;
	}

	/**
	 * Default value ordering.
	 * 
	 * @param v
	 *            Variable whose values need to be ordered
	 * @return values ordered by lowest to highest.
	 */
	public List<Integer> getValuesInOrder(Variable v) {
		List<Integer> values = v.getDomain().getValues();

		Comparator<Integer> valueComparator = new Comparator<Integer>() {

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}

	/**
	 * TODO: LCV heuristic
	 */
	public List<Integer> getValuesLCVOrder(Variable v) {
		List<Integer> values = v.getDomain().getValues();
		List<Integer> countVal = new ArrayList<Integer>();
		Map<Integer,Integer> valueMap = new HashMap<Integer,Integer>();
		for (int val : values) {
			int count = 0;
			for (Variable vOther : network.getNeighborsOfVariable(v)) {
				if (vOther.isAssigned()) {
					if (vOther.getAssignment() != val) {
						count++;
					}
				}
			}
			countVal.add(count);
		}
		for(int i = 0; i < values.size(); i++){
			valueMap.put(values.get(i), countVal.get(i));
		}
		final Map<Integer, Integer> mapforComp = valueMap;
		Collections.sort(values, 
				new Comparator<Integer>() {
					@Override
						public int compare(Integer i1, Integer i2) {
									Integer i1Key = i1;
									Integer i2Key = i2;
									Integer i1Value = mapforComp.get(i1Key);
									Integer i2Value = mapforComp.get(i2Key);
									return i1Value.compareTo(i2Value);
					}
		});
		return values;
	}

	/**
	 * Called when solver finds a solution
	 */
	private void success() {
		hasSolution = true;
		sudokuGrid = Converter.ConstraintNetworkToSudokuFile(network, sudokuGrid.getN(), sudokuGrid.getP(),
				sudokuGrid.getQ());
	}

	// ===============================================================================
	// Solver
	// ===============================================================================

	/**
	 * Method to start the solver
	 */
	public void solve() {
		startTime = System.currentTimeMillis();
		try {
			solve(0);
		} catch (VariableSelectionException e) {
			System.out.println("error with variable selection heuristic.");
		} catch (TimeoutException t) {
			System.out.println("timeout error");
			statusTime = true;
		}
		endTime = System.currentTimeMillis();
		Trail.clearTrail();
	}

	/**
	 * Solver
	 * 
	 * @param level
	 *            How deep the solver is in its recursion.
	 * @throws VariableSelectionException
	 */

	private void solve(int level) throws VariableSelectionException, TimeoutException {
		if (!Thread.currentThread().isInterrupted())

		{// Check if assignment is completed
			if (hasSolution) {
				return;
			}

			prepStart = System.currentTimeMillis();
			prepDone = System.currentTimeMillis();
			if ((timeout > (prepDone - prepStart) + (System.currentTimeMillis() - startTime))) {
				// Select unassigned variable
				Variable v = selectNextVariable();

				// check if the assignment is complete
				if (v == null) {
					for (Variable var : network.getVariables()) {
						if (!var.isAssigned()) {
							throw new VariableSelectionException(
									"Something happened with the variable selection heuristic");
						}
					}
					success();
					return;
				}

				// loop through the values of the variable being checked LCV

				for (Integer i : getNextValues(v)) {
					trail.placeBreadCrumb();

					// check a value
					v.updateDomain(new Domain(i));
					numAssignments++;
					boolean isConsistent = checkConsistency();

					// move to the next assignment
					if (isConsistent) {
						solve(level + 1);
					}

					// if this assignment failed at any stage, backtrack
					if (!hasSolution) {
						trail.undo();
						numBacktracks++;
					}

					else {
						return;
					}
				}
			} else {
				throw new TimeoutException("");
			}
		}
	}

	@Override
	public void run() {
		solve();
	}
}
