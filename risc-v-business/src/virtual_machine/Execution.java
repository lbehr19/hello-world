package virtual_machine;

import abstract_instructions.*;
import assembly_and_absInstr_parsing.SymbolTable;
import operation_syntax.*;
import register_syntax.RegisterException;
import window.MainTest;

public class Execution {
	private VirtualMachine _vm;
	private SymbolTable _st;
	int RA;
	int SP;
	static final int STACK_START = VirtualMachine.STACK;
	static final int HEAP_START = VirtualMachine.HEAP;
	
	public Execution(VirtualMachine vm){
		_vm = vm;
		RA = _vm.getRA();
		SP = _vm.getSP();
		_vm.putReg(SP, STACK_START);
	}

	public void executeNextInstr() throws OperationException, MemoryException, RegisterException {
		Abs_Instruction i = _vm.getNextInstr();

		if(i instanceof Label) {
			i = ((Label) i).getInstruction();
		}

		if(i instanceof Move) {
			Move m = (Move) i;
			Abs_Expression dest = m.getDestination();
			Abs_Expression source = m.getSource();
			
			//sw
			if(dest instanceof MemoryAddress && source instanceof Register) {
				MemoryAddress a = (MemoryAddress) dest;
				int address = evaluateExp(a.getAddress());
				int size = a.getSize();
				int value = evaluateExp((Register) source);
				_vm.putMainMem(address, size, value);
				
			//arithmetic and lw
			}else{
				int destination = ((Register) dest).get();
				int value = evaluateExp(source);
				_vm.putReg(destination, value);
			}
			
		}else if(i instanceof Branch) {
			Branch b = (Branch) i;
			int dest = evaluateExp(b.getDestination());
			int result = evaluateExp(b.getRelOp());
			if(result == 1) {
				_vm.changePC(dest);
			}
		}else if(i instanceof Call) {
			Call c = (Call) i;
			int dest = evaluateExp(c.getSymbol());
			_vm.putReg(RA, _vm.getPC());
			_vm.changePC(dest);
		}else if(i instanceof Return) {
			int newPC = _vm.getReg(RA);
			_vm.changePC(newPC);
		}else if(i instanceof Jump) {
			Jump j = (Jump) i;
			int newPC = evaluateExp(j.getDestination());
			_vm.changePC(newPC);
		}else if(i instanceof SystemCall) {
			SystemCall s = (SystemCall) i;
			evaluateCommand((Command) s.getCommand());
		}
	}

	private int evaluateExp(Abs_Expression e) throws MemoryException, OperationException {
		int result = 0;
		if(e instanceof BinOp) {
			result = binOps((BinOp) e);
		}else if(e instanceof Literal) {
			result = ((Literal) e).get();
		}else if(e instanceof Register) {
			result = _vm.getReg(((Register) e).get());
		}else if(e instanceof MemoryAddress) {
			MemoryAddress m = (MemoryAddress) e;
			result = _vm.getMainMem(evaluateExp(m.getAddress()), m.getSize());
		}else if(e instanceof Symbol){
			result = _st.getCodeLine(((Symbol) e).get());
		}
		return result;
	}
	
	private int binOps(BinOp e) throws OperationException, MemoryException {
		Operation op = e.getOp();
		int arg1 = evaluateExp(e.getArg1());
		int arg2 = evaluateExp(e.getArg2());
		int result = 0;
		switch (op) {
			case ADD:
			result = arg1 + arg2;
			break;
			case SUB:
			result = arg1 - arg2;
			break;
			case SHIFT_LEFT:
			result = arg1 << arg2;
			break;
			case SHIFT_RIGHT_L:
			result = arg1 >>> arg2;
			break;
			case SHIFT_RIGHT_A:
			result = arg1 >> arg2;
			break;
			case OR:
			result = arg1 | arg2;
			break;
			case AND:
			result = arg1 & arg2;
			break;
			case X_OR:
			result = arg1 ^ arg2;
			break;
			case EQUAL:
			result = arg1 == arg2 ? 1 : 0;
			break;
			case NOT_EQUAL:
			result = arg1 != arg2 ? 1 : 0;
			break;
			case LESS:
			result = arg1 < arg2 ? 1 : 0;
			break;
			case LESS_U:
			result = Integer.compareUnsigned(arg1, arg2) < 0 ? 1 : 0;
			break;
			case LESS_EQUAL:
			result = arg1 <= arg2 ? 1 : 0;
			break;
			case LESS_EQUAL_U:
			result = Integer.compareUnsigned(arg1, arg2) <= 0 ? 1 : 0;
			break;
			case GREATER:
			result = arg1 > arg2 ? 1 : 0;
			break;
			case GREATER_U:
			result = Integer.compareUnsigned(arg1, arg2) > 0 ? 1 : 0;
			break;
			case GREATER_EQUAL:
			result = arg1 >= arg2 ? 1 : 0;
			break;
			case GREATER_EQUAL_U:
			result = Integer.compareUnsigned(arg1, arg2) >= 0 ? 1 : 0;
			break;
			default:
			throw new OperationException(op.toString());
		}
		return result;
	}
	
	private void evaluateCommand(Command command) throws OperationException, MemoryException {
		String c = command.getCommand();
		if(c.equals("stop")) {
			_vm.endPC();
			MainTest.iof.printOutput("Program halted at " + (_vm.getPC()));
		}else if(c.equals("print")){
			Abs_Expression a = command.getValue();
			int i = evaluateExp(a);
			MainTest.iof.printOutput("Value of " + a + " is " + i);
		}else {
			throw new OperationException(c);
		}	
	}
	
	public void loadSymbolTable(SymbolTable st) {
		_st = st;
	}
	


}
