package game;

import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Console 
{

	FlowPane pane;
	String welcome = "Welcome to my Dungeon! Use the arrow keys to move, space to attack nearby enemies.";
	private static int MAX_LINE_LEN = 28;
	private Font cFont = Font.font("Courier New", FontWeight.NORMAL, 14);
	
	public Console() {
		pane = new FlowPane();
		pane.setPrefWidth(250);
		pane.setPrefHeight(600);
		pane.setStyle("-fx-background-color: #1A5C09");
		printMessage(welcome);
	}
	
	public FlowPane getPane() {
		return pane;
	}
	
	public void printMessage(String text) {
		Text message = new Text(formatMessage(text));
		message.setFont(cFont);
		message.setFill(Color.GREENYELLOW);
		pane.getChildren().add(message);
	}
	
	private String formatMessage(String text) {
		String[] words = text.split(" ");
		String message = words[0];
		int lineCharLength = message.length();
		for (int i = 1; i < words.length; i++) {
			String nextWord = words[i];
			int currentLineLen = lineCharLength + nextWord.length() + 1;
			if (currentLineLen > MAX_LINE_LEN) {
				message += ("\n" + nextWord);
				lineCharLength = nextWord.length();
			} else {
				message += (" " + nextWord);
				lineCharLength = currentLineLen;
			}
		}
		return message;
	}
	
	public void clearMessages() {
		pane.getChildren().clear();
		//optional - add welcome message back in
	}
}
