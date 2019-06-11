package window;

import java.util.ArrayList;

import org.fxmisc.richtext.CodeArea;

import javafx.scene.text.Text;

public class InOutErrorField {

	private CodeArea log = new CodeArea(DEFAULT_ERROR_MESSAGE);
	private CodeArea input = new CodeArea(DEFAULT_INPUT_MESSAGE);
	private static final String DEFAULT_ERROR_MESSAGE = "No program running;";
	private static final String DEFAULT_INPUT_MESSAGE = "Input goes here;";
	private ArrayList<Text> _messages;
	private int errorCount;
	
	public InOutErrorField() {
		log.setEditable(false);
		log.setId("error-field");
		log.setPrefHeight(100);
		input.setId("input-field");
		input.setPrefHeight(100);
		//add function/event handler so that when focus on input, default message goes away?
		_messages = new ArrayList<Text>();
		errorCount = 0;
	}
	
	CodeArea getErrorField() {
		return log;
	}
	
	CodeArea getInputField() {
		return input;
	}
	
	//to report an error happening - if line number is given, highlights the line in code where the error happens
	public void reportError(String message, Integer line, Highlighter method) {
		String displayMessage;
		if (line == null || line == 0) {
			displayMessage = "Error: " + message + ";\n";
		} else {
			displayMessage = "Error: " + message + " @line " + line + ";\n";
			method.colorLine(line);
		}
		Text s = new Text(displayMessage);
		s.setStyle(".error-text");
		_messages.add(s);
		errorCount++;
		showOut();
	}
	
	public void printOutput(String message) {
		Text m = new Text(message + "; \n");
		_messages.add(m);
		showOut();
	}

	
	//for use to show errors after program is done running (or something like that)
	public void logErrors(String form) {
		String countMessage = form + " finished: " + errorCount + " errors found\n";
		Text m = new Text(countMessage);
		_messages.add(m);
		showOut();
		errorCount = 0;
		_messages.clear();
	}
	
	public void clear() {
		_messages.clear();
		log.clear();
		log.replaceText(0,0,DEFAULT_ERROR_MESSAGE);
		errorCount = 0;
		input.clear();
		input.replaceText(0,0,DEFAULT_INPUT_MESSAGE);
	}
	
	public void showOut() {
		String displayMessage = "";
		for (Text m : _messages) {
			displayMessage += m.getText();
		}
		log.clear();
		log.replaceText(0,0,displayMessage);
	}

	
	//TODO: adjust inputMode, etc. so that eventually it returns the input text. 
	//I think in order to do that, I'll need to declare the handler in MainTest. 
//	public String inputMode(EventHandler<KeyEvent> handler) {
//		input.addEventHandler(KeyEvent.KEY_PRESSED, handler);
//	}
//	
//	public void inputOff(EventHandler<KeyEvent> handler) {
//		input.removeEventHandler(KeyEvent.KEY_PRESSED, handler);
//	}
//	
//	private EventHandler<KeyEvent> handler = event -> {
//				String code = event.getCode().toString();
//				if (code.equals("ENTER")) {
//					getInput();
//				}
//			};
	
	public String getInput() {
		return input.getText();
	}
	
	public int getIntInput() {
		int ret = 0;
		try {
			ret = Integer.parseInt(input.getText());
		} catch (NumberFormatException e) {
			printOutput("Error: expected decimal input: " + input.getText());
		}
		return ret;
	}
	
	public interface Highlighter {
		public void colorLine(int i);
	}
}

