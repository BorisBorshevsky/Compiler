package IC.Semantic;

import IC.AST.*;
import IC.Parser.StringUtils;
import IC.SymbolTypes.SymbolType;
import IC.Symbols.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ScopeChecker implements Visitor {

    private Stack<SymbolTable> symScopeStack = new Stack<>();
    private List<SemanticError> errors = new ArrayList<>();

    public ScopeChecker() {
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    private boolean verifySymbolIsOfKind(ASTNode node, String name, Symbol.Kind... kinds) {
        Symbol symbol;
        try {
            symbol = getCurrentScope().lookup(name);
        } catch (SymbolTableException e) {
            errors.add(new SemanticError(e.getMessage(), node.getLine()));
            return false;
        }
        if (currentScopeIsStaticAndSymbolIsVirtualMethodOrField(symbol)) {
            errors.add(new SemanticError("Trying to reference a non-static class member.", node.getLine()));
            return false;
        }
        return verifySymbolIsOfKind(node, symbol, kinds);
    }

    private boolean verifySymbolInOtherScopeIsOfKind(String otherScopeName, String symbolName, ASTNode node, Symbol.Kind... kinds) {
        Symbol symbol;
        try {
            SymbolTable otherScope = getCurrentScope().lookupScope(otherScopeName);
            symbol = otherScope.lookup(symbolName);
        } catch (SymbolTableException e) {
            errors.add(new SemanticError(e.getMessage(), node.getLine()));
            return false;
        }
        return verifySymbolIsOfKind(node, symbol, kinds);
    }

    private boolean verifySymbolIsOfKind(ASTNode node, Symbol symbol, Symbol.Kind... kinds) {
        if (!Arrays.asList(kinds).contains(symbol.getKind())) {
            String kindsStr = StringUtils.joinStrings(Arrays.asList(kinds));
            errors.add(new SemanticError("Symbol is not of kind '" + kindsStr + "'", node.getLine(), symbol.getName()));
            return false;
        }
        return true;
    }

    private SymbolTable getCurrentScope() {
        return symScopeStack.peek();
    }

    /*
     * When visit fails return null otherwise return true (!= null)
     */
    @Override
    public Object visit(Program program) {
        // recursive call to class
        symScopeStack.push(program.getGlobalSymbolTable());
        for (ICClass clazz : program.getClasses()) {
            clazz.accept(this);
        }
        return true;
    }

    @Override
    public Object visit(ICClass clazz) {
        symScopeStack.push(clazz.getClassSymbolTable());
        for (Method meth : clazz.getMethods()) {
            meth.accept(this);
        }
        for (Field fld : clazz.getFields()) {
            fld.accept(this);
        }
        symScopeStack.pop();
        return true;
    }

    @Override
    public Object visit(Field field) {
        field.getType().accept(this);
        verifyFieldDoesntHideBaseClassMember(field);
        return true;
    }


    /**
     * checks for mase class members
     * @param field
     */
    private void verifyFieldDoesntHideBaseClassMember(Field field) {
        SymbolTable classScope = getCurrentScope();
        try {
            if (classScope.getParent() != null && classScope.getParent() instanceof ClassSymbolTable) {
                Symbol inBase = classScope.getParent().lookup(field.getName());
                SymbolType baseMemberType = classScope.getTypeTable().getSymbolById(inBase.getTypeId());
                errors.add(new SemanticError("Field '" + field.getName() + "' hides base class member " + baseMemberType + " '" + inBase.getName() + "'", field.getLine()));
            }
        } catch (SymbolTableException e) {
            // This means that symbol doesn't exist in base class.
        }
    }

    @Override
    public Object visit(VirtualMethod method) {
        visitMethod(method);
        return true;
    }

    @Override
    public Object visit(StaticMethod method) {
        visitMethod(method);
        return true;
    }

    @Override
    public Object visit(LibraryMethod method) {
        visitMethod(method);
        return true;
    }

    private void visitMethod(Method method) {
        symScopeStack.push(method.getMethodSymbolTable());
        for (Formal foraml : method.getFormals()) {
            foraml.accept(this);
        }
        for (Statement stmnt : method.getStatements()) {
            stmnt.accept(this);
        }
        method.getType().accept(this);
        symScopeStack.pop();
    }

    @Override
    public Object visit(Formal formal) {
        formal.getType().accept(this);

        return true;
    }

    @Override
    public Object visit(PrimitiveType type) {
        // always defined
        return true;
    }

    @Override
    public Object visit(UserType type) {
        verifySymbolIsOfKind(type, type.getName(), Symbol.Kind.CLASS);
        return true;
    }

    @Override
    public Object visit(Assignment assignment) {
        assignment.getAssignment().accept(this);
        assignment.getVariable().accept(this);

        return true;
    }

    @Override
    public Object visit(CallStatement callStatement) {
        callStatement.getCall().accept(this);
        return true;
    }

    @Override
    public Object visit(Return returnStatement) {
        if (returnStatement.hasValue()) {
            returnStatement.getValue().accept(this);
        }
        return true;
    }

    @Override
    public Object visit(If ifStatement) {
        ifStatement.getCondition().accept(this);
        ifStatement.getOperation().accept(this);
        if (ifStatement.hasElse()) {
            ifStatement.getElseOperation().accept(this);
        }
        return true;
    }

    @Override
    public Object visit(While whileStatement) {
        whileStatement.getCondition().accept(this);
        whileStatement.getOperation().accept(this);
        return true;
    }

    @Override
    public Object visit(Break breakStatement) {
        return true;
    }

    @Override
    public Object visit(Continue continueStatement) {
        return true;
    }

    @Override
    public Object visit(StatementsBlock statementsBlock) {
        symScopeStack.push(statementsBlock.getStatementsBlockSymbolTable());
        for (Statement stmt : statementsBlock.getStatements()) {
            stmt.accept(this);
        }
        symScopeStack.pop();
        return true;
    }

    @Override
    public Object visit(LocalVariable localVariable) {
        localVariable.getType().accept(this);
        if (localVariable.hasInitValue()) {
            localVariable.getInitValue().accept(this);
        }
        return true;
    }

    @Override
    public Object visit(VariableLocation location) {
        if (location.isExternal()) {
            location.getLocation().accept(this);
        } else {
            verifySymbolIsOfKind(location, location.getName(), Symbol.Kind.LOCAL_VARIABLE, Symbol.Kind.PARAMETER, Symbol.Kind.FIELD);
        }

        return true;
    }

    @Override
    public Object visit(ArrayLocation location) {
        location.getIndex().accept(this);
        location.getArray().accept(this);
        return true;
    }

    @Override
    public Object visit(StaticCall call) {
        for (Expression arg : call.getArguments()) {
            arg.accept(this);
        }
        if (!verifySymbolIsOfKind(call, call.getClassName(), Symbol.Kind.CLASS)) {
            return true;
        }
        verifySymbolInOtherScopeIsOfKind(call.getClassName(), call.getName(), call, Symbol.Kind.STATIC_METHOD);
        return true;
    }

    @Override
    public Object visit(VirtualCall call) {
        for (Expression arg : call.getArguments()) {
            arg.accept(this);
        }
        if (call.isExternal()) {
            call.getLocation().accept(this);

        } else {
            verifySymbolIsOfKind(call, call.getName(), Symbol.Kind.VIRTUAL_METHOD, Symbol.Kind.STATIC_METHOD);
        }

        return true;
    }

    @Override
    public Object visit(This thisExpression) {
        return true;
    }

    @Override
    public Object visit(NewClass newClass) {
        verifySymbolIsOfKind(newClass, newClass.getName(), Symbol.Kind.CLASS);
        return true;
    }

    @Override
    public Object visit(NewArray newArray) {
        newArray.getSize().accept(this);
        newArray.getType().accept(this);
        return true;
    }

    @Override
    public Object visit(Length length) {
        length.getArray().accept(this);
        return true;
    }

    @Override
    public Object visit(MathBinaryOp binaryOp) {
        binaryOp.getFirstOperand().accept(this);
        binaryOp.getSecondOperand().accept(this);
        return true;
    }

    @Override
    public Object visit(LogicalBinaryOp binaryOp) {
        binaryOp.getFirstOperand().accept(this);
        binaryOp.getSecondOperand().accept(this);
        // FIXME: need to validate that these are a part can be up casted to the
        // specific operation types
        binaryOp.getFirstOperand();
        binaryOp.getSecondOperand();
        return true;
    }

    @Override
    public Object visit(MathUnaryOp unaryOp) {
        unaryOp.getOperand().accept(this);
        // FIXME: need to validate that these are a part can be up casted to the
        // specific operation types
        unaryOp.getOperand();
        return true;
    }

    @Override
    public Object visit(LogicalUnaryOp unaryOp) {
        unaryOp.getOperand().accept(this);
        // FIXME: need to validate that these are a part can be up casted to the
        // specific operation types
        unaryOp.getOperand();
        return true;
    }

    @Override
    public Object visit(Literal literal) {
        return true;
    }

    @Override
    public Object visit(ExpressionBlock expressionBlock) {
        expressionBlock.getExpression().accept(this);
        return true;
    }

    /**
     *  This checks if the symbol that was currently found is in the instance-scope of a class, while the call has been made from the static-scope
     * @param symbol
     * @return
     */
    private boolean currentScopeIsStaticAndSymbolIsVirtualMethodOrField(Symbol symbol) {
        Symbol scopeSymbol;
        try {
            scopeSymbol = findClosestEnclosingMethodScope();
        } catch (SymbolTableException e) {
            return false;
        }
        boolean currentScopeIsStatic = scopeSymbol != null && scopeSymbol.getKind() == Symbol.Kind.STATIC_METHOD;
        boolean symbolIsNonStaticClassMember = symbol.getKind() == Symbol.Kind.FIELD || symbol.getKind() == Symbol.Kind.VIRTUAL_METHOD;
        return currentScopeIsStatic && symbolIsNonStaticClassMember;
    }

    private Symbol findClosestEnclosingMethodScope()
        throws SymbolTableException {
        SymbolTable scope = getCurrentScope();
        while (scope != null && !(scope instanceof MethodSymbolTable)) {
            scope = scope.getParent();
        }
        if (scope == null) {
            return null;
        }
        return getScopeSymbolInEnclosingScope(scope);
    }

    private Symbol getScopeSymbolInEnclosingScope(SymbolTable scope)
        throws SymbolTableException {
        return getCurrentScope().getParent().lookup(scope.getName());
    }

}
