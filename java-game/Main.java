import java.lang.Object;
import javafx.application.Application; //yes
import javafx.animation.AnimationTimer; //yes
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene; //yes
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage; //yes
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Group; //yes
import javafx.scene.canvas.Canvas; //yes
import javafx.scene.canvas.GraphicsContext; //yes
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color; //yes
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import java.util.ArrayList;
import javafx.scene.input.KeyEvent;

/**
 * To start with, this class will initialize the window to run the program.
 * The start window simply has instructions and start button (for now)
 * 
 * BUG REPORT: window appears without the button
 *
 * @author Leah Behr
 * @version 2.21.18
 */
public class Main extends Application
{

    

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("the game");
        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        Canvas cvs = new Canvas(900, 600);
        root.getChildren().add(cvs);
        GraphicsContext gc = cvs.getGraphicsContext2D();
        
        ArrayList<String> keyStack = new ArrayList<String>();
        
        scene.setOnKeyPressed(
            new EventHandler<KeyEvent>() {
                public void handle(KeyEvent e) {
                    String code = e.getCode().toString();
                    if(!keyStack.contains(code))
                        keyStack.add(code);
                }
            });
            
        scene.setOnKeyReleased(
            new EventHandler<KeyEvent>() {
                public void handle(KeyEvent e) {
                    String code = e.getCode().toString();
                    keyStack.remove(code);
                }
            });
        
        final long startNT = System.nanoTime();
        EnemyGlitch glitches = new EnemyGlitch();
        Sprite player = new Sprite();
        Image PLAYER_IMAGE = new Image("player_sprite.png", 50, 50, false, false);
        Pair<Integer, Integer> PLAYER_SPAWN = new Pair<Integer, Integer>(9, 10);
        player.setImage(PLAYER_IMAGE);
        player.setPos(PLAYER_SPAWN);
        Walls wallset = new Walls();

        Sprite key = new Sprite();
        key.setImage(new Image("key_sprite.png", 50, 50, false, false));
        key.setPos(new Pair<Integer, Integer>(7, 3));
        Sprite door = new Sprite();
        door.setImage(new Image("door_sprite.png", 50, 50, false, false));
        door.setPos(new Pair<Integer, Integer>(8, 1));
        
        
        new AnimationTimer() {
            public void handle(long currentNT) {
                //clear canvas
                gc.setFill(new Color(0.85, 0.85, 1.0, 1.0));
                gc.fillRect(0, 0, 900, 600);
                
                wallset.render(gc);
                glitches.render(gc);
                player.render(gc);
                door.render(gc);
                key.render(gc);
            }
        }.start();
        
        stage.show();
    }
}
