package virtual_machine;

public class MemoryException extends Exception{

	private static final long serialVersionUID = 1L;
	int address;
	 public MemoryException(int _address) {
		 address = _address;
	 }
	 
	 public String getMessage() {
		 return "Memory address " + address + " is not word aligned";
	 }
}
