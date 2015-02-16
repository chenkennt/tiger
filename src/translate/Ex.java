package translate;

import tree.*;
import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-2
 * Time: 21:37:33
 * To change this template use Options | File Templates.
 */
class Ex extends Exp {
    private tree.Exp exp;

	public Ex(tree.Exp e) {
		exp = e;
	}

	public tree.Exp unEx() {
		return exp;
	}

	public tree.Stm unNx() {
		return new EXPSTM(exp);
	}

	public tree.Stm unCx(Label t, Label f) {
		return new CJUMP(CJUMP.EQ, unEx(), new CONST(0), f, t);
	}

	public boolean canEx() {
		return true;
	}
}