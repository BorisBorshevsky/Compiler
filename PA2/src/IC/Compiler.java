package IC;
import IC.AST.ASTNode;
import IC.AST.ICClass;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.Parser.*;
import java_cup.runtime.Symbol;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Main class of the jar project
 */
public class Compiler {

    public static void main(String[] args){

        Options options = Options.parseCommandLineArgs(args);
        String libFileName = options.getLibicPath();
        String icFileName = options.getICFile();
        boolean printAst = options.isPrintAST();

        try {
            if (libFileName != null) {
                FileReader libFile = new FileReader(libFileName);
                Lexer libLexer = new Lexer(libFile);
                LibParser libParser = new LibParser(libLexer);
                try {
                    Symbol libRootSymbol = libParser.parse();
                    if (libRootSymbol == null) {
                        System.err.println(String.format("Parsed %s Failed!", libFileName));
                    } else {
                        System.out.println(String.format("Parsed %s successfully!", libFileName));
                    }

                } catch (SyntaxError e) {
                    System.err.print("Syntax error while parsing Library File " + libFileName + ": ");
                    System.err.println(e.toString());
                } catch (LexicalError e) {
                    System.err.print("Lexical error while parsing Library File " + libFileName + ": ");
                    System.err.println(e.toString());
                } finally {
                    libFile.close();
                }
            }
            FileReader programFile = new FileReader(icFileName);
            Lexer scanner = new Lexer(programFile);
            Parser parser = new Parser(scanner);
            try {
                Symbol rootSymbol = parser.parse();
                Program ICRootClass = (Program)rootSymbol.value;
                System.out.println(String.format("Parsed %s successfully!", icFileName));

                if (printAst){
                    PrettyPrinter prettyPrinter = new PrettyPrinter(options.getICFile());
                    String output = (String) ICRootClass.accept(prettyPrinter);
                    System.out.println(output);
                }
            } catch (SyntaxError e) {
                System.err.print("Syntax Error while parsing IC File " + icFileName+ ": ");
                System.err.println(e.toString());
            } catch (LexicalError e) {
                System.err.print("Lexical error while parsing IC File " + icFileName+ ": ");
                System.err.println(e.toString());
            }
            finally {
                programFile.close();
            }
        } catch (FileNotFoundException e) {
            System.err.print("File not found");
            System.err.println(e.getMessage());
        } catch (Exception e ){
            System.err.println(e.getMessage());
        }
    }

}

