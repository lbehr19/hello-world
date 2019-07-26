package game;
import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.Iterator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;

/**
 * Glitches follow the player and can't move through walls.  
 *
 * @author Leah Behr
 * @version 4.25.18
 */
public class EnemyGlitch
{
    //list created to keep track of multiple enemies
    ArrayList<Sprite> glist = new ArrayList<Sprite>();
    
    private Image GLITCH_IMAGE = new Image("glitch_sprite.png", 50, 50, false, false);
    private int ENEMY_COUNT = 4;
    private ArrayList<Pair<Integer, Integer>> ENEMY_POS = new ArrayList<Pair<Integer, Integer>>();
    
    
    public EnemyGlitch(Walls w) {
        enemySpawns();
        for (int i = 0; i < ENEMY_COUNT; i++) {
            Sprite babyGlitch = new Sprite(w);
            babyGlitch.setImage(GLITCH_IMAGE);
            babyGlitch.setPos(ENEMY_POS.get(i));
            glist.add(babyGlitch);
        }
    }
    
    //update function will update each of the glitches on the list
    public void update() {
        Iterator<Sprite> gliter = glist.iterator();
        Pair<Integer, Integer> playerPos = Player.getPos();
        int pX = playerPos.first();
        int pY = playerPos.second();
        int dx = 0;
        int dy = 0;
        while(gliter.hasNext()) {
            Sprite glitch = gliter.next();
            Pair<Integer, Integer> gPos = glitch.getPos();
            int x = gPos.first();
            int y = gPos.second();
            if ((pX - x) > 0) {
            	dx = 1;
            } else if ((pX - x) < 0) {
            	dx = -1;
            }
            if (!glitch.update(dx, dy)) {
            	dx = 0;
            	if ((pY - y) > 0) {
            		dy = 1;
            	} else if ((pY - y) < 0){
            		dy = -1;
            	} else {
            		dy = 0;
            	}
            	glitch.update(dx, dy);
            }
        }
    }
    
    //render function renders each glitch on the list
    public void render(GraphicsContext gc) {
        Iterator<Sprite> gliter = glist.iterator();
        while(gliter.hasNext()) {
            gliter.next().render(gc);
        }
    }
    
    public List<Pair<Integer, Integer>> getPos(List<Pair<Integer, Integer>> target) {
    	target.clear();
    	for (Sprite g : glist) {
    		target.add(g.getPos());
    	}
    	return target;
    }
    
    public boolean killGlitch(Pair<Integer, Integer> target) {
    	for (Sprite glitch : glist) {
    		Pair<Integer, Integer> gPos = glitch.getPos();
    		if (gPos.equals(target)) {
    			return glist.remove(glitch);
    		}
    	}
    	return false;
    }
    
    //enemySpawns function creates enemy spawn points by adding Pairs to the list of spawns
    private void enemySpawns() {
        ENEMY_POS.add(new Pair<Integer, Integer>(1, 6));
        ENEMY_POS.add(new Pair<Integer, Integer>(5, 3));
        ENEMY_POS.add(new Pair<Integer, Integer>(12, 3));
        ENEMY_POS.add(new Pair<Integer, Integer>(16, 4));
    }
}
