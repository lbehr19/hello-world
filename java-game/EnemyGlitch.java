import java.util.Random;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.util.Iterator;
import javafx.scene.canvas.GraphicsContext;

/**
 * Glitches move at random and can move through walls. 
 *
 * @author Leah
 * @version 3.28.18
 */
public class EnemyGlitch extends Sprite
{
    //list created to keep track of multiple enemies
    ArrayList<Sprite> glist = new ArrayList<Sprite>();
    
    private static Random randomizer = new Random();
    
    private Image GLITCH_IMAGE = new Image("glitch_sprite.png", 50, 50, false, false);
    private int ENEMY_COUNT = 4;
    private ArrayList<Pair<Integer, Integer>> ENEMY_POS = new ArrayList<Pair<Integer, Integer>>();
    
    
    public EnemyGlitch() {
        enemySpawns();
        int i = 0;
        while(i < ENEMY_COUNT) {
            Sprite babyGlitch = new Sprite();
            babyGlitch.setImage(GLITCH_IMAGE);
            babyGlitch.setPos(ENEMY_POS.get(i));
            glist.add(babyGlitch);
            i++;
        }
    }
    
    //update function will update each of the glitches on the list
    public void update() {
        Iterator<Sprite> gliter = glist.iterator();
        while(gliter.hasNext()) {
            //randomizer here
            int dx = ((randomizer.nextInt() * 3) - 1);
            int dy = ((randomizer.nextInt() * 3) - 1);
            gliter.next().update(dx, dy);
        }
    }
    
    //render function renders each glitch on the list
    public void render(GraphicsContext gc) {
        Iterator<Sprite> gliter = glist.iterator();
        while(gliter.hasNext()) {
            gliter.next().render(gc);
        }
    }
    
    //enemySpawns function creates enemy spawn points by adding Pairs to the list of spawns
    private void enemySpawns() {
        ENEMY_POS.add(new Pair<Integer, Integer>(1, 8));
        ENEMY_POS.add(new Pair<Integer, Integer>(5, 3));
        ENEMY_POS.add(new Pair<Integer, Integer>(12, 3));
        ENEMY_POS.add(new Pair<Integer, Integer>(16, 4));
    }
}
