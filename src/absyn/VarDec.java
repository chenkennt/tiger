package absyn;

import symbol.Symbol;

public class VarDec extends Dec {
	public Symbol name;
	public boolean escape = true;
	public NameTy typ; /* optional */
	public Exp init;
	public boolean readonly;

	public VarDec(int p, Symbol n, NameTy t, Exp i) {
		pos = p;
		name = n;
		typ = t;
		init = i;
		readonly = false;
	}

	public VarDec(int p, Symbol n, NameTy t, Exp i, boolean r) {
		pos = p;
		name = n;
		typ = t;
		init = i;
		readonly = r;
	}
}