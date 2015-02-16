package frame.virtualframe;

import temp.Temp;
import tree.Exp;
import tree.TEMP;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-7
 * Time: 14:39:42
 * To change this template use Options | File Templates.
 */
class InReg extends frame.Access {
	public Temp temp;

	public Exp exp(Exp fp) {
		return new TEMP(temp);
	}

	public int getOffset() {
		return 0;
	}
}