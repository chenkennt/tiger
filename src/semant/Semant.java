package semant;

import errormsg.ErrorMsg;
import absyn.*;
import types.*;
import symbol.Table;
import symbol.Symbol;
import translate.Level;
import translate.Access;
import translate.AccessList;
import translate.Translate;
import util.BoolList;
import temp.Label;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2003-11-22
 * Time: 19:52:29
 * To change this template use Options | File Templates.
 */
public class Semant {
	private Env env;
	private ErrorMsg errorMsg;
	private Label done;
	private Translate translate;

	private void error(int pos, int errno) {
		errorMsg.error(pos, errno);
	}

	public Semant(ErrorMsg err) {
		errorMsg = err;
		env = new Env();
	}

	public Translate transProg(Exp exp) {
		done = null;
		translate = new Translate();
		ExpResult result = transExp(exp, env.getGlobalLevel());
		translate.addMain(env.getGlobalLevel(), result.getExp());
		if (errorMsg.anyErrors) {
			System.out.println("Error occured, stop compiling");
			return null;
		}
		else return translate;
	}

	private ExpResult transExp(Exp exp, Level level) {
		if (exp instanceof IntExp) return new ExpResult(env.getInt(), level.intExp(((IntExp)exp).value));
		else if (exp instanceof StringExp) return new ExpResult(env.getString(), level.stringExp(((StringExp)exp).value, translate));
		else if (exp instanceof NilExp) return new ExpResult(env.getNil(), level.nilExp());
		else if (exp instanceof VarExp) {
			VarResult var = transVar(((VarExp)exp).var, level);
			return new ExpResult(var.getType(), var.getExp());
		}
		else if (exp instanceof OpExp) {
			OpExp opexp = (OpExp)exp;
			ExpResult left = transExp(opexp.left, level);	//translate left
			ExpResult right = transExp(opexp.right, level);	//translate right
			translate.Exp e = null;
			switch (opexp.oper) {
				case OpExp.PLUS:
				case OpExp.MINUS:
				case OpExp.MUL:
				case OpExp.DIV:
					if (!left.getType().isInt()) error(opexp.left.pos, ErrorMsg.OPERAND_MUST_BE_INT);
					if (!right.getType().isInt()) error(opexp.right.pos, ErrorMsg.OPERAND_MUST_BE_INT);
					e = level.opExp(opexp.oper, left.getExp(), right.getExp());
					break;
				case OpExp.LT:
				case OpExp.LE:
				case OpExp.GT:
				case OpExp.GE:
					if (!left.getType().isInt()) error(opexp.left.pos, ErrorMsg.OPERAND_MUST_BE_INT);
					if (!right.getType().isInt()) error(opexp.right.pos, ErrorMsg.OPERAND_MUST_BE_INT);
					e = level.opExp(opexp.oper, left.getExp(), right.getExp());
					break;
				case OpExp.EQ:
				case OpExp.NE:
					if (!left.getType().canCompare(right.getType())) error(exp.pos, ErrorMsg.INCOMPATIBLE_TYPES);
					if (left.getType().isString()) e = level.stringCompare(left.getExp(), right.getExp(), opexp.oper);	//compare strings
					else e = level.opExp(opexp.oper, left.getExp(), right.getExp());	//other comparison
					break;
			}
			return new ExpResult(env.getInt(), e);
		}
		else if (exp instanceof ArrayExp) {
			ArrayExp arrayexp = (ArrayExp)exp;
			Type type = env.getType(arrayexp.typ);
			ExpResult size = transExp(arrayexp.size, level);
			ExpResult init = transExp(arrayexp.init, level);
			Type result = env.getDefaultArray();	//if type is not declared or not array, type of expr will be array of int
			if (type == null) error(exp.pos, ErrorMsg.UNDECLARED_TYPE);	//type is not declared
			else if (!type.isArray()) error(exp.pos, ErrorMsg.TYPE_NOT_ARRAY);	//type is not array
			else {
				result = type;
				ARRAY array = (ARRAY)type.actual();
				if (!init.getType().canAssign(array.element))	//init type differs from array element type
					error(arrayexp.init.pos, ErrorMsg.INCOMPATIBLE_TYPES);
			}
			if (!size.getType().isInt()) error(exp.pos, ErrorMsg.OPERAND_MUST_BE_INT);	//array size must be int
			return new ExpResult(result, level.initArray(size.getExp(), init.getExp()));
		}
		else if (exp instanceof RecordExp) {
			RecordExp recordexp = (RecordExp)exp;
			Type type = env.getType(recordexp.typ);
			//translate fieldexplist
			FieldExpList p = recordexp.fields;
			translate.ExpList el = null;
			RECORDFIELD fields = null;
			while (p != null) {
				ExpResult t = transExp(p.init, level);
				fields = new RECORDFIELD(p.name, t.getType(), fields);
				el = new translate.ExpList(t.getExp(), el);
				p = p.tail;
			}
			Type result = env.getDefaultRecord();
			if (type == null) error(exp.pos, ErrorMsg.UNDECLARED_TYPE);	//type not declared
			else if (!(type.actual() instanceof RECORD)) error(exp.pos, ErrorMsg.TYPE_NOT_RECORD);	//type is not record
			else {
				result = type;
				RECORD record = (RECORD)type.actual();
				if (!record.canInit(fields)) error(recordexp.fields.pos, ErrorMsg.FIELD_INCONSISTENT);	//init fields differs from record fields
			}
			return new ExpResult(result, level.initRecord(el));
		}
		else if (exp instanceof AssignExp) {
            AssignExp assignexp = (AssignExp)exp;
			VarResult var = transVar(assignexp.var, level);
			ExpResult expr = transExp(assignexp.exp, level);
			if (var.isReadonly()) error(exp.pos, ErrorMsg.ASSIGN_READONLY_VARIABLE);
			if (!expr.getType().canAssign(var.getType())) error(exp.pos, ErrorMsg.INCOMPATIBLE_TYPES);
			return new ExpResult(env.getVoid(), level.assignExp(var.getExp(), expr.getExp()));
		}
		else if (exp instanceof SeqExp) {
			ExpList p = ((SeqExp)exp).list;
			ExpResult expty = new ExpResult(env.getVoid(), level.dummyExp());	//return value for empty sequence
			translate.ExpList el = null;
			translate.ExpList t = null;
			while (p != null) {
				expty = transExp(p.head, level);
				if (t == null) el = t = new translate.ExpList(expty.getExp(), null);
				else {
					t.tail = new translate.ExpList(expty.getExp(), null);
					t = t.tail;
				}
				p = p.tail;
			}
			return new ExpResult(expty.getType(), level.seqExp(el));
		}
		else if (exp instanceof CallExp) {
			CallExp callexp = (CallExp)exp;
			Entry entry = env.getEntry(callexp.func);
			//translate arguments
			ExpList p = callexp.args;
			RECORDFIELD args = null;
			translate.ExpList el = null;
			while (p != null) {
				ExpResult fml = transExp(p.head, level);
				args = new RECORDFIELD(null, fml.getType(), args);
				el = new translate.ExpList(fml.getExp(), el);
				p = p.tail;
			}
			Type result = env.getVoid();	//if error occurs function return void
			Label l = null;
			Level le = null;
			if (entry == null) error(exp.pos, ErrorMsg.UNDECLARED_IDENTIFIER);	//undeclared entry
            else if (!(entry instanceof FunEntry)) error(exp.pos, ErrorMsg.IDENTIFIER_NOT_FUNCTION);	//entry is not function
			else {
				FunEntry func = (FunEntry)entry;
				l = func.label;
				le = func.level;
				result = func.result;
				if (!func.canInit(args)) error(exp.pos, ErrorMsg.PARAMETER_INCONSISTENT);	//arguments inconsistent
			}
			if (l == null) return new ExpResult(result, level.dummyExp());
			else return new ExpResult(result, level.callExp(l, le, el));
		}
		else if (exp instanceof LetExp) {
			LetExp letexp = (LetExp)exp;
			env.beginScope();	//begin scope
			//translate declarations
			DecList p = letexp.decs;
			translate.ExpList el = null;
			translate.ExpList t = null;
			while (p != null) {
				translate.Exp e = transDec(p.head, level);
				if (t == null) el = t = new translate.ExpList(e, null);
				else {
					t.tail = new translate.ExpList(e, null);
					t = t.tail;
				}
				p = p.tail;
			}
			ExpResult body = transExp(letexp.body, level);	//translate body
			if (t == null) el = t = new translate.ExpList(body.getExp(), null);
			else t.tail = new translate.ExpList(body.getExp(), null);
			env.endScope();	//end scope
			return new ExpResult(body.getType(), level.seqExp(el));
		}
		else if (exp instanceof BreakExp) {
			if (done == null) {
				error(exp.pos, ErrorMsg.CALL_BREAK_OUTSIDE_LOOP);
				return new ExpResult(env.getVoid(), level.dummyExp());
			}
			else return new ExpResult(env.getVoid(), level.breakExp(done));
		}
		else if (exp instanceof ForExp) {
			ForExp forexp = (ForExp)exp;
			env.beginScope();	//begin scope
			translate.Exp var = transDec(forexp.var, level);	//translate loop variable
			//get varentry
			Entry entry = env.getEntry(forexp.var.name);
			VarEntry ve = null;
			if (entry instanceof VarEntry) ve = (VarEntry)entry;
			ExpResult hi = transExp(forexp.hi, level);
			Label temp = done;	//save inloop
			done = new Label();	//set inloop true
			ExpResult body = transExp(forexp.body, level);	//translate body
			translate.Exp e = level.forExp(ve.access, var, hi.getExp(), body.getExp(), done);
			done = temp;	//restore inloop
			if (!hi.getType().isInt()) error(forexp.hi.pos, ErrorMsg.OPERAND_MUST_BE_INT);	//high limit must be int
			if (!body.getType().isVoid()) error(forexp.body.pos, ErrorMsg.EXPRESSION_MUST_PRODUCE_NO_VALUE);	//body must produce no value
			env.endScope();
			return new ExpResult(env.getVoid(), e);
		}
		else if (exp instanceof WhileExp) {
			WhileExp whileexp = (WhileExp)exp;
			ExpResult test = transExp(whileexp.test, level);	//translate test
			Label temp = done;	//save inloop
			done = new Label();	//set inloop true
			ExpResult body = transExp(whileexp.body, level);	//translate body
			translate.Exp e = level.whileExp(test.getExp(), body.getExp(), done);
			done = temp;	//restore inloop
			if (!test.getType().isInt()) error(whileexp.test.pos, ErrorMsg.OPERAND_MUST_BE_INT);	//result of test must be int
			if (!body.getType().isVoid()) error(whileexp.body.pos, ErrorMsg.EXPRESSION_MUST_PRODUCE_NO_VALUE);	//body must produce no value
			return new ExpResult(env.getVoid(), e);
		}
		else if (exp instanceof IfExp) {
			IfExp ifexp = (IfExp)exp;
			ExpResult test = transExp(ifexp.test, level);	//translate test
			if (!test.getType().isInt()) error(ifexp.test.pos, ErrorMsg.OPERAND_MUST_BE_INT);	//result of test must be int
			ExpResult thenc = transExp(ifexp.thenclause, level);	//translate then
			if (ifexp.elseclause != null) {
				ExpResult elsec = transExp(ifexp.elseclause, level);	//translate else
				Type result = thenc.getType().equalType(elsec.getType());	//get result type
				if (result == null) {	//then and else return different types
					error(ifexp.thenclause.pos, ErrorMsg.INCOMPATIBLE_TYPES);
					result = env.getVoid();
				}
				return new ExpResult(result, level.ifExp(test.getExp(), thenc.getExp(), elsec.getExp()));
			}
			else {
				if (!thenc.getType().isVoid())	//without else, then can only return void
					error(ifexp.thenclause.pos, ErrorMsg.EXPRESSION_MUST_PRODUCE_NO_VALUE);
				return new ExpResult(env.getVoid(), level.ifExp(test.getExp(), thenc.getExp()));
			}
		}
		else return null;	//it is impossible
	}

	private Type transTy(Ty ty) {
        if (ty instanceof ArrayTy) {
			ArrayTy arrayty = (ArrayTy)ty;
			Type type = env.getType(arrayty.typ);	//get array element type
			if (type == null) {	//array element not declared
				error(ty.pos, ErrorMsg.UNDECLARED_TYPE);
				type = env.getInt();	//default array type(for error recovery)
			}
			return new ARRAY(type);
		}
		else if (ty instanceof RecordTy) {
			RecordTy recordty = (RecordTy)ty;
			RECORDFIELD fields = null;
            FieldList p = recordty.fields;
			while (p != null) {
				Type type = env.getType(p.typ);	//get record field type
				if (type == null) {	//undeclared record field type
					error(p.pos, ErrorMsg.UNDECLARED_TYPE);
					type = env.getInt();	//default record type(for error recovery)
				}
				fields = new RECORDFIELD(p.name, type, fields);
				p = p.tail;
			}
			return new RECORD(fields);
		}
		else if (ty instanceof NameTy) {
			Type type = env.getType(((NameTy)ty).name);	//get name type
			if (type == null) {	//type not declared
				error(ty.pos, ErrorMsg.UNDECLARED_TYPE);
				type = env.getInt();	//default name type(for error recovery)
			}
			return type;
		}
		else return null;
	}

	private translate.Exp transDec(Dec dec, Level level) {
		if (dec instanceof FunctionDec) {	//todo:function declaration
			//1st pass
			FunctionDec functiondec = (FunctionDec)dec;
			Table tempenv = new Table();
			while (functiondec != null) {
				if (tempenv.get(functiondec.name) != null) error(functiondec.pos, ErrorMsg.FUNCTION_REDECLARATION);	//redeclaration
				Type result;
				if (functiondec.result != null) result = transTy(functiondec.result);	//get result type
				else result = env.getVoid();	//default type for function result(for error recovery)
				//translate arguments
				RECORDFIELD params = null;
				BoolList blist = null;
				FieldList p = functiondec.params;
				while (p != null) {
					Type ptype = env.getType(p.typ);	//get argument type
					if (ptype == null) {
						error(p.pos, ErrorMsg.UNDECLARED_TYPE);	//argument type not declared
						ptype = env.getInt();	//default argument type(for error recovery)
					}
					params = new RECORDFIELD(p.name, ptype, params);
					blist = new BoolList(p.escape, blist);
					p = p.tail;
				}
				Level nlev = new Level(level, functiondec.name, blist);
				FunEntry funentry = new FunEntry(nlev, result, params, new Label(functiondec.name));
				tempenv.put(functiondec.name, funentry);	//put entry
				functiondec = functiondec.next;
			}
			//move entry from tempenv to env
			java.util.Enumeration keys = tempenv.keys();
			while (keys.hasMoreElements()) {
				Symbol key = (Symbol)keys.nextElement();
				env.putEntry(key, (Entry)tempenv.get(key));
			}

			//2nd pass
			functiondec = (FunctionDec)dec;
			while (functiondec != null) {
				env.beginScope();	//begin scope
				FunEntry funentry = (FunEntry)env.getEntry(functiondec.name);	//get entry
				//put arguments into env
				RECORDFIELD p = funentry.formals;
				AccessList al = funentry.level.formals.tail;
				while (p != null && al != null) {
					env.putEntry(p.fieldName, new VarEntry(al.head, p.fieldType, false));	//put entry
					p = p.tail;
					al = al.tail;
				}
				//save done
				Label temp = done;
				done = null;
				ExpResult body = transExp(functiondec.body, funentry.level);	//translate body
				//restore done
				done = temp;
				env.endScope();	//end scope
				if (!body.getType().canReturn(funentry.result)) error(functiondec.body.pos, ErrorMsg.INCOMPATIBLE_TYPES);	//incompatible types
				funentry.level.procEntryExit(body.getExp(), funentry.label, translate);
				functiondec = functiondec.next;
			}
			return level.dummyExp();
		}
		else if (dec instanceof TypeDec) {
			//1st pass(build headers)
			TypeDec typedec = (TypeDec)dec;
			Table tempenv = new Table();
			while (typedec != null) {
				if (tempenv.get(typedec.name) != null) error(typedec.pos, ErrorMsg.TYPE_REDECLARATION);	//redeclaration
				tempenv.put(typedec.name, new NAME(typedec.name));
				typedec = typedec.next;
			}
			//move entry from tempenv to env
			java.util.Enumeration keys = tempenv.keys();
			while (keys.hasMoreElements()) {
				Symbol key = (Symbol)keys.nextElement();
				env.putType(key, (Type)tempenv.get(key));
			}

			//2nd pass(translate declaration)
			typedec = (TypeDec)dec;
			while (typedec != null) {
				Type type = transTy(typedec.ty);	//translate type
				((NAME)env.getType(typedec.name)).bind(type);	//bind
				typedec = typedec.next;
			}
			//3rd pass(check loop declaration)
			typedec = (TypeDec)dec;
			while (typedec != null) {
				NAME name = (NAME)env.getType(typedec.name);
				if (name.isLoop()) {
					error(typedec.pos, ErrorMsg.LOOP_DECLARATION);
					name.bind(env.getInt());	//for error recovery
				}
				typedec = typedec.next;
			}
			return level.dummyExp();
		}
		else if (dec instanceof VarDec) {
			VarDec vardec = (VarDec)dec;
			Type type = null;
			if (vardec.typ != null) type = transTy(vardec.typ);	//get variable type
			ExpResult init = transExp(vardec.init, level);	//translate init
			Type ttype = init.getType().varType(type);
			if (ttype == null) error(vardec.init.pos, ErrorMsg.INCOMPATIBLE_TYPES);
			else if (ttype.isNil()) error(dec.pos, ErrorMsg.TYPE_EXPECTED);
			if (ttype == null || ttype.isNil()) {	//for error recovery
				if (type != null) ttype = type;
				else if (init.getType().isNil() || init.getType().isVoid()) ttype = env.getInt();
				else ttype = init.getType();
			}
			Access access = level.allocLocal(vardec.escape);
			VarEntry varentry = new VarEntry(access, ttype, vardec.readonly);
			env.putEntry(vardec.name, varentry);	//put entry
			return level.assignExp(level.simpleVar(access), init.getExp());
		}
		else return null;	//it is impossible
	}

	private VarResult transVar(Var var, Level level) {
		if (var instanceof FieldVar) {
			FieldVar fieldvar = (FieldVar)var;
			VarResult vari = transVar(fieldvar.var, level);
            if (!vari.getType().isRecord()) {
				error(fieldvar.var.pos, ErrorMsg.TYPE_NOT_RECORD);	//type not record
				return new VarResult(env.getInt(), level.dummyExp(), vari.isReadonly());	//error recovery
			}
			else {
				RECORD type = (RECORD)vari.getType().actual();
				Type ftype = type.getFieldType(fieldvar.field);
				int index = type.getFieldIndex(fieldvar.field);
				if (ftype == null) {
					error(var.pos, ErrorMsg.FIELD_INCONSISTENT);
					ftype = env.getInt();	//error recovery
				}
				return new VarResult(ftype, level.fieldVar(vari.getExp(), index), vari.isReadonly());
			}
		}
		else if (var instanceof SimpleVar) {
			boolean ro = false;
			Entry entry = env.getEntry(((SimpleVar)var).name);
			if (entry == null) {
				error(var.pos, ErrorMsg.UNDECLARED_IDENTIFIER);
				return new VarResult(env.getInt(), level.dummyExp(), ro);	//error recovery
			}
			if (!(entry instanceof VarEntry)) {
				error(var.pos, ErrorMsg.IDENTIFIER_NOT_VARIABLE);
				return new VarResult(env.getInt(), level.dummyExp(), ro);	//error recovery
			}
			else return new VarResult(((VarEntry)entry).type, level.simpleVar(((VarEntry)entry).access), ((VarEntry)entry).readonly);
		}
		else if (var instanceof SubscriptVar) {
			SubscriptVar subscriptvar = (SubscriptVar)var;
			VarResult vari = transVar(subscriptvar.var, level);
			Type type = env.getInt();	//default type for error recovery
            if (!vari.getType().isArray()) error(subscriptvar.var.pos, ErrorMsg.TYPE_NOT_ARRAY);
			else type = ((ARRAY)vari.getType().actual()).element;
			ExpResult index = transExp(subscriptvar.index, level);
			if (!index.getType().isInt()) error(subscriptvar.index.pos, ErrorMsg.OPERAND_MUST_BE_INT);	//index must be int
			return new VarResult(type, level.subscriptVar(vari.getExp(), index.getExp()), vari.isReadonly());
		}
		else return null;
	}
}