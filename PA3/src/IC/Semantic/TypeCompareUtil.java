package IC.Semantic;

import IC.SymbolTypes.ClassSymbolType;
import IC.SymbolTypes.PrimitiveSymbolType;
import IC.SymbolTypes.PrimitiveSymbolType.PrimitiveSymbolTypes;
import IC.SymbolTypes.SymbolType;
import IC.SymbolTypes.SymbolTypeTable;

public class TypeCompareUtil {
    private static final PrimitiveSymbolType NULL_TYPE = new PrimitiveSymbolType(PrimitiveSymbolTypes.NULL);
    private SymbolTypeTable typeTable;

    public TypeCompareUtil(SymbolTypeTable typeTable) {
        this.typeTable = typeTable;
    }

    /**
     * check if 2 types are equal including inharitance and type
     * @param type1
     * @param type2
     * @return
     */
    public boolean isTypeSameOrExtends(SymbolType type1, SymbolType type2) {
        if (type1.equals(type2)) {
            return true;
        }
        if ((type1.equals(NULL_TYPE) && type2.isReferenceType()) || (type2.equals(NULL_TYPE) && type1.isReferenceType())) {
            return true;
        }
        if (type1 instanceof ClassSymbolType) {
            ClassSymbolType classType = (ClassSymbolType) type1;
            if (isExtends(classType, type2)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExtends(ClassSymbolType subType, SymbolType superType) {
        while (subType.hasBaseClass()) {
            ClassSymbolType baseClass = (ClassSymbolType) typeTable
                .getSymbolById(subType.getBaseClassTypeId());
            if (baseClass.equals(superType)) {
                return true;
            } else {
                subType = baseClass;
            }
        }
        return false;
    }

}
