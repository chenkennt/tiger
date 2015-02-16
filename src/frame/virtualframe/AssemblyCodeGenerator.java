package frame.virtualframe;

import java.io.PrintStream;
import translate.*;
import tree.*;
import tree.Exp;
import tree.ExpList;
import temp.Temp;
import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-4
 * Time: 16:39:08
 * To change this template use Options | File Templates.
 */
public class AssemblyCodeGenerator extends frame.AssemblyCodeGenerator {
	public AssemblyCodeGenerator(PrintStream o) {
		super(o);
	}

	public void generate(Translate t) {
		Frag f = t.getResult();
		while (f != null) {
			if (f instanceof ProcFrag) {
				ProcFrag pf = (ProcFrag)f;
				emit("SP", pf.frame.SP().toString());
				emit("FP", pf.frame.FP().toString());
				emit("RV", pf.frame.RV().toString());
				Temp r = new Temp();
				emit("INST", r.toString(), t.getMain().label.toString());
				emit("JMP", r.toString());
				break;
			}
			f = f.next;
		}
		f = t.getResult();
		//translate fragments
		while (f != null) {
			if (f instanceof DataFrag) {
				DataFrag df = (DataFrag)f;
				emit(df.label, df.data);
			}
			else if (f instanceof ProcFrag) {
				ProcFrag pf = (ProcFrag)f;
				transStm(pf.frame.procEntryExit(pf.label, pf.body), pf.frame);
			}
			f = f.next;
		}
	}

	private Temp transExp(Exp exp, frame.Frame frame) {
//		comment("begin of " + exp.getClass().toString());
		if (exp instanceof BINOP) {
			Temp t = new Temp();
			BINOP binop = (BINOP)exp;
			Temp l = transExp(binop.left, frame);
			Temp r = transExp(binop.right, frame);
			String op = null;
			switch (binop.binop) {
				case BINOP.PLUS:
					op = "ADD";
					break;
				case BINOP.MINUS:
					op = "SUB";
					break;
				case BINOP.MUL:
					op = "MUL";
					break;
				case BINOP.DIV:
					op = "DIV";
					break;
				case BINOP.AND:
					op = "AND";
					break;
				case BINOP.OR:
					op = "OR";
					break;
				case BINOP.LSHIFT:
					op = "LS";
					break;
				case BINOP.RSHIFT:
					op = "RS";
					break;
				case BINOP.ARSHIFT:
					op = "ARS";
					break;
				case BINOP.XOR:
					op = "XOR";
					break;
			}
			emit(op, t.toString(), l.toString(), r.toString());
			return t;
		}
		else if (exp instanceof CALL) {
			CALL call = (CALL)exp;
			//todo:call exp
			Temp t = new Temp();
			Temp p = new Temp();
			//store arguments
			emit("MOVE", t.toString(), frame.SP().toString());
			emit("INST", p.toString(), String.valueOf(frame.wordSize()));
			emit("ADD", t.toString(), t.toString(), p.toString());
            ExpList e = call.args;
			while (e != null) {
				Temp r = transExp(e.head, frame);
				emit("ADD", t.toString(), t.toString(), p.toString());
				emit("STORE", r.toString(), t.toString());
				e = e.tail;
			}
			//store frame pointer
			emit("MOVE", t.toString(), frame.SP().toString());
			emit("STORE", frame.FP().toString(), t.toString());
			//store return address
			Label l = new Label();
			emit("ADD", t.toString(), t.toString(), p.toString());
			Temp lt = new Temp();
			emit("INST", lt.toString(), l.toString());
			emit("STORE", lt.toString(), t.toString());
			//jump to function
			Temp f = transExp(call.func, frame);
			emitSaveTemp();
			emit("JMP", f.toString());
			//return address;
			emit(l);
			emitRestoreTemp();
			//return temp
			Temp r = new Temp();
			emit("MOVE", r.toString(), frame.RV().toString());
			return r;
		}
		else if (exp instanceof CONST) {
			CONST con = (CONST)exp;
			Temp t = new Temp();
			emit("INST", t.toString(), String.valueOf(con.value));
			return t;
		}
		else if (exp instanceof ESEQ) {
			ESEQ eseq = (ESEQ)exp;
			transStm(eseq.stm, frame);
			return transExp(eseq.exp, frame);
		}
		else if (exp instanceof MEM) {
			MEM mem = (MEM)exp;
			Temp t = new Temp();
			Temp r = transExp(mem.exp, frame);
			emit("LOAD", t.toString(), r.toString());
			return t;
		}
		else if (exp instanceof NAME) {
			NAME name = (NAME)exp;
			Temp t = new Temp();
			emit("INST", t.toString(), name.label.toString());
			return t;
		}
		else if (exp instanceof TEMP) {
			TEMP temp = (TEMP)exp;
			return temp.temp;
		}
		else return null;	//it is impossible
	}

	private void transStm(Stm stm, frame.Frame frame) {
//		comment("begin of " + stm.getClass().toString());
		if (stm instanceof CJUMP) {
			CJUMP cjump = (CJUMP)stm;
			Temp l = transExp(cjump.left, frame);
			Temp r = transExp(cjump.right, frame);
			Temp tt = new Temp();
			Temp tf = new Temp();
			emit("INST", tt.toString(), cjump.iftrue.toString());
			emit("INST", tf.toString(), cjump.iffalse.toString());
			switch (cjump.relop) {
				case CJUMP.EQ:
					emit("JE", tt.toString(), l.toString(), r.toString());
					emit("JMP", tf.toString());
					break;
				case CJUMP.NE:
					emit("JE", tf.toString(), l.toString(), r.toString());
					emit("JMP", tt.toString());
					break;
				case CJUMP.LT:
				case CJUMP.ULT:
					emit("JGE", tf.toString(), l.toString(), r.toString());
					emit("JMP", tt.toString());
					break;
				case CJUMP.GT:
				case CJUMP.UGT:
					emit("JG", tt.toString(), l.toString(), r.toString());
					emit("JMP", tf.toString());
					break;
				case CJUMP.LE:
				case CJUMP.ULE:
					emit("JG", tf.toString(), l.toString(), r.toString());
					emit("JMP", tt.toString());
					break;
				case CJUMP.GE:
				case CJUMP.UGE:
					emit("JGE", tt.toString(), l.toString(), r.toString());
					emit("JMP", tf.toString());
					break;
			}
		}
		else if (stm instanceof EXPSTM) {
			EXPSTM expstm = (EXPSTM)stm;
			transExp(expstm.exp, frame);
		}
		else if (stm instanceof JUMP) {
			//todo:unfinished
			JUMP jump = (JUMP)stm;
			Temp t = transExp(jump.exp, frame);
			emit("JMP", t.toString());
		}
		else if (stm instanceof LABEL) {
			LABEL label = (LABEL)stm;
			emit(label.label);
		}
		else if (stm instanceof MOVE) {
			MOVE move = (MOVE)stm;
			Temp s = transExp(move.src, frame);
			if (move.dst instanceof MEM) {
				Temp d = transExp(((MEM)move.dst).exp, frame);
				emit("STORE", s.toString(), d.toString());
			}
			else {
				Temp d = transExp(move.dst, frame);
				emit("MOVE", d.toString(), s.toString());
			}
		}
		else if (stm instanceof SEQ) {
			SEQ seq = (SEQ)stm;
			transStm(seq.left, frame);
			transStm(seq.right, frame);
		}
	}

	private void emit(String cmd, String d, String s1, String s2) {
		out.println(cmd + " " + d + ", " + s1 + ", " + s2);
	}

	private void emit(Label l) {
		out.println("LABEL " + l.toString());
	}

	private void emit(String cmd, String d, String s) {
		out.println(cmd + " " + d + ", " + s);
	}

	private void emit(String cmd, String d) {
		out.println(cmd + " " + d);
	}

	private void comment(String comment) {
		out.println("//" + comment);
	}

	private void emit(Label l, String s) {
		out.println("LABEL " + l.toString());
		out.println("STRING "+ s.length());
		out.println(s);
	}

	private void emitSaveTemp() {
		out.println("PUSH");
	}

	private void emitRestoreTemp() {
		out.println("POP");
	}
}