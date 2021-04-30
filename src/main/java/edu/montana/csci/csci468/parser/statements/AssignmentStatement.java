package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class AssignmentStatement extends Statement {
    private Expression expression;
    private String variableName;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        CatscriptType symbolType = symbolTable.getSymbolType(getVariableName());
        if (symbolType == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            if(!symbolType.isAssignableFrom(expression.getType())){
                addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        }
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        runtime.setValue(variableName, expression.evaluate(runtime));
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Integer integer = code.resolveLocalStorageSlotFor(variableName);
        if (integer != null) {
            expression.compile(code);
            code.addVarInstruction(Opcodes.ISTORE, integer);
        } else {
            code.addVarInstruction(Opcodes.ALOAD, 0);
            expression.compile(code);
            if(expression.getType().equals(CatscriptType.INT)){
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, "I", code.getProgramInternalName());
            } else {
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, "L" + internalNameFor(expression.getType().getJavaType()) + ";", code.getProgramInternalName());
                unbox(code,expression.getType());
            }
        }
    }
}