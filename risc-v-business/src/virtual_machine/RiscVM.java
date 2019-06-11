package virtual_machine;

import java.util.List;
import abstract_instructions.*;
import register_syntax.*;

public class RiscVM implements VirtualMachine{
	private List<Abs_Instruction> _instructions;
	private RegisterFile _registers;
	private MemorySystem _mainMem;
	private boolean _run;
	
	private int _pc;
	private int _RA;
	private int _SP;
	
	public RiscVM() {
		_registers = new RegisterFile(new RiscRegisters());
		_mainMem = new MemorySystem();
		_run = true;
		_pc = 0;
		_RA = _registers.getRA();
		_SP = _registers.getSP();
	}
	
	public void putReg(int key, int value){
		_registers.putRegister(key, value);
	}
	
	public void resetReg() {
		_registers = new RegisterFile(new RiscRegisters());
	}
	
	public void resetMainMem() {
		_mainMem = new MemorySystem();
	}
	
	public int getReg(int key) {
		return _registers.getRegister(key);
	}
	
	public void changePC(int x) {
		_pc = x;
	}
	
	public void resetPC() {
		_pc = 0;
	}
	
	void incPC(int x) {
		_pc += x;
	}
	
	public void endPC() {
		_run = false;
	}
	
	public int getPC() {
		return _pc;
	}
	
	public int getRA() {
		return _RA;
	}
	
	public int getSP() {
		return _SP;
	}
	
	public void putMainMem(int address, int size, int value) throws MemoryException {
		_mainMem.putMemory(address, size, value);
	}
	
	public int getMainMem(int address, int size) throws MemoryException {
		return _mainMem.getMemory(address, size);
	}
	
	public boolean hasNext(){
		if(_pc < _instructions.size() && _run) {
			return true;
		}else {
			return false;
		}
	}
	
	public Abs_Instruction getNextInstr() {
		Abs_Instruction next = _instructions.get(_pc);
		incPC(1);
		return next;
	}
	
	public RegisterFile getVmRegFile() {
		return _registers;
	}
	
	public MemorySystem getVmMainMem() {
		return _mainMem;
	}
	
	public void loadInstructions(List<Abs_Instruction> instructions) {
		_instructions = instructions;
		resetPC();
	}
		
}
