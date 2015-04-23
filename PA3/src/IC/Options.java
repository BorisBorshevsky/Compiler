package IC;

import java.io.File;

/**
 * helper class to handle args from user.
 */
public class Options {
    private boolean dumpSymTab;
    private String libicPath;
    private String icFile;
    private boolean printAST;

    private Options() {
        this.libicPath = null;
        this.printAST = false;
        this.icFile = null;
        this.dumpSymTab = false;
    }

    private static void handleWrongSyntax() {
        System.out.println("Can't run compiler.");
        System.out.println("Usage:\n\tjava IC.Compiler <file.ic> [ -L</path/to/libic.sig> ] [ -print-ast ]");
        System.exit(1);
    }

    public static IC.Options parseCommandLineArgs(String[] args) {
        IC.Options options = new IC.Options();
        if (args.length == 0) {
            handleWrongSyntax();
        }

        for (String arg : args) {
            if (arg.startsWith("-L")) {
                options.libicPath = arg.substring(2);
            } else if (arg.equals("-print-ast")) {
                options.printAST = true;
            } else if (!arg.startsWith("-") && options.icFile == null) {
                options.icFile = arg;
            } else if (arg.equals("-dump-symtab")) {
                options.dumpSymTab = true;
            } else {
                System.out.println("Unrecognized flag: " + arg);
                handleWrongSyntax();
            }
        }

        if (options.icFile == null) {
            handleWrongSyntax();
        }

        options.makeSureValid();
        return options;
    }

    public String getLibicPath() {
        return libicPath;
    }

    public String getICFile() {
        return icFile;
    }

    public boolean isPrintAST() {
        return printAST;
    }

    public boolean isDumpSymTab() {
        return dumpSymTab;
    }

    private void makeSureValid() {
        boolean valid = true;
        if (libicPath != null) {
            File f = new File(libicPath);
            if (!f.exists()) {
                System.out.println("Can't find library signature file at path: " + libicPath);
                valid = false;
            }
        }
        File f = new File(icFile);
        if (!f.exists()) {
            System.out.println("Can't find source file at path: " + icFile);
            valid = false;
        }
        if (!valid) {
            handleWrongSyntax();
        }
    }
}