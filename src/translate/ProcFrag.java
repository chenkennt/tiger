package translate;

import tree.Stm;
import frame.Frame;
import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-4
 * Time: 13:59:44
 * To change this template use Options | File Templates.
 */
public class ProcFrag extends Frag {
	public Stm body;
	public Frame frame;
	public Label label;

	public ProcFrag(Stm b, Frame f, Label l) {
		body = b;
		frame = f;
		label = l;
	}
}