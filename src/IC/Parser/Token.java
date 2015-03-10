package IC.Parser;

import java_cup.runtime.Symbol;

import java.lang.reflect.Field;

/**
 * Represents analyzer tokens
 */
public class Token extends Symbol {

    private int line;
    private int column;
    private String value;

    /**
     *
     * @param type sym Type
     * @param value value of scanned char
     * @param line line number
     * @param column column number
     */
    public Token(int type, Object value, int line, int column){
        super(type, value);
        this.line = line;
        this.column = column;
        this.value = value instanceof String ? (String) value : "";
    }

    /**
     * @return line number
     */
    public int getLine() {
        return line + 1;
    }

    /**
     *
     * @return column number
     */
    public int getColumn() {
        return column + 1;
    }

    /**
     *
     * @return input string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Overrides toString method for output purpose
     * @return line looks like - token	tag	line :column
     */
    @Override
    public String toString() {
        //return line + ": " + getTokenName(sym); // Document style
        return getValue() + "\t" + getValue() +"\t" + getLine() + ":" + getColumn();
    }

    /**
     * Converts an int token code into the name of the
     * token by reflection on the cup symbol class/interface sym
     *
     */
    protected String getTokenName(int token) {
        try {
            java.lang.reflect.Field [] classFields = sym.class.getFields();
            for (Field classField : classFields) {
                if (classField.getInt(null) == token) {
                    return classField.getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return "UNKNOWN TOKEN";
    }
}