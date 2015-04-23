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
    int stringLine = 0;
    int stringColumn = 0;

    public int getLine() { return yyline+1; }
    
    private Token NewToken(int type)
    {
    	return new Token(type, yyline+1, yycolumn+1, yytext());
    }

    private Token NewToken(int type, Object value, String tag)
    {
    	if (type == sym.QUOTE)
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

%%

<YYINITIAL> {

  /* keywords */
  "boolean"                      { return NewToken(sym.BOOLEAN); }
  "break"                        { return NewToken(sym.BREAK); }
  "class"                        { return NewToken(sym.CLASS); }
  "continue"                     { return NewToken(sym.CONTINUE); }
  "else"                         { return NewToken(sym.ELSE); }
  "extends"                      { return NewToken(sym.EXTENDS); }
  "int"                          { return NewToken(sym.INT); }
  "new"                          { return NewToken(sym.NEW); }
  "if"                           { return NewToken(sym.IF); }
  "return"                       { return NewToken(sym.RETURN); }
  "void"                         { return NewToken(sym.VOID); }
  "static"                       { return NewToken(sym.STATIC); }
  "while"                        { return NewToken(sym.WHILE); }
  "this"                         { return NewToken(sym.THIS); }
  "string"		   				 { return NewToken(sym.STRING); }
  "length"						 { return NewToken(sym.LENGTH); }

  /* boolean literals */
  "true"                         { return NewToken(sym.TRUE); }
  "false"                        { return NewToken(sym.FALSE); }

  /* null literal */
  "null"                         { return NewToken(sym.NULL); }


  /* separators */
  "("                            { return NewToken(sym.LP); }
  ")"                            { return NewToken(sym.RP); }
  "{"                            { return NewToken(sym.LCBR); }
  "}"                            { return NewToken(sym.RCBR); }
  "["                            { return NewToken(sym.LB); }
  "]"							 { return NewToken(sym.RB); }
  ";"                            { return NewToken(sym.SEMI); }
  ","                            { return NewToken(sym.COMMA); }
  "."                            { return NewToken(sym.DOT); }

  /* operators */
  "="                            { return NewToken(sym.ASSIGN); }
  ">"                            { return NewToken(sym.GT); }
  "<"                            { return NewToken(sym.LT); }
  "!"                            { return NewToken(sym.LNEG); }
  "=="                           { return NewToken(sym.EQUAL); }
  "<="                           { return NewToken(sym.LTE); }
  ">="                           { return NewToken(sym.GTE); }
  "!="                           { return NewToken(sym.NEQUAL); }
  "&&"                           { return NewToken(sym.LAND); }
  "||"                           { return NewToken(sym.LOR); }
  "+"                            { return NewToken(sym.PLUS); }
  "-"                            { return NewToken(sym.MINUS); }
  "*"                            { return NewToken(sym.MULTIPLY); }
  "/"                            { return NewToken(sym.DIVIDE); }
  "%"							 { return NewToken(sym.MOD); }
	  
  /* string literal */
  \"                             { string.setLength(0); stringLine = yyline+1; stringColumn = yycolumn + 1; yybegin(STRING);  }

  /* numeric literals */
  [0]+{DecIntegerLiteralNoZero}	 { 	throw new LexicalError(String.format("%d:%d : lexical error; Integer must not start with leading zeroes", yyline+1, yycolumn+1),yyline+1); }
  
  {DecIntegerLiteralNoZero}      { return NewToken(sym.INTEGER, yytext(), "INTEGER"); }
  {DecIntegerLiteralWithZero}    { return NewToken(sym.INTEGER, yytext(), "INTEGER"); }
    
  /* comments */
  {EndOfLineComment}               { /* ignore */ }
  "/*"							 { yybegin(COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */
  {ClassIdentifier}				 { return NewToken(sym.CLASS_ID, yytext(), "CLASS_ID"); } 
  [_]+{Identifier}				 { throw new LexicalError(String.format("%d:%d : lexical error; Identifier must start with lower case letters", yyline+1, yycolumn+1),yyline+1); }
  {Identifier}                   { return NewToken(sym.ID, yytext(), "ID"); }  
}

<STRING> {
  \"                             { yybegin(YYINITIAL); return NewToken(sym.QUOTE, string.toString(), "QUOTE"); }
  \t							 { throw new LexicalError(String.format("%d:%d : lexical error; Illegel tab in string literal", yyline + 1, yycolumn+1),yyline+1); }
  
  {StringCharacter}+             { string.append( yytext() ); }
  
  /* escape sequences */
  "\\t"                          { string.append( "\t" ); }
  "\\n"                          { string.append( "\n" ); }
  "\\r"                          { string.append( "\r" ); }
  "\\\""                         { string.append( "\\\"" ); }
  "\\\\"                         { string.append( "\\\\" ); }
  
  /* error cases */
  \\.                            { throw new LexicalError(String.format("%d:%d : lexical error; Illegal escape sequence \""+yytext()+"\"", yyline+1, yycolumn+1),yyline+1); }
  {LineTerminator}               { throw new LexicalError(String.format("%d:%d : lexical error; Unterminated string at end of line", yyline+1, yycolumn+1),yyline+1); }
}

<COMMENT> {
	
	~"*/"						 { yybegin(YYINITIAL); }
	
	{RegularComment}			 { /* Ignore */ }	
}


/* error fallback */
[^]                              { throw new LexicalError(String.format("%d:%d : lexical error; Illegal character \""+yytext() + "\"", yyline+1, yycolumn+1),yyline+1); }

<STRING> <<EOF>>				 { throw new LexicalError(String.format("%d:%d : lexical error; Unterminated string at end of file", yyline+1, yycolumn+1),yyline+1); }
<COMMENT> <<EOF>>				 { throw new LexicalError(String.format("%d:%d : lexical error; Unterminated Comment", yyline+1, yycolumn+1),yyline+1); }
<<EOF>>              			 { return NewToken(sym.EOF); }
