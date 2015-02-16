package translate;

import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-4
 * Time: 13:57:45
 * To change this template use Options | File Templates.
 */
public class DataFrag extends Frag {
	public Label label;
	public String data;

	public DataFrag(String d, Label l) {
		data = d;
		label = l;
	}
}