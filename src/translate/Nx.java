package translate;

import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-2
 * Time: 21:41:27
 * To change this template use Options | File Templates.
 */
class Nx extends Exp {
    private tree.Stm stm;

	public Nx(tree.Stm s) {
		stm = s;
	}

	public tree.Exp unEx() {
		//it is impossible
		return null;
	}

	public tree.Stm unNx() {
		return stm;
	}

	public tree.Stm unCx(Label t, Label f) {
		//it is impossible
		return null;
	}

	public boolean canEx() {
		return false;
	}
}