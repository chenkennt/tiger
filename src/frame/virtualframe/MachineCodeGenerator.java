package frame.virtualframe;

import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import symbol.Symbol;
import temp.Temp;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-5
 * Time: 1:08:37
 * To change this template use Options | File Templates.
 */
public class MachineCodeGenerator extends frame.MachineCodeGenerator {
	public static final String[] cmds = {
		"ADD",		//arithmetic instructions
		"SUB",
		"MUL",
		"DIV",
		"AND",
		"OR",
		"LS",
		"RS",
		"ARS",
		"XOR",
		"MOVE",		//move from temp to temp
		"INST",		//move instant number or label position to temp
		"STORE",	//store temp to memory
		"LOAD",		//load memory to temp
		"JMP",		//jump
		"JE",		//conditional jump
		"JGE",
		"JG",
		"PUSH",		//push temp
		"POP"		//pop temp
	};
	public static final int[] cmdSize = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 2, 4, 4, 4, 1, 1};
	public static final int[] cmdCode = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
	public static final int WORD_SIZE = 4;

	private Hashtable ht;
	private int pos;

	public MachineCodeGenerator(BufferedReader i, DataOutputStream o) {
		super(i, o);
	}

	public void generate() {
		try {
			//generate sp, fp, rv
			String sp = in.readLine();
			if (sp == null) {
				System.out.println("invalid format");
				return;
			}
			String fp = in.readLine();
			if (fp == null) {
				System.out.println("invalid format");
				return;
			}
			String rv = in.readLine();
			if (rv == null) {
				System.out.println("invalid format");
				return;
			}
			StringTokenizer t1 = new StringTokenizer(sp);
			StringTokenizer t2 = new StringTokenizer(fp);
			StringTokenizer t3 = new StringTokenizer(rv);
			if (!t1.hasMoreTokens()) {
				System.out.println("invalid format");
				return;
			}
			String pn = t1.nextToken();
			if (!pn.equals("SP") || !generateTemporary(t1)) {
				System.out.println("invalid format");
				return;
			}
			if (!t2.hasMoreTokens()) {
				System.out.println("invalid format");
				return;
			}
			pn = t2.nextToken();
			if (!pn.equals("FP") || !generateTemporary(t2)) {
				System.out.println("invalid format");
				return;
			}
			if (!t3.hasMoreTokens()) {
				System.out.println("invalid format");
				return;
			}
			pn = t3.nextToken();
			if (!pn.equals("RV") || !generateTemporary(t3)) {
				System.out.println("invalid format");
				return;
			}
			//write total temp
			out.writeInt(Temp.getCount());
			//1st pass(generate label position)
			pos = 0x100;
			in.mark(65536);
			ht = new Hashtable();
			while (true) {
				String s = in.readLine();
                if (s == null) break;
				if (s.trim().equals("")) continue;
				StringTokenizer token = new StringTokenizer(s);
				if (!token.hasMoreTokens()) {
					System.out.println("invalid format");
					return;
				}
				String cmd = token.nextToken();
				if (cmd.equals("LABEL")) {
					if (!token.hasMoreTokens()) {
						System.out.println("invalid format");
						return;
					}
					ht.put(Symbol.symbol(token.nextToken()), new Integer(pos));
				}
				else if (cmd.equals("STRING")) {
					if (!token.hasMoreTokens()) {
						System.out.println("invalid format");
						return;
					}
					int l = Integer.parseInt(token.nextToken());
					pos += (l + 1) * WORD_SIZE;
					for (int i = 0; i < l; i++) in.read();
					in.readLine();
				}
				else
					for (int i = 0; i < cmds.length; i++) {
						if (cmd.equals(cmds[i])) {
							pos += cmdSize[i] * WORD_SIZE;
							break;
						}
					}
			}
			//2nd pass(generate code)
			in.reset();
			while (true) {
				String s = in.readLine();
                if (s == null) break;
				if (s.trim().equals("")) continue;
				StringTokenizer token = new StringTokenizer(s, " \t,");
				if (!token.hasMoreTokens()) {
					System.out.println("invalid format");
					return;
				}
				String cmd = token.nextToken();

				for (int i = 0; i < 10; i++)
					if (cmd.equals(cmds[i]))
						if (!generateArithmeticCode(cmdCode[i], token)) {
							System.out.println("invalid format");
							return;
						}
				if (cmd.equals("MOVE"))
					if (!generateMoveCode(token)) {
						System.out.println("invalid format");
						return;
					}
				if (cmd.equals("INST"))
					if (!generateInstCode(token)) {
						System.out.println("invalid format");
						return;
					}
				if (cmd.equals("LOAD"))
					if (!generateLoadCode(token)) {
						System.out.println("invalid format");
						return;
					}
				if (cmd.equals("STORE"))
					if (!generateStoreCode(token)) {
						System.out.println("invalid format");
						return;
					}
				if (cmd.equals("JMP"))
					if (!generateJumpCode(token)) {
						System.out.println("invalid format");
						return;
					}
				for (int i = 15; i < 18; i++)
					if (cmd.equals(cmds[i]))
						if (!generateCJumpCode(cmdCode[i], token)) {
							System.out.println("invalid format");
							return;
						}
				if (cmd.equals("STRING")) {
					if (!token.hasMoreTokens()) {
						System.out.println("invalid format");
						return;
					}
					int l = Integer.parseInt(token.nextToken());
					out.writeInt(l);
					for (int i = 0; i < l; i++) out.writeInt(in.read());
					in.readLine();
				}
				if (cmd.equals("PUSH")) out.writeInt(cmdCode[18]);
				if (cmd.equals("POP")) out.writeInt(cmdCode[19]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean generateArithmeticCode(int code, StringTokenizer token) throws IOException {
		if (!token.hasMoreTokens()) return false;
		String d = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s1 = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s2 = token.nextToken();
		String regex = "t([0-9]+)";
		if (!d.matches(regex) || !s1.matches(regex) || !s2.matches(regex)) return false;
		String replace = "$1";
		try {
			int id = Integer.parseInt(d.replaceAll(regex, replace));
			int is1 = Integer.parseInt(s1.replaceAll(regex, replace));
			int is2 = Integer.parseInt(s2.replaceAll(regex, replace));
			out.writeInt(code);
			out.writeInt(id);
			out.writeInt(is1);
			out.writeInt(is2);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean generateCJumpCode(int code, StringTokenizer token) throws IOException {
		if (!token.hasMoreTokens()) return false;
		String d = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s1 = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s2 = token.nextToken();
		String regex = "t([0-9]+)";
		if (!d.matches(regex) || !s1.matches(regex) || !s2.matches(regex)) return false;
		String replace = "$1";
		try {
			int id = Integer.parseInt(d.replaceAll(regex, replace));
			int is1 = Integer.parseInt(s1.replaceAll(regex, replace));
			int is2 = Integer.parseInt(s2.replaceAll(regex, replace));
			out.writeInt(code);
			out.writeInt(id);
			out.writeInt(is1);
			out.writeInt(is2);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean generateMoveCode(StringTokenizer token) throws IOException {
		if (!token.hasMoreTokens()) return false;
		String d = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s = token.nextToken();
		String regex = "t([0-9]+)";
		if (!d.matches(regex) || !s.matches(regex)) return false;
		String replace = "$1";
		try {
			int id = Integer.parseInt(d.replaceAll(regex, replace));
			int is = Integer.parseInt(s.replaceAll(regex, replace));
			out.writeInt(cmdCode[10]);
			out.writeInt(id);
			out.writeInt(is);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean generateInstCode(StringTokenizer token) throws IOException {
		if (!token.hasMoreTokens()) return false;
		String d = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s = token.nextToken();
		String regex = "t([0-9]+)";
		if (!d.matches(regex)) return false;
		String replace = "$1";
		try {
			int id = Integer.parseInt(d.replaceAll(regex, replace));
			try {
				int is = Integer.parseInt(s);
				out.writeInt(cmdCode[11]);
				out.writeInt(id);
				out.writeInt(is);
			}
			catch (NumberFormatException e) {
				Object t = ht.get(Symbol.symbol(s));
				if (t != null) {
					out.writeInt(cmdCode[11]);
					out.writeInt(id);
					out.writeInt(((Integer)t).intValue());
				}
				else {
					int addr = getSystemFunctionMapping(s);
					if (addr == -1) return false;
					out.writeInt(cmdCode[11]);
					out.writeInt(id);
					out.writeInt(addr * WORD_SIZE);
				}
			}
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean generateLoadCode(StringTokenizer token) throws IOException {
		if (!token.hasMoreTokens()) return false;
		String d = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s = token.nextToken();
		String regex = "t([0-9]+)";
		if (!d.matches(regex) || !s.matches(regex)) return false;
		String replace = "$1";
		try {
			int id = Integer.parseInt(d.replaceAll(regex, replace));
			int is = Integer.parseInt(s.replaceAll(regex, replace));
			out.writeInt(cmdCode[13]);
			out.writeInt(id);
			out.writeInt(is);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean generateStoreCode(StringTokenizer token) throws IOException {
		if (!token.hasMoreTokens()) return false;
		String d = token.nextToken();
		if (!token.hasMoreTokens()) return false;
		String s = token.nextToken();
		String regex = "t([0-9]+)";
		if (!d.matches(regex) || !s.matches(regex)) return false;
		String replace = "$1";
		try {
			int id = Integer.parseInt(d.replaceAll(regex, replace));
			int is = Integer.parseInt(s.replaceAll(regex, replace));
			out.writeInt(cmdCode[12]);
			out.writeInt(id);
			out.writeInt(is);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean generateJumpCode(StringTokenizer token) throws IOException {
		if (!token.hasMoreTokens()) return false;
		String d = token.nextToken();
		String regex = "t([0-9]+)";
		if (!d.matches(regex)) return false;
		String replace = "$1";
		try {
			int id = Integer.parseInt(d.replaceAll(regex, replace));
			out.writeInt(cmdCode[14]);
			out.writeInt(id);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private int getSystemFunctionMapping(String name) {
		String sysfunc[] = {
			"print",
			"printi",
			"flush",
			"getchar",
			"ord",
			"chr",
			"size",
			"substring",
			"concat",
			"not",
			"exit",
			"initArray",
			"malloc",
			"stringEqual"};
		int funcaddr[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
		for (int i = 0; i < sysfunc.length; i++)
			if (name.equals(sysfunc[i])) return funcaddr[i];
		return -1;
	}

	private boolean generateTemporary(StringTokenizer token) throws IOException{
		if (!token.hasMoreTokens()) return false;
		String p = token.nextToken();
		String regex = "t([0-9]+)";
		if (!p.matches(regex)) return false;
		String replace = "$1";
		try {
			int ip = Integer.parseInt(p.replaceAll(regex, replace));
			out.writeInt(ip);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
}