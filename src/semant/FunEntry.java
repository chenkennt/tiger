package semant;

import types.Type;
import types.RECORDFIELD;
import translate.Level;
import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-11-22
 * Time: 20:00:02
 * To change this template use Options | File Templates.
 */
class FunEntry extends Entry {
	public Type result;
	public RECORDFIELD formals;
	public Level level;
	public Label label;

	public FunEntry(Level l, Type r, RECORDFIELD f, Label la) {
		level = l;
		result = r;
		formals = f;
		label = la;
	}

	public boolean canInit(RECORDFIELD f) {
        RECORDFIELD p = f;
		RECORDFIELD r = formals;
		while (p != null && r != null) {
			if (!p.fieldType.canAssign(r.fieldType)) return false;	//incompatible types
			p = p.tail;
			r = r.tail;
		}
		if (p != null || r != null) return false;	//number of argument inconsisitent
		return true;
	}
}