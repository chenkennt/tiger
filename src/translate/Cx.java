package translate;

import temp.Temp;
import temp.Label;
import tree.*;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-2
 * Time: 21:44:49
 * To change this template use Options | File Templates.
 */
abstract class Cx extends Exp {
	public tree.Exp unEx() {
        Temp r = new Temp();
		Label t = new Label();
		Label f = new Label();
		Stm s1 = new MOVE(new TEMP(r), new CONST(1));
		Stm s2 = unCx(t, f);
		Stm s3 = new LABEL(f);
		Stm s4 = new MOVE(new TEMP(r), new CONST(0));
		Stm s5 = new LABEL(t);
		SEQ seq = new SEQ(s1, new SEQ(s2, new SEQ(s3, new SEQ(s4, s5))));
        return new ESEQ(seq, new TEMP(r));
	}

	public tree.Stm unNx() {
		Label l = new Label();
		Stm s1 = unCx(l, l);
		Stm s2 = new LABEL(l);
		return new SEQ(s1, s2);
	}

	public boolean canEx() {
		return true;
	}
}