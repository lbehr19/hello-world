package abstract_instructions;

public class Label extends Abs_Instruction{
	private String _label;
	private Abs_Instruction _instruction;
	
	public Label(String label, Abs_Instruction instruction) {
		_label = label;
		_instruction = instruction;
	}
	
	public String getLabel() {
		return _label.replaceAll(":", "");
	}
	public Abs_Instruction getInstruction() {
		return _instruction; 
	}
	
	@Override 
	public String toString() {
		String s = "Label (" + _label + " " + _instruction.toString() + ")";
		return s;
	}
}
