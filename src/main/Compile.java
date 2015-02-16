package main;

import parse.Parse;
import semant.Semant;
import translate.Translate;

import java.io.*;

import absyn.Exp;
import errormsg.ErrorMsg;

public class Compile {
	public static void main(String argv[]) {
		try {
			if (argv.length == 0) {
				System.out.println("Please input filename.");
				return;
			}

			String filename = argv[0];
			ErrorMsg errorMsg = new ErrorMsg(filename);

			//parse
			System.out.println("Parsing...");
			Parse parse = new Parse(errorMsg);;
			Exp exp = parse.parse(filename);
			if (exp == null) return;	//error occured, stop compile
			System.out.println("complete.");

			//translate into trees
			System.out.println("Type checking...");
			Semant semant = new Semant(errorMsg);
			Translate translate = semant.transProg(exp);
			if (translate == null) return;	//error occured, stop compile
			System.out.println("complete.");

			//translate into assembly code
			System.out.println("Generating assembly code...");
			PrintStream out = new PrintStream(new FileOutputStream(filename + ".acode"));
			frame.virtualframe.AssemblyCodeGenerator acg = new frame.virtualframe.AssemblyCodeGenerator(out);
			acg.generate(translate);
			out.close();
			System.out.println("complete.");
			System.out.println("Assembly code has been written into " + filename + ".acode.");

			//translate into machine code
			System.out.println("Generating machine code...");
			BufferedReader in = new BufferedReader(new FileReader(filename + ".acode"));
			DataOutputStream out1 = new DataOutputStream(new FileOutputStream(filename + ".mcode"));
			frame.virtualframe.MachineCodeGenerator mcg = new frame.virtualframe.MachineCodeGenerator(in, out1);
			mcg.generate();
			in.close();
			out.close();
			System.out.println("complete.");
			System.out.println("Machine code has been written into " + filename + ".mcode.");
			System.out.println();
			System.out.println("Compilation completed successfully");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}