package IC.AST;

import IC.SymbolTypes.ArraySymbolType;
import IC.SymbolTypes.SymbolType;
import IC.Symbols.Symbol;
import IC.Symbols.SymbolTableException;

/**
 * Pretty printing visitor - travels along the AST and prints info about each
 * node, in an easy-to-comprehend format.
 *
 * @author Tovi Almozlino
 */
public class PrettyPrinter implements Visitor {

    private int depth = 0; // depth of indentation

    private String ICFilePath;

    /**
     * Constructs a new pretty printer visitor.
     *
     * @param ICFilePath The path + name of the IC file being compiled.
     */
    public PrettyPrinter(String ICFilePath) {
        this.ICFilePath = ICFilePath;
    }

    private void indent(StringBuffer output, ASTNode node) {
        output.append("\n");
        for (int i = 0; i < depth * 2; ++i)
            output.append(" ");
        if (node != null)
            output.append(node.getLine() + ": ");
    }

    private void indent(StringBuffer output) {
        indent(output, null);
    }

    public Object visit(Program program) {
        StringBuffer output = new StringBuffer();

        indent(output);
        output.append("Abstract Syntax Tree: " + ICFilePath + "\n");
        for (ICClass icClass : program.getClasses()) {
            icClass.setParent(program.getGlobalSymbolTable());
            output.append(icClass.accept(this));
        }
        return output.toString();
    }

    public Object visit(ICClass icClass) {
        StringBuffer output = new StringBuffer();

        indent(output, icClass);
        output.append("Declaration of class: " + icClass.getName());
        if (icClass.hasSuperClass()) {
            output.append(", subclass of " + icClass.getSuperClassName());
        }
        output.append(", Type: " + icClass.getName() + ", Symbol table: " + icClass.getClassSymbolTable().getParent().getName());
        depth += 2;
        for (Field field : icClass.getFields()) {
            indent(output, field);
            output.append("Declaration of field: " + field.getName() + ", Type: " + field.getType().getName() + ", Symbol table: " + icClass.getClassSymbolTable().getName());
//            output.append("\n    2: Declaration of field: str, Type: string, Symbol table: A");
//            output.append(field.accept(this));
        }
        for (Method method : icClass.getMethods()) {
            method.setParent(icClass.getClassSymbolTable());
            output.append(method.accept(this));
        }
        depth -= 2;
        return output.toString();
    }

    public Object visit(PrimitiveType type) {
        StringBuffer output = new StringBuffer();

        output.append(getTypeStr(type));

        for (int i = 0; i < type.getDimension(); i++) {
            output.append("[]");
        }
        output.append(", Symbol table: " + type.getParent().getName());
        return output.toString();
    }

    public Object visit(UserType type) {
        StringBuffer output = new StringBuffer();


        output.append(getTypeStr(type));

        for (int i = 0; i < type.getDimension(); i++) {
            output.append("[]");
        }
        output.append(", Symbol table: " + type.getParent().getName());
        return output.toString();
    }

    private static String getTypeStr(Type type) {
        return ", Type: " + type.getName();
    }

    public Object visit(Field field) {
        StringBuffer output = new StringBuffer();

        indent(output, field);
        output.append("Declaration of field: " + field.getName());
        ++depth;
        field.getType().setParent(field.getParent());
        output.append(field.getType().accept(this));
        --depth;
        return output.toString();
    }

    public Object visit(LibraryMethod method) {
        StringBuffer output = new StringBuffer();

        indent(output, method);
        output.append("Declaration of library method: " + method.getName());
        depth += 2;
        output.append(method.getType().accept(this));
        for (Formal formal : method.getFormals()) {
            formal.setParent(method.getMethodSymbolTable());
            output.append(formal.accept(this));
        }
        depth -= 2;
        return output.toString();
    }

    public Object visit(Formal formal) {
        StringBuffer output = new StringBuffer();

        indent(output, formal);
        output.append("Parameter: " + formal.getName());
        ++depth;
        formal.getType().setParent(formal.getParent());
        output.append(formal.getType().accept(this));
        --depth;
        return output.toString();
    }

    public Object visit(VirtualMethod method) {
        StringBuffer output = new StringBuffer();

        indent(output, method);
        output.append("Declaration of virtual method: " + method.getName() + ", Type: " + method.toString() + ", Symbol table: " + method.getMethodSymbolTable().getParent().getName());
//        output.append("Declaration of virtual method: " + method.getName());
        depth += 2;
//        method.getType().setParent(method.getMethodSymbolTable());
//        output.append(method.getType().accept(this));
        for (Formal formal : method.getFormals()) {
            formal.setParent(method.getMethodSymbolTable());
            output.append(formal.accept(this));
        }
        for (Statement statement : method.getStatements()) {
            statement.setParent(method.getMethodSymbolTable());
            output.append(statement.accept(this));
        }
        depth -= 2;
        return output.toString();
    }

    public Object visit(StaticMethod method) {
        StringBuffer output = new StringBuffer();

        indent(output, method);
        output.append("Declaration of static method: " + method.getName());
        output.append(", Type: " + method.toString());
        output.append(", Symbol table: " + method.getMethodSymbolTable().getParent().getName());
        depth += 2;
//        output.append(method.getType().accept(this));
        for (Formal formal : method.getFormals()) {
            formal.setParent(method.getMethodSymbolTable());
            output.append(formal.accept(this));// + ", Symbol table: " + method.getMethodSymbolTable().getName());
        }
        for (Statement statement : method.getStatements()) {
            statement.setParent(method.getMethodSymbolTable());
            output.append(statement.accept(this));
        }
        depth -= 2;
        return output.toString();
    }

    public Object visit(Assignment assignment) {
        StringBuffer output = new StringBuffer();

        indent(output, assignment);
        output.append("Assignment statement" + ", Symbol table: " + assignment.getParent().getName());
        depth += 2;
        assignment.getVariable().setParent(assignment.getParent());
        output.append(assignment.getVariable().accept(this));
        assignment.getAssignment().setParent(assignment.getParent());
        output.append(assignment.getAssignment().accept(this));
        depth -= 2;
        return output.toString();
    }

    public Object visit(CallStatement callStatement) {
        StringBuffer output = new StringBuffer();

        indent(output, callStatement);
        output.append("Method call statement");
        ++depth;
        output.append(callStatement.getCall().accept(this));
        --depth;
        return output.toString();
    }

    public Object visit(Return returnStatement) {
        StringBuffer output = new StringBuffer();

        indent(output, returnStatement);
        output.append("Return statement");
        if (returnStatement.hasValue()) {
            output.append(", with return value");
            output.append(", Symbol table: " + returnStatement.getParent().getName());
        }
        if (returnStatement.hasValue()) {
            ++depth;
            returnStatement.getValue().setParent(returnStatement.getParent());
            output.append(returnStatement.getValue().accept(this));
            --depth;
        }
        return output.toString();
    }

    public Object visit(If ifStatement) {
        StringBuffer output = new StringBuffer();

        indent(output, ifStatement);
        output.append("If statement");
        if (ifStatement.hasElse())
            output.append(", with Else operation");
        output.append(", Symbol table: " + ifStatement.getParent().getName());
        depth += 2;

        ifStatement.getCondition().setParent(ifStatement.getParent());
        output.append(ifStatement.getCondition().accept(this));

        ifStatement.getOperation().setParent(ifStatement.getParent());
        output.append(ifStatement.getOperation().accept(this));

        if (ifStatement.hasElse()) {
            ifStatement.getElseOperation().setParent(ifStatement.getParent());
            output.append(ifStatement.getElseOperation().accept(this));
        }
        depth -= 2;
        return output.toString();
    }

    public Object visit(While whileStatement) {
        StringBuffer output = new StringBuffer();

        indent(output, whileStatement);
        output.append("While statement, Symbol table: " + whileStatement.getParent().getName());
        depth += 2;
        whileStatement.getCondition().setParent(whileStatement.getParent());
        output.append(whileStatement.getCondition().accept(this));
        whileStatement.getOperation().setParent(whileStatement.getParent());
        output.append(whileStatement.getOperation().accept(this));
        depth -= 2;
        return output.toString();
    }

    public Object visit(Break breakStatement) {
        StringBuffer output = new StringBuffer();

        indent(output, breakStatement);
        output.append("Break statement");
        output.append(", Symbol table: " + breakStatement.getParent().getName());
        return output.toString();
    }

    public Object visit(Continue continueStatement) {
        StringBuffer output = new StringBuffer();

        indent(output, continueStatement);
        output.append("Continue statement");
        return output.toString();
    }

    public Object visit(StatementsBlock statementsBlock) {
        StringBuffer output = new StringBuffer();

        indent(output, statementsBlock);
        output.append("Block of statements, Symbol table: " + statementsBlock.getStatementsBlockSymbolTable().getParent().getName());
        depth += 2;
        for (Statement statement : statementsBlock.getStatements()) {
            statement.setParent(statementsBlock.getStatementsBlockSymbolTable());
            output.append(statement.accept(this));
        }
        depth -= 2;
        return output.toString();
    }

    public Object visit(LocalVariable localVariable) {
        StringBuffer output = new StringBuffer();

        indent(output, localVariable);
        output.append("Declaration of local variable: " + localVariable.getName());
        if (localVariable.hasInitValue()) {
            output.append(", with initial value");
            ++depth;
        }
        ++depth;
        localVariable.getType().setParent(localVariable.getParent());
        output.append(localVariable.getType().accept(this));
 //       output.append(", Symbol table: " + localVariable.getParent().getName());
        if (localVariable.hasInitValue()) {
            localVariable.getInitValue().setParent(localVariable.getParent());
            output.append(localVariable.getInitValue().accept(this));
            --depth;
        }
        --depth;
        return output.toString();
    }

    public Object visit(VariableLocation location) {
        StringBuffer output = new StringBuffer();
        try {
            indent(output, location);
            int symbolTypeId = location.getParent().lookup(location.getName()).getTypeId();
            SymbolType symbolType = location.getParent().getTypeTable().getSymbolById(symbolTypeId);
            output.append("Reference to variable: " + location.getName());

            if (location.isExternal()) {
                output.append(", in external scope");
            }
            output.append(", Type: " + symbolType.toString());
            output.append(", Symbol table: " + location.getParent().getName());

//        int symbolTypeId = getCurrentScope().lookup(symbolName).getTypeId();
//        symbolType = getTypeTable().getSymbolById(symbolTypeId);


            if (location.isExternal()) {
                ++depth;
                location.getLocation().setParent(location.getParent());
                output.append(location.getLocation().accept(this));
                --depth;
            }

        } catch (SymbolTableException e) {

            //shoud not happend
            e.printStackTrace();
        }
        return output.toString();
    }

    public Object visit(ArrayLocation location) {
        StringBuffer output = new StringBuffer();
        try {
            indent(output, location);
            output.append("Reference to array");
            int symbolTypeId = location.getParent().lookup(((VariableLocation) location.getArray()).getName()).getTypeId();
            SymbolType symbolType = location.getParent().getTypeTable().getSymbolById(symbolTypeId);
            output.append(", Type: " + ((ArraySymbolType)symbolType).getBaseType());
            output.append(", Symbol table: " + location.getParent().getName());
            depth += 2;
            location.getArray().setParent(location.getParent());
            output.append(location.getArray().accept(this));
            location.getIndex().setParent(location.getParent());
            output.append(location.getIndex().accept(this));
            depth -= 2;
        }catch (SymbolTableException ste){

        }
        return output.toString();
    }

    public Object visit(StaticCall call) {
        StringBuffer output = new StringBuffer();

        indent(output, call);
        output.append("Call to static method: " + call.getName()
            + ", in class " + call.getClassName());
        depth += 2;
        for (Expression argument : call.getArguments())
            output.append(argument.accept(this));
        depth -= 2;
        return output.toString();
    }

    public Object visit(VirtualCall call) {
        StringBuffer output = new StringBuffer();

        indent(output, call);
        output.append("Call to virtual method: " + call.getName());
        if (call.isExternal())
            output.append(", in external scope");
        depth += 2;
        if (call.isExternal())
            output.append(call.getLocation().accept(this));
        for (Expression argument : call.getArguments())
            output.append(argument.accept(this));
        depth -= 2;
        return output.toString();
    }

    public Object visit(This thisExpression) {
        StringBuffer output = new StringBuffer();

        indent(output, thisExpression);
        output.append("Reference to 'this' instance");
        return output.toString();
    }

    public Object visit(NewClass newClass) {
        StringBuffer output = new StringBuffer();

        indent(output, newClass);
        output.append("Instantiation of class: " + newClass.getName() + ", Type: " + newClass.getName() + ", Symbol table: " + newClass.getParent().getName());
        return output.toString();
    }

    public Object visit(NewArray newArray) {
        StringBuffer output = new StringBuffer();

        indent(output, newArray);
        output.append("Array allocation");

//        int symbolTypeId = newArray.getParent().lookup(newArray.getName()).getTypeId();
//        SymbolType symbolType = location.getParent().getTypeTable().getSymbolById(symbolTypeId);
//        output.append("Reference to variable: " + location.getName());

        depth += 2;
        newArray.getType().setParent(newArray.getParent());
        output.append(newArray.getType().accept(this));
        newArray.getSize().setParent(newArray.getParent());
        output.append(newArray.getSize().accept(this));
        depth -= 2;
        return output.toString();
    }

    public Object visit(Length length) {
        StringBuffer output = new StringBuffer();

        indent(output, length);
        output.append("Reference to array length");
        ++depth;
        output.append(length.getArray().accept(this));
        --depth;
        return output.toString();
    }

    public Object visit(MathBinaryOp binaryOp) {
        StringBuffer output = new StringBuffer();

        indent(output, binaryOp);
        output.append("Mathematical binary operation: " + binaryOp.getOperator().getDescription());
        output.append(", Type: " + "int");
        output.append(", Symbol table: " + binaryOp.getParent().getName());
        depth += 2;
        binaryOp.getFirstOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getFirstOperand().accept(this));
        binaryOp.getSecondOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getSecondOperand().accept(this));
        depth -= 2;
        return output.toString();
    }

    public Object visit(LogicalBinaryOp binaryOp) {
        StringBuffer output = new StringBuffer();

        indent(output, binaryOp);
        output.append("Logical binary operation: " + binaryOp.getOperator().getDescription() + ", Type: " + "boolean" + ", Symbol table: " + binaryOp.getParent().getName());
        depth += 2;
        binaryOp.getFirstOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getFirstOperand().accept(this));
        binaryOp.getSecondOperand().setParent(binaryOp.getParent());
        output.append(binaryOp.getSecondOperand().accept(this));
        depth -= 2;
        return output.toString();
    }

    public Object visit(MathUnaryOp unaryOp) {
        StringBuffer output = new StringBuffer();

        indent(output, unaryOp);
        output.append("Mathematical unary operation: " + unaryOp.getOperator().getDescription());
        ++depth;
        unaryOp.getOperand().setParent(unaryOp.getParent());
        output.append(unaryOp.getOperand().accept(this));
        --depth;
        return output.toString();
    }

    public Object visit(LogicalUnaryOp unaryOp) {
        StringBuffer output = new StringBuffer();

        indent(output, unaryOp);
        output.append("Logical unary operation: " + unaryOp.getOperator().getDescription());
        ++depth;
        unaryOp.getOperand().setParent(unaryOp.getParent());
        output.append(unaryOp.getOperand().accept(this));
        --depth;
        return output.toString();
    }

    public Object visit(Literal literal) {
        StringBuffer output = new StringBuffer();

        indent(output, literal);
//        output.append(literal.getType().getDescription() + ": " + literal.getType().toFormattedString(literal.getValue()) + ", Type: " + literal.getType().getDescription() + ", Symbol table: ");
        output.append(literal.getType().getDescription() + ": " + literal.getType().toFormattedString(literal.getValue()) + ", Type: " + literal.getType().getShortName() + ", Symbol table: " + literal.getParent().getName());
        return output.toString();
    }

    public Object visit(ExpressionBlock expressionBlock) {
        StringBuffer output = new StringBuffer();

        indent(output, expressionBlock);
        output.append("Parenthesized expression");
        ++depth;
        output.append(expressionBlock.getExpression().accept(this));
        --depth;
        return output.toString();
    }
}