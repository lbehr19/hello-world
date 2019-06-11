package abstract_instructions;

public class Command extends Abs_Expression{

	private String _command;
	private Abs_Expression _value;
	
	public Command(String c) {
		_command = c;
	}
	
	public Command(String c, Abs_Expression v) {
		_command = c;
		_value = v;
	}
	
	public String getCommand() {
		return _command;
	}
	
	public Abs_Expression getValue() {
		return _value;
	}

	@Override
	public String toString() {
		return _command;
	}
}
