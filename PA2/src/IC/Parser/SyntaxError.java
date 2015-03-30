package IC.Parser;

public class SyntaxError extends Exception {

    private Token token;
    private int line;
    private int column;
    private String desc;

    public  SyntaxError(Token token){
        this.token = token;
    }

    public SyntaxError(String msg, Token token) {
        super(msg);
        setColumn(token.getColumn());
        setLine(token.getLine());
        setDesc(token.getTokenName(token.sym));
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public SyntaxError(String message, int line, int column, String desc) {
        super(message);
        this.line = line;
        this.column = column;
        this.desc = desc;
    }

    public SyntaxError(String message, int line, String desc) {
        super(message);
        this.line = line;
        this.desc = desc;
    }

    public String toString() {
        String msg = "Line " + this.line + ": Syntax error " + this.getMessage();
        if (desc != null) {
            msg += "; " + desc;
        }
        return msg;
    }
}
