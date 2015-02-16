package translate;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-12-6
 * Time: 21:31:06
 * To change this template use Options | File Templates.
 */
public class Access {
	private Level home;
	private frame.Access acc;

	public Access(Level h, frame.Access a) {
		home = h;
		acc = a;
	}

	public frame.Access getAccess() {
		return acc;
	}

	public Level getHome() {
		return home;
	}
}