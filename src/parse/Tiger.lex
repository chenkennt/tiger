package parse;
import errormsg.ErrorMsg;

%% 

%cup
%char
%full
%unicode

%init{
	commentlevel = 0;
%init}

%{
	private ErrorMsg errorMsg;
	private String str;
	private int commentlevel;
	private int strstart;

	Yylex(java.io.InputStream s, ErrorMsg e) {
		this(s);
		errorMsg=e;
	}

	private void newline() {
		errorMsg.newline(yychar);
	}

	private void err(int pos, String s) {
		errorMsg.error(pos,s);
	}

	private void err(String s) {
		err(yychar,s);
	}

	private java_cup.runtime.Symbol tok(int kind, Object value) {
		if (kind == sym.STRING) return new java_cup.runtime.Symbol(kind, strstart, yychar+yylength(), value);
		else return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
	}
%}

%eofval{
	{return tok(sym.EOF, null);}
%eofval}

%state STRING, COMMENT

%%

<YYINITIAL> [" "\t\r\f]+
						{}
\n       				{newline();}

<YYINITIAL> array		{return tok(sym.ARRAY, null);}
<YYINITIAL> break		{return tok(sym.BREAK, null);}
<YYINITIAL> do			{return tok(sym.DO, null);}
<YYINITIAL> else		{return tok(sym.ELSE, null);}
<YYINITIAL> end			{return tok(sym.END, null);}
<YYINITIAL> for			{return tok(sym.FOR, null);}
<YYINITIAL> function	{return tok(sym.FUNCTION, null);}
<YYINITIAL> if			{return tok(sym.IF, null);}
<YYINITIAL> in			{return tok(sym.IN, null);}
<YYINITIAL> let			{return tok(sym.LET, null);}
<YYINITIAL> nil			{return tok(sym.NIL, null);}
<YYINITIAL> of			{return tok(sym.OF, null);}
<YYINITIAL> then		{return tok(sym.THEN, null);}
<YYINITIAL> to			{return tok(sym.TO, null);}
<YYINITIAL> type		{return tok(sym.TYPE, null);}
<YYINITIAL> var			{return tok(sym.VAR, null);}
<YYINITIAL> while		{return tok(sym.WHILE, null);}

<YYINITIAL> ,			{return tok(sym.COMMA, null);}
<YYINITIAL> :			{return tok(sym.COLON, null);}
<YYINITIAL> ;			{return tok(sym.SEMICOLON, null);}
<YYINITIAL> \(			{return tok(sym.LPAREN, null);}
<YYINITIAL> \)			{return tok(sym.RPAREN, null);}
<YYINITIAL> \[			{return tok(sym.LBRACK, null);}
<YYINITIAL> \]			{return tok(sym.RBRACK, null);}
<YYINITIAL> \{			{return tok(sym.LBRACE, null);}
<YYINITIAL> \}			{return tok(sym.RBRACE, null);}
<YYINITIAL> \.			{return tok(sym.DOT, null);}
<YYINITIAL> \+			{return tok(sym.PLUS, null);}
<YYINITIAL> -			{return tok(sym.MINUS, null);}
<YYINITIAL> \*			{return tok(sym.TIMES, null);}
<YYINITIAL> /			{return tok(sym.DIVIDE, null);}
<YYINITIAL> =			{return tok(sym.EQ, null);}
<YYINITIAL> <>			{return tok(sym.NEQ, null);}
<YYINITIAL> <			{return tok(sym.LT, null);}
<YYINITIAL> <=			{return tok(sym.LE, null);}
<YYINITIAL> >			{return tok(sym.GT, null);}
<YYINITIAL> >=			{return tok(sym.GE, null);}
<YYINITIAL> &			{return tok(sym.AND, null);}
<YYINITIAL> \|			{return tok(sym.OR, null);}
<YYINITIAL> :=			{return tok(sym.ASSIGN, null);}

<YYINITIAL> [A-Za-z][0-9A-Za-z_]*
						{return tok(sym.ID, yytext());}

<YYINITIAL> [0-9]+		{return tok(sym.INT, new Integer(yytext()));}

<YYINITIAL>	\"			{yybegin(STRING); str = ""; strstart = yychar;}
<STRING>	\"			{yybegin(YYINITIAL); return tok(sym.STRING, str);}
<STRING>    [\x20-\xff]
						{str += yytext();}
<STRING>	\\n			{str += "\n";}
<STRING>	\\t			{str += "\t";}
<STRING>	\\\"		{str += "\"";}
<STRING>	\\\\		{str += "\\";}
<STRING>	\\"^"[@A-Z"[\]^_"]
						{str += yytext();}
<STRING>	\\[0-9][0-9][0-9]
						{str += (char)Integer.parseInt(yytext().substring(1));}
<STRING>	\\[" "\r\n\t\f]+\\
						{}
<STRING>	[\x00-\xff]	{err(yytext());}

<YYINITIAL>	/\*			{yybegin(COMMENT); commentlevel++;}
<COMMENT>	/\*			{commentlevel++;}
<COMMENT>	\*/			{commentlevel--; if (commentlevel == 0) yybegin(YYINITIAL);}
<COMMENT>   [^\n]		{}

.						{err(yytext());}