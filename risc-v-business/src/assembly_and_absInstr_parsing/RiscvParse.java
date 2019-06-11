package assembly_and_absInstr_parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import operation_syntax.RiscOperations;
import register_syntax.RegisterException;
import register_syntax.RiscRegisters;
import operation_syntax.Operation;
import operation_syntax.OperationSpec;
import operation_syntax.RiscOperations;
import util.Pair;

public class RiscvParse {
	
	//TODO: labels are not included in abstract instructions
	
	private static final Pattern REG_REG_REG =
			Pattern.compile("(\\w+)\\s*,\\s*(\\w+)\\s*,\\s*(\\w+)");  // t0, t1, t2
	private static final Pattern REG_REG_LIT =
			Pattern.compile("(\\w+)\\s*,\\s*(\\w+),\\s*(-?\\d+)");  // t0, t1, 37
	private static final Pattern REG_LIT_REG = 
			Pattern.compile("(\\w+)\\s*,\\s*(-?\\d+)\\s*\\(\\s*(\\w+)\\s*\\)");  // t0, -48(t1)
	private static final Pattern REG_SYMBOL =
			Pattern.compile("(\\w+)\\s*,\\s*(\\w+)");  // t0, loop_exit 
	private static final Pattern SYMBOL =
			Pattern.compile("(\\w+)");

	//TODO: possibly create list of pairs of integers representing source and valid code lines?
	
	private static OperationSpec ops = new RiscOperations();

	public static Pair<SymbolTable, List<Pair<Abs_Instruction, Integer>>> parse(String source)
			throws ParseException {

		
		List<Pair<Abs_Instruction, Integer>> riscCodes = 
				new ArrayList<Pair<Abs_Instruction, Integer>>();

		Pair<SymbolTable, List<Pair<String, Integer>>> p = 
				Preprocess.preprocess(source);		
		
		SymbolTable symbolTable = p.first();
		List<Pair<String, Integer>> codeLines = p.second();
		
		for (Pair<String, Integer> codePair : codeLines) {
			Abs_Instruction ai = parseInstruction(codePair.first(), codePair.second());
			riscCodes.add(new Pair<Abs_Instruction, Integer>(ai, codePair.second()));
		}

		return new Pair<SymbolTable, List<Pair<Abs_Instruction, Integer>>>(symbolTable, riscCodes);
	}

	public static Abs_Instruction parseInstruction(String code, int sourceLine) throws ParseException {
		Pattern p = Pattern.compile("^([a-z]+)\\b(.*)"); // split into command and rest of line
		Matcher m = p.matcher(code);

		if (m.matches()) {
			String command = m.group(1);
			String rest = m.group(2);
			rest = rest.trim();

			if (command.equals("add") || command.equals("sub") || 
					command.equals("sll") || command.equals("srl") || 
					command.equals("sra") || command.equals("xor") ||
					command.equals("and") || command.equals("or") || 
					command.equals("slt") || command.equals("sltu")) {
				return parseRRR(command, rest, sourceLine);	

			} else if (command.equals("addi") || command.equals("slli") ||
					command.equals("srli") || command.equals("srai") ||
					command.equals("slti") || command.equals("ori") ||
					command.equals("andi") || command.equals("xori") ||
					command.equals("sltiu")) {
				return parseRRL(command, rest, sourceLine);

			} else if (command.equals("beq") || command.equals("bne") ||
					command.equals("blt") || command.equals("bge") ||
					command.equals("bgeu") || command.equals("bltu")) {
				return parseRRS(command, rest, sourceLine);

			} else if (command.equals("jal")) {
				return parseJAL(command, rest, sourceLine);

			} else if (command.equals("jalr")) {
				return parseJALR(command, rest, sourceLine);
				
			} else if (command.equals("lw") || command.equals("lb") || 
					command.equals("lh")) {
				return parseLW(command, rest, sourceLine);

			} else if (command.equals("sw") || command.equals("sb") || 
					command.equals("sh")) {
				return parseSW(command, rest, sourceLine);

			} else if (command.equals("stop")) {
				return parseStop(command, rest, sourceLine);

			} else if (command.equals("print")) {
				return parsePrint(command, rest, sourceLine);

			} else {
				throw new ParseException("invalid command: " + command, sourceLine);
			}
		} else {
			throw new ParseException("invalid characters at start of code line ", sourceLine);
		}
	}


	private static Abs_Instruction parseRRR(String command, String rest, int sourceLine) 
			throws ParseException {
		
		Matcher rrrMatcher = REG_REG_REG.matcher(rest);
		if (rrrMatcher.matches()) {
			String rd = rrrMatcher.group(1);
			String rs1 = rrrMatcher.group(2);
			String rs2 = rrrMatcher.group(3);

			Abs_Expression source = new BinOp(parseOp(command),
					parseRegister(rs1, sourceLine), parseRegister(rs2, sourceLine));

			return new Move(parseRegister(rd, sourceLine), source);
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}
	}

	private static Abs_Instruction parseRRL(String command, String rest, int sourceLine) 
			throws ParseException {

		Matcher rrlMatcher = REG_REG_LIT.matcher(rest);
		if (rrlMatcher.matches()) {
			String rd = rrlMatcher.group(1);
			String rs1 = rrlMatcher.group(2);
			String imm = rrlMatcher.group(3);

			Abs_Expression source = new BinOp(parseOp(command),
					parseRegister(rs1, sourceLine), parseLiteral(imm));

			return new Move(parseRegister(rd, sourceLine), source);
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}

	}


	private static Abs_Instruction parseRRS(String command, String rest, int sourceLine) 
			throws ParseException {

		Matcher rrsMatcher = REG_REG_REG.matcher(rest);
		if (rrsMatcher.matches()) {
			String rs1 = rrsMatcher.group(1);
			String rs2 = rrsMatcher.group(2);
			String sym = rrsMatcher.group(3);

			Abs_Expression source = new BinOp(parseOp(command),
					parseRegister(rs1, sourceLine), parseRegister(rs2, sourceLine));

			return new Branch(source, parseSymbol(sym));
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}

	}


	private static Abs_Instruction parseJAL(String command, String rest, int sourceLine) 
			throws ParseException {

		Matcher rsMatcher = REG_SYMBOL.matcher(rest);
		if (rsMatcher.matches()) {
			String rs1 = rsMatcher.group(1);
			String symbol = rsMatcher.group(2);
			
			if (rs1.equals("zero")) {
				Abs_Expression sym = parseSymbol(symbol);
				return new Jump(sym);
			} else if (rs1.equals("ra")){
				Abs_Expression source = parseSymbol(symbol);
				return new Call(source);
			} else {
				throw new ParseException("command not yet implemented: " + command, sourceLine);
			}
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}

	}

	private static Abs_Instruction parseJALR(String command, String rest, int sourceLine) 
		throws ParseException {
		
		Matcher rrlMatcher = REG_REG_LIT.matcher(rest);
		if (rrlMatcher.matches()) {
			String rs1 = rrlMatcher.group(1);
			String rs2 = rrlMatcher.group(2);
			int offset = Integer.parseInt(rrlMatcher.group(3));
			
			if (rs1.equals("zero") && rs2.equals("ra") && offset == 0) {
				return new Return();
			} else {
				throw new ParseException("command not yet implemented: " + command, sourceLine);
			}
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}


	}

	private static Abs_Instruction parseLW(String command, String rest, int sourceLine) 
			throws ParseException {

		Matcher rlrMatcher = REG_LIT_REG.matcher(rest);
		if (rlrMatcher.matches()) {
			String rd = rlrMatcher.group(1);
			String offset = rlrMatcher.group(2);
			String rs1 = rlrMatcher.group(3);
			
			Abs_Expression source = new MemoryAddress(
									new BinOp(Operation.ADD, parseLiteral(offset), parseRegister(rs1, sourceLine)), 
									addressBytes(command)
									);
			
			return new Move(parseRegister(rd, sourceLine), source);
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}
	}

	private static Abs_Instruction parseSW(String command, String rest, int sourceLine) 
			throws ParseException {

		Matcher rlrMatcher = REG_LIT_REG.matcher(rest);
		if (rlrMatcher.matches()) {
			String rd = rlrMatcher.group(1);
			String offset = rlrMatcher.group(2);
			String rs1 = rlrMatcher.group(3);
			
			Abs_Expression source = new MemoryAddress(
					new BinOp(Operation.ADD, parseLiteral(offset), parseRegister(rs1, sourceLine)), 
					addressBytes(command));
			
			return new Move(source, parseRegister(rd, sourceLine));
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}
	}


	private static Abs_Instruction parseStop(String command, String rest, int sourceLine) 
			throws ParseException {

		if (rest.isEmpty()) {
			Abs_Expression stop = new Command(command);
			return new SystemCall(stop);
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}
	}


	//TODO: get updated SystemCall so print can be implemented
	private static Abs_Instruction parsePrint(String command, String rest, int sourceLine) 
			throws ParseException {

		Matcher sMatcher = SYMBOL.matcher(rest);
		if (sMatcher.matches()) {
			return null;
		} else {
			throw new ParseException("bad syntax for command: " + command, sourceLine);
		}
	}

	private static Abs_Expression parseRegister(String r, int sourceLine) 
			throws ParseException {

		final RiscRegisters REGISTERS = new RiscRegisters();  // TODO: clean up
		try {
			return new Register(REGISTERS.index(r));
		} catch (RegisterException e) {
			throw new ParseException("invalid register: " + r, sourceLine);
		}
	}

	private static Abs_Expression parseLiteral(String imm) {
		int numericVal = Integer.parseInt(imm);
		return new Literal(numericVal);
	}

	private static Abs_Expression parseSymbol(String symbol) {
		//TODO: make symbol access better
		return new Symbol(symbol);
	}
	
	private static int addressBytes(String f) {
		if (f.equals("lw") || f.equals("sw")) {
			return 4;
		} else if (f.equals("lh") || f.equals("sw")) {
			return 2;
		} else if (f.equals("lb") || f.equals("sb")) {
			return 1;
		} else {
			return 0;
		}
	}
	
	private static Operation parseOp(String op) {
		return ops.opOf(op);
	}
}