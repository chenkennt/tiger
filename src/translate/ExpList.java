package translate;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-3
 * Time: 21:56:56
 * To change this template use Options | File Templates.
 */
public class ExpList {
	public Exp head;
	public ExpList tail;

	public ExpList(Exp h, ExpList t) {
		head = h;
		tail = t;
	}
}