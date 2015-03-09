package IC.Parser;

/**
 * Created by user on 3/9/2015.
 */
public class TokenWithIdentifier extends Token {

    public TokenWithIdentifier(int type, Object value, int line, int column){
        super(type, value, line, column);
    }

    @Override
    public String toString() {
//        return getLine() + ": " + getTokenName(sym) + "(" + getValue() + ")";
          return getValue() + "\t" + getTokenName(sym) +"\t" + getLine() + ":" + getColumn();
    }
}
