package util;

public class InvalidOptionException extends IllegalArgumentException {


    private static final long serialVersionUID = 1L;

    public InvalidOptionException(String s){
		super(s);
	}
}
