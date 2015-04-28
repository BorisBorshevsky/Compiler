package IC.Semantic;

import IC.SymbolTypes.ClassSymbolType;
import IC.SymbolTypes.MethodSymbolType;

public class TypeCheckingContext {
    public ClassSymbolType getCurrentClassSymbolType() {
        return currentClassSymbolType;
    }

    public void setCurrentClassSymbolType(ClassSymbolType currentClassSymbolType) {
        this.currentClassSymbolType = currentClassSymbolType;
    }

    public MethodSymbolType getCurrentMethodSymbolType() {
        return currentMethodSymbolType;
    }

    public void setCurrentMethodSymbolType(MethodSymbolType currentMethodSymbolType) {
        this.currentMethodSymbolType = currentMethodSymbolType;
    }

    private ClassSymbolType currentClassSymbolType; // Used for 'this' type checking
    private MethodSymbolType currentMethodSymbolType; // Used for 'return' type checking
}
