package abstract_instructions;

public class SystemCall extends Abs_Instruction{
	
	private Abs_Expression _command;
	
	public SystemCall(Abs_Expression command) {
		_command = command;
	}
	
	public Abs_Expression getCommand(){
		return _command;
	}
	
	@Override
	public String toString() {
		String s = "SystemCall (" + _command.toString() + ")";
		return s;
	}

}
