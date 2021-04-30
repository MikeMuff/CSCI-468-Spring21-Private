package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Opcodes;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;
import static edu.montana.csci.csci468.tokenizer.TokenType.MINUS;
import static edu.montana.csci.csci468.tokenizer.TokenType.PLUS;

public class AdditiveExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public AdditiveExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
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
    public boolean isAdd() {
        return operator.getType() == TokenType.PLUS;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
        if (getType().equals(CatscriptType.INT)) {
            if (!leftHandSide.getType().equals(CatscriptType.INT)) {
                leftHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
            if (!rightHandSide.getType().equals(CatscriptType.INT)) {
                rightHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        }
        // I'm not sure if this handles strings
        //    there was a problem in my tokenizer showing that the + in  'a' + 1
        //    was a string
        //    I think evaluate handles strings not validate...
    }

    @Override
    public CatscriptType getType() {
        if (leftHandSide.getType().equals(CatscriptType.STRING) || rightHandSide.getType().equals(CatscriptType.STRING)) {
            return CatscriptType.STRING;
        } else {
            return CatscriptType.INT;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        if(CatscriptType.INT.equals(this.getType())){
            Integer lhsValue = (Integer) leftHandSide.evaluate(runtime);
            Integer rhsValue = (Integer) rightHandSide.evaluate(runtime);

            if (isAdd()) {
                return lhsValue + rhsValue;
            } else {
                return lhsValue - rhsValue;
            }
        }else{
            Object lhsValue = leftHandSide.evaluate();
            Object rhsValue = rightHandSide.evaluate();
            return String.valueOf(lhsValue) + String.valueOf(rhsValue);
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        getLeftHandSide().transpile(javascript);
        javascript.append(operator.getStringValue());
        getRightHandSide().transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        if (CatscriptType.INT.equals(this.getType())){
            getLeftHandSide().compile(code);
            getRightHandSide().compile(code);
            if (isAdd()) {
                code.addInstruction(Opcodes.IADD);
            } else {
                code.addInstruction(Opcodes.ISUB);
            }
        } else {
            CatscriptType rhsType = rightHandSide.getType();
            CatscriptType lhsType = leftHandSide.getType();

            if (rhsType.equals(CatscriptType.STRING)){
                getRightHandSide().compile(code);
                box(code, rightHandSide.getType());
            } else if (!rhsType.equals(CatscriptType.NULL)){
                getRightHandSide().compile(code);
                code.addMethodInstruction(Opcodes.INVOKESTATIC, internalNameFor(String.class),
                        "valueOf", "(I)Ljava/lang/String;");
            } else {
                getRightHandSide().compile(code);
                box(code, CatscriptType.STRING);
            }

            if (lhsType.equals(CatscriptType.STRING)){
                getLeftHandSide().compile(code);
                box(code, leftHandSide.getType());
            } else if (!lhsType.equals(CatscriptType.NULL)){
                getLeftHandSide().compile(code);
                code.addMethodInstruction(Opcodes.INVOKESTATIC, internalNameFor(String.class),
                        "valueOf", "(I)Ljava/lang/String;");
            } else {
                getLeftHandSide().compile(code);
                box(code, CatscriptType.STRING);
            }


            code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(String.class),
                    "concat", "(Ljava/lang/String;)Ljava/lang/String;");
        }
    }

}