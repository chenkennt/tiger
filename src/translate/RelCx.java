package translate;

import tree.Stm;
import tree.CJUMP;
import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-3
 * Time: 16:52:19
 * To change this template use Options | File Templates.
 */
public class RelCx extends Cx {
	private Exp left;
	private Exp right;
	private int relop;

	public RelCx(int op, Exp l, Exp r) {
		left = l;
		right = r;
		relop = op;
	}

	public Stm unCx(Label t, Label f) {
		return new CJUMP(relop, left.unEx(), right.unEx(), t, f);
	}
}