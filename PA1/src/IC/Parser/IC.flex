package IC.Parser;
import java_cup.runtime.*;

%%

%class Lexer
%public
%function next_token

%type Token

%line
%column
%scanerror LexicalError

%cup


%{
  StringBuffer string = new StringBuffer();
%}

%state STRING

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*

/* special chars */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* Somple Regex */
ALPHA=[A-Za-z]
DIGIT=[0-9]
ALPHA_NUMERIC={ALPHA}|{DIGIT}
DecIntegerLiteral = 0 | [1-9][0-9]*
PrintableAsciiChar = [ -~]

/* identifiers */
VariableIdentifier = [a-z]({ALPHA_NUMERIC})*
ClassIdentifier = [A-Z]({ALPHA_NUMERIC})*
%%

/* QOUTE */
<YYINITIAL> \"                     { string.setLength(0);  string.append('"'); yybegin(STRING); }

<STRING> {
    \"                             { yybegin(YYINITIAL); string.append('"'); return new TokenWithIdentifier(sym.STRING, string.toString(), yyline, yycolumn  - string.toString().length() + 1); }
    "\\\""                         { string.append( '\"' ); }
    "\\t"                          { string.append( "\t" ); }
    "\\n"                          { string.append( "\\n" ); }
    {PrintableAsciiChar}           { string.append( yytext() ); }
    {LineTerminator}               {  throw new LexicalError("String wasn't terminated - line "+yyline+", column "+yycolumn);}
}

/* comments */
<YYINITIAL> {Comment}   { /* ignore */ }


  /* whitespace */
<YYINITIAL> {WhiteSpace} { /* ignore */ }


<YYINITIAL> {
    /* keywords */
    "="			        { return new Token(sym.ASSIGN, yytext(), yyline, yycolumn); }
    "boolean"			{ return new Token(sym.BOOLEAN, yytext(), yyline, yycolumn); }
    "break"		    	{ return new Token(sym.BREAK, yytext(), yyline, yycolumn); }
    "class"		    	{ return new Token(sym.CLASS, yytext(), yyline, yycolumn); }
    ","			        { return new Token(sym.COMMA, yytext(), yyline, yycolumn); }
    "continue"			{ return new Token(sym.CONTINUE, yytext(), yyline, yycolumn); }
    "/"	        		{ return new Token(sym.DIVIDE, yytext(), yyline, yycolumn); }
    "."		        	{ return new Token(sym.DOT, yytext(), yyline, yycolumn); }
    "=="			    { return new Token(sym.EQUAL, yytext(), yyline, yycolumn); }
    "extends"			{ return new Token(sym.EXTENDS, yytext(), yyline, yycolumn); }
    "else"		    	{ return new Token(sym.ELSE, yytext(), yyline, yycolumn); }
    ">"			        { return new Token(sym.GT, yytext(), yyline, yycolumn); }
    ">="			    { return new Token(sym.GTE, yytext(), yyline, yycolumn); }
    "if"		    	{ return new Token(sym.IF, yytext(), yyline, yycolumn); }
    "int"		    	{ return new Token(sym.INT, yytext(), yyline, yycolumn); }
    "&&"		    	{ return new Token(sym.LAND, yytext(), yyline, yycolumn); }
    "["		    	    { return new Token(sym.LB, yytext(), yyline, yycolumn); }
    "("			        { return new Token(sym.LP, yytext(), yyline, yycolumn); }
    "{"			        { return new Token(sym.LCBR, yytext(), yyline, yycolumn); }
    "length"			{ return new Token(sym.LENGTH, yytext(), yyline, yycolumn); }
    "new"	    		{ return new Token(sym.NEW, yytext(), yyline, yycolumn); }
    "!"		        	{ return new Token(sym.LNEG, yytext(), yyline, yycolumn); }
    "||"		    	{ return new Token(sym.LOR, yytext(), yyline, yycolumn); }
    "<"			        { return new Token(sym.LT, yytext(), yyline, yycolumn); }
    "<="		    	{ return new Token(sym.LTE, yytext(), yyline, yycolumn); }
    "-"	        		{ return new Token(sym.MINUS, yytext(), yyline, yycolumn); }
    "%"	    	    	{ return new Token(sym.MOD, yytext(), yyline, yycolumn); }
    "*"	        		{ return new Token(sym.MULTIPLY, yytext(), yyline, yycolumn); }
    "!="		    	{ return new Token(sym.NEQUAL, yytext(), yyline, yycolumn); }
    "null"			    { return new Token(sym.NULL, yytext(), yyline, yycolumn); }
    "+"	        		{ return new Token(sym.PLUS, yytext(), yyline, yycolumn); }
    "]"     			{ return new Token(sym.RB, yytext(), yyline, yycolumn); }
    "}"	        		{ return new Token(sym.RCBR, yytext(), yyline, yycolumn); }
    ")"	        		{ return new Token(sym.RP, yytext(), yyline, yycolumn); }
    ";"	        		{ return new Token(sym.SEMI, yytext(), yyline, yycolumn); }
    "return"			{ return new Token(sym.RETURN, yytext(), yyline, yycolumn); }
    "static"			{ return new Token(sym.STATIC, yytext(), yyline, yycolumn); }
    "this"		    	{ return new Token(sym.THIS, yytext(), yyline, yycolumn); }
    "true"		    	{ return new Token(sym.TRUE, yytext(), yyline, yycolumn); }
    "false"		    	{ return new Token(sym.FALSE, yytext(), yyline, yycolumn); }
    "void"		    	{ return new Token(sym.VOID, yytext(), yyline, yycolumn); }
    "while"		    	{ return new Token(sym.WHILE, yytext(), yyline, yycolumn); }
    "string"			{ return new Token(sym.string, yytext(), yyline, yycolumn); }
    {DecIntegerLiteral}			{ return new TokenWithIdentifier(sym.INTEGER, yytext(), yyline, yycolumn); }
    {VariableIdentifier}	      { return new TokenWithIdentifier(sym.ID, yytext(), yyline, yycolumn); }
    {ClassIdentifier}     	{ return new TokenWithIdentifier(sym.CLASS_ID, yytext(), yyline, yycolumn); }
}

/* error fallback */
[^]                              { throw new LexicalError("Error!	" + (yyline+1)  + ": Lexical error: " + yytext()); }


<<EOF>>                          {  return new Token(sym.EOF, yytext(), yyline, yycolumn); }


