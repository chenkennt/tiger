package translate;

import frame.Frame;
import symbol.Symbol;
import util.BoolList;
import temp.Label;
import temp.Temp;
import absyn.OpExp;
import tree.*;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-6
 * Time: 21:31:01
 * To change this template use Options | File Templates.
 */
public class Level {
	private Frame frame;
    public AccessList formals;
	private Level parent;
	public int depth;

	public Level(Level parent, Symbol name, BoolList fmls) {
		this.parent = parent;
		frame = parent.frame.newFrame(new Label(name), new BoolList(true, fmls));
		frame.AccessList p = frame.formals;
		AccessList f = null;
		while (p != null) {
			if (f == null) formals = f = new AccessList(new Access(this, p.head), null);
			else {
				f.tail = new AccessList(new Access(this, p.head), null);
				f = f.tail;
			}
			p = p.tail;
		}
		depth = parent.depth + 1;
	}

	public Level(Frame f) {
		frame = f;
		frame.AccessList p = frame.formals;
		formals = null;
		AccessList t = null;
		while (p != null) {
			if (t == null) formals = t = new AccessList(new Access(this, p.head), null);
			else {
				t.tail = new AccessList(new Access(this, p.head), null);
				t = t.tail;
			}
			p = p.tail;
		}
		depth = 0;
	}

	public Frame getFrame() {
		return frame;
	}

	public Access allocLocal(boolean escape) {
		return new Access(this, frame.allocLocal(escape));
	}

	public Exp simpleVar(Access a) {
        Level p = this;
		tree.Exp s = new TEMP(frame.FP());
		while (p != null && p != a.getHome()) {
			s = new MEM(new BINOP(BINOP.PLUS, s, new CONST(p.formals.head.getAccess().getOffset())));
			p = p.parent;
		}
		return new Ex(a.getAccess().exp(s));
	}

	public Exp opExp(int b, Exp l, Exp r) {
		switch (b) {
			//arithmetic
			case OpExp.PLUS:
				return new Ex(new BINOP(BINOP.PLUS, l.unEx(), r.unEx()));
			case OpExp.MINUS:
				return new Ex(new BINOP(BINOP.MINUS, l.unEx(), r.unEx()));
			case OpExp.MUL:
				return new Ex(new BINOP(BINOP.MUL, l.unEx(), r.unEx()));
			case OpExp.DIV:
				return new Ex(new BINOP(BINOP.DIV, l.unEx(), r.unEx()));
			//arithmetic comparison
			case OpExp.LT:
				return new RelCx(CJUMP.LT, l, r);
			case OpExp.LE:
				return new RelCx(CJUMP.LE, l, r);
			case OpExp.GT:
				return new RelCx(CJUMP.GT, l, r);
			case OpExp.GE:
				return new RelCx(CJUMP.GE, l, r);
			//variable comparison
			case OpExp.EQ:
				return new RelCx(CJUMP.EQ, l, r);
			case OpExp.NE:
				return new RelCx(CJUMP.NE, l, r);
		}
		//it is impossible
		return null;
	}

	public Exp stringCompare(Exp l, Exp r, int relop) {
		Exp e = new Ex(frame.externalCall("stringEqual", new tree.ExpList(l.unEx(), new tree.ExpList(r.unEx(), null))));
		if (relop == OpExp.EQ) return e;
		else if (relop == OpExp.NE) return new Ex(new BINOP(BINOP.MINUS, new CONST(1), e.unEx()));
		else return null;	//it is impossible
	}

	public Exp ifExp(Exp cond, Exp thene, Exp elsee) {
		return new IfThenElseExp(cond, thene, elsee);
	}

	public Exp ifExp(Exp cond, Exp thene) {
		return new IfThenElseExp(cond, thene, null);
	}

	public Exp intExp(int value) {
        return new Ex(new CONST(value));
	}

	public Exp stringExp(String value, Translate t) {
		Label l = t.putString(value);
		return new Ex(new NAME(l));
	}

	public Exp nilExp() {
		return new Ex(new CONST(0));
	}

	public Exp initRecord(ExpList fields) {
		ExpList p = fields;
		int n = 0;
		while (p != null) {
			p = p.tail;
			n++;
		}
		Temp r = new Temp();
		//allocate memory
		Stm s = new MOVE(new TEMP(r), frame.externalCall("malloc", new tree.ExpList(new CONST(n * frame.wordSize()), null)));
		//initialize fields
		p = fields;
		int i = 0;
		while (p != null) {
			s = new SEQ(s, new MOVE(new MEM(new BINOP(BINOP.PLUS, new TEMP(r), new CONST(i * frame.wordSize()))), p.head.unEx()));
			i++;
			p = p.tail;
		}
		return new Ex(new ESEQ(s, new TEMP(r)));
	}

	public Exp initArray(Exp size, Exp init) {
		return new Ex(frame.externalCall("initArray", new tree.ExpList(size.unEx(), new tree.ExpList(init.unEx(), null))));
	}

	public Exp assignExp(Exp l, Exp r) {
		return new Nx(new MOVE(l.unEx(), r.unEx()));
	}

	public Exp seqExp(ExpList exps) {
		if (exps == null) return dummyExp();	//it is impossible

		ExpList p = exps;
		Stm s = null;
		Temp r = new Temp();
		while (p.tail != null) {
			if (s == null) s = p.head.unNx();
			else s = new SEQ(s, p.head.unNx());
			p = p.tail;
		}
		Stm l;
		if (!p.head.canEx()) l = p.head.unNx();
		else l = new MOVE(new TEMP(r), p.head.unEx());
		if (s == null) s = l;
		else s = new SEQ(s, l);
		return new Ex(new ESEQ(s, new TEMP(r)));
	}

	public Exp callExp(Label name, Level level, ExpList fmls) {
		int n = depth - level.depth + 1;
		tree.Exp s = new TEMP(frame.FP());
		for (int i = 0; i < n; i++)
			s = new MEM(new BINOP(BINOP.PLUS, s, new CONST(formals.head.getAccess().getOffset())));
		tree.ExpList f = new tree.ExpList(s, null);
		tree.ExpList t = f;
		ExpList p = fmls;
		while (p != null) {
			t.tail = new tree.ExpList(p.head.unEx(), null);
			t = t.tail;
			p = p.tail;
		}

		return new Ex(new CALL(new NAME(name), f));
	}

	public Exp whileExp(Exp test, Exp body, Label done) {
		Label start = new Label();
		Label main = new Label();
		Stm s1 = new LABEL(start);
		Stm s2 = test.unCx(main, done);
		Stm s3 = new LABEL(main);
		Stm s4 = body.unNx();
		Stm s5 = new JUMP(start);
		Stm s6 = new LABEL(done);
		return new Nx(new SEQ(s1, new SEQ(s2, new SEQ(s3, new SEQ(s4, new SEQ(s5, s6))))));
	}

	public Exp breakExp(Label done) {
		if (done == null) return null;	//it is impossible

        return new Nx(new JUMP(done));
	}

	public Exp forExp(Access acc, Exp init, Exp hi, Exp body, Label done) {
		Temp h = new Temp();
		Label main = new Label();
		Label start = new Label();
		//init
		Stm s1 = init.unNx();
		Stm s2 = new MOVE(new TEMP(h), hi.unEx());
		//check
		Stm s3 = new LABEL(start);
		Stm s4 = new RelCx(CJUMP.LE, simpleVar(acc), new Ex(new TEMP(h))).unCx(main, done);
		//body
        Stm s5 = new LABEL(main);
		Stm s6 = body.unNx();
		//increase loop variable
        Stm s7 = new MOVE(simpleVar(acc).unEx(), new BINOP(BINOP.PLUS, simpleVar(acc).unEx(), new CONST(1)));
		Stm s8 = new JUMP(start);
		//done
		Stm s9 = new LABEL(done);
		return new Nx(new SEQ(s1, new SEQ(s2, new SEQ(s3, new SEQ(s4, new SEQ(s5, new SEQ(s6, new SEQ(s7, new SEQ(s8, s9)))))))));
	}

	public Exp dummyExp() {
		return new Ex(new CONST(0));
	}

	public Exp subscriptVar(Exp var, Exp index) {
		return new Ex(new MEM(new BINOP(BINOP.PLUS, var.unEx(), new BINOP(BINOP.MUL, index.unEx(), new CONST(frame.wordSize())))));
	}

	public Exp fieldVar(Exp var, int index) {
		return new Ex(new MEM(new BINOP(BINOP.PLUS, var.unEx(), new CONST(index * frame.wordSize()))));
	}

	public void procEntryExit(Exp body, Label l, Translate t) {
		t.procEntryExit(this, body, l);
	}
}