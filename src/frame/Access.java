package frame;

import tree.Exp;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-6
 * Time: 21:15:17
 * To change this template use Options | File Templates.
 */
public abstract class Access {
	public abstract Exp exp(Exp fp);

	public abstract int getOffset();
}