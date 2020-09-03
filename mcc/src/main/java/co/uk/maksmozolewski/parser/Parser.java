package co.uk.maksmozolewski.parser;

import java.util.LinkedList;
import java.util.Queue;

import co.uk.maksmozolewski.lexer.Token;
import co.uk.maksmozolewski.lexer.Tokeniser;
import co.uk.maksmozolewski.lexer.Token.TokenClass;

public class Parser {
    
    Tokeniser tokeniser;

    /** the tokens we've read but not processed so far */
    Queue<Token> buffer = new LinkedList<Token>();

    Token currToken;
    
    int errorCount;

    Token lastErrorCausingToken;

    public Parser(Tokeniser tokeniser){
        this.tokeniser = tokeniser;
    }

    public void parse(){

        //set currToken to the first token
        nextToken();
        parseProgram();
    }

    public int getErrorCount(){
        return errorCount;
    }

    private void nextToken(){
        if(buffer.isEmpty()){
            currToken = tokeniser.nextToken();
        } else {
            currToken = buffer.poll();
        }

    }

    private void error(TokenClass... expected){
        
        // only write first error msg for each offending token, otherwise there will be an avalanche of errors
        // as the program tries to match multiple different rules
        if(lastErrorCausingToken == currToken){
            return;
        } else {
            lastErrorCausingToken = currToken;
        }

        StringBuilder msg = new StringBuilder();

        msg.append("Parsing error: Expected one of: [");

        String sep = "";
        for (TokenClass tokenClass : expected) {
            msg.append(sep);
            msg.append(tokenClass.toString());
            sep = ",";
        }
        msg.append("]. But encountered: ");
        msg.append(currToken.toString());
        msg.append(". At ");
        msg.append(currToken.position);
        msg.append('.');
        
        errorCount++;
        System.out.println(msg.toString());
    }

    /**
     * populates the buffer with at least i tokens and returns the i'th token in the buffer (i starting at 1).
     * i >= 1
     * @param i
     * @return
     */
    private Token lookAhead(int i){
        assert i >= 1;
        int cnt = 1;
        Token ithToken = null;

        // we try to fill the buffer up to the required capacity and at the same time capture
        // the ith element without going through the buffer
        while(cnt <= i && buffer.size() <= i){
            Token nextToken = tokeniser.nextToken();
            buffer.add(nextToken);
            if(cnt == i){
                ithToken = nextToken;
            }
            cnt++;
        }

        assert buffer.size() >= i;

        // if we havent captured the ith token (i.e. our buffer was already filled)
        // then we go through it and search for the ith token
        if(ithToken == null){
            cnt = 1;
            for (Token token : buffer) {
                if(cnt++ == i){
                    ithToken = token;
                    break;
                }
            }
        }

        assert ithToken != null;
        return ithToken;
    }


    /**
     * returns a token corresponding to any of the given token classes if the next token is one of them or null otherwise
     * @param expected
     * @return Token of one of the expected classes or null if next token is none of them.
     */
    private Token expect(TokenClass... expected){
        for (TokenClass expectedClass : expected) {
            if(currToken.tokenClass == expectedClass){
                nextToken();
                return currToken;
            }
        }

        error(expected);
        return null;
    }

    /**
     * returns true if the next token is any of the expected ones
     * @param expected
     */
    private boolean accept(TokenClass... expected){
        boolean result = false;
        int idx = 0;
        while(result == false && expected.length - 1 >= idx ){
            result = currToken.tokenClass == expected[idx++];
        }
        return result;
    }
    

    // // // // MAIN PARSER FUNCTIONALITY // // // //

    // // // FIRST // // //
    // functions which check the current token is in the set of tokens at the root of a parse tree for 
    // the corresponding non terminal

    private boolean varDeclFirstHasCurrToken(){
        return typeFirstHasCurrToken();
    }

    private boolean funcDeclFirstHasCurrToken(){
        return typeFirstHasCurrToken();
    }

    private boolean typeFirstHasCurrToken(){
        return accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
    }

    private boolean stmntFirstHasCurrToken(){
        return terminalExpFirstHasCurrToken() ||
               accept(TokenClass.WHILE,
                    TokenClass.IF,
                    TokenClass.RETURN,
                    TokenClass.LBRA);
    }

    private boolean expFirstHasCurrToken(){
        return terminalExpFirstHasCurrToken();
    }

    private boolean terminalExpFirstHasCurrToken(){
        return accept(TokenClass.LPAR,
            TokenClass.IDENTIFIER,
            TokenClass.INT_LITERAL,
            TokenClass.CHAR_LITERAL,
            TokenClass.STRING_LITERAL);
    }

    // // // PARSING // // // 

    private void parseProgram(){
        parseIncludes();
        parseStructDecls();
        parseVarDecls();
        parseFunDecls();
        expect(TokenClass.EOF);
    }

    private void parseIncludes(){
        //(include)*
        if(accept(TokenClass.INCLUDE)){
            parseInclude();
            parseIncludes();
        }
    }

    private void parseInclude(){
        expect(TokenClass.INCLUDE);
        expect(TokenClass.STRING_LITERAL);
    }

    private void parseStructDecls(){
        // (structdecl)*
        if(accept(TokenClass.STRUCT)){
            parseStructDecl();
            parseStructDecls();
        }
    }

    private void parseStructDecl(){
        // structtype "{"
        parseStructType();
        expect(TokenClass.LBRA);

        // (vardecl)+
        parseVarDecl();
        // either funcall 
        while(varDeclFirstHasCurrToken()){
            parseVarDecl();
        }

        // "}"";"
        expect(TokenClass.RBRA);
        expect(TokenClass.SC);
    }

    private void parseStructType(){
        // "struct" IDENT
        expect(TokenClass.STRUCT);
        expect(TokenClass.IDENTIFIER);
    }



    private void parseVarDecls(){
        TokenClass TokenClassTwoAhead = lookAhead(2).tokenClass;
        boolean notFuncDecl = TokenClassTwoAhead != TokenClass.LPAR;
        if(varDeclFirstHasCurrToken() && notFuncDecl){
            parseVarDecl();
            parseVarDecls();
        }
    }



    private void parseVarDecl(){
        // type IDENT
        parseType();
        expect(TokenClass.IDENTIFIER);

        // (";" | 
        //    "[" INT_LITERAL "]" ";")
        switch(currToken.tokenClass){
            case SC:
                nextToken();
                break;
            case LSBR:
                expect(TokenClass.LSBR);
                expect(TokenClass.INT_LITERAL);
                expect(TokenClass.RSBR);
                expect(TokenClass.SC);
                break;
            default:
                error(TokenClass.SC,TokenClass.LSBR);
                break;
        }
    }

    private void parseFunDecls(){
        if(funcDeclFirstHasCurrToken()){
            parseFunDecl();
            parseFunDecls();
        }
        
    }

    private void parseFunDecl(){
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseParams();
        expect(TokenClass.RPAR);
        parseBlock();
    }

    private void parseParams(){
        if(typeFirstHasCurrToken()){
            parseType();
            expect(TokenClass.IDENTIFIER);
            parseParamsRec();
        }
    }

    private void parseParamsRec(){
        while(accept(TokenClass.COMMA)){
            nextToken();
            parseType();
            expect(TokenClass.IDENTIFIER);
        }
    }

    private void parseBlock(){
        expect(TokenClass.LBRA);
        parseVarDecls();
        parseStmnts();
        expect(TokenClass.RBRA);
    }

    private void parseStmnts(){
        if(stmntFirstHasCurrToken()){
            parseStmnt();
            parseStmnts();
        }
    }

    private void parseStmnt(){
        switch(currToken.tokenClass){
            case LBRA:
                parseBlock();
                break;

            case WHILE:
                nextToken();
                expect(TokenClass.LPAR);
                parseExp();
                expect(TokenClass.RPAR);
                parseStmnt();
                break;

            case IF:
                nextToken();
                expect(TokenClass.LPAR);
                parseExp();
                expect(TokenClass.RPAR);
                parseStmnt();
                if(accept(TokenClass.ELSE)){
                    nextToken();
                    parseStmnt();
                }
                break;

            case RETURN:
                nextToken();
                if(expFirstHasCurrToken()){
                    parseExp();
                }
                expect(TokenClass.SC);
                break;

            default:
                parseExp();
                switch(currToken.tokenClass){
                    case ASSIGN:
                        nextToken();
                        parseExp();
                        expect(TokenClass.SC);
                        break;
                    case SC:
                        nextToken();
                        break;
                    default:
                        error(TokenClass.ASSIGN,TokenClass.SC);
                        break;
                }
        }
    }

    // EXP

    private void parseExp(){
        parseOr();
    }

    private void parseOr(){
        parseAnd();
        parseOrRec();
    }

    private void parseOrRec(){
        if(accept(TokenClass.OR)){
            nextToken();
            parseAnd();
            parseOrRec();
        }
    }

    private void parseAnd(){
        parseRelEq();
        parseAndRec();
    }
    private void parseAndRec(){
        if(accept(TokenClass.AND)){
            nextToken();
            parseRelEq();
            parseAndRec();
        }
    }

    private void parseRelEq(){
        parseRelComp();
        parseRelEqRec();
    }
    private void parseRelEqRec(){
        if(accept(TokenClass.EQ,TokenClass.NE)){
            nextToken();
            parseRelComp();
            parseRelEqRec();
        }
    }

    private void parseRelComp(){
        parseSummative();
        parseRelCompRec();
    }
    private void parseRelCompRec(){
        if(accept(TokenClass.LE,TokenClass.LE,TokenClass.GT,TokenClass.GE)){
            nextToken();
            parseSummative();
            parseRelCompRec();
        }
    }

    private void parseSummative(){
        parseMulti();
        parseSummativeRec();
    }
    private void parseSummativeRec(){
        if(accept(TokenClass.PLUS, TokenClass.MINUS)){
            nextToken();
            parseMulti();
            parseSummativeRec();
        }
    }

    private void parseMulti(){
        parseUnarySecondary();
        parseMultiRec();
    }
    private void parseMultiRec(){
        if(accept(TokenClass.ASTERIX,TokenClass.DIV,TokenClass.REM)){
            nextToken();
            parseUnarySecondary();
            parseMultiRec();
        }
    }

    private void parseUnarySecondary(){

        switch(currToken.tokenClass){
            case SIZEOF:
                parseSizeOf();
                break;
            case ASTERIX:
                parseValueAt();
                break;
            case MINUS:
                parseNegation();
                break;
            default:
                // either ( type ) or unaryPrimary
                TokenClass tokenAhead = lookAhead(1).tokenClass;
                if(tokenAhead == TokenClass.INT || 
                    tokenAhead == TokenClass.CHAR || 
                    tokenAhead == TokenClass.STRUCT || 
                    tokenAhead == TokenClass.VOID){
                        parseTypecast();
                } else {
                    parseUnaryPrimary();
                }
                break;
        }
    }

    private void parseUnaryPrimary(){
        parseTerminalExp();
        parseUnaryPrimaryRec();
    }
    private void parseUnaryPrimaryRec(){
        if(accept(TokenClass.IDENTIFIER,TokenClass.LSBR,TokenClass.DOT,TokenClass.MINUS)){
            switch (currToken.tokenClass){
                case IDENTIFIER:
                    parseFunCall();
                    break;
                case LSBR:
                    parseArrayAccess();
                    break;
                case DOT:
                    parseFieldAccess();
                    break;
                case MINUS:
                    parseNegation();
                    break;
                default:
                    // nothing
                    break;
            }
        }
    }

    private void parseTerminalExp(){
        switch(currToken.tokenClass){
            case LPAR:
                nextToken();
                parseExp();
                expect(TokenClass.RPAR);
                break;

            case IDENTIFIER:

                TokenClass nextTokenClass = lookAhead(1).tokenClass;
                if(nextTokenClass == TokenClass.LPAR){
                    // funcall
                    parseFunCall();
                } else {
                    // identifier
                    nextToken();
                }
                break;
            case INT_LITERAL:
            case CHAR_LITERAL:
            case STRING_LITERAL:
                nextToken();
                break;

        }
    }

    private void parseArrayAccess(){
        expect(TokenClass.LSBR);
        parseExp();
        expect(TokenClass.RSBR);
    }
    private void parseFieldAccess(){
        expect(TokenClass.DOT);
        expect(TokenClass.IDENTIFIER);
    }

    private void parseFunCall(){
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        if(expFirstHasCurrToken()){
            parseExp();
            parseFunCallArgs();
        }
        expect(TokenClass.RPAR);
    }
    private void parseFunCallArgs(){
        while(accept(TokenClass.COMMA)){
            parseExp();
        }
    }
    private void parseSizeOf(){
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LBRA);
        parseType();
        expect(TokenClass.RBRA);
        parseExp();
    }

    private void parseValueAt(){
        expect(TokenClass.ASTERIX);
        parseExp();
    }

    private void parseTypecast(){
        expect(TokenClass.LPAR);
        parseType();
        expect(TokenClass.RPAR);
        parseExp();
    }

    private void parseNegation(){
        expect(TokenClass.MINUS);
        parseExp();
    }
    
    private void parseType(){
        // type IDENT
        expect(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
        // ['*']
        if(accept(TokenClass.ASTERIX)){
            nextToken();
        }

    }
    

}