package IC.Parser;
import java_cup.runtime.*;

%%

%cup
%public
%class Lexer
%type Token

%unicode

%line
%column

%scanerror LexicalError

%{
    StringBuffer string = new StringBuffer();
    int stringLine = 0;
    int stringColumn = 0;

    private Token NewToken(int type)
    {
    	return new Token(type, yyline+1, yycolumn+1, yytext());
    }

    private Token NewToken(int type, Object value)
    {
    	return new Token(type, yyline+1, yycolumn+1, value);
    }

    private Token NewToken(int type, Object value, String tag)
    {
    	if (type == sym.STRING_L)
    		return new Token(type, stringLine, stringColumn, value, tag);
    	else
    		return new Token(type, yyline+1, yycolumn+1, value, tag);
    }

    public int GetLineNumber()
    {
    	return yyline+1;
    }

    public int GetColumnNumber()
    {
    	return yycolumn+1;
    }

%}



/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

LowerCaseLetter = [a-z]
UpperCaseLetter = [A-Z]
Letter = [a-z_A-Z]
Digit = [0-9]
LetterDigit = {Letter}|{Digit}

WhiteSpace = {LineTerminator} | [\s\t]

/* comments */
EndOfLineComment = "//"{InputCharacter}*{LineTerminator}?
RegularComment = [^*] | "*"+ | [^/*]

/* identifiers */
Identifier = {LowerCaseLetter}{LetterDigit}*
ClassIdentifier = {UpperCaseLetter}{LetterDigit}*

/* integer literals */
DecIntegerLiteralNoZero = [1-9][0-9]*
DecIntegerLiteralWithZero = 0+

//HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
//HexDigit          = [0-9a-fA-F]

//OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
//OctDigit          = [0-7]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

%state STRING, COMMENT










/* string literal */
  \"                             { string.setLength(0); stringLine = yyline+1; stringColumn = yycolumn + 1; string.append("\""); yybegin(STRING);  }

  /* numeric literals */
  [0]+{DecIntegerLiteralNoZero}	 { 	throw new LexicalError(String.format("%d:%d : lexical error; Integer must not start with leading zeroes", yyline+1, yycolumn+1)); }

  {DecIntegerLiteralNoZero}      { return NewToken(sym.INTEGER_L, yytext(), "INTEGER"); }
  {DecIntegerLiteralWithZero}    { return NewToken(sym.INTEGER_L, yytext(), "INTEGER"); }

  /* comments */
  {EndOfLineComment}               { /* ignore */ }
  "/*"							 { yybegin(COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */
  {ClassIdentifier}				 { return NewToken(sym.CLASS_ID, yytext(), "CLASS_ID"); }
  [_]+{Identifier}				 { throw new LexicalError(String.format("%d:%d : lexical error; Identifier must start with lower case letters", yyline+1, yycolumn+1)); }
  {Identifier}                   { return NewToken(sym.ID, yytext(), "ID"); }
}

<STRING> {
  \"                             { yybegin(YYINITIAL); string.append("\""); return NewToken(sym.STRING_L, string.toString(), "STRING"); }
  \t							 { throw new LexicalError(String.format("%d:%d : lexical error; Illegel tab in string literal", yyline + 1, yycolumn+1)); }

  {StringCharacter}+             { string.append( yytext() ); }

  /* escape sequences */
  "\\t"                          { string.append( "\\t" ); }
  "\\n"                          { string.append( "\\n" ); }
  "\\r"                          { string.append( "\\r" ); }
  "\\\""                         { string.append( "\\\"" ); }
  "\\\\"                         { string.append( "\\\\" ); }

  /* error cases */
  \\.                            { throw new LexicalError(String.format("%d:%d : lexical error; Illegal escape sequence \""+yytext()+"\"", yyline+1, yycolumn+1)); }
  {LineTerminator}               { throw new LexicalError(String.format("%d:%d : lexical error; Unterminated string at end of line", yyline+1, yycolumn+1)); }
}

<COMMENT> {

	~"*/"						 { yybegin(YYINITIAL); }

	{RegularComment}			 { /* Ignore */ }
}


/* error fallback */
[^]                              { throw new LexicalError(String.format("%d:%d : lexical error; Illegal character \""+yytext() + "\"", yyline+1, yycolumn+1)); }

<STRING> <<EOF>>				 { throw new LexicalError(String.format("%d:%d : lexical error; Unterminated string at end of file", yyline+1, yycolumn+1)); }
<COMMENT> <<EOF>>				 { throw new LexicalError(String.format("%d:%d : lexical error; Unterminated Comment", yyline+1, yycolumn+1)); }
<<EOF>>              			 { return NewToken(sym.EOF); }

