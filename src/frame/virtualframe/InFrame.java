package frame.virtualframe;

import tree.*;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-7
 * Time: 14:39:49
 * To change this template use Options | File Templates.
 */
class InFrame extends frame.Access {
	private int offset;

	public InFrame(int o) {
		offset = o;
	}

	public Exp exp(Exp fp) {
		return new MEM(new BINOP(BINOP.PLUS, fp, new CONST(offset)));
	}

	public int getOffset() {
		return offset;
	}
}