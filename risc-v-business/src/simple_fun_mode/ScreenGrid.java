package simple_fun_mode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import util.Pair;

public class ScreenGrid extends Application {
	
	private final static int SCREEN_WIDTH = 750; //width of the stage (simulated screen)
	private final static int SCREEN_HEIGHT = 600; //height of the stage (simulated screen)
	private final static int BLOCK_WIDTH = 25; //size of the block units
	private final static int BLOCK_HEIGHT = 40;
	private final static int COLUMNS = SCREEN_WIDTH/BLOCK_WIDTH; //number of total columns (max x pos)
	private final static int ROWS = SCREEN_HEIGHT/BLOCK_HEIGHT; //number of total rows (max y pos)
	private final static int MEMORY_SIZE = COLUMNS * ROWS; //total size of the underlying "memory" structure
	
	private Map<Integer, CharacterIcon> memory = new HashMap<Integer, CharacterIcon>(MEMORY_SIZE);
	private Stage screen = new Stage();
	Canvas canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
	
	public static void main(String[] args) {
		launch();
	}
	
	//FOR TESTING
	@Override
	public void start(Stage arg0) throws Exception {
		screen = new Stage();
		Group root = new Group();
		Scene base = new Scene(root);
		screen.setScene(base);
		
		canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
		root.getChildren().add(canvas);
		
		int code = CharacterIcon.PERCENT.getCode();
		System.out.println(code);
		poke(code, 0);
		code = CharacterIcon.HASH.getCode();
		System.out.println(code);
		poke(code, 1);
		code = CharacterIcon.LETTER_A.getCode();
		System.out.println(code);
		poke(code, 2);
		code = CharacterIcon.AT.getCode();
		System.out.println(code);
		poke(code, 3);
		code = CharacterIcon.ASTERISK.getCode();
		System.out.println(code);
		poke(code, 4);
		code = CharacterIcon.DOLLAR.getCode();
		System.out.println(code);
		poke(code, 5);
		code = CharacterIcon.BACKSLASH.getCode();
		System.out.println(code);
		poke(code, 6);
		code = CharacterIcon.FORWARDSLASH.getCode();
		System.out.println(code);
		poke(code, 7);
		code = CharacterIcon.VERTICAL_BAR.getCode();
		System.out.println(code);
		poke(code, 8);
		code = CharacterIcon.UNDERSCORE.getCode();
		System.out.println(code);
		poke(code, 9);
		code = CharacterIcon.HYPHEN.getCode();
		System.out.println(code);
		poke(code, 10);
		code = CharacterIcon.PLUS.getCode();
		System.out.println(code);
		poke(code, 11);
		code = CharacterIcon.LETTER_E.getCode();
		System.out.println(code);
		poke(code, 12);
		code = CharacterIcon.QUESTION_MARK.getCode();
		System.out.println(code);
		poke(code, 13);
		code = CharacterIcon.AMPERSAND.getCode();
		System.out.println(code);
		poke(code, 14);
		
		GraphicsContext gc = canvas.getGraphicsContext2D();
		render(gc);
		
		screen.setTitle("RISC-y Business: SimpleScreen view");
		screen.show();
	}
	
	public void showScreenGrid() {
		Group root = new Group();
		
		root.getChildren().add(canvas);

		Scene base = new Scene(root);
		screen.setScene(base);
		screen.setTitle("RISC-y Business: SimpleScreen view");
		screen.show();
	}
	
	public GraphicsContext getGC() {
		return canvas.getGraphicsContext2D();
	}
	
	public void render(GraphicsContext gc) {
		gc.setFill(Color.web("#143800"));
		gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		Set<Integer> keyValues = memory.keySet();
		for (int i : keyValues) {
			CharacterIcon icon = memory.getOrDefault(i, null);
			int x = i % COLUMNS;
			int y = i / ROWS;
			Pair<Integer, Integer> position = new Pair<Integer, Integer>(x, y);
			icon.render(gc, position); 
			//Note: icon.render may need to use fillText in addition to strokeText;
			//if so, don't forget to set fill to WHITE prior to using this function. 
		}
	}
	
	//Given the address, return the icon stored at that address
	public int peek(int address) {
		CharacterIcon icon = memory.get(address);
		return icon.getCode();
	}
	
	public void poke(int iconCode, int address) {
		CharacterIcon icon = null;
		for (CharacterIcon i : CharacterIcon.values()) {
			if (iconCode == i.getCode()) {
				icon = i;
			}
		}
		if (icon == null) {
			//TODO: throw an exception?
			System.out.println("No such character available");
		} else {
			memory.put(address, icon);
		}
		//TODO: insert render() here?
	}
	
	public void highlight(GraphicsContext gc, int address) {
		gc.setFill(Color.GREY);
		int x = address % ROWS;
		int y = address / COLUMNS;
		gc.fillRect(x*BLOCK_WIDTH, y*BLOCK_HEIGHT, BLOCK_WIDTH, BLOCK_HEIGHT);
	}
	
	public void clear(GraphicsContext gc) {
		memory.clear();
		render(gc);
	}
}
