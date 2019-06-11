package abstract_instructions;

public class Call extends Abs_Instruction{

	private Abs_Expression _symbol; //go to here
	
	public Call (Abs_Expression symbol) {
		_symbol = symbol;
	}
	

	public Abs_Expression getSymbol() {
		return _symbol;
	}
	
	@Override 
	public String toString() {
		String s = "Call (" + _symbol.toString() + ")";
		return s;
	}
}
