package co.uk.maksmozolewski.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

import co.uk.maksmozolewski.ast.ArrayAccessExpr;
import co.uk.maksmozolewski.ast.ArrayType;
import co.uk.maksmozolewski.ast.Assign;
import co.uk.maksmozolewski.ast.BaseType;
import co.uk.maksmozolewski.ast.BinOp;
import co.uk.maksmozolewski.ast.Block;
import co.uk.maksmozolewski.ast.ChrLiteral;
import co.uk.maksmozolewski.ast.Expr;
import co.uk.maksmozolewski.ast.ExprStmt;
import co.uk.maksmozolewski.ast.FieldAccessExpr;
import co.uk.maksmozolewski.ast.FunCallExpr;
import co.uk.maksmozolewski.ast.FunDecl;
import co.uk.maksmozolewski.ast.If;
import co.uk.maksmozolewski.ast.IntLiteral;
import co.uk.maksmozolewski.ast.Op;
import co.uk.maksmozolewski.ast.PointerType;
import co.uk.maksmozolewski.ast.Program;
import co.uk.maksmozolewski.ast.Return;
import co.uk.maksmozolewski.ast.SizeOfExpr;
import co.uk.maksmozolewski.ast.Stmt;
import co.uk.maksmozolewski.ast.StrLiteral;
import co.uk.maksmozolewski.ast.StructType;
import co.uk.maksmozolewski.ast.StructTypeDecl;
import co.uk.maksmozolewski.ast.Type;
import co.uk.maksmozolewski.ast.TypecastExpr;
import co.uk.maksmozolewski.ast.ValueAtExpr;
import co.uk.maksmozolewski.ast.VarDecl;
import co.uk.maksmozolewski.ast.VarExpr;
import co.uk.maksmozolewski.ast.While;
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

    public Program parse(){

        //set currToken to the first token
        nextToken();
        return parseProgram();
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
        System.err.println(msg.toString());
    }

    /**
     * populates the buffer with at least i tokens and returns the i'th token in the buffer (i starting at 1).
     * i >= 1
     * @param i
     * @return
     */
    private Token lookAhead(int i){
        assert i >= 1;
        Token ithToken = null;

        // we try to fill the buffer up to the required capacity and at the same time capture
        // the ith element without going through the buffer
        while(buffer.size() <= i){
            Token nextToken = tokeniser.nextToken();
            buffer.add(nextToken);
        }

        assert buffer.size() >= i;

        // if we havent captured the ith token (i.e. our buffer was already filled)
        // then we go through it and search for the ith token
        int cnt = 1;
        for (Token token : buffer) {
            if(cnt++ == i){
                ithToken = token;
                break;
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
                Token output;
                output = currToken;
                nextToken();
                return output;
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
        return expFirstHasCurrToken() ||
               accept(TokenClass.WHILE,
                    TokenClass.IF,
                    TokenClass.RETURN,
                    TokenClass.LBRA);
    }

    private boolean expFirstHasCurrToken(){
        return terminalExpFirstHasCurrToken() || accept(TokenClass.ASTERIX,TokenClass.SIZEOF,TokenClass.LPAR,TokenClass.MINUS);
    }

    private boolean terminalExpFirstHasCurrToken(){
        return accept(TokenClass.LPAR,
            TokenClass.IDENTIFIER,
            TokenClass.INT_LITERAL,
            TokenClass.CHAR_LITERAL,
            TokenClass.STRING_LITERAL);
    }

    // // // PARSING // // // 

    private Program parseProgram(){
        parseIncludes();
        List<StructTypeDecl> structTypeDecls = parseStructDecls();
        List<VarDecl> varDecls = parseVarDecls();
        List<FunDecl> funDecls = parseFunDecls();

        expect(TokenClass.EOF);

        return new Program(structTypeDecls, varDecls, funDecls);
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

    private List<StructTypeDecl> parseStructDecls(){
        List<StructTypeDecl> structTypeDecls = new ArrayList<StructTypeDecl>();
        // (structdecl)*
        if(accept(TokenClass.STRUCT)){

            StructTypeDecl newDecl;
            if((newDecl = parseStructDecl()) != null) structTypeDecls.add(newDecl);
            
            structTypeDecls.addAll(parseStructDecls());
        }

        return structTypeDecls;
    }

    private StructTypeDecl parseStructDecl(){
        // structtype 
        StructType structType;
        if((structType = parseStructType()) == null) return null;

        // "{"
        expect(TokenClass.LBRA);

        // (vardecl)+
        List<VarDecl> varDecls = parseVarDecls();

        // "}"";"
        expect(TokenClass.RBRA);
        expect(TokenClass.SC);

        return new StructTypeDecl(structType,varDecls);
    }

    private StructType parseStructType(){
        // "struct" IDENT
        expect(TokenClass.STRUCT);

        Token ident;
        if((ident = expect(TokenClass.IDENTIFIER)) == null) return null;

        return new StructType(ident.data);
    }



    private List<VarDecl> parseVarDecls(){
        TokenClass TokenClassTwoAhead = lookAhead(2).tokenClass;
        boolean notFuncDecl = TokenClassTwoAhead != TokenClass.LPAR;

        List<VarDecl> varDecls = new ArrayList<VarDecl>();

        if(varDeclFirstHasCurrToken() && notFuncDecl){
            varDecls.add(parseVarDecl());
            varDecls.addAll(parseVarDecls());
        }

        return varDecls;
    }



    private VarDecl parseVarDecl(){

        // type (won't return array types, those come up during var decls)
        Type varTypeToken;
        if((varTypeToken = parseType()) == null) return null;

        // IDENT
        Token varIdentifierToken;
        if((varIdentifierToken = expect(TokenClass.IDENTIFIER)) == null) return null;

        // (";" | normal var decl
        //    "[" INT_LITERAL "]" ";") array decl
        switch(currToken.tokenClass){
            case SC:
                nextToken();
                return new VarDecl(varTypeToken, varIdentifierToken.data);
            case LSBR:
                // for arrays we need a size
                expect(TokenClass.LSBR);

                Token arrayCountToken;
                if((arrayCountToken = expect(TokenClass.INT_LITERAL)) == null) return null;

                expect(TokenClass.RSBR);
                expect(TokenClass.SC);

                Type arrayType = new ArrayType(varTypeToken, Integer.parseInt(arrayCountToken.data));
                return new VarDecl(arrayType, varIdentifierToken.data);
            default:
                error(TokenClass.SC,TokenClass.LSBR);
                return null;
        }
    }

    private List<FunDecl> parseFunDecls(){
        List<FunDecl> funDecls = new LinkedList<FunDecl>();
        if(funcDeclFirstHasCurrToken()){
            funDecls.add(parseFunDecl());
            funDecls.addAll(parseFunDecls());
        }

        return funDecls;
        
    }

    private FunDecl parseFunDecl(){
        Type funType;
        if((funType = parseType()) == null) return null;

        Token funIdent;
        if((funIdent = expect(TokenClass.IDENTIFIER)) == null) return null;

        expect(TokenClass.LPAR);

        List<VarDecl> params = parseParams();

        expect(TokenClass.RPAR);
        
        Block funBlock;
        if((funBlock = parseBlock()) == null) return null;
        
        return new FunDecl(funType, funIdent.data, params, funBlock);
    }

    private List<VarDecl> parseParams(){
        List<VarDecl> params = new LinkedList<VarDecl>();
        if(typeFirstHasCurrToken()){
            Type varType;
            if((varType = parseType()) == null) return null;
        
            Token varIden;
            if((varIden = expect(TokenClass.IDENTIFIER)) == null) return null;

            params.add(new VarDecl(varType, varIden.data));

            params.addAll(parseParamsRec());
        }

        return params;
    }

    private List<VarDecl> parseParamsRec(){
        List<VarDecl> params = new LinkedList<VarDecl>();

        while(accept(TokenClass.COMMA)){
            nextToken();

            Type varType;
            if((varType = parseType()) == null) return null;
        
            Token varIden;
            if((varIden = expect(TokenClass.IDENTIFIER)) == null) return null;

            params.add(new VarDecl(varType, varIden.data));
        }

        return params;
    }

    private Block parseBlock(){
        expect(TokenClass.LBRA);
        List<VarDecl> varDecls =  parseVarDecls();
        List<Stmt> stmts = parseStmnts();
        expect(TokenClass.RBRA);

        return new Block(varDecls, stmts);
    }

    private List<Stmt> parseStmnts(){
        List<Stmt> stmts = new LinkedList<Stmt>();
        if(stmntFirstHasCurrToken()){
            stmts.add(parseStmnt());
            stmts.addAll(parseStmnts());
        }
        return stmts;
    }

    private Stmt parseStmnt(){
        switch(currToken.tokenClass){
            case LBRA:
                return parseBlock();
            case WHILE:
                nextToken();
                expect(TokenClass.LPAR);
                
                Expr whileExp;
                if((whileExp = parseExp()) == null) return null;

                expect(TokenClass.RPAR);

                Stmt whileStmt;
                if((whileStmt = parseStmnt()) == null) return null;

                return new While(whileExp, whileStmt);

            case IF:
                nextToken();
                expect(TokenClass.LPAR);
                
                Expr ifExp;
                if((ifExp = parseExp()) == null) return null;

                expect(TokenClass.RPAR);
                
                Stmt ifStmt;
                if((ifStmt = parseStmnt()) == null) return null;
                
                Stmt elseStmt = null;
                if(accept(TokenClass.ELSE)){
                    nextToken();

                    if((elseStmt = parseStmnt()) == null) return null;
                }

                return new If(ifExp, ifStmt, elseStmt);

            case RETURN:
                nextToken();

                Expr returnExp = null;
                if(expFirstHasCurrToken()){
                    returnExp = parseExp();
                }

                expect(TokenClass.SC);

                return new Return(returnExp);

            default:
                
                Expr lhs;
                if((lhs = parseExp()) == null) return null;

                switch(currToken.tokenClass){
                    case ASSIGN:
                        nextToken();
                        
                        Expr rhs;
                        if((rhs = parseExp()) == null) return null;

                        expect(TokenClass.SC);
                        
                        return new Assign(lhs, rhs);

                    case SC:
                        // expression statement
                        nextToken();
                        return new ExprStmt(lhs);
                    default:
                        error(TokenClass.ASSIGN,TokenClass.SC);
                        return null;
                }
        }
    }

    // EXP
    private Op opTokenToASTOp(TokenClass tc){
        switch(tc){
            case PLUS:
                return Op.ADD;
            case MINUS:
                return Op.SUB;
            case ASTERIX:
                return Op.MUL;
            case DIV:
                return Op.DIV;
            case REM:
                return Op.MOD;
            case GT:
                return Op.GT;
            case LT:
                return Op.LT;
            case GE:    
                return Op.GE;
            case LE:
                return Op.LE;
            case NE:
                return Op.NE;
            case EQ:
                return Op.EQ;
            case OR:
                return Op.OR;
            case AND:
                return Op.AND;
            default:
                // shouldnt happen;
                assert false;
                return null;
        }
    }

    /**
     * helper function for binary ops
     * @param prefixExpr
     * @param postfixExpr
     * @param opTokens
     * @return
     */
    private Expr binaryOpRecFunc(final Expr lhs,Supplier<Expr> prefixExpr, Function<Expr,Expr> postfixExpr,TokenClass...opTokens){
        // binaryOpRecFunc = ["Op" prefixExpr binaryOpRecFunc]
        if(accept(opTokens)){
            TokenClass firstOpTokenClass= currToken.tokenClass;
            nextToken();
            
            Expr rhs;
            if((rhs = prefixExpr.get()) == null) return null;

            // we check if there is more tail operators
            if(accept(opTokens)){
                Expr secondExpr;
                // if so we pass new expr as lhs deeper
                if((secondExpr = postfixExpr.apply(new BinOp(lhs,opTokenToASTOp(firstOpTokenClass),rhs))) == null) return null;
                else
                    return secondExpr;
    
            } else {
                // if no tail just end the recursion
                return new BinOp(lhs,opTokenToASTOp(firstOpTokenClass),rhs);
            }

        
            
        }
        
        // no operator found
        return null;
    }

    private Expr binaryOpFunc(Supplier<Expr> prefixExpr, Function<Expr,Expr> postfixExpr,TokenClass...opTokens){
        // binaryop = prefixExpr binaryOpRecFunc
        Expr lhs;
        if(( lhs = prefixExpr.get()) == null) return null;
        
        TokenClass opToken = currToken.tokenClass;

        // if we dont find a bin op sign, we just return lhs, this is ok
        if(!accept(opTokens)) return lhs;
        nextToken();

        Expr rhs;
        // we then parse the prefix to the next op as the rhs to this bin op, for left associativity of all bin operators
        if((rhs = prefixExpr.get()) == null){
            // shouldn't happen, we should always have a rhs to an op
            return null;
        } else {
            // a binary op
            Expr tail;
            // check we have a tail of operators ( another OP ) if so we pass this new exp as the lhs itself
            if(accept(opTokens)){
                if((tail = postfixExpr.apply(new BinOp(lhs,opTokenToASTOp(opToken),rhs))) == null) return null;
                else
                    // we create a bin op and pass it on as the lhs to the recursion
                    return tail;
            } else {
                // otherwise just return self
                return new BinOp(lhs,opTokenToASTOp(opToken),rhs);
            }
           
        }
    }

    private Expr parseExp(){
        return parseOr();
    }

    private Expr parseOr(){
        return binaryOpFunc(()->parseAnd(),(l)-> parseOrRec(l), TokenClass.OR);
    }
    private Expr parseOrRec(final Expr lhs){
        return binaryOpRecFunc(lhs,()->parseAnd(), (l)->parseOrRec(l), TokenClass.OR);
    }

    private Expr parseAnd(){
        return binaryOpFunc(()->parseRelEq(), (l)->parseAndRec(l), TokenClass.AND);
    }
    private Expr parseAndRec(Expr lhs){
        return binaryOpRecFunc(lhs,()->parseRelEq(), (l)->parseAndRec(l), TokenClass.AND);
    }
    
    
    private Expr parseRelEq(){
        return binaryOpFunc(()->parseRelComp(), (l)->parseRelEqRec(l), TokenClass.EQ,TokenClass.NE);
    }
    private Expr parseRelEqRec(Expr lhs){
        return binaryOpRecFunc(lhs,()->parseRelComp(), (l)->parseRelEqRec(l), TokenClass.EQ,TokenClass.NE);
    }


    private Expr parseRelComp(){
        return binaryOpFunc(()->parseSummative(), (l)->parseRelCompRec(l), TokenClass.LT,TokenClass.LE,TokenClass.GT,TokenClass.GE);
    }
    private Expr parseRelCompRec(Expr lhs){
        return binaryOpRecFunc(lhs,()->parseSummative(), (l)->parseRelCompRec(l), TokenClass.LT,TokenClass.LE,TokenClass.GT,TokenClass.GE);
    }


    private Expr parseSummative(){
        return binaryOpFunc(()->parseMulti(), (l)->parseSummativeRec(l), TokenClass.PLUS,TokenClass.MINUS);
    }
    private Expr parseSummativeRec(Expr lhs){
        return binaryOpRecFunc(lhs,()->parseMulti(), (l)->parseSummativeRec(l), TokenClass.PLUS,TokenClass.MINUS);
    }


    private Expr parseMulti(){
        return binaryOpFunc(()->parseUnarySecondary(), (l)->parseMultiRec(l), TokenClass.ASTERIX,TokenClass.DIV,TokenClass.REM);
    }
    private Expr parseMultiRec(Expr lhs){
        return binaryOpRecFunc(lhs,()->parseUnarySecondary(), (l)->parseMultiRec(l), TokenClass.ASTERIX,TokenClass.DIV,TokenClass.REM);
    }

    private Expr parseUnarySecondary(){

        switch(currToken.tokenClass){
            case SIZEOF:
                return parseSizeOf();
            case ASTERIX:
                return parseValueAt();
            case MINUS:
                return parseNegation();
            default:
                // either ( type ) or unaryPrimary
                TokenClass tokenAhead = lookAhead(1).tokenClass;
                if(tokenAhead == TokenClass.INT || 
                    tokenAhead == TokenClass.CHAR || 
                    tokenAhead == TokenClass.STRUCT || 
                    tokenAhead == TokenClass.VOID){
                        return parseTypecast();
                } else {
                    return parseUnaryPrimary();
                }
        }
    }

    private Expr parseUnaryPrimary(){
        // slightly different than in the actual grammar, but effect is the same
        // let each parse handle the LHS terminal expr just to avoid passing params \~(o.o)~/


        // TERMINALEXPR or FUNCALL
        Token nxtToken = lookAhead(1);

        // it's an identifier it could be just IDENT from EXPR or FUNCALL
        if(currToken.tokenClass == TokenClass.IDENTIFIER && 
            nxtToken.tokenClass == TokenClass.LPAR){
            // funcall
            return parseFunCall();
        }
        else if(nxtToken.tokenClass == TokenClass.DOT){
            // fieldaccess
            return parseFieldAccess();
        }else if (nxtToken.tokenClass == TokenClass.LSBR){
            //array access
            return parseArrayAccess();
        } else {
            // just a terminal expr or function call
            return parseTerminalExp();
        }

        

    }


    private Expr parseTerminalExp(){
        switch(currToken.tokenClass){
            case IDENTIFIER:
                // IDENT
                Token identifier = currToken;
                nextToken();
                return new VarExpr(identifier.data);
            case LPAR:
                // "(" expr ")"
                nextToken();
                
                Expr insideExpr;
                if((insideExpr = parseExp()) == null) return null;

                expect(TokenClass.RPAR);
                
                return insideExpr;

            case INT_LITERAL:
                Token intLiteral = currToken;
                nextToken();
                return new IntLiteral(Integer.parseInt(intLiteral.data));

            case CHAR_LITERAL:
                Token charLiteral = currToken;

                assert charLiteral.data.length() == 1;
                
                nextToken();
                return new ChrLiteral((char)charLiteral.data.charAt(0));

            case STRING_LITERAL:
                Token strLiteral = currToken;
                nextToken();

                return new StrLiteral(strLiteral.data);

            default:
                // something's not right in the code if we reach here
                return null;

        }
    }

    private Expr parseArrayAccess(){
        // terminalExpr "[" exp "]"
        Expr lhs;
        if((lhs = parseTerminalExp()) == null) return null;
        expect(TokenClass.LSBR);
        
        Expr idx;
        if((idx = parseExp())==null) return null;
        expect(TokenClass.RSBR);

        return new ArrayAccessExpr(lhs, idx);
    }

    private Expr parseFieldAccess(){
        // terminalExpr "." IDENT

        Expr lhs;
        if((lhs = parseTerminalExp()) == null) return null;
        
        expect(TokenClass.DOT);

        Token ident;
        if((ident = expect(TokenClass.IDENTIFIER)) == null) return null;


        return new FieldAccessExpr(lhs, ident.data);
    }

    private Expr parseFunCall(){
        // IDENT "(" [exp funcallargs] ")"
        Token ident;
        if((ident = expect(TokenClass.IDENTIFIER)) == null) return null;

        expect(TokenClass.LPAR);

        List<Expr> args = new LinkedList<Expr>();
        if(expFirstHasCurrToken()){
            args.add(parseExp());
            args.addAll(parseFunCallArgs());
        }

        expect(TokenClass.RPAR);

        return new FunCallExpr(ident.data,args);
    }

    private List<Expr> parseFunCallArgs(){
        List<Expr> args = new LinkedList<Expr>();
        while(accept(TokenClass.COMMA)){
            args.add(parseExp());
        }

        return args;
    }

    private Expr parseSizeOf(){
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);

        Type type;
        if((type = parseType()) == null) return null;
        expect(TokenClass.RPAR);

        return new SizeOfExpr(type);
    }

    private Expr parseValueAt(){
        expect(TokenClass.ASTERIX);

        Expr ptr;
        if((ptr = parseExp()) == null) return null;
        
        return new ValueAtExpr(ptr);
    }

    private Expr parseTypecast(){
        expect(TokenClass.LPAR);
        
        Type type;
        if((type = parseType()) == null) return null;

        expect(TokenClass.RPAR);
        
        Expr castedExpr;
        if((castedExpr = parseExp()) == null) return null;

        return new TypecastExpr(type, castedExpr);
    }

    private Expr parseNegation(){
        expect(TokenClass.MINUS);

        Expr negatedExpr;
        if((negatedExpr = parseExp()) == null) return null;

        // negation is just 0 - expr, that's how it'll end up in assembly anyway
        return new BinOp(new IntLiteral(0), Op.SUB, negatedExpr);
    }
    
    private Type parseType(){
        // type IDENT
        Type returnType = null;
        // decide base type

        switch(currToken.tokenClass){
            case INT:
                returnType = BaseType.INT;
                nextToken();
                break;
            case CHAR:
                returnType = BaseType.CHAR;
                nextToken();
                break;
            case VOID:
                returnType = BaseType.VOID;
                nextToken();
                break;
            case STRUCT:
                
                // cannot create struct type without identifier
                StructType structType;
                if((structType = parseStructType()) == null) return null;
                returnType = structType;
                break;
            default:
                error(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
                return null;
        }
        
        // ['*']
        if(accept(TokenClass.ASTERIX)){
            returnType = new PointerType(returnType);
            nextToken();
        }

        // if we reached here we have a valid Type
        return returnType;

    }
}