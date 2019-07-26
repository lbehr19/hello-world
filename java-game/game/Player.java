package game;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
/**
 * Player class manages the Sprites for the player and the key. 
 *
 * @author Leah Behr
 * @version 4.11.18
 */
public class Player
{
    static Sprite p;
    Sprite key;
    
    Image PLAYER_IMAGE = new Image("player_sprite.png", 50, 50, false, false);
    Image KEY_IMAGE = new Image("key_sprite.png", 50, 50, false, false);
    
    Pair<Integer, Integer> PLAYER_SPAWN = new Pair<Integer, Integer>(9, 10);
    Pair<Integer, Integer> KEY_SPAWN = new Pair<Integer, Integer>(7, 3);
    
    
    /**
     * Constructor for objects of class Player
     */
    public Player(Walls w)
    {
    	p = new Sprite(w);
    	key = new Sprite(w);
        p.setImage(PLAYER_IMAGE);
        p.setPos(PLAYER_SPAWN);
        key.setImage(KEY_IMAGE);
        key.setPos(KEY_SPAWN);
    }
    
    public void update(String keyCode) {
    	int x = p.getPos().first();
    	int y = p.getPos().second();
    	int dx = 0;
    	int dy = 0;
        if (keyCode.equals("UP")) {
            dy = -1;
        } else if (keyCode.equals("DOWN")) {
            dy = 1;
        } else if (keyCode.equals("LEFT")) {
            dx = -1;
        } else if (keyCode.equals("RIGHT")) {
            dx = 1;
        } //else if (keyCode.equals("SPACE")) {
        	//attack();
        //}
        
        Pair<Integer, Integer> temPos = new Pair<Integer, Integer>(x+dx, y+dy);
        if(temPos.equals(KEY_SPAWN)) {
        	p.hasKey = true;
        }
        p.update(dx, dy);
    }
    
    public void render(GraphicsContext gc) {
        p.render(gc);
        if (!p.hasKey) {
            key.render(gc);
        }
    }
    
    public boolean hasKey() {
    	return p.hasKey;
    }
    
    public static Pair<Integer, Integer> getPos() {
        return p.getPos();
    }
}
