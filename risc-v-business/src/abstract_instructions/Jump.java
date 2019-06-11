package abstract_instructions;

public class Jump extends Abs_Instruction{
	private Abs_Expression _destination;
	
	public Jump (Abs_Expression destination) {
		_destination = destination;
	}
	
	public Abs_Expression getDestination() {
		return _destination;
	}
	
	@Override 
	public String toString() {
		String s = "Jump (" + _destination.toString() + ")";
		return s;
	}
}
