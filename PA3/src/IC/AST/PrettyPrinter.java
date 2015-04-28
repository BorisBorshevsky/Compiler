package IC.AST;

import IC.SymbolTypes.ArraySymbolType;
import IC.SymbolTypes.SymbolType;
import IC.Symbols.SymbolTableException;

/**
 * Pretty printing visitor - travels along the AST and prints info about each
 * node, in an easy-to-comprehend format.
 *
 */
public class PrettyPrinter implements Visitor {

    private static final String SYMBOL_TABLE_PRE_STRING = ", Symbol table: ";
    private static final String TYPE_PRE_STRING = ", Type: ";
    private boolean libraryPrintingEnabled = true;
    private int depth = 0; // depth of indentation
    private final String ICFilePath;

    /**
     * Constructs a new pretty printer visitor.
     *
     * @param ICFilePath The path + name of the IC file being compiled.
     */
    public PrettyPrinter(String ICFilePath) {
        this.ICFilePath = ICFilePath;
    }

    private static String getTypeStr(Type type) {
        return TYPE_PRE_STRING + type.getName();
    }

    boolean isLibraryPrintingEnabled() {
        return libraryPrintingEnabled;
    }


    /**
     * used for skipping library class printing
     * @param isLibraryPrintingEnabled
     */
    public void isEnabledASTLibraryPrinting(boolean isLibraryPrintingEnabled) {
        this.libraryPrintingEnabled = isLibraryPrintingEnabled;
    }

    private void indentAndGetLine(StringBuffer output, ASTNode node) {
        output.append("\n");
        for (int i = 0; i < depth * 2; ++i)
            output.append(" ");
        if (node != null)
            output.append(node.getLine()).append(": ");
    }

    private void indent(StringBuffer output) {
        indentAndGetLine(output, null);
    }

    /**
     * main visit
     * here you can see the skip library
     * @param program
     * @return
     */
    public Object visit(Program program) {
        StringBuffer output = new StringBuffer();

        indent(output);
        output.append("Abstract Syntax Tree: ").append(ICFilePath).append("\n");
        for (ICClass icClass : program.getClasses()) {
            icClass.setParent(program.getGlobalSymbolTable());
            if (!isLibraryPrintingEnabled()) {
                if (!icClass.getName().equals("Library")) {
                    output.append(icClass.accept(this));
                }
            } else {
                output.append(icClass.accept(this));
            }
        }
        return output.toString();
    }

    public Object visit(ICClass icClass) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, icClass);
        output.append("Declaration of class: ").append(icClass.getName());
        if (icClass.hasSuperClass()) {
            output.append(", subclass of ").append(icClass.getSuperClassName());
        }
        output.append(TYPE_PRE_STRING).append(icClass.getName());
        output.append(SYMBOL_TABLE_PRE_STRING).append(icClass.getClassSymbolTable().getParent().getAstName());
        addToDepth(2);
        for (Field field : icClass.getFields()) {
            indentAndGetLine(output, field);
            output.append("Declaration of field: ").append(field.getName()).append(TYPE_PRE_STRING).append(field.getType().getName()).append(SYMBOL_TABLE_PRE_STRING).append(icClass.getClassSymbolTable().getName());
        }
        for (Method method : icClass.getMethods()) {
            method.setParent(icClass.getClassSymbolTable());
            output.append(method.accept(this));
        }
        addToDepth(-2);
        return output.toString();
    }

    private void addToDepth(int howMany) {
        depth += howMany;
    }

    public Object visit(PrimitiveType type) {
        StringBuilder output = new StringBuilder();

        output.append(getTypeStr(type));

        for (int i = 0; i < type.getDimension(); i++) {
            output.append("[]");
        }
        output.append(SYMBOL_TABLE_PRE_STRING).append(type.getParent().getName());
        return output.toString();
    }

    public Object visit(UserType type) {
        StringBuilder output = new StringBuilder();
        output.append(getTypeStr(type));

        for (int i = 0; i < type.getDimension(); i++) {
            output.append("[]");
        }
        output.append(SYMBOL_TABLE_PRE_STRING).append(type.getParent().getName());
        return output.toString();
    }

    public Object visit(Field field) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, field);
        output.append("Declaration of field: ").append(field.getName());
        addToDepth(1);
        field.getType().setParent(field.getParent());
        output.append(field.getType().accept(this));
        addToDepth(-1);
        return output.toString();
    }

    public Object visit(LibraryMethod method) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, method);
        output.append("Declaration of library method: ").append(method.getName());
        addToDepth(2);
        method.getType().setParent(method.getMethodSymbolTable());
        output.append(method.getType().accept(this));
        for (Formal formal : method.getFormals()) {
            formal.setParent(method.getMethodSymbolTable());
            output.append(formal.accept(this));
        }
        addToDepth(-2);
        return output.toString();

    }

    public Object visit(Formal formal) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, formal);
        output.append("Parameter: ").append(formal.getName());
        addToDepth(1);
        formal.getType().setParent(formal.getParent());
        output.append(formal.getType().accept(this));
        addToDepth(-1);
        return output.toString();
    }

    public Object visit(VirtualMethod method) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, method);
        output.append("Declaration of virtual method: ").append(method.getName()).append(TYPE_PRE_STRING).append(method.toString()).append(SYMBOL_TABLE_PRE_STRING).append(method.getMethodSymbolTable().getParent().getName());
        addToDepth(2);
        for (Formal formal : method.getFormals()) {
            formal.setParent(method.getMethodSymbolTable());
            output.append(formal.accept(this));
        }
        for (Statement statement : method.getStatements()) {
            statement.setParent(method.getMethodSymbolTable());
            output.append(statement.accept(this));
        }
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(StaticMethod method) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, method);
        output.append("Declaration of static method: ").append(method.getName());
        output.append(TYPE_PRE_STRING).append(method.toString());
        output.append(SYMBOL_TABLE_PRE_STRING).append(method.getMethodSymbolTable().getParent().getName());
        addToDepth(2);
        for (Formal formal : method.getFormals()) {
            formal.setParent(method.getMethodSymbolTable());
            output.append(formal.accept(this));
        }
        for (Statement statement : method.getStatements()) {
            statement.setParent(method.getMethodSymbolTable());
            output.append(statement.accept(this));
        }
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(Assignment assignment) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, assignment);
        output.append("Assignment statement" + SYMBOL_TABLE_PRE_STRING).append(assignment.getParent().getName());
        addToDepth(2);
        assignment.getVariable().setParent(assignment.getParent());
        output.append(assignment.getVariable().accept(this));
        assignment.getAssignment().setParent(assignment.getParent());
        output.append(assignment.getAssignment().accept(this));
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(CallStatement callStatement) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, callStatement);
        output.append("Method call statement");
        addToDepth(1);
        output.append(callStatement.getCall().accept(this));
        addToDepth(-1);
        return output.toString();
    }

    public Object visit(Return returnStatement) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, returnStatement);
        output.append("Return statement");
        if (returnStatement.hasValue()) {
            output.append(", with return value");
            output.append(SYMBOL_TABLE_PRE_STRING).append(returnStatement.getParent().getName());
        }
        if (returnStatement.hasValue()) {
            addToDepth(1);
            returnStatement.getValue().setParent(returnStatement.getParent());
            output.append(returnStatement.getValue().accept(this));
            addToDepth(-1);
        }
        return output.toString();
    }

    public Object visit(If ifStatement) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, ifStatement);
        output.append("If statement");
        if (ifStatement.hasElse())
            output.append(", with Else operation");
        output.append(SYMBOL_TABLE_PRE_STRING).append(ifStatement.getParent().getName());
        addToDepth(2);

        ifStatement.getCondition().setParent(ifStatement.getParent());
        output.append(ifStatement.getCondition().accept(this));

        ifStatement.getOperation().setParent(ifStatement.getParent());
        output.append(ifStatement.getOperation().accept(this));

        if (ifStatement.hasElse()) {
            ifStatement.getElseOperation().setParent(ifStatement.getParent());
            output.append(ifStatement.getElseOperation().accept(this));
        }
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(While whileStatement) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, whileStatement);
        output.append("While statement, Symbol table: ").append(whileStatement.getParent().getName());
        addToDepth(2);
        whileStatement.getCondition().setParent(whileStatement.getParent());
        output.append(whileStatement.getCondition().accept(this));
        whileStatement.getOperation().setParent(whileStatement.getParent());
        output.append(whileStatement.getOperation().accept(this));
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(Break breakStatement) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, breakStatement);
        output.append("Break statement");
        output.append(SYMBOL_TABLE_PRE_STRING).append(breakStatement.getParent().getName());
        return output.toString();
    }

    public Object visit(Continue continueStatement) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, continueStatement);
        output.append("Continue statement");
        return output.toString();
    }

    public Object visit(StatementsBlock statementsBlock) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, statementsBlock);
        output.append("Block of statements, Symbol table: ").append(statementsBlock.getStatementsBlockSymbolTable().getParent().getName());
        addToDepth(2);
        for (Statement statement : statementsBlock.getStatements()) {
            statement.setParent(statementsBlock.getStatementsBlockSymbolTable());
            output.append(statement.accept(this));
        }
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(LocalVariable localVariable) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, localVariable);
        output.append("Declaration of local variable: ").append(localVariable.getName());
        if (localVariable.hasInitValue()) {
            output.append(", with initial value");
            addToDepth(1);
        }
        addToDepth(1);
        localVariable.getType().setParent(localVariable.getParent());
        output.append(localVariable.getType().accept(this));
        //       output.append(SYMBOL_TABLE_PRE_STRING + localVariable.getParent().getName());
        if (localVariable.hasInitValue()) {
            localVariable.getInitValue().setParent(localVariable.getParent());
            output.append(localVariable.getInitValue().accept(this));
            addToDepth(-1);
        }
        addToDepth(-1);
        return output.toString();
    }

    public Object visit(VariableLocation location) {
        StringBuffer output = new StringBuffer();
        try {
            indentAndGetLine(output, location);
            int symbolTypeId = location.getParent().lookup(location.getName()).getTypeId();
            SymbolType symbolType = location.getParent().getTypeTable().getSymbolById(symbolTypeId);
            output.append("Reference to variable: ").append(location.getName());

            if (location.isExternal()) {
                output.append(", in external scope");
            }
            output.append(TYPE_PRE_STRING).append(symbolType.toString());
            output.append(SYMBOL_TABLE_PRE_STRING).append(location.getParent().getName());

//        int symbolTypeId = getCurrentScope().lookup(symbolName).getTypeId();
//        symbolType = getTypeTable().getSymbolById(symbolTypeId);


            if (location.isExternal()) {
                addToDepth(1);
                location.getLocation().setParent(location.getParent());
                output.append(location.getLocation().accept(this));
                addToDepth(-1);
            }

        } catch (SymbolTableException e) {

            //should not happen
            e.printStackTrace();
        }
        return output.toString();
    }

    public Object visit(ArrayLocation location) {
        StringBuffer output = new StringBuffer();
        try {
            indentAndGetLine(output, location);
            output.append("Reference to array");
            int symbolTypeId = location.getParent().lookup(((VariableLocation) location.getArray()).getName()).getTypeId();
            SymbolType symbolType = location.getParent().getTypeTable().getSymbolById(symbolTypeId);
            output.append(TYPE_PRE_STRING).append(((ArraySymbolType) symbolType).getBaseType());
            output.append(SYMBOL_TABLE_PRE_STRING).append(location.getParent().getName());
            addToDepth(2);
            location.getArray().setParent(location.getParent());
            output.append(location.getArray().accept(this));
            location.getIndex().setParent(location.getParent());
            output.append(location.getIndex().accept(this));
            addToDepth(-2);
        } catch (SymbolTableException ste) {
            //its ok
        }
        return output.toString();
    }

    public Object visit(StaticCall call) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, call);
        output.append("Call to static method: ").append(call.getName()).append(", in class ").append(call.getClassName());
        addToDepth(2);
        for (Expression argument : call.getArguments())
            output.append(argument.accept(this));
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(VirtualCall call) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, call);
        output.append("Call to virtual method: ").append(call.getName());
        if (call.isExternal())
            output.append(", in external scope");
        addToDepth(2);
        if (call.isExternal())
            output.append(call.getLocation().accept(this));
        for (Expression argument : call.getArguments())
            output.append(argument.accept(this));
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(This thisExpression) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, thisExpression);
        output.append("Reference to 'this' instance");
        return output.toString();
    }

    public Object visit(NewClass newClass) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, newClass);
        output.append("Instantiation of class: ").append(newClass.getName()).append(TYPE_PRE_STRING).append(newClass.getName()).append(SYMBOL_TABLE_PRE_STRING).append(newClass.getParent().getName());
        return output.toString();
    }

    public Object visit(NewArray newArray) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, newArray);
        output.append("Array allocation");

//        int symbolTypeId = newArray.getParent().lookup(newArray.getName()).getTypeId();
//        SymbolType symbolType = location.getParent().getTypeTable().getSymbolById(symbolTypeId);
//        output.append("Reference to variable: " + location.getName());

        addToDepth(2);
        newArray.getType().setParent(newArray.getParent());
        output.append(newArray.getType().accept(this));
        newArray.getSize().setParent(newArray.getParent());
        output.append(newArray.getSize().accept(this));
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(Length length) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, length);
        output.append("Reference to array length");
        addToDepth(1);
        output.append(length.getArray().accept(this));
        addToDepth(-1);
        return output.toString();
    }

    public Object visit(MathBinaryOp binaryOp) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, binaryOp);
        output.append("Mathematical binary operation: ").append(binaryOp.getOperator().getDescription());
        output.append(TYPE_PRE_STRING + "int");
        output.append(SYMBOL_TABLE_PRE_STRING).append(binaryOp.getParent().getName());
        addToDepth(2);
        binaryOp.getFirstOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getFirstOperand().accept(this));
        binaryOp.getSecondOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getSecondOperand().accept(this));
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(LogicalBinaryOp binaryOp) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, binaryOp);
        output.append("Logical binary operation: ").append(binaryOp.getOperator().getDescription()).append(TYPE_PRE_STRING).append("boolean").append(SYMBOL_TABLE_PRE_STRING).append(binaryOp.getParent().getName());
        addToDepth(2);
        binaryOp.getFirstOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getFirstOperand().accept(this));
        binaryOp.getSecondOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getSecondOperand().accept(this));
        addToDepth(-2);
        return output.toString();
    }

    public Object visit(MathUnaryOp unaryOp) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, unaryOp);
        output.append("Mathematical unary operation: ").append(unaryOp.getOperator().getDescription());
        addToDepth(1);
        unaryOp.getOperand().setParent(unaryOp.getParent());
        output.append(unaryOp.getOperand().accept(this));
        addToDepth(-1);
        return output.toString();
    }

    public Object visit(LogicalUnaryOp unaryOp) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, unaryOp);
        output.append("Logical unary operation: ").append(unaryOp.getOperator().getDescription());
        addToDepth(1);
        unaryOp.getOperand().setParent(unaryOp.getParent());
        output.append(unaryOp.getOperand().accept(this));
        addToDepth(-1);
        return output.toString();
    }

    public Object visit(Literal literal) {
        StringBuffer output = new StringBuffer();
        indentAndGetLine(output, literal);
        output.append(literal.getType().getDescription()).append(": ").append(literal.getType().toFormattedString(literal.getValue())).append(TYPE_PRE_STRING).append(literal.getType().getShortName()).append(SYMBOL_TABLE_PRE_STRING).append(literal.getParent().getName());
        return output.toString();
    }

    public Object visit(ExpressionBlock expressionBlock) {
        StringBuffer output = new StringBuffer();

        indentAndGetLine(output, expressionBlock);
        output.append("Parenthesized expression");
        addToDepth(1);
        output.append(expressionBlock.getExpression().accept(this));
        addToDepth(-1);
        return output.toString();
    }
}