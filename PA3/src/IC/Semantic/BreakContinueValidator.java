package IC.Semantic;

import IC.AST.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class thea traversing the AST to validate Break, continue and this context
 */
public class BreakContinueValidator implements Visitor {

    private List<SemanticError> errors = new ArrayList<>();
    private Map<String, Integer> nodeAncestorsCounters = new HashMap<>();

    private void incrementCounter(Class clazz) {
        nodeAncestorsCounters.put(clazz.getSimpleName(), getCounter(clazz) + 1);
    }

    private void decrementCounter(Class clazz) {
        nodeAncestorsCounters.put(clazz.getSimpleName(), getCounter(clazz) - 1);
    }

    private int getCounter(Class clazz) {
        Integer currentValue = nodeAncestorsCounters.get(clazz.getSimpleName());
        return currentValue == null ? 0 : currentValue;
    }

    @Override
    public Object visit(Program program) {
        for (ICClass clazz : program.getClasses()) {
            clazz.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(ICClass icClass) {
        for (Method method : icClass.getMethods()) {
            method.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(Field field) {
        return null;
    }

    @Override
    public Object visit(VirtualMethod method) {
        incrementCounter(VirtualMethod.class);
        for (Statement statement : method.getStatements()) {
            statement.accept(this);
        }
        decrementCounter(VirtualMethod.class);
        return null;
    }

    @Override
    public Object visit(StaticMethod method) {
        for (Statement statement : method.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(LibraryMethod method) {
        return null;
    }

    @Override
    public Object visit(Formal formal) {
        return null;
    }

    @Override
    public Object visit(PrimitiveType type) {
        return null;
    }

    @Override
    public Object visit(UserType type) {
        return null;
    }

    @Override
    public Object visit(Assignment assignment) {
        assignment.getVariable().accept(this);
        assignment.getAssignment().accept(this);
        return null;
    }

    @Override
    public Object visit(CallStatement callStatement) {
        callStatement.getCall().accept(this);
        return null;
    }

    @Override
    public Object visit(Return returnStatement) {
        if (returnStatement.hasValue()) {
            returnStatement.getValue().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(If ifStatement) {
        ifStatement.getCondition().accept(this);
        ifStatement.getOperation().accept(this);
        if (ifStatement.hasElse()) {
            ifStatement.getElseOperation().accept(this);
        }
        return null;
    }

    /**
     * add while statment to context
     * @param whileStatement
     * @return
     */
    @Override
    public Object visit(While whileStatement) {
        incrementCounter(While.class);
        whileStatement.getCondition().accept(this);
        decrementCounter(While.class);
        return null;
    }

    /**
     * checks break context
     * @param breakStatement
     * @return
     */
    @Override
    public Object visit(Break breakStatement) {
        if (getCounter(While.class) == 0) {
            errors.add(new SemanticError("'break' not inside 'while'", breakStatement.getLine()));
        }
        return null;
    }

    /**
     * checks continue context
     * @param continueStatement
     * @return
     */
    @Override
    public Object visit(Continue continueStatement) {
        if (getCounter(While.class) == 0) {
            errors.add(new SemanticError("'continue' not inside 'while'", continueStatement.getLine()));
        }
        return null;
    }

    @Override
    public Object visit(StatementsBlock statementsBlock) {
        for (Statement statement : statementsBlock.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(LocalVariable localVariable) {
        if (localVariable.hasInitValue()) {
            localVariable.getInitValue().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(VariableLocation location) {
        if (location.isExternal()) {
            location.getLocation().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(ArrayLocation location) {
        location.getArray().accept(this);
        location.getIndex().accept(this);
        return null;
    }

    @Override
    public Object visit(StaticCall call) {
        for (Expression arg : call.getArguments()) {
            arg.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(VirtualCall call) {
        for (Expression arg : call.getArguments()) {
            arg.accept(this);
        }
        return null;
    }

    /**
     * checks "this" context
     * @param thisExpression
     * @return
     */
    @Override
    public Object visit(This thisExpression) {
        if (getCounter(VirtualMethod.class) == 0) {
            errors.add(new SemanticError("'this' not inside an instance method", thisExpression.getLine()));
        }
        return null;
    }

    @Override
    public Object visit(NewClass newClass) {
        return null;
    }

    @Override
    public Object visit(NewArray newArray) {
        newArray.getSize().accept(this);
        return null;
    }

    @Override
    public Object visit(Length length) {
        length.getArray().accept(this);
        return null;
    }

    @Override
    public Object visit(MathBinaryOp binaryOp) {
        binaryOp.getFirstOperand().accept(this);
        binaryOp.getSecondOperand().accept(this);
        return null;
    }

    @Override
    public Object visit(LogicalBinaryOp binaryOp) {
        binaryOp.getFirstOperand().accept(this);
        binaryOp.getSecondOperand().accept(this);
        return null;
    }

    @Override
    public Object visit(MathUnaryOp unaryOp) {
        unaryOp.getOperand().accept(this);
        return null;
    }

    @Override
    public Object visit(LogicalUnaryOp unaryOp) {
        unaryOp.getOperand().accept(this);
        return null;
    }

    @Override
    public Object visit(Literal literal) {
        return null;
    }

    @Override
    public Object visit(ExpressionBlock expressionBlock) {
        expressionBlock.getExpression().accept(this);
        return null;
    }

    public List<SemanticError> getErrors() {
        return errors;
    }
}
