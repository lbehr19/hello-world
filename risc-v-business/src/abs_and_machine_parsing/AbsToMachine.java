package abs_and_machine_parsing;

import java.util.ArrayList;
import java.util.List;

import abstract_instructions.Abs_Expression;
import abstract_instructions.Abs_Instruction;
import abstract_instructions.BinOp;
import abstract_instructions.Branch;
import abstract_instructions.Call;
import abstract_instructions.Jump;
import abstract_instructions.Label;
import abstract_instructions.Literal;
import abstract_instructions.MemoryAddress;
import abstract_instructions.Move;
import abstract_instructions.Register;
import abstract_instructions.Return;
import abstract_instructions.Symbol;
import abstract_instructions.SystemCall;
import assembly_and_absInstr_parsing.SymbolTable;
import operation_syntax.Operation;

/**
 * A class that takes abstract instructions and translates them to
 * RISC-V machine code. The method parseAbstracts takes an ArrayList
 * of abstract instructions and returns an ArrayList of corresponding
 * integers representing machine code, with a one-to-one relationship
 * between each ArrayList.
 */
public class AbsToMachine {
	//TODO: make methods that create specific machine code formats
	//TODO: change this
	//Temporary until we get the ra register from user
	public final static Register RA = new Register(1);
	//TODO: call this within individual needed methods
	private static SymbolTable _st;

	public static ArrayList<Integer> parseAbstracts
	(List<Abs_Instruction> abs, SymbolTable st) {
		ArrayList<Integer> mc = new ArrayList<Integer>();
		_st = st;
		for (Abs_Instruction a : abs) {
			int i = parseInstruction(a);
			mc.add(i);
		}
		return mc;
	}

	public static int parseInstruction(Abs_Instruction i) {

		if (i instanceof Move) {
			return moveCode((Move) i);
		} else if (i instanceof Branch) {
			return branchCode((Branch) i);
		} else if (i instanceof Call) {
			return callCode((Call) i);
		} else if (i instanceof Label) {
			return labelCode((Label) i);
		} else if (i instanceof Jump) {
			return jumpCode((Jump) i);
		} else if (i instanceof Return) {
			return returnCode((Return) i);
		} else if (i instanceof SystemCall) {
			//TODO: refine SystemCall machine code
			return MachineNumber.SYSCALL;
		} else {
			throw new RuntimeException
			("Disassembly not implemented for instruction");
		}
	}

	public static int moveCode(Move i) {
		int machine = 0;


		//////////// DESTINATION PARSE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\		
		Abs_Expression dest = ((Move) i).getDestination();

		if (dest instanceof Register) {
			int rd = ((Register) dest).get();
			machine += rd << MachineNumber.RD_SHAMT;

		} else if (dest instanceof MemoryAddress) { //Store instruction
			Abs_Expression address = ((MemoryAddress) dest).getAddress();
			int size = ((MemoryAddress) dest).getSize();
			int funct3 = 0; //temp

			if (address instanceof BinOp) {

				Operation op = ((BinOp) address).getOp();

				//SW, SB, SH INSTRUCTIONS
				if (op.equals(Operation.ADD)) {
					machine += MachineNumber.S_OPCODE;

					if(size == MachineNumber.W_BYTES) { //sw
						funct3 = MachineNumber.SW_FUNCT3;
					}else if(size == MachineNumber.H_BYTES) { //sh
						funct3 = MachineNumber.SH_FUNCT3;
					}else if(size == MachineNumber.B_BYTES) {//sb
						funct3 = MachineNumber.SB_FUNCT3;
					}
				} else {
					throw new RuntimeException
					("Incorrect operand in store instruction " + op);
				}

				Abs_Expression arg1 = ((BinOp) address).getArg1();
				if (arg1 instanceof Literal) {
					int imm = ((Literal) arg1).get();
					
					machine += (imm & MachineNumber.BITS_4TO0_MASK) 
							<< MachineNumber.BITS_4TO1_SHAMT;
					
					machine += (imm & MachineNumber.BITS_11TO5_MASK)
							<< MachineNumber.BITS_11TO5_SHAMT;
					
					machine += funct3 << MachineNumber.FUNCT3_SHAMT;
				} else {
					throw new RuntimeException
					("Incorrect arg type for store instruction " + 
							arg1.toString());
				}

				Abs_Expression arg2 = ((BinOp) address).getArg2();
				if (arg2 instanceof Register) {
					int rs1 = ((Register) arg2).get();
					machine += rs1 << MachineNumber.RS1_SHAMT;
				} else {
					throw new RuntimeException
					("Incorrect arg type for store instruction " + 
							arg2.toString());
				}
			} else {
				throw new RuntimeException
				("Incorrect address type in MemAddress " + 
						address.toString());
			}
		} else {
			throw new RuntimeException
			("Unknown destination type in Move code " + 
					dest.toString());
		}

		//////////// SOURCE PARSE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\	
		Abs_Expression src = ((Move) i).getSource();

		if (src instanceof BinOp) {
			Operation op = ((BinOp) src).getOp();
			Abs_Expression arg1 = ((BinOp) src).getArg1();
			Abs_Expression arg2 = ((BinOp) src).getArg2();

			//add, sub, sll, srl, sra, or, xor, and, slt
			if (arg1 instanceof Register) { 
				int rs1 = ((Register) arg1).get();
				machine += rs1 << MachineNumber.RS1_SHAMT;

				//addi, slli, srli, srai, slti
			} else {
				throw new RuntimeException
				("Unknown argument in BinOp in Move " + 
						arg1.toString());
			}
			
			if(arg2 instanceof Register) {
				int rs2 = ((Register) arg2).get();
				rs2 = rs2 << MachineNumber.RS2_SHAMT;
				machine += rs2;
				//ADD INSTRUCTION
				if (op.equals(Operation.ADD)) {
					machine += MachineNumber.R_OPCODE;  //opcode for add instruction
					machine += MachineNumber.ADD_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.ADD_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//SUB INSTRUCTION
				else if (op.equals(Operation.SUB)) {
					machine += MachineNumber.R_OPCODE; //opcode for sub instruction
					machine += MachineNumber.SUB_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.SUB_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//AND INSTRUCTION
				else if (op.equals(Operation.AND)) {
					machine += MachineNumber.R_OPCODE; //opcode for and instruction
					machine += MachineNumber.AND_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.AND_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//OR INSTRUCTION
				else if (op.equals(Operation.OR)) {
					machine += MachineNumber.R_OPCODE; //opcode for or instruction
					machine += MachineNumber.OR_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.OR_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//XOR INSTRUCTION 
				else if (op.equals(Operation.X_OR)) {
					machine += MachineNumber.R_OPCODE; //opcode for xor instruction
					machine += MachineNumber.XOR_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.XOR_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//SLT INSTRUCTION
				else if (op.equals(Operation.LESS)) {
					machine += MachineNumber.R_OPCODE; //opcode for slt instruction
					machine += MachineNumber.SLT_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.SLT_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//SLTU INSTRUCTION
				else if(op.equals(Operation.LESS_U)) {
					machine += MachineNumber.R_OPCODE; //opcode for sltu instruction
					machine += MachineNumber.SLTU_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.SLTU_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//SRL INSTRUCTION
				else if (op.equals(Operation.SHIFT_RIGHT_L)) {
					machine += MachineNumber.R_OPCODE; //opcode for srl instruction
					machine += MachineNumber.SRL_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.SRL_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//SRA INSTRUCTION
				else if (op.equals(Operation.SHIFT_RIGHT_A)) {
					machine += MachineNumber.R_OPCODE; //opcode for sra instruction
					machine += MachineNumber.SRA_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.SRA_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

				//SLL INSTRUCTION
				else if (op.equals(Operation.SHIFT_LEFT)) {
					machine += MachineNumber.R_OPCODE; //opcode for sll instruction
					machine += MachineNumber.SLL_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					machine += MachineNumber.SLL_FUNCT7 << MachineNumber.FUNCT7_SHAMT;
				}

			}else if (arg2 instanceof Literal) {
				int imm = ((Literal) arg2).get();
				machine += imm << MachineNumber.I_IMM_SHAMT;

				//ADDI INSTRUCTION
				if (op.equals(Operation.ADD)) { 
					machine += MachineNumber.I_OPCODE; //opcode for addi instruction
					machine += MachineNumber.ADDI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				}

				//SLLI INSTRUCTION
				else if (op.equals(Operation.SHIFT_LEFT)) {
					machine += MachineNumber.I_OPCODE; //opcode for slli instruction
					machine += MachineNumber.SLLI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				}

				//SRLI INSTRUCTION
				else if (op.equals(Operation.SHIFT_RIGHT_L)) {
					machine += MachineNumber.I_OPCODE; //opcode for srli instruction
					machine += MachineNumber.SRLI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				}

				//SRAI INSTRUCTION
				else if (op.equals(Operation.SHIFT_RIGHT_A)) {
					machine += MachineNumber.I_OPCODE; //opcode for srli instruction
					machine += MachineNumber.SRAI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
					//turn bit on in high immediate to differentiate b/w srli and srai
					machine += MachineNumber.SRAI_BIT;
				}
				
				//ANDI INSTRUCTION
				else if (op.equals(Operation.AND)) {
					machine += MachineNumber.I_OPCODE; //opcode for srli instruction
					machine += MachineNumber.ANDI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				}

				//ORI INSTRUCTION
				else if (op.equals(Operation.OR)) {
					machine += MachineNumber.I_OPCODE; //opcode for srli instruction
					machine += MachineNumber.ORI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				}
				
				//XORI INSTRUCTION
				else if (op.equals(Operation.X_OR)) {
					machine += MachineNumber.I_OPCODE; //opcode for srli instruction
					machine += MachineNumber.XORI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				}
				
				//SLTI INSTRUCTION
				else if (op.equals(Operation.LESS)) {
					machine += MachineNumber.I_OPCODE; //opcode for srli instruction
					machine += MachineNumber.SLTI_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				}

				//SLTIU INSTRUCTION
				else if(op.equals(Operation.LESS_U)) {
					machine += MachineNumber.I_OPCODE; //opcode for srli instruction
					machine += MachineNumber.SLTIU_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
				} else {
					throw new RuntimeException("Unknown arg in BinOp in Move " + arg2.toString());
				}
			} else {
				throw new RuntimeException("Unknown op in BinOp in Move " + op);
			}
			

		} else if (src instanceof Register) { //Store instruction
			int rs2 = ((Register) src).get();
			machine += rs2 << MachineNumber.RS2_SHAMT;

		} else if (src instanceof MemoryAddress) { //Load instruction
			Abs_Expression address = ((MemoryAddress) src).getAddress();
			int size = ((MemoryAddress) src).getSize();
			int funct3 = 0; //temp

			if (address instanceof BinOp) {

				Operation op = ((BinOp) address).getOp();

				//LW, LB, LH INSTRUCTIONS
				if (op.equals(Operation.ADD)) {

					machine += MachineNumber.L_OPCODE;
					if (size == MachineNumber.W_BYTES) { //lw
						funct3 = MachineNumber.LW_FUNCT3;
					} else if (size == MachineNumber.B_BYTES) { //lb
						funct3 = MachineNumber.LB_FUNCT3;
					} else if (size == MachineNumber.H_BYTES) {//lh
						funct3 = MachineNumber.LH_FUNCT3;
					}

				} else {
					throw new RuntimeException
					("Invalid rel op in BinOp in Memory Address " + op);
				}

				Abs_Expression arg1 = ((BinOp) address).getArg1();
				if (arg1 instanceof Literal) {
					int imm = ((Literal) arg1).get();
					machine += imm << MachineNumber.I_IMM_SHAMT;
					machine += funct3 << MachineNumber.FUNCT3_SHAMT;
				} else {
					throw new RuntimeException
					("Incorrect arg type for load instruction " +
							arg1.toString());
				}

				Abs_Expression arg2 = ((BinOp) address).getArg2();
				if (arg2 instanceof Register) {
					int rs1 = ((Register) arg2).get();
					machine += rs1 << MachineNumber.RS1_SHAMT;
				}
				else {
					throw new RuntimeException
					("Incorrect arg type for load instruction " + 
							arg2.toString());
				}
			} else {
				throw new RuntimeException
				("Unknown kind of address in MemAdd " + 
						address.toString());
			}
		} else {
			throw new RuntimeException
			("Unknown abstract expression in Move " + 
					src.toString());
		}

		return machine;
	}

	public static int branchCode(Branch i) {
		int machine = 0;
		Abs_Expression relOp = ((Branch) i).getRelOp();
		if (relOp instanceof BinOp) {

			Operation op = ((BinOp) relOp).getOp();

			//BEQ INSTRUCTION
			if (op.equals(Operation.EQUAL)) {
				machine += MachineNumber.B_OPCODE;
				machine += MachineNumber.BEQ_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
			}

			//BNE INSTRUCTION
			else if (op.equals(Operation.NOT_EQUAL)) {
				machine += MachineNumber.B_OPCODE;
				machine += MachineNumber.BNE_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
			}

			//BLT INSTRUCTION
			else if (op.equals(Operation.LESS)) {
				machine += MachineNumber.B_OPCODE;
				machine += MachineNumber.BLT_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
			}

			//BLTU INSTRUCTION
			else if (op.equals(Operation.LESS_U)) {
				machine += MachineNumber.B_OPCODE;
				machine += MachineNumber.BLTU_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
			}


			//BGE INSTRUCTION
			else if (op.equals(Operation.GREATER_EQUAL)) {
				machine += MachineNumber.B_OPCODE;
				machine += MachineNumber.BGE_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
			}

			//BGEU INSTRUCTION
			else if (op.equals(Operation.GREATER_EQUAL_U)) {
				machine += MachineNumber.B_OPCODE;
				machine += MachineNumber.BGEU_FUNCT3 << MachineNumber.FUNCT3_SHAMT;
			} else {
				throw new RuntimeException("Invalid RelOp in Branch " + op);
			}

			Abs_Expression arg1 = ((BinOp) relOp).getArg1();
			if (arg1 instanceof Register) {
				int rs1 = ((Register) arg1).get();            
				machine += rs1 << MachineNumber.RS1_SHAMT;
			} else {
				throw new RuntimeException
				("Invalid argument type in BinOp in Branch " + 
						arg1.toString());
			}

			Abs_Expression arg2 = ((BinOp) relOp).getArg2();
			if (arg2 instanceof Register) {
				int rs2 = ((Register) arg2).get();
				machine +=  rs2 << MachineNumber.RS2_SHAMT;
			} else {
				throw new RuntimeException
				("Invalid argument type in BinOp in Branch " + 
						arg2.toString());
			}
		}

		Abs_Expression dest = ((Branch) i).getDestination();
		if (dest instanceof Symbol) {
			String s = ((Symbol) dest).get();
			int linePos = _st.getCodeLine(s);
			
			machine += (linePos & MachineNumber.BIT_12_MASK_INT)
					<< MachineNumber.BIT_12_SHAMT;
			
			machine += (linePos & MachineNumber.BITS_10TO5_MASK_INT)
					<< MachineNumber.BITS_10TO5_SHAMT;

			machine += (linePos & MachineNumber.BITS_4TO1_MASK_INT)
					<< MachineNumber.BITS_4TO1_SHAMT;

			machine += (linePos & MachineNumber.BIT_11_MASK)
					<< MachineNumber.B_BIT_11_SHAMT;

		} else {
			throw new RuntimeException
			("Invalid destination type in Branch " +
					dest.toString());
		}
		return machine;
	}

	public static int callCode(Call i) {
		int machine = 0;
		Abs_Expression symbol = i.getSymbol(); 

		if (symbol instanceof Symbol) {
			machine += MachineNumber.JAL_OPCODE; //jal opcode
			String s = ((Symbol) symbol).get();
			int linePos = _st.getCodeLine(s);
			
			machine += (linePos & MachineNumber.BIT_12_MASK_INT)
					<< MachineNumber.BIT_12_SHAMT;

			machine += (linePos & MachineNumber.BITS_19TO12_MASK_INT)
					<< MachineNumber.UPPER_MIDDLE_BITS_SHAMT;

			machine += (linePos & MachineNumber.BITS_10TO1_MASK_INT) 
					<< MachineNumber.BITS_10TO1_SHAMT;

			machine += (linePos & MachineNumber.BIT_11_MASK)
					<< MachineNumber.J_BIT_11_SHAMT;
			
		} else {
			throw new RuntimeException
			("Invalid symbol type in Call " + symbol.toString());
		}

		return machine;
	}

	public static int labelCode(Label i) {
		Abs_Instruction instruct = i.getInstruction();
		return parseInstruction(instruct);
	}

	public static int jumpCode(Jump i) {
		int machine = 0;
		Abs_Expression dest = i.getDestination();

		if (dest instanceof Symbol) {
			machine += MachineNumber.JAL_OPCODE; //jal opcode
			String s = ((Symbol) dest).get();
			int linePos = _st.getCodeLine(s);

			machine += (linePos & MachineNumber.BIT_12_MASK_INT)
					<< MachineNumber.BIT_12_SHAMT;

			machine += (linePos & MachineNumber.BITS_19TO12_MASK_INT)
					<< MachineNumber.UPPER_MIDDLE_BITS_SHAMT;

			machine += (linePos & MachineNumber.BITS_10TO1_MASK_INT)
					<< MachineNumber.BITS_10TO1_SHAMT;

			machine += (linePos & MachineNumber.BIT_11_MASK)
					<< MachineNumber.J_BIT_11_SHAMT;
		} else {
			throw new RuntimeException
			("Invalid destination type in Jump " + 
					dest.toString());
		}

		return machine;
	}

	public static int returnCode(Return i) {
		int machine = 0;
		machine += MachineNumber.JALR_OPCODE; //jalr opcode
		int ra = RA.get();
		machine += ra << MachineNumber.RA_SHAMT;
		return machine;
	}

}