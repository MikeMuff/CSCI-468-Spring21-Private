package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        if (tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        /* Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        } */
        Statement statement = (Statement) parseStatement();
        if (statement != null) return statement;
        statement = parseFunctionDeclaration();
        if (statement != null) return statement;
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseFunctionDeclaration(){
        if(tokens.match(FUNCTION)){
            FunctionDefinitionStatement functionDefinitionStatement = new FunctionDefinitionStatement();
            functionDefinitionStatement.setStart(tokens.consumeToken());
            functionDefinitionStatement.setName(require(tokens.getCurrentToken().getType(),
                    functionDefinitionStatement).getStringValue());
            require(LEFT_PAREN, functionDefinitionStatement);
            Token name = tokens.getCurrentToken();
            TypeLiteral typeLiteral = new TypeLiteral();

            while(!tokens.match(RIGHT_PAREN)){
                if(tokens.match(COMMA)){
                    Token comma = tokens.consumeToken();
                }
                if(tokens.match(IDENTIFIER)){
                    name = tokens.consumeToken();
                }
                if(tokens.match(COLON)){
                    tokens.consumeToken();
                    if(tokens.getCurrentToken().getStringValue().equals("int")){
                        tokens.consumeToken();
                        typeLiteral.setType(CatscriptType.INT);
                        functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                    }else if(tokens.getCurrentToken().getStringValue().equals("string")){
                        tokens.consumeToken();
                        typeLiteral.setType(CatscriptType.STRING);
                        functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                    }else if(tokens.getCurrentToken().getStringValue().equals("bool")){
                        tokens.consumeToken();
                        typeLiteral.setType(CatscriptType.BOOLEAN);
                        functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                    }else if(tokens.getCurrentToken().getStringValue().equals("object")){
                        tokens.consumeToken();
                        typeLiteral.setType(CatscriptType.OBJECT);
                        functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                    }else if(tokens.getCurrentToken().getStringValue().equals("list")){
                        tokens.consumeToken();
                        int count = 0;
                        if(tokens.match(LESS)){
                            require(LESS, functionDefinitionStatement);
                            count = 1;
                            while(tokens.getCurrentToken().getStringValue().equals("list")){
                                tokens.consumeToken();
                                require(LESS, functionDefinitionStatement);
                                count = count + 1;
                            }
                            if(tokens.getCurrentToken().getStringValue().equals("int")){
                                tokens.consumeToken();
                                typeLiteral.setType(CatscriptType.getListType(CatscriptType.INT));
                                functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                            }else if(tokens.getCurrentToken().getStringValue().equals("string")){
                                tokens.consumeToken();
                                typeLiteral.setType(CatscriptType.getListType(CatscriptType.STRING));
                                functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                            }else if(tokens.getCurrentToken().getStringValue().equals("bool")){
                                tokens.consumeToken();
                                typeLiteral.setType(CatscriptType.getListType(CatscriptType.BOOLEAN));
                                functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                            }else if(tokens.getCurrentToken().getStringValue().equals("object")){
                                tokens.consumeToken();
                                typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
                                functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                            }
                            if(count != 0){
                                for(int k = 0; k < count; k++){
                                    require(GREATER, functionDefinitionStatement);
                                }
                            }
                        }else{
                            typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
                            functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                        }
                    }
                }else{
                    typeLiteral.setType(CatscriptType.OBJECT);
                    functionDefinitionStatement.addParameter(name.getStringValue(), typeLiteral);
                }
            }
            require(RIGHT_PAREN, functionDefinitionStatement);

           //return type starts
            TypeLiteral returnType = new TypeLiteral();
            if(tokens.match(COLON)){
                Token colon = tokens.consumeToken();
                if(tokens.getCurrentToken().getStringValue().equals("int")){
                    tokens.consumeToken();
                    returnType.setType(CatscriptType.INT);
                    functionDefinitionStatement.setType(returnType);
                }else if(tokens.getCurrentToken().getStringValue().equals("string")){
                    tokens.consumeToken();
                    returnType.setType(CatscriptType.STRING);
                    functionDefinitionStatement.setType(returnType);
                }else if(tokens.getCurrentToken().getStringValue().equals("bool")){
                    tokens.consumeToken();
                    returnType.setType(CatscriptType.BOOLEAN);
                    functionDefinitionStatement.setType(returnType);
                }else if(tokens.getCurrentToken().getStringValue().equals("object")){
                    tokens.consumeToken();
                    returnType.setType(CatscriptType.OBJECT);
                    functionDefinitionStatement.setType(returnType);
                }else if(tokens.getCurrentToken().getStringValue().equals("list")) {
                    tokens.consumeToken();
                    int count= 0;
                    if(tokens.match(LESS)){
                        require(LESS, functionDefinitionStatement);
                        count = 1;
                        while(tokens.getCurrentToken().getStringValue().equals("list")){
                            tokens.consumeToken();
                            require(LESS, functionDefinitionStatement);
                            count = count + 1;
                        }
                        if(tokens.getCurrentToken().getStringValue().equals("int")){
                            tokens.consumeToken();
                            returnType.setType(CatscriptType.getListType(CatscriptType.INT));
                            functionDefinitionStatement.setType(returnType);
                        }else if(tokens.getCurrentToken().getStringValue().equals("string")){
                            tokens.consumeToken();
                            returnType.setType(CatscriptType.getListType(CatscriptType.STRING));
                            functionDefinitionStatement.setType(returnType);
                        }else if(tokens.getCurrentToken().getStringValue().equals("bool")){
                            tokens.consumeToken();
                            returnType.setType(CatscriptType.getListType(CatscriptType.BOOLEAN));
                            functionDefinitionStatement.setType(returnType);
                        }else if(tokens.getCurrentToken().getStringValue().equals("object")){
                            tokens.consumeToken();
                            returnType.setType(CatscriptType.getListType(CatscriptType.OBJECT));
                            functionDefinitionStatement.setType(returnType);
                        }
                        if(count != 0){
                            for(int k = 0; k < count; k++){
                                require(GREATER, functionDefinitionStatement);
                            }
                        }
                    }else{
                        returnType.setType(CatscriptType.getListType(CatscriptType.OBJECT));
                        functionDefinitionStatement.setType(returnType);
                    }
                }
            }else{
                returnType.setType(CatscriptType.VOID);
                functionDefinitionStatement.setType(returnType);
            }

            ReturnStatement returnStatement = new ReturnStatement();
            require(LEFT_BRACE, functionDefinitionStatement);
            List<Statement> statementList = new ArrayList<>();
            while(!tokens.match(RIGHT_BRACE)){
                if(tokens.match(EOF)){
                    break;
                }
                if(tokens.match(RETURN)){
                    tokens.consumeToken();
                    if(tokens.match(RIGHT_BRACE)){
                        statementList.add(returnStatement);
                        break;
                    }else{
                        returnStatement.setExpression(parseExpression());
                        statementList.add(returnStatement);
                    }
                    break;
                }
                Statement aStatement = parseProgramStatement();
                statementList.add(aStatement);

            }
            functionDefinitionStatement.setBody(statementList);
            require(RIGHT_BRACE, functionDefinitionStatement);
            returnStatement.setFunctionDefinition(functionDefinitionStatement);
            return functionDefinitionStatement;
        }else{
            return null;
        }
    }





    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    private Object parseStatement() {
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        if (tokens.match(FOR)) {
            return parseForStatement();
        }
        if (tokens.match(IF)) {
            return parseIfStatement();
        }
        if (tokens.match(VAR)) {
            return parseVarStatement();
        }
        if (tokens.match(IDENTIFIER)) {
            return parseAssOrFunStatement();
        }
        Statement funcDeclare = parseFunctionDeclaration();
        if(funcDeclare != null){
            return funcDeclare;
        }


        //require identifier and then look ahead 'is there an open param or ='

        return new SyntaxErrorStatement(tokens.consumeToken());
        // if all return null than return an error.
    }

    private Statement parseAssOrFunStatement() {
        Token identifierToken = tokens.consumeToken();
        if(tokens.match(LEFT_PAREN)){
            return parseFunctionCallStatement(identifierToken);
        }else{
            return parseAssignmentStatement(identifierToken);
        }
    }

    private Statement parseFunctionCallStatement(Token identifierToken){
        List<Expression> argumentList = new ArrayList<>();
        FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), argumentList);
        if(tokens.match(LEFT_PAREN)) {
            Token start = tokens.consumeToken();
            if (tokens.match(RIGHT_PAREN)) {
                functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), argumentList);
            } else {
                Expression val = parseExpression();
                argumentList.add(val);
            }
            while (tokens.match(COMMA)) {
                Token comma = tokens.consumeToken();
                Expression val = parseExpression();
                argumentList.add(val);
            }
            require(RIGHT_PAREN, functionCallExpression);
            functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), argumentList);
        }
        FunctionCallStatement functionStatement = new FunctionCallStatement(functionCallExpression);
        return functionStatement;
    }


    private Statement parseForStatement() {
        List<Statement> statementLinkedList = new ArrayList<>();
        ForStatement forStatement = new ForStatement();
        forStatement.setStart(tokens.consumeToken());
        require(LEFT_PAREN, forStatement);
        forStatement.setVariableName(require(IDENTIFIER, forStatement).getStringValue());
        require(IN, forStatement);
        forStatement.setExpression(parseExpression());
        require(RIGHT_PAREN, forStatement);
        require(LEFT_BRACE, forStatement);
        while (!tokens.match(RIGHT_BRACE, EOF)) {
            Statement statement = parseProgramStatement();
            statementLinkedList.add(statement);
        }

        forStatement.setBody(statementLinkedList);
        require(RIGHT_BRACE, forStatement);
        return forStatement;
    }

    private Statement parseIfStatement() {
        IfStatement ifStatement = new IfStatement();
        ifStatement.setStart(tokens.consumeToken()); //consume the if
        require(LEFT_PAREN, ifStatement); //require
        ifStatement.setExpression(parseExpression());
        require(RIGHT_PAREN, ifStatement);
        require(LEFT_BRACE, ifStatement);
        List<Statement> statementList = new ArrayList<>();
        while (!tokens.match(RIGHT_BRACE, EOF)) {
            if (tokens.match(COMMA)) {
                tokens.consumeToken();
            } else {
                Statement statement = parseProgramStatement();
                statementList.add(statement);
            }
        }
        require(RIGHT_BRACE, ifStatement);

        List<Statement> statementList2 = new ArrayList<>();
        if (tokens.match(ELSE)) {
            tokens.matchAndConsume(); //consume the else

            if (tokens.match(RIGHT_BRACE)) { //
                while (!tokens.match(RIGHT_BRACE, EOF)) {
                    Statement statement = parseProgramStatement();
                    statementList2.add(statement);
                }
            }
            else
                parseIfStatement();
        }

        ifStatement.setTrueStatements(statementList);
        ifStatement.setElseStatements(statementList2);
        require(RIGHT_BRACE, ifStatement);
        return ifStatement;


    }


    private Statement parseVarStatement() {
        VariableStatement variableStatement = new VariableStatement();
        variableStatement.setStart(tokens.consumeToken()); //consume the var
        variableStatement.setVariableName(require(IDENTIFIER, variableStatement).getStringValue()); //require
        CatscriptType explicitType = null;
        if (tokens.matchAndConsume(COLON)) {
            // handle the type expression
            TypeLiteral typeLiteral = parseTypeLiteral();

            variableStatement.setExplicitType(typeLiteral.getType());

            //parseTypeLiteral(explicitType);  want to do this
        }
        require(EQUAL, variableStatement);
        variableStatement.setExpression(parseExpression());

        //call some setters on varStatement
        return variableStatement;

    }

    private Statement parseAssignmentStatement(Token identifierToken){
        AssignmentStatement assignmentStatement = new AssignmentStatement();
        assignmentStatement.setStart(identifierToken);
        assignmentStatement.setVariableName(identifierToken.getStringValue());
        require(EQUAL, assignmentStatement);
        assignmentStatement.setExpression(parseExpression());
        return assignmentStatement;
    }

    private Statement parseReturnStatement(){
        return null;
    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            additiveExpression.setToken(operator);
            expression = additiveExpression;
        }
        return expression;
    }

    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression();
        while (tokens.match(SLASH, STAR)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            FactorExpression additiveExpression = new FactorExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            additiveExpression.setToken(operator);
            expression = additiveExpression;
        }
        return expression;

    }

    private Expression parseEqualityExpression() {
        Expression lhs = parseComparisonExpression();
        if (tokens.match(EQUAL_EQUAL, BANG_EQUAL)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseEqualityExpression();
            rhs.setToken(token);
            return new EqualityExpression(token, lhs, rhs);
        } else {
            return lhs;
        }
    }

    private Expression parseComparisonExpression() {
        Expression lhs = parseAdditiveExpression();
        if (tokens.match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseAdditiveExpression();
            rhs.setToken(token);
            return new ComparisonExpression(token, lhs, rhs);
        } else {
            return lhs;
        }
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if(tokens.match(IDENTIFIER)) {
            Token identifierToken = tokens.consumeToken();
            if(tokens.match(LEFT_PAREN)){
                Token start = tokens.consumeToken();
                List<Expression> argumentList = new ArrayList<>();
                if(tokens.match(RIGHT_PAREN)){
                    Token closing_paren = tokens.consumeToken();
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), argumentList);
                    return functionCallExpression;
                }else{
                    Expression val = parseExpression();
                    argumentList.add(val);
                }
                while(tokens.match(COMMA)){
                    Token comma = tokens.consumeToken();
                    Expression val = parseExpression();
                    argumentList.add(val);
                }
                boolean rightParen = tokens.match(RIGHT_PAREN);
                if(rightParen){
                    Token closing_paren = tokens.consumeToken();
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), argumentList);
                    return functionCallExpression;
                }else{
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), argumentList);
                    functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                    return functionCallExpression;
                }
            }else {
                IdentifierExpression identifierExpression = new IdentifierExpression(identifierToken.getStringValue());
                identifierExpression.setToken(identifierToken);
                return identifierExpression;
            }
        } else if(tokens.match(STRING)){
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringExpression.setToken(stringToken);
            return stringExpression;
        } else if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if(tokens.match(TRUE) || tokens.match(FALSE)){
            Token booleanToken = tokens.consumeToken();
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(booleanToken.getType() == TRUE);
            booleanLiteralExpression.setToken(booleanToken);
            return booleanLiteralExpression;
        } else if(tokens.match(NULL)){
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setToken(nullToken);
            return nullLiteralExpression;
        } else if(tokens.match(LEFT_BRACKET)){
            Token start = tokens.consumeToken();
            List<Expression> exprs = new ArrayList<>();
            if(tokens.match(RIGHT_BRACKET)){
                Token end = tokens.consumeToken();
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(exprs);
                return listLiteralExpression;
            }else{
                Expression val = parseExpression();
                exprs.add(val);
            }
            while(tokens.match(COMMA)){
                Token comma = tokens.consumeToken();
                Expression val = parseExpression();
                exprs.add(val);
            }
            boolean rightBracket = tokens.match(RIGHT_BRACKET);
            if(rightBracket){
                Token end = tokens.consumeToken();
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(exprs);
                return listLiteralExpression;
            }else{
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(exprs);
                listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
                return listLiteralExpression;
            }

        } else if(tokens.match(LEFT_PAREN)){
            Token start = tokens.consumeToken();
            Expression expression = parseExpression();
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(expression);
            require(RIGHT_PAREN, parenthesizedExpression);
            return parenthesizedExpression;
        } else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression();
            syntaxErrorExpression.setToken(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    private TypeLiteral parseTypeLiteral() {
        Token startToken = tokens.getCurrentToken();
        TypeLiteral typeLiteral = new TypeLiteral();
        typeLiteral.setToken(startToken);
        if(startToken.getType() == IDENTIFIER) {
            if(startToken.getStringValue().equals("int")) {
                tokens.consumeToken();
                typeLiteral.setToken(startToken);
                typeLiteral.setType(CatscriptType.INT);
                return typeLiteral;
            }
            if(startToken.getStringValue().equals("string")) {
                tokens.consumeToken();
                typeLiteral.setToken(startToken);
                typeLiteral.setType(CatscriptType.STRING);
                return typeLiteral;
            }
            if(startToken.getStringValue().equals("bool")) {
                tokens.consumeToken();
                typeLiteral.setToken(startToken);
                typeLiteral.setType(CatscriptType.BOOLEAN);
                return typeLiteral;
            }
            if(startToken.getStringValue().equals("object")) {
                tokens.consumeToken();
                typeLiteral.setToken(startToken);
                typeLiteral.setType(CatscriptType.OBJECT);
                return typeLiteral;
            }

            else if (startToken.getStringValue().equals("list")) {
                tokens.consumeToken();
                if (tokens.matchAndConsume(LESS)) {
                    TypeLiteral componentTypeLiteral = parseTypeLiteral();
                    CatscriptType componentType = componentTypeLiteral.getType();
                    CatscriptType listType = CatscriptType.getListType(componentType);
                    typeLiteral.setType(listType);
                    require(GREATER, typeLiteral);
                    return typeLiteral;

                } else { //match greater

                    CatscriptType listType = CatscriptType.getListType(CatscriptType.OBJECT);
                    typeLiteral.setType(listType);
                }
            }

        }
        return typeLiteral;


    }



    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if (tokens.match(type)) {
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
