package semant;

import types.Type;
import translate.Exp;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-11-22
 * Time: 20:28:52
 * To change this template use Options | File Templates.
 */
class ExpResult {
	private Type type;
	private Exp exp;

	public ExpResult(Type t, Exp e) {
		type = t;
		exp = e;
	}

	public Type getType() {
		return type;
	}

	public Exp getExp() {
		return exp;
	}
}