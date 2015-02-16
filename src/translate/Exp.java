package translate;

import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-11-22
 * Time: 20:30:22
 * To change this template use Options | File Templates.
 */
public abstract class Exp {
	public abstract tree.Exp unEx();
	public abstract tree.Stm unNx();
	public abstract tree.Stm unCx(Label t, Label f);
	public abstract boolean canEx();
}