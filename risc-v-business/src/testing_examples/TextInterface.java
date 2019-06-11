package testing_examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import abs_and_machine_parsing.AbsToMachine;
import abstract_instructions.Abs_Instruction;
import util.Pair;
import assembly_and_absInstr_parsing.ParseException;
import assembly_and_absInstr_parsing.RiscvParse;
import assembly_and_absInstr_parsing.SymbolTable;
import operation_syntax.OperationException;
import register_syntax.RegisterException;
import virtual_machine.Execution;
import virtual_machine.MemoryException;
import virtual_machine.RiscVM;
import virtual_machine.VirtualMachine;

public class TextInterface {
	private static Scanner _scanner = new Scanner(System.in);
	private static VirtualMachine _vm;
	private static Execution _exec;
	
	static Pair<SymbolTable, List<Pair<Abs_Instruction, Integer>>> _parsedInput;
	static List<Abs_Instruction> _instructions;
	public static void main(String args[]) throws IOException, ParseException, RegisterException {

		String commands = 
				" l) load instructions from file \n" +
				" i) enter single instruction \n" +
				" e) execute all instructions \n" + 
				" n) execute next instruction \n" + 
				" r) view altered registers \n" + 
				" m) view altered  main memory \n" +
				" p) view program counter \n" +
				" c) disassemble to machine code \n" +
				" a) disassemble to abstract instructions \n" +
				" q) quit";
		
		String initializeError = "Load instructions to initialize execution environment";

		System.out.println(commands);

		while(_scanner.hasNext()) {
			String command = _scanner.nextLine();
			command = command.trim();
			
			if(command.equals("l")) {
				System.out.println("Text file of instructions: ");
				String filename = _scanner.nextLine();
				String source = new String(Files.readAllBytes(Paths.get(filename)));
				initializeExecutionEnvironment(source);

			}else if(command.equals("i")) {
				System.out.println("Enter RISC-V instruction: ");
				String source = _scanner.nextLine();
				initializeExecutionEnvironment(source);	

			}else if(command.equals("e")) {
				try{
					long startTime = System.currentTimeMillis();
					while(_vm.hasNext()) {
						_exec.executeNextInstr();
					}
					long endTime = System.currentTimeMillis();
					long runTime = endTime - startTime;
					System.out.println("Execution complete in " + runTime + "ms");
					
				}catch(NullPointerException | MemoryException | OperationException e) {
					System.out.println(initializeError);
				}

			}else if(command.equals("n")) {
				try {
					if(_vm.hasNext()) {
						_exec.executeNextInstr();
					}else {
						System.out.println("No instruction to execute");
					}
				}catch(NullPointerException | MemoryException | OperationException e) {
					System.out.println(initializeError);
				}

			}else if(command.equals("r")) {
				try {
					System.out.println("Registers: " + _vm.getVmRegFile().printRegisters());
				}catch(NullPointerException e) {
					System.out.println(initializeError);
				}

			}else if(command.equals("m")) {
				try {
					System.out.println("Main memory: " + _vm.getVmMainMem().printMainMem());
				}catch(NullPointerException e) {
					System.out.println(initializeError);
				}
				
			}else if(command.equals("p")) {
				try {
					System.out.println("PC: " + _vm.getPC());
				}catch(NullPointerException e) {
					System.out.println(initializeError);
				}
			}else if(command.equals("c")) {
				ArrayList<Integer> machineCode = AbsToMachine.parseAbstracts(_instructions, _parsedInput.first());
				System.out.println("Machine Code: " + printMachineCode(machineCode));
			
			}else if(command.equals("a")) {
				System.out.println("Abstract Instructions:" + printAbstracts(_instructions));
			
			}else if(command.equals("q")) {
				System.out.println("Execution environment exited");
				break;
				
			} else {
				System.out.println("Invalid command: " + command + "\n" + commands);
				continue;
			}

			System.out.println("...");
		}

		_scanner.close(); 
	}

	private static void initializeExecutionEnvironment(String source) throws ParseException, RegisterException {

		//PARSE INPUT
		_parsedInput = RiscvParse.parse(source);
		_instructions = new ArrayList<>();
		for (Pair<Abs_Instruction, Integer> pair : _parsedInput.second()) {
			_instructions.add(pair.first());
		}

		//INITIALIZE EXECUTION ENVIRONMENT
		_vm = new RiscVM();
		_vm.loadInstructions(_instructions);
		_exec = new Execution(_vm);
		_exec.loadSymbolTable(_parsedInput.first());
		

	}
	
	private static String printAbstracts(List<Abs_Instruction> instructions) {
		String s = "";
		int j = 0;
		for(Abs_Instruction i : instructions) {
			s += "\n" + (j++) + ": "+ i ;
		}
		return s;
	}

	private static String printMachineCode(List<Integer> instructions) {
		String s = "";
		int j = 0;
		for(int i : instructions) {
			s += "\n" + (j++) + ": " + String.format("%08x", i);
		}
		return s;
	}
}
