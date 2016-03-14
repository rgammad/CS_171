package scripts;

import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SudokuFileExample {

	public static void main(String[] args) {
		int N = 18;
		int p = 3;
		int q = 6;
		List<Integer> M = Arrays.asList(16, 32, 48, 64, 68, 72, 76, 80, 84, 88, 96, 112, 128, 144);
		for (int m : M) {
			for (int i = 1; i <= 10; i++) {
				File file = new File("C:/Users/Roldan/Documents/Eclipse_Workspace/CS_171/sudoku_bts/test/Part_5/M" + m
						+ "/M" + m + "_" + i + ".txt");
				SudokuFile gSF1 = SudokuBoardGenerator.generateBoard(N, p, q, m);
				try {
					FileWriter fw = new FileWriter(file);
					fw.write(gSF1.toString());
					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
