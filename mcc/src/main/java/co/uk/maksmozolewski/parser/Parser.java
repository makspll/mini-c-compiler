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

    public void parse(){

    }

    public int getErrorCount(){
        return errorCount;
    }

    private Token nextToken(){
        if(buffer.isEmpty()){
            return tokeniser.nextToken();
        } else {
            return buffer.poll();
        }
    }

    private void error(TokenClass... expected){
        
        StringBuilder msg = new StringBuilder();

        msg.append("Parsing error: Expected one of:[");
        for (TokenClass tokenClass : expected) {
            msg.append(tokenClass.toString());
            msg.append(',');
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
        while(cnt <= i && buffer.size() < i){
            Token nextToken = tokeniser.nextToken();
            buffer.add(nextToken);
            cnt++;
            if(cnt == i){
                ithToken = nextToken;
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
        while(result == false || expected.length - 1 < idx ){
            result = currToken.tokenClass == expected[idx];
        }
        return result;
    }
    
}