package simple_fun_mode;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import util.Pair;

public enum CharacterIcon {
	//TODO: this class should represent the objects being "stored" and "loaded" on the screen
	
	ASTERISK ('*', "asterisk", Color.RED),
	AMPERSAND ('&', "ampersand", Color.MAGENTA),
	AT ('@', "at", Color.YELLOWGREEN),
	DOLLAR ('$', "dollar", Color.GOLD),
	HASH ('#', "hash", Color.GREY),
	PERCENT ('%', "percent", Color.GOLD),
	LETTER_E ('e', "e", Color.RED),
	LETTER_A ('a', "a", Color.BLUE),
	BACKSLASH ('/', "back", Color.GREY),
	FORWARDSLASH ('\\', "forward", Color.GREY),
	VERTICAL_BAR ('|', "vertical", Color.GREY),
	UNDERSCORE ('_', "under", Color.GREY),
	HYPHEN ('-', "hyphen", Color.GREY),
	PLUS ('+', "plus", Color.YELLOWGREEN),
	QUESTION_MARK ('?', "question", Color.BLUE);
	
	
	//members:
	private final String charSymbol;
	private final String name;
	private final int code;
	private final Color stroke;
	private static final int BLOCK_WIDTH = 25; //this comes from BLOCK_WIDTH in ScreenGrid
	private static final int BLOCK_HEIGHT = 40; //this is from BLOCK_HEIGHT in ScreenGrid
	
	CharacterIcon(char c, String name, Color stroke) {
		this.charSymbol = Character.toString(c);
		this.name = name; //only used in toString method - used instead of charSymbol for clarity
		this.code = (int) c; //the code is the ascii/unicode for the symbol
		this.stroke = stroke;
	}
	
	public String fromCode(int c) {
		return Character.toString((char) c);
	}
	
	//Takes in the gc and draws this CharacterIcon's image
	public void render(GraphicsContext gc, Pair<Integer, Integer> position) {
		gc.setStroke(this.stroke);
		gc.setFill(Color.WHITE);
		gc.setFont(Font.font("Courier New", FontWeight.BOLD, 36));
		gc.setTextBaseline(VPos.TOP);
		gc.fillText(this.charSymbol, position.first()*BLOCK_WIDTH, position.second()*BLOCK_HEIGHT);
		gc.strokeText(this.charSymbol, position.first()*BLOCK_WIDTH, position.second()*BLOCK_HEIGHT);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public int getCode() {
		return this.code;
	}
}
