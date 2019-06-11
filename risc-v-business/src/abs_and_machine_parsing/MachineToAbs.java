package abs_and_machine_parsing;
 
import java.util.ArrayList;
import abstract_instructions.Abs_Expression;
import abstract_instructions.Abs_Instruction;
import abstract_instructions.BinOp;
import abstract_instructions.Branch;
import abstract_instructions.Call;
import abstract_instructions.Command;
import abstract_instructions.Jump;
import abstract_instructions.Literal;
import abstract_instructions.MemoryAddress;
import abstract_instructions.Move;
import abstract_instructions.Register;
import abstract_instructions.Return;
import abstract_instructions.Symbol;
import abstract_instructions.SystemCall;
import operation_syntax.Operation;
import operation_syntax.OperationSpec;
import operation_syntax.RiscOperations;

public class MachineToAbs {	

	OperationSpec ops = new RiscOperations();
	
	public ArrayList<Abs_Instruction> machineToAbs(String source) {
		ArrayList<Integer> data = new ArrayList<Integer>();
		String[] s = source.split("\n");
		for (String str : s) { 
			int num = Integer.parseInt(str);
			data.add(num);
		}
		return constructCode(data);
	}
	
	public ArrayList<Abs_Instruction> constructCode(ArrayList<Integer> raw) {
		Integer[] tokens = null;
		ArrayList<Abs_Instruction> instruct = new ArrayList<Abs_Instruction>();
		for (Integer i : raw) {
			int op = i & MachineNumber.OP_MASK;
			switch (op) {
				case MachineNumber.R_OPCODE: //R instruction
					tokens = rParse(i);
					instruct.add(translateRLineToAbs(tokens));
				    break;
				case MachineNumber.L_OPCODE: // lw, lb, lh
					tokens = iParse(i);
					instruct.add(translateLToAbs(tokens));
					break;
				case MachineNumber.I_OPCODE: //I instruction
					tokens = iParse(i);
					instruct.add(translateImmToAbs(tokens));
					break;
				case MachineNumber.S_OPCODE: //S instruction
					tokens = rParse(i); //since format of R and S exact same, can parse the same
					instruct.add(translateSLineToAbs(tokens)); 
					break;
				case MachineNumber.B_OPCODE: //B instruction
					tokens = bParse(i);
					instruct.add(translateBLineToAbs(tokens));
					break;
				case MachineNumber.JAL_OPCODE: //J instructions (jal)
					tokens = jParse(i);
					instruct.add(translateJALLineToAbs(tokens));
					break;
				case MachineNumber.JALR_OPCODE: //J instruction (jalr)
					tokens = jParse(i); //same structure as jal w/ dif opcode
					instruct.add(translateJRLineToAbs(tokens));
					break;
				case MachineNumber.SYSCALL_OPCODE:
					instruct.add(translateSysCallToAbs());
					break;
				default:
					throw new RuntimeException("Invalid opcode: " + (op) + " " + raw.indexOf(i));
			}
		}
		return instruct;	
	}
	
	public Integer[] rParse(Integer instruct) {
		Integer[] tokens = new Integer[5]; //only 6 tokens in an R format instruction, opcode unnecessary
		
		Integer funct7 = instruct & MachineNumber.FUNCT7_MASK;
		tokens[0] = funct7 >> MachineNumber.FUNCT7_SHAMT;
		
		Integer rs2 = instruct & MachineNumber.RS2_MASK;
		tokens[1] = rs2 >> MachineNumber.RS2_SHAMT;
		
		Integer rs1 = instruct & MachineNumber.RS1_MASK;
		tokens[2] = rs1 >> MachineNumber.RS1_SHAMT;
		
		Integer funct3 = instruct & MachineNumber.FUNCT3_MASK;
		tokens[3] = funct3 >> MachineNumber.FUNCT3_SHAMT;
		
		Integer rd = instruct & MachineNumber.RD_MASK;
		tokens[4] = rd >> MachineNumber.RD_SHAMT;
		
		return tokens;
	}
	
	public Integer[] iParse(Integer instruct) {
		Integer[] tokens = new Integer[6]; //only 5 tokens in an I format instruction, plus srai/srli bit
		Integer imm = instruct & MachineNumber.IMM_MASK;
		tokens[0] = imm >> MachineNumber.I_IMM_SHAMT;
		
		Integer rs1 = instruct & MachineNumber.RS1_MASK;
		tokens[1] = rs1 >> MachineNumber.RS1_SHAMT;
		
		Integer funct3 = instruct & MachineNumber.FUNCT3_MASK;
		tokens[2] = funct3 >> MachineNumber.FUNCT3_SHAMT;
		
		Integer rd = instruct & MachineNumber.RD_MASK;
		tokens[3] = rd >> MachineNumber.RD_SHAMT;
		
		Integer op = instruct & MachineNumber.OP_MASK; //opcode necessary to keep as it differs between I commands
		tokens[4] = op;
		
		Integer srBit = instruct & MachineNumber.SRAI_BIT;
		tokens[5] = srBit;
		
		return tokens;
	}
	
	
	public Integer[] bParse(Integer instruct) {
		Integer[] tokens = new Integer[4]; //only 8 tokens in a B format instruction, immediates combine

		Integer upper_bit = instruct & MachineNumber.BIT_12_MASK_MACHINE;
		upper_bit = upper_bit >> MachineNumber.B_UPPER_BIT_SHAMT; //want highest bit to still represent accurate value
		
		Integer second_upper_bit = instruct & MachineNumber.BIT_11_MASK_B;
		second_upper_bit = second_upper_bit << MachineNumber.SECOND_UPPER_BIT_SHAMT;
		
		Integer middle_bits = instruct & MachineNumber.BITS_10TO5_MASK_MACHINE;
		middle_bits = middle_bits >> MachineNumber.MIDDLE_BITS_SHAMT;
		
		Integer lower_bits = instruct & MachineNumber.BITS_4TO1_MASK_MACHINE; 
		lower_bits = lower_bits >> MachineNumber.B_LOWER_BITS_SHAMT;
		
		Integer imm = upper_bit + second_upper_bit + middle_bits + lower_bits;
		tokens[0] =  imm << MachineNumber.B_IMM_SHAMT; //since we discarded last bit during assembly, need to bring it back

		Integer rs2 = instruct & MachineNumber.RS2_MASK; 
		tokens[1] = rs2 >> MachineNumber.RS2_SHAMT;
		
		Integer rs1 = instruct & MachineNumber.RS1_MASK;
		tokens[2] = rs1 >> MachineNumber.RS1_SHAMT;
		
		Integer funct3 = instruct & MachineNumber.FUNCT3_MASK;
		tokens[3] = funct3 >> MachineNumber.FUNCT3_SHAMT;
		
		return tokens;
	}
	
	public Integer[] jParse(Integer instruct) {
		Integer[] tokens = new Integer[2]; //only 6 tokens in a J format instruction, immediates combine
		
		Integer upper_bit = instruct & MachineNumber.BIT_12_MASK_MACHINE;
		upper_bit = upper_bit >> MachineNumber.J_UPPER_BIT_SHAMT;
		
		Integer upper_middle_bits = instruct & MachineNumber.BITS_19TO12_MASK_MACHINE;
		upper_middle_bits = upper_middle_bits >> MachineNumber.UPPER_MIDDLE_BITS_SHAMT;
		
		Integer bit_11 = instruct & MachineNumber.BIT_11_MASK_J;
		bit_11 = bit_11 >> MachineNumber.BIT_11_SHAMT_MACHINE;
		
		Integer lower_bits = instruct & MachineNumber.BITS_10TO1_MASK_MACHINE;
		lower_bits = lower_bits >> MachineNumber.J_LOWER_BITS_SHAMT;
		
		tokens[0] = upper_bit + upper_middle_bits + bit_11 + lower_bits;
		
		Integer rd = instruct & MachineNumber.RD_MASK;
		tokens[1] = rd >> MachineNumber.RD_SHAMT;
		
		return tokens;
	}
	
	//Currently unused, but will keep in case we add U type instructions
	public Integer[] uParse(Integer instruct) {
		Integer[] tokens = new Integer[1]; //we only really care about the immediate here, assume rd always 0

		Integer imm = instruct & MachineNumber.BITS_31TO12_MASK;
		tokens[0] = imm >> MachineNumber.U_IMM_SHAMT;
		return tokens;
	}
	
	public Abs_Instruction translateRLineToAbs(Integer[] token) {
		Abs_Expression rd = new Register(token[4]);
		String command = null;
		if (token[0] == MachineNumber.ADD_FUNCT7 && 
		    token[3] == MachineNumber.ADD_FUNCT3) {
			command = "add";
		} else if (token[0] == MachineNumber.AND_FUNCT7 && 
				   token[3] == MachineNumber.AND_FUNCT3) {
			command = "and";
		} else if (token[0] == MachineNumber.SRL_FUNCT7 && 
				   token[3] == MachineNumber.SRL_FUNCT3) {
			command = "srl";
		} else if (token[0] == MachineNumber.SLL_FUNCT7 && 
				   token[3] == MachineNumber.SLL_FUNCT3) {
			command = "sll";
		} else if (token[0] == MachineNumber.SUB_FUNCT7 && 
				   token[3] == MachineNumber.SUB_FUNCT3) {
			command = "sub";
		} else if (token[0] == MachineNumber.SRA_FUNCT7 && 
				   token[3] == MachineNumber.SRA_FUNCT3) {
			command = "sra";
		} else if (token[0] == MachineNumber.XOR_FUNCT7 && 
				   token[3] == MachineNumber.XOR_FUNCT3) {
			command = "xor";
		} else if (token[0] == MachineNumber.OR_FUNCT7 && 
				   token[3] == MachineNumber.OR_FUNCT3) {
			command = "or";
		} else if (token[0] == MachineNumber.SLT_FUNCT7 && 
				   token[3] == MachineNumber.SLT_FUNCT3) {
			command = "slt";
		} else if (token[0] == MachineNumber.SLTU_FUNCT7 && 
				   token[3] == MachineNumber.SLTU_FUNCT3) {
			command = "sltu";
		} else {
			throw new RuntimeException("Invalid R type command");
		}
		
		Abs_Expression rs1 = new Register(token[2]);
		Abs_Expression rs2 = new Register(token[1]);
		Abs_Expression source = new BinOp(parseOp(command), rs1, rs2);
		Abs_Instruction a = new Move(rd, source);
		return a;
	}
	
	public Abs_Instruction translateImmToAbs(Integer[] token) {
		String command = null;
		if (token[4] == MachineNumber.I_OPCODE && 
			token[2] == MachineNumber.ADDI_FUNCT3) {
			command = "addi";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.SLLI_FUNCT3) {
			command = "slli";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.SRLI_FUNCT3 &&
				   token[5] == 0) {
			command = "srli";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.SRAI_FUNCT3 &&
				   token[5] == 1) {
			command = "srai";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.SLTI_FUNCT3) {
			command = "slti";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.ORI_FUNCT3) {
			command = "ori";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.XORI_FUNCT3) {
			command = "xori";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.ANDI_FUNCT3) {
			command = "andi";
		} else if (token[4] == MachineNumber.I_OPCODE && 
				   token[2] == MachineNumber.SLTIU_FUNCT3) {
			command = "sltiu";
		} else {
			throw new RuntimeException("Invalid I type command");
		}
		
		Abs_Expression rd = new Register(token[3]);
		Abs_Expression rs1 = new Register(token[1]);
		Abs_Expression imm = new Literal(token[0]);
		Abs_Expression source = new BinOp(parseOp(command), rs1, imm);
		Abs_Instruction a = new Move(rd, source);
		return a;
	}
	
	public Abs_Instruction translateLToAbs(Integer[] token) {
		String command = "";
		int bytes;
		if (token[2] == MachineNumber.LW_FUNCT3) {
			command = "lw"; //LW
			bytes = MachineNumber.W_BYTES;
		} else if (token[2] == MachineNumber.LB_FUNCT3) {
			command = "lb";
			bytes = MachineNumber.B_BYTES;
		} else if (token[2] == MachineNumber.LH_FUNCT3) {
			command = "lh";
			bytes = MachineNumber.H_BYTES;
		} else {
			throw new RuntimeException("Invalid L type command");
		}
		
		Abs_Expression rd = new Register(token[3]);
		Abs_Expression imm = new Literal(token[0]);
		Abs_Expression rs1 = new Register(token[1]);
		Abs_Expression binop = new BinOp(parseOp(command), imm, rs1);
		Abs_Expression source = new MemoryAddress(binop, bytes);
		Abs_Instruction a = new Move(rd, source);
		return a;
	}
	
	public Abs_Instruction translateSLineToAbs(Integer[] token) {
		String command = null;
		int bytes;
		if (token[3] == MachineNumber.SW_FUNCT3) {
			command = "sw"; //SW
			bytes = MachineNumber.W_BYTES;
		} else if (token[3] == MachineNumber.SB_FUNCT3) {
			command = "sb";
			bytes = MachineNumber.B_BYTES;
		} else if (token[3] == MachineNumber.SH_FUNCT3) {
			command = "sh";
			bytes = MachineNumber.H_BYTES;
		}else {
			throw new RuntimeException("Invalid S type command: " + command);
		}
		
		Abs_Expression rs2 = new Register(token[1]);
		Abs_Expression imm = new Literal(token[4] + token[0]);
	    Abs_Expression rs1 = new Register(token[2]);
		Abs_Expression binop = new BinOp(parseOp(command), imm, rs1);
		Abs_Expression source = new MemoryAddress(binop, bytes);
		Abs_Instruction a = new Move(rs2, source);
		return a;
	}
	
	public Abs_Instruction translateBLineToAbs(Integer[] token) {
		String command = null;
		if (token[3] == MachineNumber.BEQ_FUNCT3) {
			command = "beq";
		} else if (token[3] == MachineNumber.BNE_FUNCT3) {
			command = "bne";
		} else if (token[3] == MachineNumber.BLT_FUNCT3) {
			command = "blt";
		} else if (token[3] == MachineNumber.BGE_FUNCT3) {
			command = "bge";
		} else if (token[3] == MachineNumber.BGEU_FUNCT3) {
			command = "bgeu";
		} else if (token[3] == MachineNumber.BLTU_FUNCT3) {
			command = "bltu";
		} else {
			throw new RuntimeException("Invalid B type command " +  token[3]);
		}
		
		Abs_Expression rs1 = new Register(token[2]);
		Abs_Expression rs2 = new Register(token[1]); 
		Abs_Expression imm = new Literal(token[0]); 
		Abs_Expression symbol = new Symbol("line" + imm);
		Abs_Expression binop = new BinOp(parseOp(command), rs1, rs2);
		Abs_Instruction branch = new Branch(binop, symbol);
		return branch;
	}
	
	public Abs_Instruction translateJALLineToAbs(Integer[] token) {
		//"jal" instruction
		
		Abs_Expression imm = new Literal(token[0]);
		Abs_Expression newLabel = new Symbol("line" + imm); //convention of making all labels "line[linenum]" easier for parse
		Abs_Instruction call = new Call(newLabel);
		return call;
	}
	
	public Abs_Instruction translateJLineToAbs(Integer[] token) {
		//"j" instruction
		
		Abs_Expression imm = new Literal(token[0]);
		Abs_Expression newLabel = new Symbol("line" + imm);
		Abs_Instruction jump = new Jump(newLabel);
		return jump;
	}
	
	public Abs_Instruction translateJRLineToAbs(Integer[] token) {
		//"jr" instruction
		
		Abs_Instruction ret = new Return();
		return ret;	
	}
	
	public Abs_Instruction translateSysCallToAbs() {	
		return new SystemCall( new Command("") );
	}	
	

	private Operation parseOp(String op) {
		return ops.opOf(op);
	}
}
