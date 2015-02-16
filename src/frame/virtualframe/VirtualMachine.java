package frame.virtualframe;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-5
 * Time: 13:49:13
 * To change this template use Options | File Templates.
 */
public class VirtualMachine {
	private static final int ADD = 1;
	private static final int SUB = 2;
	private static final int MUL = 3;
	private static final int DIV = 4;
	private static final int AND = 5;
	private static final int OR = 6;
	private static final int LS = 7;
	private static final int RS = 8;
	private static final int ARS = 9;
	private static final int XOR = 10;
	private static final int MOVE = 11;
	private static final int INST = 12;
	private static final int STORE = 13;
	private static final int LOAD = 14;
	private static final int JMP = 15;
	private static final int JE = 16;
	private static final int JGE = 17;
	private static final int JG = 18;
	private static final int PUSH = 19;
	private static final int POP = 20;
	private static final int WORD_SIZE = 4;

	private int[] temporary;	//temporary variables
	private int[] heap;			//heap(for global variables, start at 0x20000000)
	private int[] stack;		//stack(for activation records, start at 0x10000000)
	private int[] code;			//code segment(for code and string literal, start at 0x00000000)
	private boolean running;
	private int ip;	//instruction pointer
	private int fp;	//frame pointer
	private int sp;	//stack pointer
	private int rv;	//result variable
	private int hp;	//heap pointer
	private int nTemp;	//number of temp
	private TempList templist;

	public VirtualMachine(int hsize, int ssize, int csize) {
		heap = new int[hsize];
		stack = new int[ssize];
		code = new int[csize];
	}

	public void run(String filename) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(filename));
			//read sp, fp, rv
			try {
				sp = in.readInt();
				fp = in.readInt();
				rv = in.readInt();
				nTemp = in.readInt();
				temporary = new int[nTemp];
			}
			catch (EOFException e) {
				System.out.println("System Error: data format error");
				return;
			}
			//read program
			int i = 0x100 / WORD_SIZE;
			while (true) {
				try {
					code[i] = in.readInt();
					i++;
				}
				catch (EOFException e) {
					break;
				}
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found.");
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			//init
			running = true;
			ip = 0x100 / WORD_SIZE;
			temporary[sp] = 0x10000000;
			hp = 0;
			//running
			while (running) fetchInstruction();
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("System Error: out of memory");
		}
	}

	private void fetchInstruction() {
		int cmd = code[ip];
		switch (cmd) {
			case ADD:
				temporary[code[ip + 1]] = temporary[code[ip + 2]] + temporary[code[ip + 3]];
				ip += 4;
				break;
			case SUB:
				temporary[code[ip + 1]] = temporary[code[ip + 2]] - temporary[code[ip + 3]];
				ip += 4;
				break;
			case MUL:
				temporary[code[ip + 1]] = temporary[code[ip + 2]] * temporary[code[ip + 3]];
				ip += 4;
				break;
			case DIV:
				temporary[code[ip + 1]] = temporary[code[ip + 2]] / temporary[code[ip + 3]];
				ip += 4;
				break;
			case AND:
			case OR:
			case LS:
			case RS:
			case ARS:
			case XOR:
				//todo:unimplemented
				break;
			case MOVE:
				temporary[code[ip + 1]] = temporary[code[ip + 2]];
				ip += 3;
				break;
			case INST:
				temporary[code[ip + 1]] = code[ip + 2];
				ip += 3;
				break;
			case STORE:
				MemoryAccess sma = getMemoryAccess(temporary[code[ip + 2]]);
				sma.base[sma.offset] = temporary[code[ip + 1]];
				ip += 3;
				break;
			case LOAD:
				MemoryAccess lma = getMemoryAccess(temporary[code[ip + 2]]);
				temporary[code[ip + 1]] = lma.base[lma.offset];
				ip += 3;
				break;
			case JMP:
				int addr = temporary[code[ip + 1]];
				if (addr < 0x100) systemCall(addr / WORD_SIZE);
				else ip = addr / WORD_SIZE;
				break;
			case JE:
				if (temporary[code[ip + 2]] != temporary[code[ip + 3]]) ip += 4;
				else {
					int jaddr = temporary[code[ip + 1]];
					if (jaddr < 0x100) systemCall(jaddr / WORD_SIZE);
					else ip = jaddr / WORD_SIZE;
				}
				break;
			case JGE:
				if (temporary[code[ip + 2]] < temporary[code[ip + 3]]) ip += 4;
				else {
					int jaddr = temporary[code[ip + 1]];
					if (jaddr < 0x100) systemCall(jaddr / WORD_SIZE);
					else ip = jaddr / WORD_SIZE;
				}
				break;
			case JG:
				if (temporary[code[ip + 2]] <= temporary[code[ip + 3]]) ip += 4;
				else {
					int jaddr = temporary[code[ip + 1]];
					if (jaddr < 0x100) systemCall(jaddr / WORD_SIZE);
					else ip = jaddr / WORD_SIZE;
				}
				break;
			case PUSH:
				templist = new TempList(temporary, templist);
				temporary = new int[nTemp];
				for (int i = 0; i < nTemp; i++) temporary[i] = templist.temp[i];
				ip += 1;
				break;
			case POP:
				int bsp = temporary[sp];
				int bfp = temporary[fp];
				int brv = temporary[rv];
				temporary = templist.temp;
				if (templist == null) {
					System.out.println("System Error: stack overflow");
					running = false;
					return;
				}
				templist = templist.next;
				temporary[sp] = bsp;
				temporary[fp] = bfp;
				temporary[rv] = brv;
				ip += 1;
				break;
		}
	}

	private MemoryAccess getMemoryAccess(int addr) {
		int[] base;
		int off;
		if (addr >= 0x20000000) {
			base = heap;
			off = (addr - 0x20000000) / WORD_SIZE;
		}
		else if (addr >= 0x10000000) {
			base = stack;
			off = (addr - 0x10000000) / WORD_SIZE;
		}
		else {
			base = code;
			off = addr / WORD_SIZE;
		}
		return new MemoryAccess(base, off);
	}

	private void systemCall(int addr) {
		MemoryAccess ma = getMemoryAccess(temporary[sp]);
		switch (addr) {
			case 0:	//print
				MemoryAccess str = getMemoryAccess(ma.base[ma.offset + 3]);
				int l = str.base[str.offset];
				for (int i = 0; i < l; i++) System.out.print((char)str.base[str.offset + i + 1]);
				break;
			case 1:	//printi
				int i = ma.base[ma.offset + 3];
				System.out.print(i);
				break;
			case 2:	//flush
				System.out.flush();
				break;
			case 3:	//getchar
				try {
					int c;
					do {
						c = System.in.read();
					}
					while (c == '\r');
					heap[hp] = 1;
					heap[hp + 1] = c;
					temporary[rv] = hp * WORD_SIZE + 0x20000000;
					hp += 2;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 4:	//ord
				str = getMemoryAccess(ma.base[ma.offset + 3]);
				l = str.base[str.offset];
				if (l == 0) temporary[rv] = -1;
				else temporary[rv] = str.base[str.offset + 1];
				break;
			case 5:	//chr
				int k = ma.base[ma.offset + 3];
				heap[hp] = 1;
				heap[hp + 1] = k;
				temporary[rv] = hp * WORD_SIZE + 0x20000000;
				hp += 2;
				break;
			case 6:	//size
				str = getMemoryAccess(ma.base[ma.offset + 3]);
				l = str.base[str.offset];
				temporary[rv] = l;
				break;
			case 7:	//substring
				str = getMemoryAccess(ma.base[ma.offset + 5]);
				l = str.base[str.offset];
				int st = ma.base[ma.offset + 4];
				int len = ma.base[ma.offset + 3];
				if (st < 0) st = 0;
				if (st + len >= l) len = l - 1 - st;
				heap[hp] = len;
				for (int p = 0; p < len; p++)
					heap[hp + p + 1] = str.base[str.offset + 1 + st + p];
				temporary[rv] = hp * WORD_SIZE + 0x20000000;
				hp += len + 1;
				break;
			case 8:	//concat
				MemoryAccess str1 = getMemoryAccess(ma.base[ma.offset + 4]);
				MemoryAccess str2 = getMemoryAccess(ma.base[ma.offset + 3]);
				int l1 = str1.base[str1.offset];
				int l2 = str1.base[str2.offset];
				heap[hp] = l1 + l2;
				for (int p = 0; p < l1; p++)
					heap[hp + p + 1] = str1.base[str1.offset + 1 + p];
				for (int p = 0; p < l2; p++)
					heap[hp + l1 + p + 1] = str2.base[str2.offset + 1 + p];
				temporary[rv] = hp * WORD_SIZE + 0x20000000;
				hp += l1 + l2 + 1;
				break;
			case 9:	//not
				int n = ma.base[ma.offset + 3];
				if (n != 0) temporary[rv] = 0;
				else temporary[rv] = 1;
				break;
			case 10:	//exit
				running = false;
				break;
			case 11:	//initArray
				int size = ma.base[ma.offset + 2];
				int init = ma.base[ma.offset + 3];
				for (int p = hp; p < hp + size; p++) heap[p] = init;
				temporary[rv] = hp * WORD_SIZE + 0x20000000;
				hp += size;
				break;
			case 12:	//malloc
				len = ma.base[ma.offset + 2];
				temporary[rv] = hp * WORD_SIZE + 0x20000000;
				hp += len;
				break;
			case 13:	//stringEqual
				str1 = getMemoryAccess(ma.base[ma.offset + 2]);
				str2 = getMemoryAccess(ma.base[ma.offset + 3]);
				l1 = str1.base[str1.offset];
				l2 = str2.base[str2.offset];
				if (l1 != l2) temporary[rv] = 0;
				else {
					int p = 0;
					while (p < l1 && str1.base[str1.offset + 1 + p] == str2.base[str2.offset + 1 + p]) p++;
					if (p == l1) temporary[rv] = 1;
					else temporary[rv] = 0;
				}
				break;
		}
		ip = ma.base[ma.offset + 1] / WORD_SIZE;
	}
}

class MemoryAccess {
	public int[] base;
	public int offset;

	public MemoryAccess(int[] b, int o) {
		base = b;
		offset = o;
	}
}

class TempList {
	public int[] temp;
	public TempList next;

	public TempList(int[] t, TempList n) {
		temp = t;
		next = n;
	}
}