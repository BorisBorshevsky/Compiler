package IC;

import IC.Parser.Lexer;
import IC.Parser.Token;
import IC.Parser.sym;

import java.io.*;

public class Compailer {
    public static void main(String[] args) {
        Token currToken;
        StringBuilder sb = new StringBuilder();
        sb.append("token\ttag\tline :column");
        sb.append(System.getProperty("line.separator"));
        try {
            //FileReader txtFile = new FileReader(args[0]);
//            FileReader txtFile = new FileReader(new File("C:\\Tools\\IntellijWorkspace\\IC_COMPILER\\test\\IO examples\\Quicksort_bad.ic"));
//            FileReader txtFile = new FileReader(new File("C:\\Tools\\IntellijWorkspace\\IC_COMPILER\\test\\IO examples\\Quicksort.ic"));
//            FileReader txtFile = new FileReader(new File("C:\\Tools\\IntellijWorkspace\\IC_COMPILER\\test\\TestString.ic"));
            FileReader txtFile = new FileReader(new File("C:\\Tools\\IntellijWorkspace\\IC_COMPILER\\test\\IO examples\\Sieve.ic"));
            Lexer scanner = new Lexer(txtFile);
            do {
                currToken = scanner.next_token();
                sb.append(currToken.toString());
                sb.append(System.getProperty("line.separator"));
            } while (currToken.sym != sym.EOF);

        } catch (Exception e) {
            sb.append(e.getMessage().toString());
        } finally {
            System.out.println(sb.toString());
        }

    }
}