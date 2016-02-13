

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import sudoku.SudokuFile;
import cspSolver.BTSolver;


public class outputLog {
	private long startTime;
	private long searchStartTime;
	private long searchEndTime;
	private long sudokuSolveTime;
	private long prepStart;
	private long prepDone;
	private String status;
	private String solution;
	private int numAssignments;
	private int deadEnds;
	
	public void setLogTime(long prepStart, long prepDone, long programStart, long searchStart, long searchEnd, long solveTime){
		this.prepStart = prepStart/1000L;
		this.prepDone = prepDone/1000L;
		this.startTime = (programStart)/1000L;
		this.searchStartTime = (searchStart)/1000L;
		this.searchEndTime = (searchEnd)/1000L;
		this.sudokuSolveTime = (solveTime)/1000L;
	}
	
	public void setStatus(boolean success, boolean timeout, boolean error){
		if(success){
			this.status = "Success";
		}
		else if(timeout){
			this.status = "Timeout";
		}
		else if(error){
			this.status = "Error";
		}
	}
	public void getSolution(BTSolver solver){
		String retStr = "(";
		SudokuFile solnBoard = solver.getSolution();
		int[][] sfBoard = solnBoard.getBoard();
		if(solver.hasSolution()){
			for(int i = 0; i < solnBoard.getN(); i++){
				for(int j = 0; j < solnBoard.getN(); j++){
					retStr += (Integer.toString(sfBoard[i][j]) + ", ");
				}
			}
		}
		else{
			for(int i = 0; i < solnBoard.getN(); i++){
				for(int j = 0; j < solnBoard.getN(); j++){
					retStr += ("0, ");
				}
				
			}
		}
		retStr += ")";
		this.solution =  retStr;
	}
	public void getNodesDeadEnds(BTSolver solver){
		this.numAssignments = solver.getNumAssignments();
		this.deadEnds = solver.getNumBacktracks();
	}
	public void writeLog(String outputFile){
		FileHandler handler;
		Logger logger = Logger.getLogger("sudoku.output");
		try {
			handler = new FileHandler(outputFile, true);
			
			logger.addHandler(handler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long prepStart = System.currentTimeMillis();
		logger.info("\nTOTAL_START=  " + (this.startTime) + " seconds (System Time)"
				+ "\nPREPROCESSING_START= " + prepStart
				+ "\nPREPROCESSING_DONE= " + prepStart
				+ "\nSEARCH_START=  " + (this.searchStartTime) + " seconds (System Time)"
				+ "\nSEARCH_DONE=  " + (this.searchEndTime) + " seconds (System Time)"
				+ "\nSOLUTION_TIME= " +(this.sudokuSolveTime) + " seconds (Relative Time)"
				+ "\nSTATUS= " + this.status
				+ "\nSOLUTION= " + this.solution
				+ "\nCOUNT_NODES= " + this.numAssignments
				+ "\nCOUNT_DEADENDS= " + this.deadEnds);

	}
}
