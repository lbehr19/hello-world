package abstract_instructions;

public class MemoryAddress extends Abs_Expression{
	private Abs_Expression _address;
	private int _size;
	
	/*public MemoryAddress(Abs_Expression address) {
		_address = address;
	}*/
	
	public MemoryAddress(Abs_Expression address, int size) {
		_address = address;
		_size = size;
	}
	
	
	public Abs_Expression getAddress() {
		return _address;
	}
	
	public int getSize() {
		return _size;
	}
	
	@Override
	public String toString() {
		return "Mem(" + _address + ")";
	}
}