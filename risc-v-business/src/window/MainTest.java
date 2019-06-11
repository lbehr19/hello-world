package window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import operation_syntax.OperationException;
import register_syntax.RegisterException;
import util.Pair;
import assembly_and_absInstr_parsing.*;
import abstract_instructions.*;
import abs_and_machine_parsing.*;
import virtual_machine.*;

/**
 * This class launches the application and deals with the graphics. 
 * load/save file functions retrieved from java-buddy.blogspot.com (without permission)
 * rich text editor supplied by RichTextFX on GitHub
 * @version 6.8.18
 *
 */
public class MainTest extends Application {
	
	private Font rFont = Font.font("Courier New", FontWeight.BOLD, 10); //font for registers - might set through CSS
	private boolean hexOrBin = false; //set true if output is in hex
	private boolean memView = true; //true if memory is showing, false if registers showing
	private boolean errView = false; //true if the error box is showing, false if input box showing
	//Note that the functions which toggle errView and memView are called immediately upon initializing the window, 
	//so they will appear in the window as the opposite of their initial values. 
	
	private CodeArea _input = new CodeArea("input");
	private CodeArea _output = new CodeArea("output");
	private TextArea _middle = new TextArea();
	
	public static InOutErrorField iof = new InOutErrorField();

	assembly_and_absInstr_parsing.SymbolTable st = new assembly_and_absInstr_parsing.SymbolTable();
	SymbolTable symTab = new SymbolTable(makeTableClickHandler());
	VirtualMachine vm = new RiscVM();
	
	Execution exec = new Execution(vm); 
	RegisterGraphics regFile = new RegisterGraphics(iof, vm);
	MemoryGraphics memory = new MemoryGraphics(iof, vm);
	
	private Button switchMemReg;
	private Button switchMachineOutput;
	private Button switchInputOutput;
	HBox stepBox = new HBox();
	
	String firstLine = ""; //to compare with _input text, so that assemble only happens when text is modified
	String lastLine = ""; //to compare with _output text, so that disassemble only happens when text is modified
	
	public static void main(String[] args) {
		launch();
	}

	
	@Override
	public void start(Stage arg0) throws Exception {
		Stage stage = new Stage();
		BorderPane window = new BorderPane();
		
		//Text editor section: the left shows the assembly language code, 
		//while the right displays machine code in either bits or hex digits
		BorderPane editor = new BorderPane();
		editor.setPrefSize(800, 450);
		
		_input.setParagraphGraphicFactory(LineNumberFactory.get(_input));
		_output.setParagraphGraphicFactory(LineNumberFactory.get(_output));
		
		VirtualizedScrollPane<CodeArea> inPane = new VirtualizedScrollPane<>(_input);
		inPane.setPrefWidth(300);
		
		//Initialize button to be added to _output's StackPane
		switchMachineOutput = new Button("0x0a");
		switchMachineOutput.setOnAction(event -> {
			String source = _output.getText();
			if (hexOrBin) {
				setText(_output, changeToBin(source));
				switchMachineOutput.setText("0x0a");
			} else {
				setText(_output, changeToHex(source));
				switchMachineOutput.setText("1010");
			}
		});
		StackPane.setAlignment(switchMachineOutput, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(switchMachineOutput, new Insets(5));
		StackPane outButPane = new StackPane();
		VirtualizedScrollPane<CodeArea> outPane = new VirtualizedScrollPane<>(_output);
		outPane.setPrefWidth(300);
		outButPane.getChildren().addAll(outPane, switchMachineOutput);

		//Navigation buttons: must be declared somewhere where they can access the window itself, before the rest of the panes get set
		switchMemReg = new Button("R");
		switchMemReg.setOnAction(event -> {
			window.setBottom(regMem());
		});
		StackPane.setAlignment(switchMemReg, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(switchMemReg, new Insets(5));

		switchInputOutput = new Button("I->O");
		switchInputOutput.setOnAction(event -> {
			editor.setBottom(messageFields());
		});
		StackPane.setAlignment(switchInputOutput, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(switchInputOutput, new Insets(5));
		
		editor.setLeft(inPane);
		editor.setRight(outButPane);
		
		_middle.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
		_middle.setText("Abstract Assembly");
		_middle.setStyle("-fx-control-inner-background: #D3D3D3");
		_middle.setEditable(false);
		_middle.setPrefRowCount(25);
		_middle.setScrollLeft(50);
		_middle.setScrollTop(200);
		editor.setCenter(_middle); //TODO: remove _middle and set _input as Center instead
		
		editor.setBottom(messageFields());
		
		HBox top = toolBar(stage);
		//TODO: learn how to use drop-down menus and then set one for "Mode"
		
		window.setCenter(editor);
		//Create tool bar with buttons for manipulating the text in the editor
		window.setTop(top);
		//create register file/memory display area
		window.setBottom(regMem());
		//create symbol table display area
		window.setRight(symtab());
		
		Scene scene = new Scene(window);
		
		_input.setOnKeyPressed(event -> {
			String code = event.getCode().toString();
			//if the stepBox is not currently disabled, and the key pressed was any regular character or ENTER, then disable the stepBox
			if (!stepBox.isDisabled() && !code.equals("UP") && !code.equals("LEFT") && !code.equals("DOWN") && !code.equals("RIGHT")) {
				stepBox.setDisable(true);
			}
		});
		_output.setOnKeyPressed(event -> {
			String code = event.getCode().toString();
			//see _input.setOnKeyPressed. 
			if (!stepBox.isDisabled() && !code.equals("UP") && !code.equals("LEFT") && !code.equals("DOWN") && !code.equals("RIGHT")) {
				stepBox.setDisable(true);
			}
		});
		
		scene.getStylesheets().add(MainTest.class.getResource("/resources/run-window.css").toExternalForm());
		
		stage.setTitle("RISC-y Business");
		stage.setScene(scene);
		
		stage.show();

	}
	//	END START FUNCTION
//_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_=_
	
	//	vv HELPER FUNCTIONS vv
//---------------------------------------------------------------------------------
	//creates tool bar, makes all the fancy buttons
	private HBox toolBar(final Stage stage) {
		HBox tb = new HBox();
		tb.setSpacing(10);
		tb.setPadding(new Insets(10, 12, 10, 12));
		tb.setId("tool-bar");

		//TODO: set hover text to display what the button does
		Button assembler = new Button("Assemble");
		assembler.setOnAction(event -> assemble());
		
		Button disassembler = new Button("Disassemble");
		disassembler.setOnAction(event -> disassemble());

		Button saveButton = new Button("Save");
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				String source = _input.getText();
				FileChooser fileChooser = new FileChooser();

				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
				fileChooser.getExtensionFilters().add(extFilter);

				//TODO: set initial directory somehow!! 
				
				//Show save file dialog
				File file = fileChooser.showSaveDialog(stage);
				if (file != null) {
					SaveFile(source, file);
				}
			}
		});

		Button loadButton = new Button("Load");
		loadButton.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
				FileChooser fileChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
				fileChooser.getExtensionFilters().add(extFilter);

				//TODO: when the software gets packaged for "commercial" use we should find a way to access 
				//the application's directory and set that as starting directory for load/save files. 
				
				//Show load file dialog
				File file = fileChooser.showOpenDialog(stage);
				if(file != null){
					setText(_input, readFile(file));
				}
			}

		});
		
		Button clearButton = new Button("Clear");
		clearButton.setOnAction(event -> clear());
		
		//STEP-BOX BUTTONS: 
		Button stepButton = new Button("Step");
		stepButton.setOnAction(event -> {
			if(vm.hasNext()){
				highlightLine(vm.getPC() + 1);
				highlightMachine(vm.getPC()+1);
				try {
					exec.executeNextInstr();

				} catch (OperationException | MemoryException | RegisterException e) {
					iof.reportError(e.getMessage(), vm.getPC()+1, line -> {
						highlightLine(line);
						highlightMachine(line);
					});
				}
			}
			regFile.updateDisplay();
			memory.display(0);
			iof.logErrors("Program Step");
		});
		
		Button runButton = new Button("Run");
		runButton.setOnAction(event -> {
			while (vm.hasNext()) {
				try {
					exec.executeNextInstr();

				} catch (OperationException | MemoryException | RegisterException e) {
					iof.reportError(e.getMessage(), 0, null);
				}
			}
			regFile.updateDisplay();
			memory.display(0);
			iof.logErrors("Program Execution");	
		});
		
		Button stopButton = new Button("Stop");
		stopButton.setOnAction(event -> {
			vm.endPC();
			regFile.updateDisplay();
			memory.display(0);
		});
		
		Button resetButton = new Button("Reset");
		resetButton.setOnAction(event -> {
				regFile.reset();
				memory.clear();
				unhighlightAll();
				iof.clear();
			});
		
		stepBox.setDisable(true);
		stepBox.getChildren().addAll(runButton, stepButton, resetButton, stopButton);
		stepBox.setAlignment(Pos.CENTER_RIGHT);

		tb.getChildren().addAll(saveButton, loadButton, clearButton, assembler, disassembler);
		tb.getChildren().add(stepBox);
		HBox.setHgrow(stepBox, Priority.ALWAYS);
		return tb;
	}
	
	//makes the box where the messages get printed
	//TODO: either finish the highlighting stuff or remove the comments. 
	private StackPane messageFields() {
		StackPane stack = new StackPane();
		CodeArea inOut = iof.getErrorField();
//		inOut.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
//			int charIdx = e.getCharacterIndex();
//			int parNum = inOut.getCurrentParagraph();
//			int parLen = inOut.getParagraphLength(parNum);
//			String par = inOut.getText(parLen - 3, parLen);
//			//gotta find some way to get the paragraph instead of just the char
//			try {
//				int line = Integer.parseInt(par);
//				highlightLine(line);
//			} catch (NumberFormatException ex) {
//				//do nothing?
//			}
//		});
//		inOut.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
//			unhighlightAll();
//		});
		VirtualizedScrollPane<CodeArea> vPane = new VirtualizedScrollPane<CodeArea>(inOut);
		CodeArea userInput = iof.getInputField();
		VirtualizedScrollPane<CodeArea> inputPane = new VirtualizedScrollPane<CodeArea>(userInput);
		if (errView) {
			errView = !errView;
			stack.getChildren().add(inputPane);
			switchInputOutput.setText("O->I");
		} else {
			errView = !errView;
			stack.getChildren().add(vPane);
			switchInputOutput.setText("I->O");
		}
		stack.getChildren().add(switchInputOutput);
		return stack;
	}
	
//BUTTON-ASSOCIATED FUNCTIONS BELOW vv
//----------------------------------------------------------------------------------------
//assemble and disassemble functions
	
	private void assemble() {
		//Assembly only happens if the text has changed since the last time this function was run. 
		if (!firstLine.equals(_input.getText())) {
			firstLine = _input.getText();
			//Assembly: source string is tokenized and then turned into abstract instruction list
			//TODO: put try/catch around parsing functions
			Pair<assembly_and_absInstr_parsing.SymbolTable, List<Pair<Abs_Instruction, Integer>>> pair;
			try {
				pair = assembly_and_absInstr_parsing.RiscvParse.parse(_input.getText());
				List<Abs_Instruction> instructions = new ArrayList<Abs_Instruction>();
				for (Pair<Abs_Instruction, Integer> p : pair.second()) {
					instructions.add(p.first());
				}
			
				st = pair.first();
				symTab.updateTable(st);
				
				//Abstract instruction list is then turned into string to display, and assembled into machine code for display
				_middle.setText(absToString(instructions));
				setText(_output, abstractAssemble(instructions));
			
				//instructions and symbol table are then loaded into virtual machine and execution, in order to run the program. 
				vm.loadInstructions(instructions);
				exec.loadSymbolTable(st);
				//Now that assembly is complete, housekeeping
				stepBox.setDisable(false);
			} catch (ParseException e) {
				iof.reportError(e.getMessage(), e.getLineNumber(), line -> highlightLine(line));
			}
			iof.logErrors("Assembly");
		}
	}
	
	private void disassemble() {
		//Disassembly only happens if text has changed since the last time this program was run
		if (!lastLine.equals(_output.getText())) {
			lastLine = _output.getText();
			//Disassembly: the machine code source is translated from binary to decimal, 
			//then passed into parser and turned into abstract instructions.
			//TODO: static reference?
			//TODO: put try/catch around machineToAbs().
			String source = _output.getText();
			MachineToAbs abs = new MachineToAbs();
			ArrayList<Abs_Instruction> instructions = abs.machineToAbs(changeToDec(source));
			
			//the abstract instructions are turned into assembly language and prepared for display
			_middle.setText(absToString(instructions));
			setText(_input,abstractDisassemble(instructions));
			
			
//			symTab.updateTable(table);
			
			//instructions and symbol table are then loaded into virtual machine and execution for running the program. 
			vm.loadInstructions(instructions);
			exec.loadSymbolTable(st);
			
			//Now that disassembly is complete, housekeeping. 
			stepBox.setDisable(false);
			iof.logErrors("Disassembly");
		}
	}
	
	private String absToString(List<Abs_Instruction> instructions) {
		String s = "";
		int i = 0;
		for (Abs_Instruction abIn : instructions) {
			i++;
			String line = String.format("%2s", i);
			line += ": " + abIn.toString() + "\n";
			s+= line;
		}
		return s;
	}
	
	//takes the abstract instructions and turns it into string of machine code
	private String abstractAssemble(List<Abs_Instruction> instructions) {
		//TODO: put try/catch around parseAbstracts?
		ArrayList<Integer> machine = AbsToMachine.parseAbstracts(instructions, st);
		String s = "";
		for (int cog : machine) {
			String b = String.format("%32s", Integer.toBinaryString(cog)).replace(' ', '0');
			s+= b + "\n";
		}
		return s;
	}
	
	//takes the abstract instruction array and turns it into assembly language source
	private String abstractDisassemble(ArrayList<Abs_Instruction> absIns) {
		//TODO: static reference?
		//TODO: put try/catch around unparseMain()?
		ArrayList<String> assembled = (new UnparseAbsInstr()).unparseMain(absIns);
		return String.join("", assembled);
	}
	
//Helper functions for save/load files
	
	//changes the String _input into .txt file to save
	private void SaveFile(String content, File file){
		try (FileWriter fileWriter = new FileWriter(file)){
			fileWriter.write(content);
		} catch (IOException ex) {
			//TODO: figure out what the logger does
			Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	//takes the file from FileChooser and reads it into the program
	private String readFile(File file){
		StringBuilder stringBuffer = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
			String text;
			while ((text = bufferedReader.readLine()) != null) {
				stringBuffer.append(text);
			}
		} catch (FileNotFoundException ex) {
			//TODO: maybe take out the logger? Or else, do something with it
			Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
		}

		return stringBuffer.toString();
	}
	
//---------------------------------------------------------------------------------------------
//helpers for binary/hexadecimal conversion
	
	private String changeToHex(String source) {
		int errors = 0;
		String fin = "";
		int i = 0;
		if (source != null && !source.equals("")) {
			String[] t = source.split("\n");
			for (String line : t) {
				i++;
				String bitLine = "0x";
				try {
					int dec = Integer.parseUnsignedInt(line, 2);
					bitLine += String.format("%08x", dec);
				} catch (NumberFormatException e) {
					iof.reportError("invalid machine code: " + line, i, l -> highlightMachine(l));
					errors++;
					break;
				}				
				fin += (bitLine + "\n");
			}
		} else {
			iof.reportError("no code to convert", 0, null);
			errors++;
		}
		if (errors == 0) {
			hexOrBin = !hexOrBin;
		}
		iof.logErrors("Machine Code Conversion");
		return fin;
	}
	
	private String changeToBin(String source) {
		int errors = 0;
		String fin = "";
		int l = 0;
		if (source != null && !source.equals("")) {
			String[] t = source.split("\n");
			for (String line : t) {
				l++;
				line = line.substring(2); //cuts off the "0x" at the beginning
				String bin = "";
				for (int i = 0; i < line.length(); i++) {
					//parses the hex one digit at a time, since one hex digit is 4 binary digits
					String b = String.valueOf(line.charAt(i)); 
					try {
						int num = Integer.parseUnsignedInt(b, 16);
						b = Integer.toBinaryString(num);
					} catch (NumberFormatException e) {
						iof.reportError("invalid machine code: " + line, l, f -> highlightMachine(f));
						errors++;
						break;
					}
					while (b.length() < 4) {
						b = "0" + b;
					}
					bin += b;
				}
				fin += (bin + "\n");
			}
		} else {
			iof.reportError("no code to convert", 0, null);
			errors++;
		}
		if (errors == 0) {
			hexOrBin = !hexOrBin;
		}
		iof.logErrors("Machine Code Conversion");
		return fin;
	}

	private String changeToDec(String source) {
		String s = "";
		String[] t = source.split("\n");
		for (String line : t) {
			int num;
			if (!hexOrBin) {
				num = Integer.parseUnsignedInt(line, 2);
			} else {
				line = line.substring(2);
				num = Integer.parseUnsignedInt(line, 16);
			}
			s += (num + "\n");
		}
		return s;
	}
	
	
	private void clear() {
		symTab.clear();
		_input.clear();
		firstLine = "";
		_output.clear();
		lastLine = "";
		_middle.clear();
		regFile.reset();
		memory.clear();
		iof.clear();
		hexOrBin = false;
		unhighlightAll();
	}
//-------------------------------------------------------------------------------------
//functions that create register file/memory window and symbol table

	private HBox memoryPane() {
		HBox box = new HBox();
		box.setSpacing(10);
		GridPane mem = new GridPane();
		mem.setId("memory-display");
		mem.setGridLinesVisible(true);
		memory.display(0);
		for (int i = 0; i < 4; i++) {
			mem.add(new Text(" address"), (i*5), 0);
			mem.add(new Text(" +3"), i*5+1, 0);
			mem.add(new Text(" +2"), i*5+2, 0);
			mem.add(new Text(" +1"), i*5+3, 0);
			mem.add(new Text(" +0"), i*5+4, 0);
		}
		int addIndex = 0;
		for (int r = 1; r < 6; r++) {
			int i = 0;
			for (int c = 0; c < 20; c++) {
				if (c % 5 == 0) {
					Text display = memory.getAddDisplay(addIndex + (i*5));
					GridPane.setHalignment(display, HPos.CENTER);
					mem.add(display, c, r);
				} else {
					TextField t = memory.getValDisplays(addIndex + (i*5)).get((c%5) - 1);
					mem.add(t, c, r);
					if (c%5 == 4) {
						i++;
					}
				}
			}
			addIndex++;
		}
		mem.getRowConstraints().add(new RowConstraints(20));
		box.getChildren().addAll(mem, memButtons());
		return box;
	}
	
	private GridPane regfile() {
		GridPane rf = new GridPane();
		rf.setPadding(new Insets(10,10,10,10));
		rf.setGridLinesVisible(true);
		rf.setVgap(10);
		int reg = 0;
		int regNumber = 0;
		for (int cols = 0; cols < 16; cols++) {
			for (int rows = 0; rows < 4; rows++) {
				if ((cols % 2)== 0) {
					String t = " r" + regNumber;
					if (regNumber < 10) {
						t = " " + t;
					}
					Text register = new Text(t);
					register.setFont(rFont);
					rf.add(register, cols, rows);
					regNumber++;
				} else {
					TextField r = regFile.getField(reg);
					rf.add(r, cols, rows);
					reg++;
				}
			}
		}
		regFile.updateDisplay();
		return rf;
	}

	private VBox symtab() {
		VBox st = new VBox();
		st.setPadding(new Insets(12, 15, 12, 15));
		st.setStyle("-fx-background-color: #D3D3D3");
		st.setSpacing(10);
		st.setFillWidth(true);
		Label title = new Label("Symbol Table");
		title.getStyleClass().add("label-custom");
		Label prc = new Label("Program Counter");
		prc.getStyleClass().add("label-custom");
		TextField pc = regFile.getField(32);
		st.getChildren().add(prc);
		st.getChildren().add(pc);
		st.getChildren().add(title);
		st.getChildren().add(symTab._table);
		
		return st;
	}
	
	private StackPane regMem() {
		StackPane stack = new StackPane();
		if (memView) {
			stack.getChildren().add(regfile());
			switchMemReg.setText("R");
		} else {
			stack.getChildren().add(memoryPane());
			switchMemReg.setText("M");
		}
		memView = !memView;
		stack.getChildren().add(switchMemReg);
		return stack;
	}
	
//----------------------------------------------------------------------------------------
// Functions that go along with the symbol table, register file/memory
	
	private VBox memButtons() {
		VBox box = new VBox();
		box.setPadding(new Insets(12, 15, 12, 15));
		box.setSpacing(10);
		Label buttons = new Label("Scroll Memory");
		buttons.getStyleClass().add("label-custom");
		
		HBox buttBox = new HBox();
		buttBox.setSpacing(10);
		Button left = new Button("<---");
		left.setOnAction(event -> {
				int currentDisplay = memory.getDisplayIndex();
				if (currentDisplay < (15*4)) {
					memory.display(0);
				} else {
					memory.display((-15)*4);
				}
		});
		Button right = new Button("--->");
		right.setOnAction(event -> {
				int currentDisplay = memory.getDisplayIndex();
				int maxRight = (int) memory.MEMORY_RANGE - (15*4);
				if (currentDisplay > maxRight) {
					memory.display(maxRight);
				} else {
					memory.display(15*4);
				}
			});
		buttBox.getChildren().addAll(left, right);
		
		TextField goTo = new TextField();
		goTo.setFont(rFont);
		goTo.setPromptText("Go to address: ");
		goTo.setOnKeyPressed(event -> {
				if (event.getCode().toString().equals("ENTER")) {
					memory.go(goTo.getText());
				}
			});
		
		
		box.getChildren().addAll(buttons, buttBox, goTo);
		return box;
	}
	
	public void highlightLine(int line) {
		unhighlightAll();
		String source = _input.getText(line) + "\n";
		_input.replaceText(line, 0, line, source.length(), source);
		_input.requestFollowCaret();
		_input.setStyle(line, Collections.singleton("highlight-text"));
	}
	
	public void unhighlightAll() {
		int p = _input.getParagraphs().size();
		for (int i = 0; i < p; i++) {
			_input.setStyle(i, Collections.singleton("unhighlight-text"));
		}
		int o = _output.getParagraphs().size();
		for (int i = 0; i < o; i++) {
			_output.setStyle(i, Collections.singleton("unhighlight-text"));
		}
	}
	
	private void highlightMachine(int line) {
		String source = _output.getText(line) + "\n";
		_output.replaceText(line, 0, line, source.length(), source);
		_output.requestFollowCaret();
		_output.setStyle(line, Collections.singleton("highlight-text"));
	}
	
	private void setText(CodeArea area, String text) {
		area.clear();
		area.replaceText(0, 0, text);
	}
	
//	@SuppressWarnings("unused")
//	private void switchNumbers(int radix) {
//		
//	}
	
	private EventHandler<MouseEvent> makeTableClickHandler() {
		return new EventHandler<MouseEvent>() {
			public void handle(MouseEvent t) {
				@SuppressWarnings("unchecked")
				SymbolTable.SymbolEntry entry = symTab._table.getItems()
						.get(((TableCell<SymbolTable.SymbolEntry, String>)t.getSource()).getIndex());
				String symbol = entry.getSymbol();
				highlightLine(st.getSourceLine(symbol));
				highlightMachine(st.getCodeLine(symbol));
			}
		};
	}
}
