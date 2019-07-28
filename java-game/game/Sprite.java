package game;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;


/**
 * for making enemies and players.
 *
 * @author Leah Behr
 * @version (a version number or a date)
 */
public class Sprite {
    private Image image;
    private int posX, posY;
    boolean hasKey;
    private Pair<Integer, Integer> DOOR_POS = new Pair<Integer, Integer>(8, 1);
    
    Walls w;
    //The Sprite class has to have an internal representation of the walls because 
    //it manages a sprite's movement; it must ensure that sprites are not moving into walls. 
    
    public Sprite(Walls walls) {
        posX = 0;
        posY = 0;
        w = walls;
        hasKey = false;
    }

    //sets the sprite's location to a specific point; assumes the point is not on top of walls. 
    public void setPos(Pair<Integer, Integer> pair) {
        posX = pair.first();
        posY = pair.second();
    }

    //sets the sprite's image
    public void setImage(Image i) {
        image = i;
    }

    //Provided a direction/velocity, sets the sprite's new location (as long as it is not inside of a wall)
    public boolean update(int dx, int dy) {
        Pair<Integer, Integer> temPos = new Pair<Integer, Integer>(posX+dx, posY+dy);
        if (w.contains(temPos) || ((!hasKey)&&(temPos.equals(DOOR_POS)))) {
        	return false;
        } else {
        	posX = temPos.first();
        	posY = temPos.second();
        	return true;
        }
    }

    //redraws the image to a specific GraphicsContext
    public void render(GraphicsContext gc) {
        gc.drawImage(image, posX*50, posY*50);
    }

    //returns the Sprite's current location
    public Pair<Integer, Integer> getPos() {
        return new Pair<Integer, Integer>(posX, posY);
    }
    
    @Override
    public boolean equals(Object o) {
    	Sprite otherSprite = (Sprite)o;
    	Pair<Integer, Integer> pos = new Pair<Integer, Integer>(posX, posY);
    	return pos.equals(otherSprite.getPos());
    }

}
