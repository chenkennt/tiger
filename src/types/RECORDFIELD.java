package types;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-11-30
 * Time: 15:26:13
 * To change this template use Options | File Templates.
 */
public class RECORDFIELD {
	public symbol.Symbol fieldName;
	public Type fieldType;
	public RECORDFIELD tail;

	public RECORDFIELD(symbol.Symbol n, Type t, RECORDFIELD x) {
		fieldName = n;
		fieldType = t;
		tail = x;
	}
}