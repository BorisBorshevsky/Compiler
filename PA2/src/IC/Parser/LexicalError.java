package IC.Parser;

/**
 * Exception class for Lexer
 */
public class LexicalError extends Exception {
    private int line;


    public LexicalError(String message) {
        super(message);
    }

    public LexicalError(String message, int line) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return this.line;
    }
}