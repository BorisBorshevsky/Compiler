package IC.Semantic;

import IC.AST.*;
import IC.SymbolTypes.SymbolTypeTable;
import IC.Symbols.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *Traverses the AST, builds all the symbol table
 */
public class SymbolTableBuilder implements Visitor {

    private String programName;
    private SymbolTypeTable typeTable;
    private List<SemanticError> errors = new ArrayList<>();

    public SymbolTableBuilder(String programName) {
        this.programName = programName;
        this.typeTable = new SymbolTypeTable(programName);
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    @Override
    public GlobalSymbolTable visit(Program program) {
        // Create the main symbol table
        GlobalSymbolTable globalTable = new GlobalSymbolTable(programName, typeTable);
        program.setGlobalSymbolTable(globalTable);

        // Step 1: Go through the classes, and create a Symbol and a SymbolTable
        // for each one.
        Map<String, ClassSymbolTable> symbolTableForClass = new HashMap<>();

        for (ICClass clazz : program.getClasses()) {
            Symbol classSymbol = new Symbol(clazz.getName(), Symbol.Kind.CLASS, typeTable.getSymbolTypeId(clazz), clazz.getLine());

            insertSymbolToTable(globalTable, clazz, classSymbol);

            ClassSymbolTable classTable = (ClassSymbolTable) clazz.accept(this);
            symbolTableForClass.put(clazz.getName(), classTable);
            if (clazz.hasSuperClass()) {
                // If the class has a base class, add the class' symbol table as
                // a child at the base class' symbol table
                SymbolTable parentSymbolTable = symbolTableForClass.get(clazz.getSuperClassName());
                if (parentSymbolTable == null) {
                    errors.add(new SemanticError("Class '" + clazz.getName() + "' extends from a non-existant class", clazz.getLine(), clazz.getSuperClassName()));
                    globalTable.addChild(classTable);
                } else {
                    parentSymbolTable.addChild(classTable);
                }
                typeTable.setSuperForClass(clazz);
            } else {
                // Class doesn't have a base class.
                globalTable.addChild(classTable);
            }
        }

        return globalTable;
    }

    private void insertSymbolToTable(SymbolTable table, ASTNode declarationNode, Symbol newSymbol) {
        try {
            table.insert(newSymbol);
        } catch (SymbolTableException e) {
            errors.add(new SemanticError(e.getMessage(), declarationNode.getLine(), newSymbol.getName()));
        }
    }

    @Override
    public ClassSymbolTable visit(ICClass icClass) {
        ClassSymbolTable classTable = new ClassSymbolTable(icClass.getName(), typeTable);
        icClass.setClassSymbolTable(classTable);
        for (Field field : icClass.getFields()) {
            Symbol fieldSymbol = new Symbol(field.getName(), Symbol.Kind.FIELD, typeTable.getSymbolTypeId(field.getType(), field.getType().getDimension()), field.getLine());
            insertSymbolToTable(classTable, field, fieldSymbol);
        }

        for (Method method : icClass.getMethods()) {
            Symbol methodSymbol;
            if (method instanceof VirtualMethod) {
                methodSymbol = new Symbol(method.getName(), Symbol.Kind.VIRTUAL_METHOD, typeTable.getSymbolTypeId(method), method.getLine());
            } else { // method is a StaticMethod or a LibraryMethod)
                methodSymbol = new Symbol(method.getName(), Symbol.Kind.STATIC_METHOD, typeTable.getSymbolTypeId(method), method.getLine());
            }
            MethodSymbolTable methodTable = (MethodSymbolTable) method.accept(this);
            classTable.addChild(methodTable);
            insertSymbolToTable(classTable, method, methodSymbol);
        }
        return classTable;
    }

    @Override
    public Object visit(Field field) {
        return null;
    }

    @Override
    public MethodSymbolTable visit(VirtualMethod method) {
        return buildMethodSymbolTable(method);
    }

    private MethodSymbolTable buildMethodSymbolTable(Method method) {

        MethodSymbolTable table = new MethodSymbolTable(method.getName(), typeTable);
        method.setMethodSymbolTable(table);
        for (Formal formal : method.getFormals()) {
            Symbol symbol = new Symbol(formal.getName(), Symbol.Kind.PARAMETER, typeTable.getSymbolTypeId(formal.getType(), formal.getType().getDimension()), formal.getLine());
            insertSymbolToTable(table, formal, symbol);
        }
        getSymbolsAndChildTablesFromStatementList(table, method.getStatements());
        setParentNamesForChildren(table);
        return table;
    }

    private void setParentNamesForChildren(SymbolTable table) {
        for (SymbolTable child : table.getChildren()) {
            StatementBlockSymbolTable blockChild = (StatementBlockSymbolTable) child;
            blockChild.setParentName(table.getName());
            blockChild.setStatementOutputString(table.getName());
            setParentNamesForChildren(child);
        }
    }

    private void getSymbolsAndChildTablesFromStatementList(SymbolTable table, List<Statement> statements) {
        for (Statement statement : statements) {
            SymbolOrTables fromStatement = (SymbolOrTables) statement.accept(this);
            if (fromStatement == null) {
                continue;
            }
            fromStatement.addTo(table);
        }
    }

    @Override
    public MethodSymbolTable visit(StaticMethod method) {
        return buildMethodSymbolTable(method);
    }

    @Override
    public MethodSymbolTable visit(LibraryMethod method) {
        return buildMethodSymbolTable(method);
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
    public SymbolOrTables visit(If ifStatement) {
        SymbolOrTables fromOperation = (SymbolOrTables) ifStatement.getOperation().accept(this);
        SymbolOrTables result = new SymbolOrTables(fromOperation);
        if (ifStatement.hasElse()) {
            SymbolOrTables fromElseOperation = (SymbolOrTables) ifStatement.getElseOperation().accept(this);
            result.addTableFrom(fromElseOperation);
        }
        return result;
    }

    @Override
    public SymbolOrTables visit(While whileStatement) {
        return (SymbolOrTables) whileStatement.getOperation().accept(this);
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
    public SymbolOrTables visit(StatementsBlock statementsBlock) {
        StatementBlockSymbolTable blockTable = new StatementBlockSymbolTable(typeTable);
        statementsBlock.setBlockSymbolTable(blockTable);
        getSymbolsAndChildTablesFromStatementList(blockTable, statementsBlock.getStatements());
        return new SymbolOrTables(blockTable);
    }

    @Override
    public SymbolOrTables visit(LocalVariable localVariable) {
        Symbol symbol = new Symbol(localVariable.getName(), Symbol.Kind.LOCAL_VARIABLE, typeTable.getSymbolTypeId(localVariable.getType(), localVariable.getType().getDimension()), localVariable.getLine());
        return new SymbolOrTables(symbol, localVariable);
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

    class SymbolOrTables {
        Symbol symbol;
        ASTNode declarationNode;
        List<StatementBlockSymbolTable> tables = new ArrayList<>();

        public SymbolOrTables(Symbol symbol, ASTNode declarationNode) {
            this.symbol = symbol;
            this.declarationNode = declarationNode;
        }

        public SymbolOrTables(SymbolOrTables fromOperation) {
            this.addTableFrom(fromOperation);
        }

        public SymbolOrTables(StatementBlockSymbolTable blockTable) {
            this.tables.add(blockTable);
        }

        public void addTo(SymbolTable symbolTable) {
            if (symbol != null) {
                insertSymbolToTable(symbolTable, declarationNode, symbol);
            }
            tables.forEach(symbolTable::addChild);
        }

        public void addTableFrom(SymbolOrTables fromOperation) {
            if (fromOperation != null && fromOperation.tables.size() > 0) {
                tables.add(fromOperation.tables.get(0));
            }
        }
    }

}
