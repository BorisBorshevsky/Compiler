package IC.Semantic;

import IC.SymbolTypes.ClassSymbolType;
import IC.SymbolTypes.MethodSymbolType;

public class TypeCheckingContext {
    ClassSymbolType currentClassSymbolType; // Used for 'this' type checking
    MethodSymbolType currentMethodSymbolType; // Used for 'return' type checking
}
