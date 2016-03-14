package scripts;

import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;

import java.nio.charset.Charset;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SudokuFileExample {

	public static void main(String[] args)
	{

		int N = 35;
		int p = 5;
		int q = 7;
		int m = 303;
		for(int i = 1; i <= 10; i++){
			File file = new File("C:/Users/Roldan/Documents/Eclipse_Workspace/CS_171/sudoku_bts/test/Part_4/N"+N+"/N"+N+"_" + i  + ".txt");
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
