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
class VarResult {
	private Type type;
	private Exp exp;
	private boolean readonly;

	public VarResult(Type t, Exp e, boolean r) {
		type = t;
		exp = e;
		readonly = r;
	}

	public Type getType() {
		return type;
	}

	public Exp getExp() {
		return exp;
	}

	public boolean isReadonly() {
		return readonly;
	}
}