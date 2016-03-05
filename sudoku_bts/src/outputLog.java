

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import sudoku.SudokuFile;
import cspSolver.BTSolver;


public class outputLog {
	private double startTime;
	private double searchStartTime;
	private double searchEndTime;
	private double sudokuSolveTime;
	private double prepStart;
	private double prepDone;
	private String status;
	private String solution;
	private int numAssignments;
	private int deadEnds;
	
	DecimalFormat df = new DecimalFormat("#.###");
	
	public void setLogTime(long prepStart, long prepDone, long programStart, long searchStart, long searchEnd, long solveTime){
		this.prepStart =Double.parseDouble(df.format((System.currentTimeMillis()- prepStart)/1000L));
		this.prepDone = Double.parseDouble(df.format((System.currentTimeMillis()- prepDone)/1000L));
		this.startTime = Double.parseDouble(df.format((System.currentTimeMillis()- programStart)/1000L));
		this.searchStartTime = Double.parseDouble(df.format((System.currentTimeMillis()- searchStart)/1000L));
		this.searchEndTime = Double.parseDouble(df.format((System.currentTimeMillis()- searchEnd)/1000L));
		this.sudokuSolveTime = Double.parseDouble(df.format((solveTime)/1000L));
	}
	
	public void setStatus(boolean success, boolean timeout, boolean error){
		if(timeout){
			this.status = "Timeout";
		}
		else if(error){
			this.status = "Error";
		}
		else{
			this.status = "Success";
		}
	}
	public void getSolution(BTSolver solver){
		String retStr = "(";
		SudokuFile solnBoard = solver.getSolution();
		int[][] sfBoard = solnBoard.getBoard();
		if(solver.hasSolution()){
			for(int i = 0; i < solnBoard.getN(); i++){
				for(int j = 0; j < solnBoard.getN(); j++){
					retStr += (Integer.toString(sfBoard[j][i]) + ", ");
				}
			}
		}
		else{
			for(int i = 0; i < solnBoard.getN(); i++){
				for(int j = 0; j < solnBoard.getN()-1; j++){
					retStr += ("0, ");
				}
				
			}
		}
		retStr = retStr.substring(0,retStr.length()-2);
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
		logger.info("\nTOTAL_START=" + (this.startTime)
				+ "\nPREPROCESSING_START=" + this.prepStart 
				+ "\nPREPROCESSING_DONE=" + this.prepStart
				+ "\nSEARCH_START=" + (this.searchStartTime)
				+ "\nSEARCH_DONE=" + (this.searchEndTime)
				+ "\nSOLUTION_TIME=" +(this.sudokuSolveTime)
				+ "\nSTATUS=" + this.status
				+ "\nSOLUTION=" + this.solution
				+ "\nCOUNT_NODES=" + this.numAssignments
				+ "\nCOUNT_DEADENDS=" + this.deadEnds);

	}
}
