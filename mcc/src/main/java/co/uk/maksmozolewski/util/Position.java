package co.uk.maksmozolewski.util;
/**
 * @author cdubach
 */
public class Position {

    final int line;
    final int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return line+":"+column;
    }

    public int getLine(){
        return line;
    }

    public int getColumn(){
        return column;
    }

    @Override
    public boolean equals(Object obj) {
        Position other = obj != null && obj.getClass() == Position.class ? (Position)obj: null;

        return (other != null) && 
            other.getLine() == line && 
            other.getColumn() == column;
    }

    @Override
    public int hashCode() {
        return line + column * Integer.MAX_VALUE;
    }


}
