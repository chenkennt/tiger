package semant;

import symbol.Table;
import symbol.Symbol;
import types.*;
import translate.Level;
import util.BoolList;
import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-11-22
 * Time: 19:56:44
 * To change this template use Options | File Templates.
 */
class Env {
    private Table tenv;
	private Table venv;
	private Level globalLevel;

	public Env() {
		tenv = new Table();
		venv = new Table();
		globalLevel = new Level(new frame.virtualframe.Frame());
		tenv.put(Symbol.symbol("int"), new INT());
		tenv.put(Symbol.symbol("string"), new STRING());
		venv.put(Symbol.symbol("print"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("print"), new BoolList(true, null)),
			getVoid(),
			new RECORDFIELD(Symbol.symbol("s"), getString(), null),
			new Label(Symbol.symbol("print"))));
		venv.put(Symbol.symbol("printi"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("printi"), new BoolList(true, null)),
			getVoid(),
			new RECORDFIELD(Symbol.symbol("i"), getInt(), null),
			new Label(Symbol.symbol("printi"))));
		venv.put(Symbol.symbol("flush"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("flush"),null),
			getVoid(),
			null,
			new Label(Symbol.symbol("flush"))));
		venv.put(Symbol.symbol("getchar"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("getchar"), null),
			getString(),
			null,
			new Label(Symbol.symbol("getchar"))));
		venv.put(Symbol.symbol("ord"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("ord"), new BoolList(true, null)),
			getInt(),
			new RECORDFIELD(Symbol.symbol("s"), getString(), null),
			new Label(Symbol.symbol("ord"))));
		venv.put(Symbol.symbol("chr"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("chr"), new BoolList(true, null)),
			getString(),
			new RECORDFIELD(Symbol.symbol("i"), getInt(), null),
			new Label(Symbol.symbol("chr"))));
		venv.put(Symbol.symbol("size"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("size"), new BoolList(true, null)),
			getInt(),
			new RECORDFIELD(Symbol.symbol("s"), getString(), null),
			new Label(Symbol.symbol("size"))));
		venv.put(Symbol.symbol("substring"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("substring"), new BoolList(true, new BoolList(true, new BoolList(true, null)))),
			getString(),
			new RECORDFIELD(Symbol.symbol("n"), getInt(),
				new RECORDFIELD(Symbol.symbol("f"), getInt(),
					new RECORDFIELD(Symbol.symbol("s"), getString(), null))),
			new Label(Symbol.symbol("substring"))));
		venv.put(Symbol.symbol("concat"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("concat"), new BoolList(true, new BoolList(true, null))),
			getString(),
			new RECORDFIELD(Symbol.symbol("s1"), getString(), new RECORDFIELD(Symbol.symbol("s2"), getString(), null)),
			new Label(Symbol.symbol("concat"))));
		venv.put(Symbol.symbol("not"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("not"), new BoolList(true, null)),
			getInt(),
			new RECORDFIELD(Symbol.symbol("i"), getInt(), null),
			new Label(Symbol.symbol("not"))));
		venv.put(Symbol.symbol("exit"), new FunEntry(
			new Level(globalLevel, Symbol.symbol("exit"), new BoolList(true, null)),
			getVoid(),
			new RECORDFIELD(Symbol.symbol("i"), getInt(), null),
			new Label(Symbol.symbol("exit"))));
	}

	public void beginScope() {
		tenv.beginScope();
		venv.beginScope();
	}

	public void endScope() {
		tenv.endScope();
		venv.endScope();
	}

	public Type getType(Symbol s) {
		return (Type)tenv.get(s);
	}

	public Entry getEntry(Symbol s) {
		return (Entry)venv.get(s);
	}

	public void putType(Symbol s, Type t) {
		tenv.put(s, t);
	}

	public void putEntry(Symbol s, Entry e) {
		venv.put(s, e);
	}

	public Type getInt() {
		return new INT();
	}

	public Type getString() {
		return new STRING();
	}

	public Type getNil() {
		return new NIL();
	}

	public Type getVoid() {
		return new VOID();
	}

	public Type getDefaultArray() {
		return new ARRAY(getInt());
	}

	public Type getDefaultRecord() {
		return new RECORD(null);
	}

	public Level getGlobalLevel() {
		return globalLevel;
	}
}