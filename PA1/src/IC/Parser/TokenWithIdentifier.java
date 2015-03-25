package IC.Parser;

/**
 * Represents analyzer Identifier tokens
 */
public class TokenWithIdentifier extends Token {

    public TokenWithIdentifier(int type, Object value, int line, int column){
        super(type, value, line, column);
    }

    @Override
    public String toString() {
//        return getLine() + ": " + getTokenName(sym) + "(" + getValue() + ")";  // Document style
          return getValue() + "\t" + getTokenName(sym) +"\t" + getLine() + ":" + getColumn();
    }
}
