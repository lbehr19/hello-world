package abstract_instructions;

public class Register extends Abs_Expression{
	private int _register;
	
	public Register(int reg) {
		_register = reg;
	}
	
	public int get() {
		return _register;
	}
	

	@Override
	public String toString() {
		return "R(" + _register + ")";
	}
}