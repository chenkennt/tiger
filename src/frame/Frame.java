package frame;

import temp.Label;
import temp.Temp;
import util.BoolList;
import tree.Exp;
import tree.ExpList;
import tree.Stm;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-6
 * Time: 21:14:48
 * To change this template use Options | File Templates.
 */
public abstract class Frame {
	public Label name;
    public AccessList formals;

	public abstract Temp FP();
	public abstract int wordSize();
	public abstract Frame newFrame(Label n, BoolList fmls);
	public abstract Access allocLocal(boolean escape);
	public abstract Exp externalCall(String name, ExpList args);
	public abstract Temp SP();
	public abstract Temp RV();
	public abstract Stm procEntryExit(Label l, Stm body);

	public Frame(Label n) {
		name = n;
		formals = null;
	}

	public Frame() {
		formals = null;
	}
}