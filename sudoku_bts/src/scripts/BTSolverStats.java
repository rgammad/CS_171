package scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;
import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;

public class BTSolverStats {

	// Change these to test different configurations for the solver.
	static ConsistencyCheck cc = ConsistencyCheck.ForwardChecking;
	static ValueSelectionHeuristic valsh = ValueSelectionHeuristic.None;
	static VariableSelectionHeuristic varsh = VariableSelectionHeuristic.MinimumRemainingValue;

	public static List<SudokuFile> getPuzzlesFromFolder(File folder) {
		List<SudokuFile> puzzles = new ArrayList<SudokuFile>();
		for (File fileEntry : folder.listFiles()) {
			SudokuFile sfPE = SudokuBoardReader.readFile(fileEntry.getPath());
			if (sfPE.getN() != 0) {
				puzzles.add(sfPE);
			}
		}
		return puzzles;
	}

	public static runStats testSolver(BTSolver solver) {
		solver.setConsistencyChecks(cc);
		solver.setValueSelectionHeuristic(valsh);
		solver.setVariableSelectionHeuristic(varsh);
		long timeout = 60000;
		solver.timeout = timeout;
		Thread t1 = new Thread(solver);
		try {
			t1.start();
			t1.join(600000000);
			if (t1.isAlive()) {
				t1.interrupt();
			}
		} catch (InterruptedException e) {
		}
		long runtime;
		int numAssignments;
		int numBacktracks;
		boolean isSolution;
		runStats rs;
		runtime = solver.getTimeTaken();
		numAssignments = solver.getNumAssignments();
		numBacktracks = solver.getNumBacktracks();
		isSolution = solver.hasSolution();
		rs = new runStats(runtime, numAssignments, numBacktracks, isSolution);
		return rs;
	}

	public static void main(String[] args) {
		String sep = System.getProperty("line.separator");
		List<Integer> N = Arrays.asList(12, 15, 16, 18, 20, 21, 24, 27, 28, 30, 32, 35);
		List<Integer> M = Arrays.asList(4,8,12,16,17,18,19,20,21,22,24,28,32,36);
		
		for (int n: N) {
			int count = 1;
			File results = new File(
					"C:/Users/Roldan/Documents/Eclipse_Workspace/CS_171/sudoku_bts/test_results/Part_4/N" + n
							+ "Results.txt");
			File summary = new File(
					"C:/Users/Roldan/Documents/Eclipse_Workspace/CS_171/sudoku_bts/test_results/Part_4/N" +n
							+ "Summary" + count++ + ".txt");
			count = 1;
			File folder = new File("C:/Users/Roldan/Documents/Eclipse_Workspace/CS_171/sudoku_bts/test/Part_4/N" + n);
			List<SudokuFile> puzzles = getPuzzlesFromFolder(folder);
			List<runStats> statistics = new ArrayList<runStats>();

			puzzles = puzzles.subList(0, 10);
			System.out.println("Puzzle: N"+n);
			for (SudokuFile sf : puzzles) {
				BTSolver solver = new BTSolver(sf);
				statistics.add(testSolver(solver));
				System.out.println(statistics.get(statistics.size() - 1).toString());
			}

			try {
				FileWriter fw = new FileWriter(results);
				for (runStats rs : statistics) {
					fw.write(rs.toString() + sep);
				}
				fw.flush();
				fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				FileWriter fw = new FileWriter(summary);
				long totalRunTime = 0;
				long totalAssignments = 0;
				long totalBackTracks = 0;
				int totalSuccessful = 0;
				int totalPuzzles = 0;

				for (runStats rs : statistics) {
					if (rs.isSolution()) {
						totalRunTime += rs.getRuntime();
						totalAssignments += rs.getNumAssignments();
						totalBackTracks += rs.getNumBacktracks();
						totalSuccessful++;
					}
					totalPuzzles++;
				}
				// totalSuccessful = 5;
				//System.out.print("Solution found for " + totalSuccessful + "/" + totalPuzzles + "puzzles" + sep);
				//System.out.print("average runTime: " + (totalRunTime / totalSuccessful) + sep);
				//System.out.print("average number of assignments per puzzle: " + (totalAssignments / totalSuccessful) + sep);
				//System.out.print("average number of backtracks per puzzle: " + (totalBackTracks / totalSuccessful) + sep);
				fw.write("Consistency Check: " + cc + sep);
				fw.write("ValueSelectionHeuristic: " + valsh + sep);
				fw.write("VariableSelectionHeuristic: " + varsh + sep);
				fw.write("Solution found for " + totalSuccessful + "/" + totalPuzzles + "puzzles" + sep);
				//fw.write("average runTime: " + (totalRunTime / totalSuccessful) + sep);
				//fw.write("average number of assignments per puzzle: " + (totalAssignments / totalSuccessful) + sep);
				//fw.write("average number of backtracks per puzzle: " + (totalBackTracks / totalSuccessful) + sep);

				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
