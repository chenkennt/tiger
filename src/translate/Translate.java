package translate;

import temp.Label;
import tree.*;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-4
 * Time: 14:03:48
 * To change this template use Options | File Templates.
 */
public class Translate {
    private Frag frags;
	private Frag curr;
	private ProcFrag main;

	public Translate() {
		frags = null;
		curr = null;
	}

	private void addFrag(Frag f) {
		if (curr == null) frags = curr = f;
		else {
			curr.next = f;
			curr = f;
		}
	}

	public void procEntryExit(Level level, Exp body, Label l) {
		Stm stm = null;
		if (!body.canEx()) stm = body.unNx();
		else stm = new MOVE(new TEMP(level.getFrame().RV()), body.unEx());
		addFrag(new ProcFrag(stm, level.getFrame(), l));
	}

	public Label putString(String s) {
		Label l = new Label();
		addFrag(new DataFrag(s, l));
		return l;
	}

	public Frag getResult() {
		return frags;
	}

	public void addMain(Level level, Exp body) {
		main = new ProcFrag(new SEQ(body.unNx(), new EXPSTM(new CALL(new NAME(new Label("exit")), null))), level.getFrame(), new Label());
		addFrag(main);
	}

	public ProcFrag getMain() {
		return main;
	}
}