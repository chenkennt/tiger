package semant;

import types.Type;
import translate.Access;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-11-22
 * Time: 20:00:26
 * To change this template use Options | File Templates.
 */
class VarEntry extends Entry {
	public Type type;
	public boolean readonly;
    public Access access;

	public VarEntry(Access a, Type t, boolean r) {
		access = a;
		type = t;
		readonly = r;
	}
}