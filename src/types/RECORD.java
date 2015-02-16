package types;

import symbol.Symbol;

public class RECORD extends Type {
	public RECORDFIELD fields;

	public RECORD(RECORDFIELD f) {
		fields = f;
	}

	public boolean coerceTo(Type t) {
		return this == t.actual();
	}

	public boolean canInit(RECORDFIELD f) {
        RECORDFIELD p = f;
		RECORDFIELD r = fields;
		while (p != null && r != null) {
			if (!p.fieldType.canAssign(r.fieldType)) return false;	//incompatible types
			if (p.fieldName != r.fieldName) return false;			//incompatible names
			p = p.tail;
			r = r.tail;
		}
		if (p != null || r != null) return false;	//number of argument inconsisitent
		return true;
	}

/*	public boolean canInit(RECORDFIELD r) {
		java.util.Dictionary dict = new java.util.Hashtable();
		RECORDFIELD p = fields;
		while (p != null) {
			dict.put(p.fieldName, p.fieldType);
			p = p.tail;
		}
		p = r;
		while (p != null) {
			Type t = (Type)dict.get(p.fieldName);
			if (t == null) return false;	//field is not declared
			if (!p.fieldType.canAssign(t)) return false;	//incompatible types
			dict.remove(p.fieldName);
			p = p.tail;
		}
		if (!dict.isEmpty()) return false;
		return true;
	}*/

	public Type getFieldType(Symbol symbol) {
		RECORDFIELD p = fields;
		while (p != null) {
			if (p.fieldName == symbol) return p.fieldType;
			p = p.tail;
		}
		return null;
	}

	public int getFieldIndex(Symbol symbol) {
		RECORDFIELD p = fields;
		int i = 0;
		while (p != null) {
			if (p.fieldName == symbol) return i;
			i++;
			p = p.tail;
		}
		return 0;
	}
}