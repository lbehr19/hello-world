package register_syntax;

public interface RegisterSpec {
	
	int index(String register) throws RegisterException;
	String regName(int index) throws RegisterException;
	boolean contains(String register);
	
	int getRA();
	int getSP();

}
