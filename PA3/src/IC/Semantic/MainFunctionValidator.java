package IC.Semantic;

import IC.AST.*;
import IC.DataTypes;
import IC.Parser.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MainFunctionValidator implements Visitor {

    /**
     * counts all the main methods in the program
     * @param program
     * @return
     */
    @Override
    public SemanticError visit(Program program) {
        List<String> classesWithMain = new ArrayList<>();
        int lineNumberWithSecondMain = -1;
        for (ICClass clazz : program.getClasses()) {
            int lineNumberOfClassMainMethod = (Integer) clazz.accept(this);
            if (lineNumberOfClassMainMethod >= 0) {
                classesWithMain.add("class " + clazz.getName());
                lineNumberWithSecondMain = lineNumberOfClassMainMethod;
            }
        }
        if (classesWithMain.size() > 1) {
            return new SemanticError("More than one class has 'main' function: " + StringUtils.joinStrings(classesWithMain), lineNumberWithSecondMain);
        }
        if (classesWithMain.size() == 0) {
            return new SemanticError("Main function wasn't found in any of the classes.", program.getLine());
        }
        return null;
    }

    @Override
    public Integer visit(ICClass icClass) {
        for (Method method : icClass.getMethods()) {
            int lineNumberOfMain = (Integer) method.accept(this);
            if (lineNumberOfMain >= 0) {
                return lineNumberOfMain;
            }
        }
        return -1;
    }

    @Override
    public Object visit(Field field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer visit(VirtualMethod method) {
        return -1;
    }

    @Override
    public Integer visit(StaticMethod method) {
        if (!method.getName().equals("main")) {
            return -1;
        }
        if (!(method.getType() instanceof PrimitiveType)) {
            return -1;
        }
        PrimitiveType returnType = (PrimitiveType) method.getType();
        if (returnType.getDataType() != DataTypes.VOID) {
            return -1;
        }
        if (method.getFormals().size() != 1 || !(method.getFormals().get(0).getType() instanceof PrimitiveType)) {
            return -1;
        }
        PrimitiveType formalType = (PrimitiveType) method.getFormals().get(0).getType();
        if (formalType.getDataType() != DataTypes.STRING || formalType.getDimension() != 1) {
            return -1;
        }
        return method.getLine();
    }

    @Override
    public Integer visit(LibraryMethod method) {
        return -1;
    }

    @Override
    public Object visit(Formal formal) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(PrimitiveType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(UserType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(Assignment assignment) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(CallStatement callStatement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(Return returnStatement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(If ifStatement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(While whileStatement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(Break breakStatement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(Continue continueStatement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(StatementsBlock statementsBlock) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(LocalVariable localVariable) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(VariableLocation location) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(ArrayLocation location) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(StaticCall call) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(VirtualCall call) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(This thisExpression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(NewClass newClass) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(NewArray newArray) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(Length length) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(MathBinaryOp binaryOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(LogicalBinaryOp binaryOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(MathUnaryOp unaryOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(LogicalUnaryOp unaryOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(Literal literal) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(ExpressionBlock expressionBlock) {
        // TODO Auto-generated method stub
        return null;
    }

}
