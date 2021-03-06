package game;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class is the main bulk of the game itself - handles the screen, user input, things like that. 
 * @author Leah Behr
 * @version 5.12.18
 */
public class Main extends Application
{

	private static Random randomizer = new Random();
	int level = 0;
	//sharpness is the probability that an attack will successfully kill an enemy. 
	private double startingSharpness = 0.7;
	private double SWORD_SHARPNESS = startingSharpness - (level * 0.2);
	Walls wallset;
	
	EnemyGlitch glitches;
	private int DEFAULT_ENEMY_COUNT = 4;
	Coin currency;
	
    Player p;
    
    Sprite door;
    Pair<Integer, Integer> DOOR_SPAWN = new Pair<Integer, Integer>(8, 1);
    Image OPEN_DOOR = new Image("open_door.jpg", 50, 50, false, false);
    Image CLOSED_DOOR = new Image("door_sprite.png", 50, 50, false, false);
    
    private Console c = new Console();
    
    private int SCREEN_WIDTH = 900;
    private int SCREEN_HEIGHT = 600;
    
    boolean gameOn = true;
    
    int points = 0;
    int gold = 0;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("the game");
        BorderPane betaRoot = new BorderPane();
        Scene scene = new Scene(betaRoot);
        stage.setScene(scene);
        
        Canvas cvs = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        //TODO: make it so that the window can't be resized - keep the BorderPane set to its original size. 
        betaRoot.setCenter(cvs);
        GraphicsContext gc = cvs.getGraphicsContext2D();
        
        ScrollPane console = new ScrollPane(c.getPane());
        console.setFitToWidth(true);
        betaRoot.setRight(console);
        betaRoot.setPrefSize(SCREEN_WIDTH + 250, SCREEN_HEIGHT);
        
        wallset = new Walls();
        
        p = new Player(wallset, DOOR_SPAWN);
        glitches = new EnemyGlitch(wallset, DOOR_SPAWN, DEFAULT_ENEMY_COUNT);
        currency = new Coin(wallset, DOOR_SPAWN);
        
        door = new Sprite(wallset, DOOR_SPAWN);
        door.setImage(CLOSED_DOOR);
        door.setPos(DOOR_SPAWN);
        
        updateScreen(gc);
        
        scene.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    public void handle(KeyEvent e) {
                        String code = e.getCode().toString();
                        if (code.equals("R")) {
                        	reset(gc);
                        	c.clearMessages();
                        } else {
                        	Pair<Integer, Integer> pLoc = Player.getPos();
                        	List<Pair<Integer, Integer>> gLocs = new ArrayList<Pair<Integer, Integer>>();
                        	gLocs = glitches.getPos(gLocs);
                        	if (code.equals("SPACE")) {
                        		c.printMessage("You swing your sword...");
                        		if (attack(pLoc, gLocs)) {
                        			points+= 10;
                        			c.printMessage("You hit!");
                        		} else {
                        			c.printMessage("You miss! Watch out!");
                        		}
                        	} else {
                        		p.update(code);
                        		pLoc = Player.getPos();
                        		if (currency.coinsPresent()) {
                        			ArrayList<Pair<Integer, Integer>> goldSpots = new ArrayList<Pair<Integer, Integer>>();
                        			goldSpots = currency.getCoins(goldSpots);
                        			if (intersects(pLoc, goldSpots)) {
                        				gold += 1;
                        				currency.takeCoin(pLoc);
                        			}
                        		}
                        		if (pLoc.equals(DOOR_SPAWN)) {
                        			//level++;
                        			points += 15;
                        			gameOver("You Win!", gc);
                        		} else if (pLoc.equals(p.KEY_SPAWN)) {
                        			points += 15;
                        			c.printMessage("The door is now open.");
                        		}
                        	}
                        	glitches.update();
                        	gLocs = glitches.getPos(gLocs);
                        	boolean overlap = intersects(pLoc, gLocs);
                    		if (overlap) {
                    			gameOver("You have died.", gc);
                    		}
                        }
                        
                        updateScreen(gc);
                    }
                });
        
        stage.show();
    }
    
    /**
     * Updates the screen to show the current game map
     * @param gc - the graphicsContext for render functions
     */
    private void updateScreen(GraphicsContext gc) {
    	if (gameOn) {
    		gc.setFill(Color.rgb(165, 128, 107));
    		gc.fillRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);
    		wallset.render(gc);
    		if (!p.hasKey()) {
    			door.render(gc);
    		} else {
    			door.setImage(OPEN_DOOR);
    			door.render(gc);
    		}
    		glitches.render(gc);
    		currency.render(gc);
    		p.render(gc);
    		gc.setFill(Color.GOLD);
    		gc.setStroke(Color.GOLD);
    		gc.setTextAlign(TextAlignment.CENTER);
    		gc.setTextBaseline(VPos.TOP);
    		Font theFont = Font.font("Times New Roman", 24);
    		gc.setFont(theFont);
    		String message = "Score: " + points;
    		int centerX = SCREEN_WIDTH / 2;
    		gc.fillText(message, centerX, 10);
    		gc.strokeText(message, centerX, 10);
    	}
    }
    
    /**
     * Resets the game board to start from the beginning. 
     * @param gc - the GraphicsContext for the render stuff
     */
    private void reset(GraphicsContext gc) {
    	p = new Player(wallset, DOOR_SPAWN);
    	glitches = new EnemyGlitch(wallset, DOOR_SPAWN, DEFAULT_ENEMY_COUNT);
    	currency = new Coin(wallset, DOOR_SPAWN);
    	door.setImage(CLOSED_DOOR);
    	points = 0;
    	gold = 0;
    	level = 0;
    	gameOn = true;
    	updateScreen(gc);
    }
    
    /**
     * attack searches the positions directly adjacent to the player to see if an enemy is there.
     * If so, it attempts to kill the glitch. There is a 30% chance that the attack doesn't succeed.
     * If the attack does succeed, the enemy's position is changed to a new Coin object. 
     *  
     * @param p1 - player location
     * @param enemies - list of enemy locations
     * @return true if an enemy was successfully killed
     **/
    private boolean attack(Pair<Integer, Integer> p1, List<Pair<Integer, Integer>> enemies) {
    	int px = p1.first();
    	int py = p1.second();
    	for (int i = px - 1; i <= px + 1; i++) {
    		for (int j = py - 1; j <= py + 1; j++) {
    			Pair<Integer, Integer> search = new Pair<Integer, Integer>(i,j);
    			if (enemies.contains(search) && (randomizer.nextDouble() < SWORD_SHARPNESS)) {
    				if (glitches.killGlitch(search)) {
    					currency.dropCoin(search);
    					return true;
    				} else {
    					return false;
    				}
    				//return glitches.killGlitch(search);
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * Ends the game: puts text on the screen to show game result and points.
     * BUG: the text doesn't show up right away when the player dies, only after player presses a button. 
     * @param text - tells how the game ended, in a win or a death. 
     * @param gc - the GraphicsContext for rendering
     */
    private void gameOver(String text, GraphicsContext gc) {
    	updateScreen(gc);
    	gameOn = false;
    	gc.setTextAlign(TextAlignment.CENTER);
    	gc.setTextBaseline(VPos.CENTER);
        gc.setFill(Color.RED);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        Font theFont = Font.font( "Times New Roman", FontWeight.BOLD, 48 );
        int centerX = SCREEN_WIDTH / 2;
        int centerY = SCREEN_HEIGHT / 2;
        gc.setFont( theFont );
        gc.fillText( text, centerX, centerY - 50 );
        gc.strokeText( text, centerX, centerY - 50 );
        gc.setFill(Color.BLACK);
        theFont = Font.font("Times New Roman", 24);
        gc.setLineWidth(1);
        gc.setFont(theFont);
        String message = "Final Score: " + points + "\nPress R to restart.";
        gc.fillText(message, centerX, centerY);
        gc.strokeText(message, centerX, centerY);
    }
    
    /**
     * Quick check to see if one of the enemies has 'killed' the player
     * @param p1 - player location
     * @param enemies - list of enemy locations
     * @return true if the player location is the same as an enemy location. 
     */
    private boolean intersects(Pair<Integer, Integer> p1, List<Pair<Integer, Integer>> enemies) {
    	return enemies.contains(p1);
    }
}
