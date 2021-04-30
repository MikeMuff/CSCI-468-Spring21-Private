package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class ListLiteralExpression extends Expression {
    List<Expression> values;
    private CatscriptType type;

    public ListLiteralExpression(List<Expression> values) {
        this.values = new LinkedList<>();
        for (Expression value : values) {
            this.values.add(addChild(value));
        }
    }

    public List<Expression> getValues() {
        return values;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        for (Expression value : values) {
            value.validate(symbolTable);
        }
        if (values.size() > 0) {
            // I think I did it?
            type = CatscriptType.getListType(values.get(0).getType());
            for(int i = 0; i< values.size();i++){
                CatscriptType listType = CatscriptType.getListType(values.get(i).getType());
                if(!type.toString().equals(listType.toString())){
                    type = CatscriptType.getListType(CatscriptType.OBJECT);
                    break;
                }
            }
        } else {
            type = CatscriptType.getListType(CatscriptType.OBJECT);
        }
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Object vals[] = new Object[values.size()];
        for(int i = 0;i<vals.length;i++){
            vals[i] = values.get(i).evaluate();
        }
        List<Object> val = Arrays.asList(vals);
        return val;
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        code.addTypeInstruction(Opcodes.NEW, internalNameFor(LinkedList.class));
        code.addInstruction(Opcodes.DUP);
        code.addMethodInstruction(Opcodes.INVOKESPECIAL, internalNameFor(LinkedList.class), "<init>", "()V");

        for (Expression value : values){
            code.addInstruction(Opcodes.DUP);
            value.compile(code);
            box(code, value.getType());
            code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(LinkedList.class), "add", "(Ljava/lang/Object;)Z");
            code.addInstruction(Opcodes.POP);
        }
    }


}