
import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;
import java.util.concurrent.TimeoutException;

public class main {
	public static void main(String[] args){
		long programStartTime = System.currentTimeMillis();
		long timeout = Long.parseLong(args[2]);
		
		boolean statusHasSolution = false;
		boolean statusTimeout = false;
		boolean statusSuccess = false;
		boolean statusError = false;
		boolean FC = false;
		//boolean ACP = false;
		boolean MAC = false;
		boolean MRV = false;
		boolean DH = false;
		boolean LCV = false;
		int argSize = args.length;
		
		String[] tokens = new String[args.length - 3];
		
		SudokuFile readSF;
		BTSolver solver;
			
		try{
			if (args.length < 3) {
				throw new ArrayIndexOutOfBoundsException();
			} else {
				readSF = SudokuBoardReader.readFile(args[0]);
				solver = new BTSolver(readSF);
				solver.timeout = timeout;

				int tokenSize = argSize - 3;
				for (int i = 0; i < tokenSize; i++) {
					tokens[i] = args[i + 3];
				}
				// System.out.println(args[4]);
				if (tokens.length > 0) {
					for (String s : tokens) {
						if (s.equals("FC")) {
							FC = true;
						}
						/*if (s.equals("ACP")) {
							ACP = true;
						}*/
						if (s.equals("MRV")) {
							MRV = true;
						}
						if (s.equals("DH")) {
							DH = true;
						}
						if (s.equals("LCV")) {
							LCV = true;
						}
					}
				}
				if (FC){// && !ACP) {
					solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
				}
				/*else if (ACP || FC) {
					solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
				} else if (MAC || FC || ACP) {
					// TODO: MAC functionality
					solver.setConsistencyChecks(ConsistencyCheck.None);
				}*/
				else {
					solver.setConsistencyChecks(ConsistencyCheck.None);
				}
				if (MRV && !DH) {
					solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
				} else if (DH && !MRV) {
					solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.Degree);
				} else if (DH && MRV) {
					solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MRVDH);
				} else {
					solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
				}
				if (LCV) {
					solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
				} else {
					solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
				}
			
				Thread t1 = new Thread(solver);
				try
				{
					t1.start();
					t1.join(Long.parseLong(args[2]));
					if(t1.isAlive())
					{
						t1.interrupt();
						throw new TimeoutException();
					}
				}catch(TimeoutException t){
					statusTimeout = true;
				}catch(Exception e){
					statusError = true;
				}
				
				statusHasSolution = solver.hasSolution();
				long startTime = solver.startTime;
				long endTime = solver.endTime;
				long solveTime = solver.getTimeTaken();
				
				
				/*if(statusHasSolution)
				{	
					solver.printSolverStats();
					System.out.println(solver.getSolution());	
				}
				else{
					System.out.println("Failed to find a solution");
				}*/
				if(solver.statusTime){
					//System.out.println(solver.statusTime);
					statusTimeout = true;
				}
				else if(!statusTimeout && !statusError){
					statusSuccess = true;
				}
				outputLog output = new outputLog();
				output.setLogTime(solver.prepStart, solver.prepDone, programStartTime, startTime, endTime, solveTime);
				output.setStatus(statusSuccess, statusTimeout, statusError);
				output.getSolution(solver);
				output.getNodesDeadEnds(solver);
				output.writeLog(args[1]);
			}
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Executable 'main.jar' requires minimum command-line arguments: 'input_file', 'output file', 'timeout'.");
			System.out.println("Terminating program.");
		}
		
	}	
}
