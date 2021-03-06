package IC.Semantic;

import IC.AST.*;
import IC.SymbolTypes.*;
import IC.SymbolTypes.PrimitiveSymbolType.PrimitiveSymbolTypes;
import IC.Symbols.Symbol;

import IC.Symbols.SymbolTable;
import IC.Symbols.SymbolTableException;
import IC.UnaryOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TypeChecker implements semanticCheckVisitor<TypeCheckingContext, SymbolType> {

    private Stack<SymbolTable> symScopeStack = new Stack<>();
    private List<SemanticError> errors = new ArrayList<>();
    private TypeCompareUtil typeCompareUtil;

    public List<SemanticError> getErrors() {
        return errors;
    }

    private SymbolTable getCurrentScope() {
        return symScopeStack.peek();
    }

    @Override
    public SymbolType visit(Program program, TypeCheckingContext context) {
        symScopeStack.push(program.getGlobalSymbolTable());
        typeCompareUtil = new TypeCompareUtil(getCurrentScope().getTypeTable());
        for (ICClass clazz : program.getClasses()) {
            clazz.accept(this, context);
        }
        symScopeStack.pop();
        return null;
    }

    @Override
    public SymbolType visit(ICClass clazz, TypeCheckingContext context) {
        symScopeStack.push(clazz.getClassSymbolTable());
        Symbol classSymbol;
        try {
            classSymbol = getCurrentScope().lookup(clazz.getName());
        } catch (SymbolTableException e) {
            return null;
        }
        context.setCurrentClassSymbolType((ClassSymbolType) getTypeTable().getSymbolById(classSymbol.getTypeId()));
        // Visit child nodes.
        for (Method meth : clazz.getMethods()) {
            meth.accept(this, context);
        }
        for (Field fld : clazz.getFields()) {
            fld.accept(this, context);
        }
        context.setCurrentClassSymbolType(null);
        symScopeStack.pop();
        return null;
    }

    @Override
    public SymbolType visit(Field field, TypeCheckingContext context) {
        return null;
    }

    @Override
    public SymbolType visit(VirtualMethod method, TypeCheckingContext context) {
        MethodSymbolType methodSymbolType = visitMethod(method, context);

        try {
            Symbol methodInBaseClass = getCurrentScope().getParent().lookup(method.getName());
            SymbolType symbolInBaseClassType = getTypeTable().getSymbolById(methodInBaseClass.getTypeId());
            boolean symbolHidingLegal = false;
            String errorMessage = "";
            if (methodInBaseClass.getKind() == Symbol.Kind.VIRTUAL_METHOD) {
                MethodSymbolType methodInBaseClassType = (MethodSymbolType) symbolInBaseClassType;
                if (methodInBaseClassType.getFormalsTypes().size() != methodSymbolType.getFormalsTypes().size()) {
                    errorMessage += "Wrong number of arguments";
                } else {
                    symbolHidingLegal = true;
                    for (int i = 0; i < methodInBaseClassType.getFormalsTypes().size(); ++i) {
                        if (!typeCompareUtil.isTypeSameOrExtends(methodInBaseClassType.getFormalsTypes().get(i), methodSymbolType.getFormalsTypes().get(i))) {
                            symbolHidingLegal = false;
                            errorMessage += "Type of arg"
                                + i
                                + " ('"
                                + method.getFormals().get(i).getName()
                                + "')"
                                + " is expected to be >= '"
                                + methodInBaseClassType.getFormalsTypes()
                                .get(i) + "', but it is '"
                                + methodSymbolType.getFormalsTypes().get(i)
                                + "'\n";
                        }
                    }
                }
                if (!typeCompareUtil.isTypeSameOrExtends(methodSymbolType.getReturnType(), methodInBaseClassType.getReturnType())) {
                    errorMessage += "Return type '" + methodSymbolType.getReturnType() + "' is expected to <= '" + methodInBaseClassType.getReturnType() + "'\n";
                    symbolHidingLegal = false;
                }
            } else if (methodInBaseClass.getKind() == Symbol.Kind.STATIC_METHOD) {
                errorMessage += "Method in base class is marked as static";
            }
            if (!symbolHidingLegal) {
                errors.add(new SemanticError("Method [" + method.getName() + "] hides '" + methodInBaseClass.getKind() + "' in base class. Method signature: " + methodSymbolType + ", base class type: " + symbolInBaseClassType + ". Errors:\n" + errorMessage, method.getLine()));
            }
        } catch (SymbolTableException e) {
            // That's ok: not every method hides something in base class.
        }

        return methodSymbolType;
    }

    @Override
    public SymbolType visit(StaticMethod method, TypeCheckingContext context) {
        return visitMethod(method, context);
    }

    @Override
    public SymbolType visit(LibraryMethod method, TypeCheckingContext context) {
        return visitMethod(method, context);
    }

    private MethodSymbolType visitMethod(Method method, TypeCheckingContext context) {
        symScopeStack.push(method.getMethodSymbolTable());
        int typeId = -1;
        try {
            typeId = method.getMethodSymbolTable().getParent().lookup(method.getName()).getTypeId();
        } catch (SymbolTableException e) {
            e.printStackTrace();
        }
        MethodSymbolType symbolType = (MethodSymbolType) getTypeTable().getSymbolById(typeId);
        context.setCurrentMethodSymbolType(symbolType);
        for (Statement stmnt : method.getStatements()) {
            stmnt.accept(this, context);
        }
        context.setCurrentMethodSymbolType(null);
        symScopeStack.pop();
        return symbolType;
    }

    @Override
    public SymbolType visit(Formal formal, TypeCheckingContext context) {
        return null;
    }

    @Override
    public SymbolType visit(PrimitiveType type, TypeCheckingContext context) {
        return null;
    }

    @Override
    public SymbolType visit(UserType type, TypeCheckingContext context) {
        return null;
    }

    @Override
    public SymbolType visit(Assignment assignment, TypeCheckingContext context) {
        SymbolType variable = assignment.getVariable().accept(this, context);
        SymbolType expression = assignment.getAssignment().accept(this, context);
        checkTypeError(assignment, variable, expression);
        return getVoidType();
    }

    private boolean checkTypeError(ASTNode node, SymbolType expectedType, SymbolType actualType) {
        if (!typeCompareUtil.isTypeSameOrExtends(actualType, expectedType)) {
            errors.add(new SemanticError("Type error in node '" + node.getClass().getSimpleName() + "': unexpected type: '" + actualType + "', expected a type that is less than or equals to: '" + expectedType + "'", node.getLine()));
            return false;
        }
        return true;
    }

    private SymbolTypeTable getTypeTable() {
        return getCurrentScope().getTypeTable();
    }

    private SymbolType getVoidType() {
        return new PrimitiveSymbolType(
            PrimitiveSymbolType.PrimitiveSymbolTypes.VOID);
    }

    private SymbolType getPrimitiveType(PrimitiveSymbolTypes type) {
        return new PrimitiveSymbolType(type);
    }

    @Override
    public SymbolType visit(CallStatement callStatement, TypeCheckingContext context) {
        callStatement.getCall().accept(this, context);
        return getVoidType();
    }

    @Override
    public SymbolType visit(Return returnStatement, TypeCheckingContext context) {
        SymbolType returnType = context.getCurrentMethodSymbolType().getReturnType();
        // 1. void method, with return [expr]; -- Error
        // 2. void method, with return; -- OK
        // 3. non-void method, with return [expr] -- Check matching types
        // 4. non-void method, with return; -- Error
        if (returnStatement.hasValue()
            && context.getCurrentMethodSymbolType().getReturnType().equals(getVoidType())) {
            errors.add(new SemanticError("A 'void' method is trying to return a value", returnStatement.getLine()));
        } else if (!returnStatement.hasValue() && !context.getCurrentMethodSymbolType().getReturnType().equals(getVoidType())) {
            errors.add(new SemanticError("A non-'void' method should return a value", returnStatement.getLine()));
        } else if (returnStatement.hasValue()) {
            SymbolType expression = returnStatement.getValue().accept(this, context);
            checkTypeError(returnStatement, returnType, expression);
        }
        return getVoidType();
    }

    @Override
    public SymbolType visit(If ifStatement, TypeCheckingContext context) {
        SymbolType conditionExpression = ifStatement.getCondition().accept(this, context);
        checkTypeError(ifStatement, getPrimitiveType(PrimitiveSymbolType.PrimitiveSymbolTypes.BOOLEAN), conditionExpression);
        ifStatement.getOperation().accept(this, context);
        if (ifStatement.hasElse()) {
            ifStatement.getElseOperation().accept(this, context);
        }
        return getVoidType();
    }

    @Override
    public SymbolType visit(While whileStatement, TypeCheckingContext context) {
        SymbolType conditionExpression = whileStatement.getCondition().accept(this, context);
        checkTypeError(whileStatement, getPrimitiveType(PrimitiveSymbolType.PrimitiveSymbolTypes.BOOLEAN), conditionExpression);
        whileStatement.getOperation().accept(this, context);
        return getVoidType();
    }

    @Override
    public SymbolType visit(Break breakStatement, TypeCheckingContext context) {
        return getVoidType();
    }

    @Override
    public SymbolType visit(Continue continueStatement, TypeCheckingContext context) {
        return getVoidType();
    }

    @Override
    public SymbolType visit(StatementsBlock statementsBlock, TypeCheckingContext context) {
        symScopeStack.push(statementsBlock.getStatementsBlockSymbolTable());
        for (Statement stmt : statementsBlock.getStatements()) {
            stmt.accept(this, context);
        }
        symScopeStack.pop();
        return getVoidType();
    }

    @Override
    public SymbolType visit(LocalVariable localVariable, TypeCheckingContext context) {
        if (localVariable.hasInitValue()) {
            SymbolType variableType = getSymbolType(localVariable.getName());
            if (variableType != null) {
                SymbolType initValueType = localVariable.getInitValue().accept(
                    this, context);

                checkTypeError(localVariable, variableType, initValueType);
            }
        }
        return null;
    }

    private SymbolType getSymbolType(String symbolName) {
        SymbolType symbolType;
        try {
            int symbolTypeId = getCurrentScope().lookup(symbolName).getTypeId();
            symbolType = getTypeTable().getSymbolById(symbolTypeId);


        } catch (SymbolTableException e) {
            // Not supposed to get here: a variable was just declared.
            symbolType = null;
        }
        return symbolType;
    }

    @Override
    public SymbolType visit(VariableLocation location, TypeCheckingContext context) {
        if (location.isExternal()) {
            SymbolType locationType = location.getLocation().accept(this, context);
            if (!(locationType instanceof ClassSymbolType)) {
                errors.add(new SemanticError("Location is not a class, can't look for field '" + location.getName() + "' under expression of type '" + locationType + "'", location.getLine()));
                return getVoidType();
            } else {
                ClassSymbolType classSymbolType = (ClassSymbolType) locationType;
                String className = classSymbolType.getName();
                SymbolTable classSymbolTable;
                try {
                    classSymbolTable = getCurrentScope().lookupScope(className);
                } catch (SymbolTableException e) {
                    System.out.println("Unexpected compiler error.");
                    e.printStackTrace();
                    return null;
                }
                try {
                    Symbol variableSymbol = classSymbolTable.lookup(location.getName());
                    return getTypeTable().getSymbolById(variableSymbol.getTypeId());
                } catch (SymbolTableException e) {
                    errors.add(new SemanticError(e.getMessage(), location.getLine()));
                    return getVoidType();
                }
            }
        } else {
            return getSymbolType(location.getName());
        }
    }

    @Override
    public SymbolType visit(ArrayLocation location, TypeCheckingContext context) {
        SymbolType indexType = location.getIndex().accept(this, context);
        checkTypeError(location, getPrimitiveType(PrimitiveSymbolTypes.INT),
            indexType);
        SymbolType locationType = location.getArray().accept(this, context);
        if (!(locationType instanceof ArraySymbolType)) {
            errors.add(new SemanticError("Value is treated as an array when it is actaully of type '" + locationType + "'", location.getLine()));
            return getVoidType();
        }
        ArraySymbolType arrayType = (ArraySymbolType) locationType;
        return arrayType.getBaseType();
    }

    @Override
    public SymbolType visit(StaticCall call, TypeCheckingContext context) {
        Symbol methodSymbol;
        try {
            SymbolTable otherScope = getCurrentScope().lookupScope(call.getClassName());
            methodSymbol = otherScope.lookup(call.getName());
        } catch (SymbolTableException e) {
            return getVoidType();
        }

        MethodSymbolType methodSymbolType = (MethodSymbolType) getTypeTable().getSymbolById(methodSymbol.getTypeId());
        String methodName = call.getClassName() + "." + call.getName();

        checkMethodCallTypeMatching(call, methodName, methodSymbolType.getFormalsTypes(), context);

        return methodSymbolType.getReturnType();
    }

    private boolean checkMethodCallTypeMatching(Call call, String methodName, List<SymbolType> argumentsExpectedTypes, TypeCheckingContext context) {
        List<SymbolType> argumentsTypes = new ArrayList<>();
        for (Expression arg : call.getArguments()) {
            argumentsTypes.add(arg.accept(this, context));
        }

        boolean isCallLegal = true;
        if (argumentsExpectedTypes.size() != argumentsTypes.size()) {
            errors.add(new SemanticError("Wrong number of arguments on call to method [" + methodName + "]. Expected: " + argumentsExpectedTypes.size() + ", got: " + argumentsTypes.size(), call.getLine()));
            isCallLegal = false;
        } else {
            for (int i = 0; i < argumentsExpectedTypes.size(); ++i) {
                isCallLegal &= checkTypeError(call, argumentsExpectedTypes.get(i), argumentsTypes.get(i));
            }
        }
        return isCallLegal;
    }

    @Override
    public SymbolType visit(VirtualCall call, TypeCheckingContext context) {
        for (Expression arg : call.getArguments()) {
            arg.accept(this, context);
        }
        Symbol methodSymbol;
        String methodName;
        if (call.isExternal()) {
            // 1. Get Class type of location (type checking should have this info)
            SymbolType locationType = call.getLocation().accept(this, context);
            if (!(locationType instanceof ClassSymbolType)) {
                errors.add(new SemanticError("Can't invoke a method: expression is not a reference to a class object.", call.getLine()));
                return getVoidType();
            }
            // 2. Get symbol table for that class
            ClassSymbolType classLocation = (ClassSymbolType) locationType;
            SymbolTable classScope;
            try {
                classScope = getCurrentScope().lookupScope(classLocation.getName());
            } catch (SymbolTableException e) {
                errors.add(new SemanticError("Can't invoke a method: expression couldn't find class of type " + classLocation.getName() + ".", call.getLine()));
                return getVoidType();
            }

            try {
                methodSymbol = classScope.lookup(call.getName());
            } catch (SymbolTableException e) {
                errors.add(new SemanticError(e.getMessage(), call.getLine()));
                return getVoidType();
            }
            methodName = classLocation.getName() + "." + call.getName();
        } else {
            try {
                methodSymbol = getCurrentScope().lookup(call.getName());
            } catch (SymbolTableException e) {
                return getVoidType();
            }
            methodName = call.getName();
        }
        MethodSymbolType methodSymbolType = (MethodSymbolType) getTypeTable().getSymbolById(methodSymbol.getTypeId());

        checkMethodCallTypeMatching(call, methodName, methodSymbolType.getFormalsTypes(), context);

        return methodSymbolType.getReturnType();
    }

    @Override
    public SymbolType visit(This thisExpression, TypeCheckingContext context) {
        return context.getCurrentClassSymbolType();
    }

    @Override
    public SymbolType visit(NewClass newClass, TypeCheckingContext context) {
        Symbol classSymbol;
        try {
            classSymbol = getCurrentScope().lookup(newClass.getName());
        } catch (SymbolTableException e) {
            // ScopeChecker already checked this...
            return getVoidType();
        }
        return getTypeTable().getSymbolById(classSymbol.getTypeId());
    }

    @Override
    public SymbolType visit(NewArray newArray, TypeCheckingContext context) {
        SymbolType sizeType = newArray.getSize().accept(this, context);
        checkTypeError(newArray, getPrimitiveType(PrimitiveSymbolTypes.INT), sizeType);
        newArray.getType().incrementDimension();
        return getTypeTable().getSymbolById(getTypeTable().getSymbolTypeId(newArray.getType(), newArray.getType().getDimension()));
    }

    @Override
    public SymbolType visit(Length length, TypeCheckingContext context) {
        SymbolType arrayType = length.getArray().accept(this, context);
        if (!(arrayType instanceof ArraySymbolType)) {
            errors.add(new SemanticError("length can only be run on arrays; got type: " + arrayType, length.getLine()));
        }
        return getPrimitiveType(PrimitiveSymbolTypes.INT);
    }

    @Override
    public SymbolType visit(MathBinaryOp binaryOp, TypeCheckingContext context) {
        SymbolType leftType = binaryOp.getFirstOperand().accept(this, context);
        SymbolType rightType = binaryOp.getSecondOperand().accept(this, context);
        switch (binaryOp.getOperator()) {
            case DIVIDE:
            case MINUS:
            case MOD:
            case MULTIPLY:
                checkBinaryOp(binaryOp, context, getPrimitiveType(PrimitiveSymbolTypes.INT));
                return getPrimitiveType(PrimitiveSymbolTypes.INT);
            case PLUS:
                if (typeCompareUtil.isTypeSameOrExtends(leftType, getPrimitiveType(PrimitiveSymbolTypes.STRING)) && typeCompareUtil.isTypeSameOrExtends(rightType, getPrimitiveType(PrimitiveSymbolTypes.STRING))) {
                    return getPrimitiveType(PrimitiveSymbolTypes.STRING);
                }
                if (typeCompareUtil.isTypeSameOrExtends(leftType, getPrimitiveType(PrimitiveSymbolTypes.INT)) && typeCompareUtil.isTypeSameOrExtends(rightType, getPrimitiveType(PrimitiveSymbolTypes.INT))) {
                    return getPrimitiveType(PrimitiveSymbolTypes.INT);
                }
                errors.add(new SemanticError("'+' operator can be used for either INT addition or STRING concatenation. Types received: " + leftType + ", " + rightType, binaryOp.getLine()));
                return getPrimitiveType(PrimitiveSymbolTypes.NULL);
            case GT:
            case GTE:
            case LT:
            case LTE:
                checkBinaryOp(binaryOp, context, getPrimitiveType(PrimitiveSymbolTypes.INT));
                return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
            case EQUAL:
            case NEQUAL:
                if (!(typeCompareUtil.isTypeSameOrExtends(leftType, rightType) || typeCompareUtil.isTypeSameOrExtends(rightType, leftType))) {
                    errors.add(new SemanticError("Can't check equality in non-matching types. Types: " + leftType + ", " + rightType, binaryOp.getLine()));
                }
                return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
            default:
                // Should never get here: binary op must be of above types.
                return getVoidType();
        }
    }

    private void checkBinaryOp(BinaryOp binaryOp, TypeCheckingContext context, SymbolType opType) {
        SymbolType leftType = binaryOp.getFirstOperand().accept(this, context);
        SymbolType rightType = binaryOp.getSecondOperand().accept(this, context);
        if (!typeCompareUtil.isTypeSameOrExtends(leftType, rightType)) {
            errors.add(new SemanticError("Type error in node '" + binaryOp.getClass().getSimpleName() + "': unexpected type: '" + leftType + "', expected a type that is less than or equals to: '" + rightType + "'", binaryOp.getLine()));
        }
    }


    @Override
    public SymbolType visit(LogicalBinaryOp binaryOp, TypeCheckingContext context) {
        checkBinaryOp(binaryOp, context, getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
        return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
    }

    @Override
    public SymbolType visit(MathUnaryOp unaryOp, TypeCheckingContext context) {
        if (unaryOp.getOperand() instanceof Literal && unaryOp.getOperator() == UnaryOps.UMINUS) {
            ((Literal) unaryOp.getOperand()).yourParentIsUMinus();
        }
        return checkUnaryOp(unaryOp, context,
            getPrimitiveType(PrimitiveSymbolTypes.INT));
    }

    @Override
    public SymbolType visit(LogicalUnaryOp unaryOp, TypeCheckingContext context) {
        return checkUnaryOp(unaryOp, context,
            getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN));
    }

    private SymbolType checkUnaryOp(UnaryOp unaryOp, TypeCheckingContext context, SymbolType expectedType) {
        SymbolType operandType = unaryOp.getOperand().accept(this, context);
        checkTypeError(unaryOp, expectedType, operandType);
        return expectedType;
    }

    @Override
    public SymbolType visit(Literal literal, TypeCheckingContext context) {
        switch (literal.getType()) {
            case TRUE:
            case FALSE:
                return getPrimitiveType(PrimitiveSymbolTypes.BOOLEAN);
            case INTEGER:
                doBoundsChecking(literal);
                return getPrimitiveType(PrimitiveSymbolTypes.INT);
            case NULL:
                return getPrimitiveType(PrimitiveSymbolTypes.NULL);
            case STRING:
                return getPrimitiveType(PrimitiveSymbolTypes.STRING);
            default:
                // Should never get here, a literal must have one of the above
                // values.
                return getVoidType();
        }
    }

    private void doBoundsChecking(Literal literal) {
        if (literal.isParentUMinus() && literal.getValue().equals("2147483648")) {
            return; // It's in bounds.
        }
        try {
            Integer.valueOf((String) literal.getValue());
        } catch (NumberFormatException e) {
            errors.add(new SemanticError("Integer is out of bounds: " + literal.getValue(), literal.getLine()));
        }
    }

    @Override
    public SymbolType visit(ExpressionBlock expressionBlock, TypeCheckingContext context) {
        return expressionBlock.getExpression().accept(this, context);
    }

}
