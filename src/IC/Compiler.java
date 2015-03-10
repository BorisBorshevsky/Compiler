package IC;

import IC.Parser.Lexer;
import IC.Parser.Token;
import IC.Parser.sym;
import java.io.FileReader;

/**
 * Main class for IC language compiler
 */
public class Compiler {
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Token currToken;
        StringBuilder sb = new StringBuilder();
        sb.append("token\ttag\tline :column");
        sb.append(System.getProperty("line.separator"));
        try {
            FileReader txtFile = new FileReader(args[0]);
            Lexer scanner = new Lexer(txtFile);

            currToken = scanner.next_token();
            while (currToken.sym != sym.EOF) {
                sb.append(currToken.toString());
                sb.append(System.getProperty("line.separator"));
                currToken = scanner.next_token();
            }
        } catch (Exception e) {
            sb.append(e.getMessage());
        } finally {
            System.out.println(sb.toString());
        }

    }
}