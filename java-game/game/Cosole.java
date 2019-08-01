package game;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class Console 
{

	FlowPane pane;
	
	public Console() {
		pane = new FlowPane();
		pane.setPrefWidth(250);
		pane.setPrefHeight(600);
		//set pane styling stuff
		//add initial message (instructions?)
	}
	
	public FlowPane getPane() {
		return pane;
	}
	
	public void printMessage(String text) {
		Text message = new Text(text);
		//style message here
		pane.getChildren().add(message);
	}
	
	public void clearMessages() {
		pane = new FlowPane();
		//re-style pane here
	}
}
