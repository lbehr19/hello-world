package register_syntax;

public class RegisterException extends Exception {
	private static final long serialVersionUID = 1L;
	private String _register;
	private int _index;
	
	public RegisterException(String register) {
		_register = register;
	}
	
	public RegisterException(int index) {
		_index = index;
	}
	
	public String getRegister() {
		return _register;
	}
	
	public int getIndex() {
		return _index;
	}
	
	//intended for register name -> index / value
	public String getRegMessage() {
		return "Invalid register " + _register;
	}
	
	//intended for index -> register name
	public String getIndexMessage() {
		return "No such register " + _index;
	}
}
