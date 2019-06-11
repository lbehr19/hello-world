package assembly_and_absInstr_parsing;

public class ParseException extends Exception {


	private static final long serialVersionUID = -792400898877506310L;
	private int _lineNumber;
	
	public ParseException(String message, int lineNumber) {
		super(message + " " + lineNumber);
		_lineNumber = lineNumber;
	}
	
	public int getLineNumber() {
		return _lineNumber;
	}
}
