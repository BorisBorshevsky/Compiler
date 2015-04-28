package IC.Symbols;

public class Symbol {
    private String name;
    // Field, Method, Local Variable, Etc
    private Kind kind;
    // int, string, etc
    private int symbolTypeId;
    private int lineNumber;

    public Symbol(String name, Kind kind, int symbolTypeIndex, int lineNumber) {
        this.name = name;
        this.kind = kind;
        this.symbolTypeId = symbolTypeIndex;
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getSymbolTypeId() {
        return symbolTypeId;
    }

    public int getTypeId() {
        return symbolTypeId;
    }

    public Kind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }


    public enum Kind {
        CLASS("Class"),
        FIELD("Field"),
        VIRTUAL_METHOD("Virtual method"),
        STATIC_METHOD("Static method"),
        LOCAL_VARIABLE("Local variable"),
        PARAMETER("Parameter");

        String prettyName;

        Kind(String prettyName) {
            this.prettyName = prettyName;
        }

        @Override
        public String toString() {
            return this.prettyName;
        }
    }


}
