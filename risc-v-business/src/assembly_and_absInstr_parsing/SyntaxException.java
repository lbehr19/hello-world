package assembly_and_absInstr_parsing;

public class SyntaxException extends Exception {
	
	private static final long serialVersionUID = -5793954475896035226L;
	
	private int _lineNo;
	private String _label;

	public SyntaxException(int lineNo, String label) {
		_label = label;
		_lineNo = lineNo;
	}
	
	public SyntaxException(int lineNo) {
		_lineNo = lineNo;
	}
	
	public String getRepLabelMessage() {
		return "Duplicated label " + _label + " at line " + _lineNo;
	}
	
	public String getSyntaxMessage() {
		return "Syntax error at line " + _lineNo;
	}
 
	public String getIllegLabelMessage() {
		return "Illegal label " + _label + " at line " + _lineNo;
	}
	
	public String getNoInstMessage() {
		return "No instruction with label " + _label + " at line " + _lineNo;
	}
}
