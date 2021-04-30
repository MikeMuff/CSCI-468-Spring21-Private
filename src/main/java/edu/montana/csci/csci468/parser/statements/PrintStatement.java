package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;
import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class PrintStatement extends Statement {
    private Expression expression;

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }


    public Expression getExpression() {
        return expression;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        Object evaluate = expression.evaluate(runtime);
        Object val = runtime.getValue(String.valueOf(evaluate));
        if(evaluate == null){
            getProgram().print(val);
        }else{
            getProgram().print(evaluate);
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        code.addVarInstruction(Opcodes.ALOAD, 0);
        getExpression().compile(code);
        CatscriptType type = getExpression().getType();

        box(code, type);
        code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(CatScriptProgram.class),
                "print", "(Ljava/lang/Object;)V");
    }

}