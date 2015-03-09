package IC.Parser;

import java_cup.runtime.Symbol;

public class Token extends Symbol {

    private int line;
    private int column;
    private String value;

    public Token(int type, Object value, int line, int column){
        super(type, value);
        this.line = line;
        this.column = column;
        this.value = value instanceof String ? (String) value : "";
    }

    public int getLine() {
        return line + 1;
    }

    public int getColumn() {
        return column + 1;
    }

    public String getValue() {
        return value;
    }

    /**
     * Overrides toString method for output purpose
     * @return line looks like - token	tag	line :column
     */
    @Override
    public String toString() {
        //return getLine() + ": " + getTokenName(sym) + "(" + getValue() + ")";
        //return line + ": " + getTokenName(sym);
        //return getTokenName(sym) + "\t" + getTokenName(sym) +"\t" + getLine() + ":" + getColumn();
        return getValue() + "\t" + getValue() +"\t" + getLine() + ":" + getColumn();
    }

    /**
     * Converts an int token code into the name of the
     * token by reflection on the cup symbol class/interface sym
     *
     * This code was contributed by Karl Meissner <meissnersd@yahoo.com>
     */
    protected String getTokenName(int token) {
        try {
            java.lang.reflect.Field [] classFields = sym.class.getFields();
            for (int i = 0; i < classFields.length; i++) {
                if (classFields[i].getInt(null) == token) {
                    return classFields[i].getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return "UNKNOWN TOKEN";
    }
}