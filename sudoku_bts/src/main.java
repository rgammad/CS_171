
import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;
import java.util.concurrent.TimeoutException;

public class main {
	public static void main(String[] args){
		
		
		try{
			if(args[0] == null || args[1] == null || args[2] == null){
				throw new ArrayIndexOutOfBoundsException();
			}
			else{
				long programStartTime = System.currentTimeMillis();
				boolean statusHasSolution = false;
				boolean statusTimeout = false;
				boolean statusSuccess = false;
				boolean statusError = false;
				
				SudokuFile readSF = SudokuBoardReader.readFile(args[0]);
				outputLog output = new outputLog();
				BTSolver solver = new BTSolver(readSF);
				solver.setConsistencyChecks(ConsistencyCheck.None);
				solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
				solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
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
				
				
				if(statusHasSolution)
				{	
					solver.printSolverStats();
					System.out.println(solver.getSolution());	
				}
				else{
					System.out.println("Failed to find a solution");
				}
				if(!statusTimeout && !statusError){
					statusSuccess = true;
				}
				output.setLogTime(programStartTime, startTime, endTime, solveTime);
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
