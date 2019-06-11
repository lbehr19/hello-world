package abstract_instructions;

public class Move extends Abs_Instruction{
	private Abs_Expression _destination;
	private Abs_Expression _source;
	
	public Move(Abs_Expression destination, Abs_Expression source) {
		_destination = destination;
		_source = source;
	}
	
	public Abs_Expression getDestination(){
		return _destination;
	}
	public Abs_Expression getSource() {
		return _source;
	}
	
	@Override
	public String toString() {
		String s = "Move (" + _destination.toString() + ", " + _source.toString() + ")";
		return s;
	}
}