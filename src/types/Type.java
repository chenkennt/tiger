package types;

public abstract class Type {
	public Type actual() {
		return this;
	}

	public boolean coerceTo(Type t) {
		return false;
	}

	public boolean isInt() {
		return actual() instanceof INT;
	}

	public boolean isString() {
		return actual() instanceof STRING;
	}

	public boolean isRecord() {
		return actual() instanceof RECORD;
	}

	public boolean isArray() {
		return actual() instanceof ARRAY;
	}

	public boolean isNil() {
		return actual() instanceof NIL;
	}

	public boolean isVoid() {
		return actual() instanceof VOID;
	}

	public boolean canCompare(Type t) {
		if (isVoid() || t.isVoid()) return false;	//it is illegal to compare with void type
		if (isNil() && t.isNil()) return false;		//it is illegal to compare nil with nil
		if (coerceTo(t)) return true;				//it is ok to compare with same type
		if (isNil() && t.isRecord()) return true;	//it is ok to compare nil with record type
		if (t.isNil() && isRecord()) return true;	//it is ok to compare nil with record type
		return false;								//it is illeagal to compare with different type
	}

	public boolean canAssign(Type t) {
		if (t.isNil() || t.isVoid()) return false;	//lvalue cannot be void type or nil type
		if (isVoid()) return false;					//cannot assign void type
		if (coerceTo(t)) return true;				//it is ok to assign to same type
		if (t.isRecord() && isNil()) return true;	//it is ok to assign nil to record type
		return false;								//cannot be assigned to a different type
	}

	public boolean canReturn(Type t) {
		if (isVoid() && t.isVoid()) return true;
		if (canAssign(t)) return true;
		else return false;
	}

	public Type equalType(Type t) {
		if (coerceTo(t)) return t;					//it is ok to return same type
		if (isNil() && t.isRecord()) return t;		//it is ok to return record and nil(result is record)
		if (isRecord() && t.isNil()) return this;	//it is ok to return record and nil(result is record)
		return null;								//it is illegal to return different types
	}

	public Type varType(Type t) {
		if (t == null) {
			if (isVoid()) return null;
			return this;
		}
		else {
			if (canAssign(t)) return t;
			return null;
		}
	}
}