package register_syntax;

import java.util.ArrayList;
import java.util.Arrays;

public class RiscRegisters implements RegisterSpec{
	
	private static ArrayList<String> REGISTERS;
	
	public RiscRegisters() {
		REGISTERS = new ArrayList<>(Arrays.asList("zero", "ra", "sp", "gp", "tp","t0", "t1", "t2", 
												"s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
												"s2", "s3", "s4", "s5", "s6", "s7", "s8","s9","s10","s11",
												"t3", "t4", "t5", "t6"));
	}
	
	private static int _RA = 1;
	private static int _SP = 2;
	
	public int getRA() {
		return _RA;
	}
	public int getSP() {
		return _SP;
	}
	
	public int index(String register) throws RegisterException {
			int index = REGISTERS.indexOf(register);
			
			if(index == -1) {
				throw new RegisterException(register);
			}
			
			return index;
	}
	
	public String regName(int index) throws RegisterException{
		try {
			String register = REGISTERS.get(index);
			return register;
		}catch(IndexOutOfBoundsException e) {
			throw new RegisterException(index);
		}
		
	}
	
	public boolean contains(String register) {
		if(REGISTERS.contains(register)) {
			return true;
		}else {
			return false;
		}
	}
	
}
