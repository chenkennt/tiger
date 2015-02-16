package parse;

import errormsg.ErrorMsg;
import absyn.*;
import java.io.*;

public class Parse {
	public ErrorMsg errorMsg;

	public Parse(ErrorMsg err) {
		errorMsg = err;
	}

	public Exp parse(String filename) {
		InputStream inp;
		try {
			inp = new java.io.FileInputStream(filename);
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			return null;
		}
		try {
			parser parser = new parser(new Yylex(inp, errorMsg), errorMsg);
			Exp prog = (Exp)parser.parse().value;
			if (errorMsg.anyErrors) {
				System.out.println("Error occured, stop compiling");
				return null;
			}
			else return prog;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				inp.close();
			}
			catch (java.io.IOException e) {
			}
		}
	}
}