package translate;

import temp.Label;
import temp.Temp;
import tree.*;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-3
 * Time: 16:28:40
 * To change this template use Options | File Templates.
 */
public class IfThenElseExp extends Exp {
	private Exp cond;
	private Exp a;
	private Exp b;
	private Label t;
	private Label f;
	private Label join;

	public IfThenElseExp(Exp cc, Exp aa, Exp bb) {
		cond = cc;
		a = aa;
		b = bb;
		t = new Label();
		f = new Label();
		join = new Label();
	}

	public boolean canEx() {
		if (b == null) return false;
		if (!a.canEx()) return false;
		if (!b.canEx()) return false;
		return true;
	}

	public tree.Exp unEx() {
		if (b != null) {
			Temp r = new Temp();
			Stm s1 = cond.unCx(t, f);
			Stm s2 = new LABEL(t);
			Stm s3 = new MOVE(new TEMP(r), a.unEx());
			Stm s4 = new JUMP(join);
			Stm s5 = new LABEL(f);
			Stm s6 = new MOVE(new TEMP(r), b.unEx());
			Stm s7 = new LABEL(join);
			SEQ seq = new SEQ(s1, new SEQ(s2, new SEQ(s3, new SEQ(s4, new SEQ(s5, new SEQ(s6, s7))))));
			return new ESEQ(seq, new TEMP(r));
		}
		else return null;
	}

	public Stm unNx() {
		if (b != null) {
			Stm s1 = cond.unCx(t, f);
			Stm s2 = new LABEL(t);
			Stm s3 = a.unNx();
			Stm s4 = new JUMP(join);
			Stm s5 = new LABEL(f);
			Stm s6 = b.unNx();
			Stm s7 = new LABEL(join);
			SEQ seq = new SEQ(s1, new SEQ(s2, new SEQ(s3, new SEQ(s4, new SEQ(s5, new SEQ(s6, s7))))));
			return seq;
		}
		else {
			Stm s1 = cond.unCx(t, join);
			Stm s2 = new LABEL(t);
			Stm s3 = a.unNx();
			Stm s4 = new LABEL(join);
			SEQ seq = new SEQ(s1, new SEQ(s2, new SEQ(s3, s4)));
			return seq;
		}
	}

	public Stm unCx(Label tt, Label ff) {
		return new CJUMP(CJUMP.EQ, unEx(), new CONST(0), ff, tt);
	}
}