package frame.virtualframe;

import frame.Access;
import temp.Label;
import temp.Temp;
import util.BoolList;
import frame.AccessList;
import tree.*;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-7
 * Time: 14:36:31
 * To change this template use Options | File Templates.
 */
public class Frame extends frame.Frame {
	private int offset;	//stack pointer
	private static Temp fp = new Temp();
	private static Temp sp = new Temp();
	private static Temp rv = new Temp();

	private static final int WORD_SIZE = 4;

	public Frame(Label n) {
		super(n);
		offset = 0;
	}

	public Frame() {
		super();
		offset = 0;
	}

	public Temp FP() {
		return fp;
	}

	public int wordSize() {
		return WORD_SIZE;
	}

	public frame.Frame newFrame(Label n, BoolList fmls) {
		Frame f = new Frame(n);
		AccessList p = null;
		while (fmls != null) {
			if (p == null) f.formals = p = new AccessList(f.allocLocal(fmls.head), null);
			else {
				p.tail = new AccessList(f.allocLocal(fmls.head), null);
				p = p.tail;
			}
			fmls = fmls.tail;
		}
		return f;
	}

	public Access allocLocal(boolean escape) {
		Access local = new InFrame(offset);
		offset += WORD_SIZE;
		return local;
	}

	public Exp externalCall(String name, ExpList args) {
		//todo:to be checked
		return new CALL(new NAME(new Label(name)), args);
	}

	public Temp SP() {
		return sp;
	}

	public Temp RV() {
		return rv;
	}

	public Stm procEntryExit(Label l, Stm body) {
		//label
		Stm s1 = new LABEL(l);
		//adjust frame pointer
		Stm s2 = new MOVE(new TEMP(FP()), new BINOP(BINOP.PLUS, new TEMP(SP()), new CONST(wordSize() * 2)));
		//adjust stack pointer
		Stm s3 = new MOVE(new TEMP(SP()), new BINOP(BINOP.PLUS, new TEMP(FP()), new CONST(offset)));
		//body
		Stm s4 = body;
		//restore stack pointer
		Stm s5 = new MOVE(new TEMP(SP()), new BINOP(BINOP.MINUS, new TEMP(FP()), new CONST(wordSize() * 2)));
		//restore frame pointer
		Stm s6 = new MOVE(new TEMP(FP()), new MEM(new TEMP(SP())));
		//jump back
		Exp ret = new MEM(new BINOP(BINOP.PLUS, new TEMP(SP()), new CONST(wordSize())));
		Stm s7 = new JUMP(ret, null);
		return new SEQ(s1, new SEQ(s2, new SEQ(s3, new SEQ(s4, new SEQ(s5 , new SEQ(s6, s7))))));
	}
}