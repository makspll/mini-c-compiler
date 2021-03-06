package co.uk.maksmozolewski.lexer;

import co.uk.maksmozolewski.util.Position;

public class Token {
    public enum TokenClass {

        // the \ (backslash) is used as an escape character in the regular expression below
        // ' is used to enclose character while " is used to enclose strings

        IDENTIFIER, // ('a'|...|'z'|'A'|...|'Z'|'_')('0'|...|'9'|'a'|...|'z'|'A'|...|'Z'|'_')*

        // delimiters
        LBRA,  // '{' // left brace
        RBRA,  // '}' // right brace
        LPAR,  // '(' // left parenthesis
        RPAR,  // ')' // right parenthesis
        LSBR,  // '[' // left square brace
        RSBR,  // ']' // left square brace
        SC,    // ';' // semicolon
        COMMA, // ','

	//// KEYWORODS
        // types
        INT,  // "int"
        VOID, // "void"
        CHAR, // "char"

        // keywords
        IF,     // "if"
        ELSE,   // "else"
        WHILE,  // "while"
        RETURN, // "return"
        STRUCT, // "struct"
        SIZEOF, // "sizeof"
	////
        
	// include
        INCLUDE, // "#include"

        // literals
        STRING_LITERAL, // \".*\"  any sequence of characters enclosed within two double quote " (please be aware of the escape character backslash \)
        INT_LITERAL,    // ('0'|...|'9')+
        CHAR_LITERAL,   // \'('a'|...|'z'|'A'|...|'Z'|'\t'|'\b'|'\n'|'\r'|'\f'|'\''|'\"'|'\\'|'\0'|'.'|','|'_'|...)\'  a character starts and end with a single quote '
	
	//// OPERATORS
        
	// logical operators
        AND, // "&&"
        OR,  // "||"

        // comparisons
        EQ, // "=="
        NE, // "!="
        LT, // '<'
        GT, // '>'
        LE, // "<="
        GE, // ">="


        ASSIGN, // '='

        // operators
        PLUS,  // '+'
        MINUS, // '-'
        ASTERIX, // '*'  // can be used for multiplication or pointers
        DIV,   // '/'
        REM,   // '%'

        // struct member access
        DOT, // '.'

        //// special tokens
        
	EOF,    // signal end of file
        INVALID // in case we cannot recognise a character as part of a valid token
    }


    public final TokenClass tokenClass;
    public final String data;
    public final Position position;

    public Token(TokenClass type, int lineNum, int colNum) {
        this(type, "", lineNum, colNum);
    }

    public Token (TokenClass tokenClass, String data, int lineNum, int colNum) {
        assert (tokenClass != null);
        this.tokenClass = tokenClass;
        switch (tokenClass){
            case IDENTIFIER:
                this.data = data;
                break;
            case CHAR_LITERAL:
            case STRING_LITERAL:
                this.data = data.substring(1,data.length()-1);
                break;
            case INT_LITERAL:
                this.data = data;
                break;
            default:
                this.data = "";
                
        }
        this.position = new Position(lineNum, colNum);
    }



    @Override
    public String toString() {
        if (data.equals(""))
            return tokenClass.toString();
        else
            return tokenClass.toString()+"("+data+")";
    }


}
