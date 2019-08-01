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
    private int MAX_ENEMY_COUNT = 4;
    private int MIN_ENEMY_COUNT = 1;
    private ArrayList<Pair<Integer, Integer>> ENEMY_POS = new ArrayList<Pair<Integer, Integer>>();
    
    
    public EnemyGlitch(Walls w, int spawnCount) {
    	int enemyCount = spawnCount > 0 ? Math.min(spawnCount, MAX_ENEMY_COUNT) : MIN_ENEMY_COUNT; 
    	//if supplied number is valid (pos, non-zero number), use it (if it's less than max). Else, use min.
        enemySpawns();
        for (int i = 0; i < enemyCount; i++) {
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
        int dx;
        int dy;
        //boolean checkY = true;
        while(gliter.hasNext()) {
            Sprite glitch = gliter.next();
            Pair<Integer, Integer> gPos = glitch.getPos();
            int x = gPos.first();
            int y = gPos.second();
            //start by deciding whether to move left or right
            if (pX < x) {
            	dx = -1;
            } else if (pX > x) {
            	dx = 1;
            } else {
            	dx = 0;
            }
            //if left/right movement doesn't work, or glitch doesn't have to move left/right,
            //move on to decide whether to move up/down.
            if (!(dx != 0 && glitch.update(dx, 0))) {
            	if (pY < y) {
            		dy = -1;
            	} else if (pY > y) {
            		dy = 1;
            	} else {
            		dy = 0;
            	}
            	glitch.update(0, dy);
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
    
    //modifies a given list to contain the positions of the enemies.
    public List<Pair<Integer, Integer>> getPos(List<Pair<Integer, Integer>> target) {
    	target.clear();
    	for (Sprite g : glist) {
    		target.add(g.getPos());
    	}
    	return target;
    }
    
    //removes a glitch from the global list based on its position
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
