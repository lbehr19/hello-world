package virtual_machine;

import java.util.List;

import abstract_instructions.Abs_Instruction;

public interface VirtualMachine {

	int STACK = 1000;
	int HEAP = 2000;

	int getReg(int reg);
	int getMainMem(int addres, int size) throws MemoryException;
	
	void resetReg();
	void resetMainMem();
	
	int getPC();
	void endPC();
	void resetPC();
	
	int getRA();
	int getSP();
	
	boolean hasNext();
	
	RegisterFile getVmRegFile();
	MemorySystem getVmMainMem();
	
	//THESE ARE ONLY FOR EXECUTION ENGINE
	void loadInstructions(List<Abs_Instruction> instructions);
	void putReg(int reg, int value);
	Abs_Instruction getNextInstr();
	void putMainMem(int address, int size, int value) throws MemoryException;
	void changePC(int dest);
	
}
