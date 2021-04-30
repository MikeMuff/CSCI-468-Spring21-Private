package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import edu.montana.csci.csci468.parser.expressions.FunctionCallExpression;
import org.objectweb.asm.Opcodes;
import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

import java.util.LinkedList;
import java.util.List;

public class FunctionCallStatement extends Statement {
    private FunctionCallExpression expression;
    public FunctionCallStatement(FunctionCallExpression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public List<Expression> getArguments() {
        return expression.getArguments();
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
    }

    public String getName() {
        return expression.getName();
    }

    @Override
    public void execute(CatscriptRuntime runtime) {
        FunctionDefinitionStatement function = getProgram().getFunction(getName());
        List<Expression> args = getArguments();
        List<Object> arguments = new LinkedList<>();
        for (int i = 0; i < args.size(); i++) {
            Object arg = args.get(i).evaluate(runtime);
            arguments.add(arg);
            String name = function.getParameterName(i);
            runtime.setValue(name, arg);
        }
        function.invoke(runtime, arguments);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        code.addVarInstruction(Opcodes.ALOAD,0);
        expression.compile(code);

        String descriptor = getProgram().getFunction(getName()).getDescriptor();
        String name = getName();
        String intName = internalNameFor(expression.getClass());
        code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(expression.getClass()), getName(), descriptor);

        CatscriptType type = expression.getType();
        Boolean isVoid = type.equals(CatscriptType.VOID);
        if(!isVoid) {
            code.addInstruction(Opcodes.POP);
        }
    }

}