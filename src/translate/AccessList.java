package translate;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-6
 * Time: 21:31:12
 * To change this template use Options | File Templates.
 */
public class AccessList {
	public Access head;
	public AccessList tail;

	public AccessList(Access h, AccessList t) {
		head = h;
		tail = t;
	}
}