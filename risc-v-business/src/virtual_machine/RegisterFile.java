package virtual_machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import register_syntax.RegisterException;
import register_syntax.RegisterSpec;

public class RegisterFile {
	private Map<Integer, Integer>_registers;
	public RegisterSpec registers;
	
	public RegisterFile(RegisterSpec reg) {
		registers = reg;
		_registers = new HashMap<Integer, Integer>();
	}
	
	public Map<Integer, Integer> getRegisterMap() { 
		return _registers;
	}
	
	public void putRegister(int key, int value){
		if(key != 0) {
			_registers.put(key, value);
		}
	}
	
	public int getRegister(int key) {
		return _registers.getOrDefault(key, 0);
	}
	
	public void clearRegister() {
		_registers.clear();
	}
	
	public String printRegisters() throws RegisterException {
		ArrayList<String> nonEmpties = new ArrayList<>();
		for(Integer i : _registers.keySet()) {
			nonEmpties.add(registers.regName(i) + ": " + _registers.get(i));
		}
		return nonEmpties.toString();
	}
	
	public int getRA() {
		return registers.getRA();
	}
	
	public int getSP() {
		return registers.getSP();
	}
}
