package co.uk.maksmozolewski.lexer;

import co.uk.maksmozolewski.lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import static java.util.Map.entry;

/**
 * @author cdubach
 */
public class Tokeniser {


    public class UnrecognizedCharacterException extends Exception {
 
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public UnrecognizedCharacterException(final String message) {
            super(message);
        }
        public UnrecognizedCharacterException(final String expected,final char encountered){
            super("Expected: " + expected + ", but encountered: " + encountered);
        }
    }
    public int getErrorCount() {
        return this.error;
    }

    public Tokeniser(final Scanner scanner) {
        this.scanner = scanner;
    }


    public Token nextToken() {
        
        clearTokenString();

        Token result;
        try {
             result = next();
        } catch (final EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF,null, scanner.getLine(), scanner.getColumn());
        } catch (final IOException ioe) { 
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        //always pop in between tokens to avoid stupid errors
        popCurrentTokenString();
        return result;
    }

    private final Scanner scanner;

    private int error = 0;

    private char currChar;

    private boolean reachedEOF = false;
    
    /**
     * mechanism for working with multiple possible matches
     */
    private class Match{
        private String match;
        private Function<String,Boolean> funcMatch;
        private final TokenClass tokenClass;

        private int currPosition = 0;
        private boolean isMatching = true;

        public Match(final String match, final TokenClass matchTokenClass){
            assert(match.length() > 0);
            this.match = match;
            this.tokenClass = matchTokenClass;
        }

        public Match(final Function<String,Boolean> match, final TokenClass matchTokenClass){
            assert(match != null);
            this.funcMatch = match;
            this.tokenClass = matchTokenClass;
        }
        private boolean checkMatch(final char c){
            if(funcMatch == null){
                if(currPosition > match.length() - 1){
                    return false;
                } else {
                    return match.charAt(currPosition) == c;
                }
            } else {
                return funcMatch.apply(tokenStringSoFar.toString() + c);
            }
        }

   
        public boolean isMatching(){
            return isMatching;
        }


        public void progressChar(final char c){
            // a successfull match is failed after progressing over its maximum length

            if(!checkMatch(c)){
                isMatching = false;
            } 
            currPosition++;
            
        } 

        public Token buildToken(final String data,final int line,final int col){
            return new Token(tokenClass,data,line,col);
        }

    }

    /** list of reserved keywords which cannot be used as identifiers */
    private final HashSet<String> keywords = new HashSet<String>(
        Arrays.asList(
            "int",
            "void",
            "char",
            "if",
            "else",
            "while",
            "return",
            "struct",
            "sizeof"
        )
    );

    private final HashSet<Character> escapeSequenceEndings = new HashSet<Character>(
        Arrays.asList('r','f','\'','"','t','b','n','\\','0')
    );

    private final Map<Character,Character> escapeEndingToEscapeCharacterMap = Map.ofEntries(
        entry('r', '\r'),
        entry('f','\f'),
        entry('\'','\''),
        entry('"','\"'),
        entry('t','\t'),
        entry('b','\b'),
        entry('n','\n'),
        entry('\\','\\'),
        entry('0','\0')
    );

    private final HashSet<Character> delimeters = new HashSet<Character>(
        Arrays.asList('{','}','(',')','[',']',';',',')
    );

    private final Map<Character,TokenClass> delimeterToTokenClassMap = Map.ofEntries(
        entry('{',TokenClass.LBRA),
        entry('}',TokenClass.RBRA),
        entry('(',TokenClass.LPAR),
        entry(')',TokenClass.RPAR),
        entry('[',TokenClass.LSBR),
        entry(']',TokenClass.RSBR),
        entry(';',TokenClass.SC),
        entry(',',TokenClass.COMMA)
    );

    private final HashSet<Character> operatorStartChars = new HashSet<Character>(
        Arrays.asList('+','-','*','/','%','=','!','.','<','>','&','|')
    );

    
    private boolean isKeyword(String s){
        return keywords.contains(s);
    }

    /**
     * 
     * @param string
     * @return true if the given string is a valid Identifier string
     */
    private boolean isValidIdentifier(final String string){
        assert(string.length() != 0);

        if(!isIdentifierStartChar(string.charAt(0))) return false;

        if(isKeyword(string)) return false;

        for(int i =1 ; i <string.length();i++){
            final char c = string.charAt(i);
            if(!isIdentifierInsideChar(c)) return false;
        }
        return true;
    }

    private boolean isDelimeter(final char c){
        return delimeters.contains(c);
    }
    private TokenClass getDelimeterTokenClass(final char c){
        assert(delimeterToTokenClassMap.containsKey(c));
        return delimeterToTokenClassMap.get(c);
    }

    private boolean isOperatorStartChar(final char c){
        return operatorStartChars.contains(c);
    }

    private boolean isIdentifierStartChar(final char c){
        final int val = (int)c;
        return ((val >= 65 && val <= 90) || (val >= 97 && val <= 122)) 
            || c == '_';
    }

    private boolean isIdentifierInsideChar(final char c){
        return isIdentifierStartChar(c) || Character.isDigit(c);
    }

    private boolean isEscapeSequenceEnding(final char c){
        return escapeSequenceEndings.contains((Character)c);
    }

    private char getEscapeCharacterForEnding(final char c){
        return escapeEndingToEscapeCharacterMap.get(c);
    }

    /** holds the characters belonging to the current token */
    private final StringBuilder tokenStringSoFar = new StringBuilder();



    private void error(final char c, final int line, final int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
        error++;
    }

    private void error(final UnrecognizedCharacterException e, final int line, final int col){
        System.out.println("Lexing error: " + e.getMessage() + " at " + line + ":" + col);
        error++;
    }

    private void error(final String m, final int line, final int col){
        System.out.println(m + " at "+":" + col);
        error++;
    }

    private Token next() throws IOException {

        final int line = scanner.getLine();
        final int column = scanner.getColumn();

        // get the next character
        currChar = scanner.next();
        
        // skip white spaces
        if (Character.isWhitespace(currChar))
            return next();

        try {
            
            // tokens identifiable by first character
            switch(currChar){
                case '#':
                    expectFullString("#include");
                    return new Token(TokenClass.INCLUDE,popCurrentTokenString(),line,column);
                case '"':
                    expectStringLiteral();
                    return new Token(TokenClass.STRING_LITERAL,popCurrentTokenString(),line,column);
                case '\'':
                    expectCharLiteral();
                    return new Token(TokenClass.CHAR_LITERAL,popCurrentTokenString(),line,column);
            }

            // operators
            if(isOperatorStartChar(currChar)){
                switch(currChar){
                    case '&':
                        expectFullString("&&");
                        return new Token(TokenClass.AND,popCurrentTokenString(),line,column);
                    case '|':
                        expectFullString("||");
                        return new Token(TokenClass.OR,popCurrentTokenString(),line,column);
                    case '+':
                        expectSingleChar('+');
                        return new Token(TokenClass.PLUS,popCurrentTokenString(),line,column);
                    case '-':
                        expectSingleChar('-');
                        return new Token(TokenClass.MINUS,popCurrentTokenString(),line,column);
                    case '*':
                        expectSingleChar('*');
                        return new Token(TokenClass.ASTERIX,popCurrentTokenString(),line,column);
                    case '/':
                        expectSingleChar('/');
                        return new Token(TokenClass.DIV,popCurrentTokenString(),line,column);
                    case '%':
                        expectSingleChar('%');
                        return new Token(TokenClass.REM,popCurrentTokenString(),line,column);
                    case '.':
                        expectSingleChar('.');
                        return new Token(TokenClass.DOT,popCurrentTokenString(),line,column);
                    case '!':
                        expectFullString("!=");
                        return new Token(TokenClass.NE,popCurrentTokenString(),line,column);
                    case '=':
                        int matched = expectEither("=", "==");
                        if(matched == 0){
                            return new Token(TokenClass.ASSIGN,popCurrentTokenString(),line,column);
                        } else {
                            return new Token(TokenClass.EQ,popCurrentTokenString(),line,column);
                        }
                    case '<':
                        matched = expectEither("<","<=");
                        if(matched == 0){
                            return new Token(TokenClass.LT,popCurrentTokenString(),line,column);
                        } else {
                            return new Token(TokenClass.LE,popCurrentTokenString(),line,column);
                        }
                    case '>':
                        matched = expectEither(">",">=");
                        if(matched == 0){
                            return new Token(TokenClass.GT,popCurrentTokenString(),line,column);
                        } else {
                            return new Token(TokenClass.GE,popCurrentTokenString(),line,column);
                        }
                    
                }
            }

            // delimeters
            if(isDelimeter(currChar)){
                expectDelimeter();
                String data = popCurrentTokenString();
                return new Token(getDelimeterTokenClass((data).charAt(0)),data,line,column);
            }

            

            // int literals
            if (Character.isDigit(currChar)){
                expectIntLiteral();
                return new Token(TokenClass.INT_LITERAL,popCurrentTokenString(),line,column);
            }

            // Identifiers or keywords
            if(isIdentifierStartChar(currChar)){
                final Match rightMatch = expectLongestMatch(new ArrayList<Match>(
                    Arrays.asList(
                    new Match("if",TokenClass.IF),
                    new Match("else", TokenClass.ELSE),
                    new Match("while",TokenClass.WHILE),
                    new Match("return",TokenClass.RETURN),
                    new Match("struct", TokenClass.STRUCT),
                    new Match("sizeof",TokenClass.SIZEOF),
                    new Match("int",TokenClass.INT),
                    new Match("void",TokenClass.VOID),
                    new Match("char",TokenClass.CHAR),
                    new Match((s)->isValidIdentifier(s),TokenClass.IDENTIFIER))
                )
                , "Identifier or keyword");

                return rightMatch.buildToken(popCurrentTokenString(), line, column);
            }

        } catch(EOFException e){
            // next token will throw EOF at call to next(), meaning we get INVALID EOF at the end of the token list
            error("Unexpected EOF in the middle of token",line,column);
            return new Token(TokenClass.INVALID,popCurrentTokenString(), line, column);
        }
        catch (UnrecognizedCharacterException e) {
            error(e,line,column);
            return new Token(TokenClass.INVALID,popCurrentTokenString(),line,column);
        } 
        


        // if we reach this point, it means we did not recognise a valid token
        error(currChar, line, column);
        return new Token(TokenClass.INVALID,popCurrentTokenString(), line, column);
    }

    private void queueChar(final char symbol){
        tokenStringSoFar.append(symbol);
    }

    private void clearTokenString(){
        tokenStringSoFar.setLength(0);
    }

    private String popCurrentTokenString(){
        final String currTokenString = tokenStringSoFar.toString();
        clearTokenString();
        return currTokenString;
    }

    /**
     * if the current next character matches the given symbol returns true and advances the scanner, otherwise returns true
     * @param symbol
     * @return true if symbol is the next character
     * @throws IOException
     */
    private boolean tryAcceptChar(final char symbol) throws IOException,EOFException{
        boolean acceptedChar = false;

        if(currChar == symbol){
            acceptedChar = true;
            acceptChar(symbol);
        }

        return acceptedChar;
    }

    /**
     * Accepts any character and progresses the scanner
     * @param symbol
     * @throws IOException
     * @throws EOFException
     */
    private void acceptChar(final char symbol) throws IOException,EOFException{
        queueChar(symbol);
        try {
            currChar = scanner.next();
        } catch (final EOFException e) {
            if(reachedEOF){
                throw e;
            } else {// otherwise we expect not to use currChar again,
                currChar = (char) 0;
                reachedEOF = true;
            }
        }
    }


    /**
     * just progresses the scanner without queuing the character
     * @throws IOException
     */
    private void skipChar() throws IOException {
        try {
            currChar = scanner.next();
        } catch (final EOFException e){
            if(reachedEOF){
                throw e;
            } else {
                currChar = (char) 0;
                reachedEOF = true;
            }
        }
    }


    private Match expectLongestMatch(final ArrayList<Match> possibleMatches, final String expectErrorMessage) throws EOFException, IOException,
            UnrecognizedCharacterException {
        // keep filtering through the matches untill we have only one or none matches left
        int maxLoops = 100;
        do{
            for (final Match match : possibleMatches) {
                match.progressChar(currChar);
            }

            //get rid of non-matches
            possibleMatches.removeIf((m)-> !m.isMatching());
            // advance scanner
            acceptChar(currChar);
            
        } while(possibleMatches.size() > 1 && !reachedEOF && maxLoops-- > 0);
        
        if(possibleMatches.size() == 0 )
            throw new UnrecognizedCharacterException(expectErrorMessage, currChar);

        Match finalMatch = possibleMatches.get(0);

        maxLoops = 100;
        while(finalMatch.isMatching() && !reachedEOF && maxLoops-- > 0){
            finalMatch.progressChar(currChar);
            if(finalMatch.isMatching()){
                acceptChar(currChar);
            }
        }

        return finalMatch;

        }

    /**
     * accepts: either of the given strings, with LL(1) (only one character
     * lookahead). The longest matching string is matched first. In the cases where maximum lookahead is exceeded or EOF is reached t1 is given priority.
     * advances the scanner to the
     * length of the longest match.
     * 
     * @param t1
     * @param t2
     * @return 0 if t1 is matched, 1 if t2 is matched
     * @throws UnrecognizedCharacterException
     * @throws IOException
     * @throws EOFException
     */
    private int expectEither(final String t1, final String t2) throws UnrecognizedCharacterException, EOFException, IOException {
        // first see if both match the first character, 
        // if not trivially expect the next character to the string matching the first one
        if(t1.charAt(0) == currChar && t2.charAt(0) == currChar){

            acceptChar(currChar);

            // if we hit EOF, we attempt to match on just first character, this can only happen if one of the strings is 1 character long
            if(reachedEOF){
                if(t1.length() == 1)
                    return 0;
                else if (t2.length() == 1)
                    return 1;
                else 
                    throw new UnrecognizedCharacterException("characters for: " + t1 +" or " + t2, currChar);
            } else {
                final boolean hasSecondChar1 = t1.length() > 1;
                final boolean hasSecondChar2 = t2.length() > 1;

                // give priority to longest match under 2 chars
                final boolean t1MatchesSecondChar = t1.length() > 1 ? t1.charAt(1) == currChar : false;
                final boolean t2MatchesSecondChar = t2.length() > 1 ? t2.charAt(1) == currChar : false;
                // first prioritize by length
                if(t1.length() >= t2.length() && t1MatchesSecondChar){
                    expectFullString(t1.substring(1, t1.length()));
                    return 0;
                }
                else if(t2.length() >= t1.length() && t2MatchesSecondChar){
                    expectFullString(t2.substring(1, t2.length()));
                    return 1;
                }
                // then prioritize t1
                else if(t1MatchesSecondChar){
                    expectFullString(t1.substring(1, t1.length()));
                    return 0;
                }
                else if (t2MatchesSecondChar){
                    expectFullString(t2.substring(1, t2.length()));
                    return 1;
                }
                else{
                    throw new UnrecognizedCharacterException("characters for: " + t1 +" or " + t2 , currChar);
                }
            }
        }
        else if( t1.charAt(0) == currChar){
            //match t1
            expectFullString(t1);
            return 0;

        } else if (t2.charAt(0) == currChar){
            // match t2
            expectFullString(t2);
            return 1;

        } else {
            throw new UnrecognizedCharacterException(t1.charAt(0) + " or " + t2.charAt(0), currChar);
        }
    }

    /**
     * accepts: the given string
     * 
     * @param s
     * @throws UnrecognizedCharacterException
     * @throws IOException
     * @throws EOFException
     */
    private void expectFullString(String s) throws UnrecognizedCharacterException, EOFException, IOException {
        while(s.length() != 0){
            if(s.charAt(0) != currChar)
                throw new UnrecognizedCharacterException(""+ s.charAt(0), currChar);
            acceptChar(currChar);
            s = s.substring(1, s.length());
        }
    }

    /**
     * accepts: the given char
     * 
     * @param c
     * @throws UnrecognizedCharacterException
     * @throws IOException
     * @throws EOFException
     */
    private void expectSingleChar(final char c) throws UnrecognizedCharacterException, EOFException, IOException {
        if(c != currChar)
            throw new UnrecognizedCharacterException(""+ c, currChar);
        acceptChar(c);
    }



    /**
     * accepts: ('a'|...|'z'|'A'|...|'Z'|'_')('0'|...|'9'|'a'|...|'z'|'A'|...|'Z'|'_')*
     * @throws UnrecognizedCharacterException
     * @throws EOFException
     * @throws IOException
     */
    private void expectIdentifier() throws UnrecognizedCharacterException, EOFException, IOException {
        if (!isIdentifierStartChar(currChar))
            throw new UnrecognizedCharacterException("Character from the set: [a-zA-Z_]", currChar);
        else
            acceptChar(currChar);

        while(isIdentifierInsideChar(currChar)){
            acceptChar(currChar);
        }
    }
    
    /**
     * accepts: '{','}','(',')','[',']',';',','
     * @throws UnrecognizedCharacterException
     * @throws IOException
     * @throws EOFException
     */
    private void expectDelimeter() throws UnrecognizedCharacterException, EOFException, IOException {
        if (!isDelimeter(currChar))
            throw new UnrecognizedCharacterException("A delimeter", currChar);
        acceptChar(currChar);
    }

    /**
     * accepts: \".*\"
     * @throws IOException
     * @throws UnrecognizedCharacterException
     */
    private void expectStringLiteral() throws IOException, UnrecognizedCharacterException{
        if (!tryAcceptChar('"')) 
            throw new UnrecognizedCharacterException("Start of string literal - \"",currChar);

        while(true){

            if (currChar == '"') break;

            // check for escape characters
            else if (currChar == '\\'){

                final char nextChar = scanner.peek();
                
                if(isEscapeSequenceEnding(nextChar)){
                    acceptChar(getEscapeCharacterForEnding(nextChar));
                } else {
                    throw new UnrecognizedCharacterException("A valid escape sequence:" + currChar);
                }
                // skip to next char
                skipChar();
                
            } else {
                // accept any other character 
                acceptChar(currChar);
            }
        }

        if (!tryAcceptChar('"')) 
            throw new UnrecognizedCharacterException("End of string literal - \"",currChar);
    }

    /**
     * accepts: ('0'|...|'9')+
     * @throws UnrecognizedCharacterException
     * @throws EOFException
     * @throws IOException
     */
    private void expectIntLiteral() throws UnrecognizedCharacterException, EOFException, IOException {
        if(!Character.isDigit(currChar)) 
            throw new UnrecognizedCharacterException("A digit [0-9]", currChar);
        else{
            acceptChar(currChar);
            while(Character.isDigit(currChar)){
                acceptChar(currChar);
            }
        }
    }

    /**
     * accepts: \'('a'|...|'z'|'A'|...|'Z'|'\t'|'\b'|'\n'|'\r'|'\f'|'\''|'\"'|'\\'|'\0'|'.'|','|'_'|...)\'
     * @throws UnrecognizedCharacterException
     * @throws EOFException
     * @throws IOException
     */
    private void expectCharLiteral() throws UnrecognizedCharacterException, EOFException, IOException {
        if(!tryAcceptChar('\'')) 
            throw new UnrecognizedCharacterException("'\''", currChar);
        // check for special characters
        if(currChar == '\\'){
            final char nextChar = scanner.peek();
            if(isEscapeSequenceEnding(nextChar)){
                acceptChar(getEscapeCharacterForEnding(nextChar));
            } else {
                throw new UnrecognizedCharacterException("A valid escape sequence, ", nextChar);
            }
            skipChar();

        } else {
            // accept any character
            //TODO: make sure parsing characters works like in C, only ASCI i belive
            acceptChar(currChar);
        }

        if(!tryAcceptChar('\''))
            throw new UnrecognizedCharacterException("'\''",currChar);
    }


}
