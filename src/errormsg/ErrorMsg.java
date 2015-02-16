package errormsg;

public class ErrorMsg {
	private LineList linePos = new LineList(-1, null);
	private int lineNum = 1;
	private String filename;
	public boolean anyErrors;

	public static final int INCOMPATIBLE_TYPES = 301;
	public static final int OPERAND_MUST_BE_INT = 302;
	public static final int UNDECLARED_TYPE = 303;
	public static final int TYPE_NOT_ARRAY = 304;
	public static final int TYPE_NOT_RECORD = 305;
	public static final int PARAMETER_INCONSISTENT = 306;
	public static final int FIELD_INCONSISTENT = 307;
	public static final int UNDECLARED_IDENTIFIER = 308;
	public static final int IDENTIFIER_NOT_FUNCTION = 309;
	public static final int IDENTIFIER_NOT_VARIABLE = 310;
	public static final int TYPE_EXPECTED = 311;
	public static final int EXPRESSION_MUST_PRODUCE_NO_VALUE = 312;
	public static final int TYPE_REDECLARATION = 313;
	public static final int FUNCTION_REDECLARATION = 314;
	public static final int LOOP_DECLARATION = 315;
	public static final int CALL_BREAK_OUTSIDE_LOOP = 316;
	public static final int ASSIGN_READONLY_VARIABLE = 317;

	public ErrorMsg(String f) {
		filename = f;
	}

	public void newline(int pos) {
		lineNum++;
		linePos = new LineList(pos, linePos);
	}

	public void error(int pos, String msg) {
		int n = lineNum;
		LineList p = linePos;
		String sayPos = "0.0";

		anyErrors = true;

		while (p != null) {
			if (p.head < pos) {
				sayPos = ":" + String.valueOf(n) + "." + String.valueOf(pos - p.head);
				break;
			}
			p = p.tail;
			n--;
		}

		System.out.println("Error: " + filename + ":" + sayPos + ": " + msg);
	}

	public void error(int pos, int errno) {
		int n = lineNum;
		LineList p = linePos;
		String sayPos = "0.0";

		anyErrors = true;

		while (p != null) {
			if (p.head < pos) {
				sayPos = ":" + String.valueOf(n) + "." + String.valueOf(pos - p.head);
				break;
			}
			p = p.tail;
			n--;
		}

		System.out.println("Error: " + filename + ":" + sayPos + " Error no: " + errno);
	}
}

class LineList {
	int head;
	LineList tail;

	LineList(int h, LineList t) {
		head = h;
		tail = t;
	}
}