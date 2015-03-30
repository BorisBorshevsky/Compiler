package IC;

import java.io.File;

/**
 * helper class to handle args from user.
 */
public class Options {
    public String getLibicPath() {
        return libicPath;
    }

    public String getICFile() {
        return icFile;
    }

    public boolean isPrintAST() {
        return printAST;
    }

    private String libicPath;
    private String icFile;
    private boolean printAST;
    private Options() {
        this.libicPath = null;
        this.printAST = false;
        this.icFile = null;
    }
    private static void handleWrongSyntax() {
        System.out.println("Can't run compiler.");
        System.out.println("Usage:\n\tjava IC.Compiler <file.ic> [ -L</path/to/libic.sig> ] [ -print-ast ]");
        System.exit(1);
    }
    public static Options parseCommandLineArgs(String[] args) {
        Options options = new Options();
        if (args.length == 0) {
            handleWrongSyntax();
        }

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.startsWith("-L")) {
                options.libicPath = arg.substring(2);
            } else if (arg.equals("-print-ast")) {
                options.printAST = true;
            } else if (!arg.startsWith("-") && options.icFile == null) {
                options.icFile = arg;
            } else {
                System.out.println("Unrecognized flag: " + arg);
                handleWrongSyntax();
            }
        }
        if (options.icFile == null) {
            handleWrongSyntax();
        }
        return options;
    }
}