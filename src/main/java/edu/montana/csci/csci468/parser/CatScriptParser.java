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

    private Statement parseFunctionDeclaration() {


        if (tokens.matchAndConsume(FUNCTION)) {
            FunctionDefinitionStatement functionDefintionStatement = new FunctionDefinitionStatement();
            Token name = require(IDENTIFIER, functionDefintionStatement);
            require(LEFT_PAREN, functionDefintionStatement);
            if (!tokens.matchAndConsume(RIGHT_PAREN)) {
                do {

                    // match and identifier
                    if (tokens.matchAndConsume(COLON)) {
                        // handle the type expression
                        TypeLiteral typeLiteral = parseTypeLiteral();

                        functionDefintionStatement.setType(typeLiteral.getType());

                        //parseTypeLiteral(explicitType);  want to do this
                    }

                   // functionDefintionStatement.addParameter(identifier.getStringValue(), CatscriptType.OBJECT);
                } while (tokens.matchAndConsume(COMMA));
                require(RIGHT_PAREN, functionDefintionStatement);
            }
            if (tokens.matchAndConsume(COLON)) {
                //call parseTypeLiteral
                //  functionDefintionStatement.setType(resultofParseTypeLiteral);
            } else {
                functionDefintionStatement.setType(null);
            }

            functionDefintionStatement.setName(name.getStringValue());
            return functionDefintionStatement;
            //set body/type/name
        }
        return null;

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

        if(tokens.match(FUNCTION)) {
            return parseFunctionDeclaration();
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
        Token token;
        if (tokens.match(INTEGER)) {
            token = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(token.getStringValue());
            integerExpression.setToken(token);
            return integerExpression;
        } else if (tokens.match(IDENTIFIER)) {
            token = tokens.consumeToken();
            IdentifierExpression identifierExpression = new IdentifierExpression(token.getStringValue());
            identifierExpression.setToken(token);
            return identifierExpression;
        } else if (tokens.match(STRING)) {
            token = tokens.consumeToken();
            StringLiteralExpression stringExpression = new StringLiteralExpression(token.getStringValue());
            stringExpression.setToken(token);
            return stringExpression;
        } else if (tokens.match(TRUE)) {
            token = tokens.consumeToken();
            BooleanLiteralExpression booleanExpression = new BooleanLiteralExpression(true);
            booleanExpression.setToken(token);
            return booleanExpression;
        } else if (tokens.match(FALSE)) {
            token = tokens.consumeToken();
            BooleanLiteralExpression booleanExpression = new BooleanLiteralExpression(false);
            booleanExpression.setToken(token);
            return booleanExpression;
        } else if (tokens.match(NULL)) {
            token = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setToken(token);
            return nullLiteralExpression;
        } else if (tokens.match(FUNCTION)) {
            token = tokens.consumeToken();
            if (tokens.matchAndConsume(LEFT_PAREN)) {
                List<Expression> argumentList = new ArrayList<>(0);
                boolean terminated = true;
                if (!tokens.matchAndConsume(RIGHT_PAREN)) {
                    argumentList.add(parseExpression());
                    while (tokens.matchAndConsume(COMMA)) {
                        argumentList.add(parseExpression());
                    }
                    terminated = false;
                }
                FunctionCallExpression functionCallExpression = new FunctionCallExpression(token.getStringValue(), argumentList);
                functionCallExpression.setToken(token);
                if (!terminated) {
                    require(RIGHT_PAREN, functionCallExpression, ErrorType.UNTERMINATED_ARG_LIST);
                }
                return functionCallExpression;
            }
        } else if (tokens.matchAndConsume(LEFT_BRACKET)) {
            List<Expression> listArguments = new ArrayList<>(0);
            boolean terminated = true;
            if (!tokens.matchAndConsume(RIGHT_BRACKET)) {
                listArguments.add(parseExpression());
                while (tokens.matchAndConsume(COMMA)) {
                    listArguments.add(parseExpression());
                }
                terminated = false;
            }
            ListLiteralExpression functionCallExpression = new ListLiteralExpression(listArguments);
            if (!terminated) {
                require(RIGHT_BRACKET, functionCallExpression, ErrorType.UNTERMINATED_LIST);
            }
            return functionCallExpression;
        } else if (tokens.matchAndConsume(LEFT_PAREN)) {
            Expression expression = parseExpression();
            return new ParenthesizedExpression(expression);
        }
        return new SyntaxErrorExpression(tokens.consumeToken());
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
