package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class EqualityExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public EqualityExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
        this.leftHandSide = addChild(leftHandSide);
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isEqual() {
        return operator.getType().equals(TokenType.EQUAL_EQUAL);
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
    }

    @Override
    public CatscriptType getType() {
        return CatscriptType.BOOLEAN;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Object lhsValue = leftHandSide.evaluate(runtime);
        Object rhsValue = rightHandSide.evaluate(runtime);
        boolean equals = String.valueOf(lhsValue).equals(String.valueOf(rhsValue));
        if(operator.getStringValue().equals("==")){
            if(equals){
                return true;
            }else{
                return false;
            }
        }else{
            if(!equals){
                return true;
            }else{
                return false;
            }
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Label T = new Label();
        Label F = new Label();
        String lhsT = String.valueOf(leftHandSide.getType());
        String rhsT = String.valueOf(rightHandSide.getType());

        if(lhsT.equals(rhsT)){
            getLeftHandSide().compile(code);
            getRightHandSide().compile(code);
            if(lhsT.equals("null")){
                code.addJumpInstruction(Opcodes.IF_ACMPNE, F);
                code.addInstruction(Opcodes.ICONST_1);
                code.addJumpInstruction(Opcodes.GOTO, T);
            }else{
                code.addJumpInstruction(Opcodes.IF_ICMPNE, F);
                code.addInstruction(Opcodes.ICONST_1);
                code.addJumpInstruction(Opcodes.GOTO, T);
            }
            code.addLabel(F);
            code.addInstruction(Opcodes.ICONST_0);
            code.addLabel(T);
        }else{
            if(operator.getStringValue().equals("==")){
                code.addInstruction(Opcodes.ICONST_0);
            }else{
                code.addInstruction(Opcodes.ICONST_1);
            }
        }
    }
}