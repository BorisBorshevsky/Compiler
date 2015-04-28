package IC.Parser;

import java_cup.runtime.Symbol;

import java.lang.reflect.Field;

/**
 * Represents analyzer tokens
 */
public class Token extends Symbol {

    private String tag;
    private int id;
    private int line;
    private int column;
    private String value;

    public String getTag() {
        return tag;
    }

    public int getId() {
        return id;
    }


    public Token(int id, int line, int column)
    {
        super(id, line, column);
        this.line = line;
        this.column = column;
    }

    public Token(int id, int line, int column, Object val)
    {
        super(id, line, column, val);
//        this(id, val);
        this.line = line;
        this.column = column;
        tag = val.toString();
    }


    public Token(int id, int line, int column, Object val, String tag)
    {
        this(id, line, column, val);
        this.tag = tag;
    }


    /**
     * @return line number
     */
    public int getLine() {
        return line;
    }

    /**
     *
     * @return column number
     */
    public int getColumn() {
        return column;
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
     * @param token token id
     */
    protected String getTokenName(int token) {
        try {
            Field [] classFields = sym.class.getFields();
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