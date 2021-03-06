package IC;

import IC.AST.ICClass;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.Parser.*;
import IC.Semantic.*;
import IC.Symbols.GlobalSymbolTable;
import java_cup.runtime.Symbol;

import java.io.*;
import java.util.List;

/**
 * Main class of the jar project
 */
public class Compiler {
    private static String icFileName;
    private static String libFileName;
    private static boolean printAst;
    private static boolean dumpsymtab;

    public static void main(String[] args) {

        Options options = Options.parseCommandLineArgs(args);
        libFileName = options.getLibicPath();
        icFileName = options.getICFile();
        printAst = options.isPrintAST();
        dumpsymtab = options.isDumpSymTab();

        try { //main try

            ICClass libRootSymbol = null;
            Program icRootClass;

            if (libFileName != null) {
                libRootSymbol = (ICClass) parseLibFile(libFileName);
            }
            icRootClass = parseMainFile();

            if (icRootClass != null) {
                if (semanticChecks(libRootSymbol, icRootClass)) {
                    if (printAst) {
                        PrettyPrinter prettyPrinter = new PrettyPrinter(icFileName);
                        prettyPrinter.isEnabledASTLibraryPrinting(false);
                        String output = (String) icRootClass.accept(prettyPrinter);
                        System.out.println(output);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            System.err.print("File not found");
            System.err.println(e.getStackTrace());
        } catch (Exception e) {
            e.printStackTrace();
            //System.err.println(e.getStackTrace()  + e.getMessage();
        }
    }

    /**
     * does semntic checks and populates type tables
     *
     * @param libRootSymbol library
     * @param icRootClass   root file
     * @return is succeded
     * @throws IOException file not found
     */
    private static boolean semanticChecks(ICClass libRootSymbol, Program icRootClass) throws IOException {
        //semantic checks

        //add class to root class
        if (libRootSymbol != null) {
            icRootClass.getClasses().add(0, libRootSymbol);
        }

        SymbolTableBuilder symTabBuilder = new SymbolTableBuilder(new File(icFileName).getName());
        GlobalSymbolTable symbolTable = symTabBuilder.visit(icRootClass);
        if (printErrors(icFileName, symTabBuilder.getErrors())) {
            return false;
        }


        //scope
        ScopeChecker scopeChecker = new ScopeChecker();
        scopeChecker.visit(icRootClass);
        if (printErrors(icFileName, scopeChecker.getErrors())) {
            return false;
        }

        //type check
        TypeChecker typeChecker = new TypeChecker();
        typeChecker.visit(icRootClass, new TypeCheckingContext());
        if (printErrors(icFileName, typeChecker.getErrors())) {
            return false;
        }


        //single main check
        MainFunctionValidator mainValidator = new MainFunctionValidator();
        SemanticError mainValidatorResult = mainValidator.visit(icRootClass);
        if (mainValidatorResult != null) {
            printError(icFileName, mainValidatorResult);
            return false;
        }

        // break continue only in loop
        //this only in instance mothods
        BreakContinueValidator keywordValid = new BreakContinueValidator();
        keywordValid.visit(icRootClass);
        if (printErrors(icFileName, keywordValid.getErrors())) {
            return false;
        }

        if (dumpsymtab) {
            System.out.println();
            System.out.println(symbolTable.toString());
        }
        return true;
    }


    /**
     * parse the main IC file
     *
     * @return Program (symbol)
     * @throws Exception
     */
    private static Program parseMainFile() throws Exception {
        FileReader programFile = new FileReader(icFileName);
        Lexer scanner = new Lexer(programFile);
        Parser parser = new Parser(scanner);
        try {
            Symbol rootSymbol = parser.parse();
            System.out.println(String.format("Parsed %s successfully!", icFileName));
            return (Program) rootSymbol.value;

        } catch (SyntaxError e) {
            System.err.print("Syntax Error while parsing IC File " + icFileName + ": ");
            System.err.println(e.toString());
        } catch (LexicalError e) {
            System.err.print("Lexical error while parsing IC File " + icFileName + ": ");
            System.err.println(e.toString());
        } finally {
            programFile.close();
        }
        return null;
    }

    /**
     * parse the library file
     *
     * @param libFileName
     * @return
     * @throws Exception
     */
    private static Object parseLibFile(String libFileName) throws Exception {
        FileReader libFile = new FileReader(libFileName);
        Lexer libLexer = new Lexer(libFile);
        LibParser libParser = new LibParser(libLexer);
        try { //parsing library file
            Symbol libRootSymbol = libParser.parse();
            if (libRootSymbol != null) {
                System.out.println(String.format("Parsed %s successfully!", libFileName));
                return libRootSymbol.value;
            } else {
                System.err.println(String.format("Parsed %s Failed!", libFileName));
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
        return null;
    }

    private static boolean printErrors(String filepath, List<SemanticError> errors) throws IOException {
        boolean hadErrors2 = false;
        for (SemanticError error : errors) {
            printError(filepath, error);
            hadErrors2 = true;
        }
        return hadErrors2;
    }

    private static void printError(String filepath, ICCompilerError e)
        throws IOException {
        System.out.println();
        System.out.println(e);
        printLine(filepath, e.getLine());
    }

    private static void printLine(String filepath, int line) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filepath));
        int currentLine = 0;
        String strLine;
        while ((strLine = in.readLine()) != null) {
            if (++currentLine == line) {
                // Print the content on the console
                System.out.println("Line " + line + ": " + strLine);
                break;
            }
        }
    }
}

