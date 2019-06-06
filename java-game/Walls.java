import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import java.util.Iterator;
import javafx.scene.image.Image;
public class Walls
{
    private ArrayList<Pair<Integer, Integer>> walls = new ArrayList<Pair<Integer, Integer>>();
    private Image WALL_IMAGE = new Image("wall_texture.png", 50, 50, false, false);

    /**
     * Creates the list of wall positions to put on the ArrayList
     * Wall position is not random, so positions specified in constructor. 
     */
    public Walls()
    {
        int y = 0; //row 0: 18 walls
        walls.add(pair(0,y));
        walls.add(pair(1,y));
        walls.add(pair(2,y));
        walls.add(pair(3,y));
        walls.add(pair(4,y));
        walls.add(pair(5,y));
        walls.add(pair(6,y));
        walls.add(pair(7,y));
        walls.add(pair(8,y));
        walls.add(pair(9,y));
        walls.add(pair(10,y));
        walls.add(pair(11,y));
        walls.add(pair(12,y));
        walls.add(pair(13,y));
        walls.add(pair(14,y));
        walls.add(pair(15,y));
        walls.add(pair(16,y));
        walls.add(pair(17,y));
        walls.add(pair(18,y));
        
        y++; //row 1: 4 walls
        walls.add(pair(0, y));
        walls.add(pair(9, y));
        walls.add(pair(15, y));
        walls.add(pair(17, y));
        
        y++; //row 2: 13 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(4, y));
        walls.add(pair(5, y));
        walls.add(pair(6, y));
        walls.add(pair(7, y));
        walls.add(pair(8, y));
        walls.add(pair(9, y));
        walls.add(pair(11, y));
        walls.add(pair(12, y));
        walls.add(pair(13, y));
        walls.add(pair(15, y));
        walls.add(pair(17, y));
        
        y++; //row 3: 8 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(4, y));
        walls.add(pair(6, y));
        walls.add(pair(11, y));
        walls.add(pair(13, y));
        walls.add(pair(15, y));
        walls.add(pair(17, y));
        
        y++; //row 4: 11 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(4, y));
        walls.add(pair(6, y));
        walls.add(pair(7, y));
        walls.add(pair(8, y));
        walls.add(pair(9, y));
        walls.add(pair(10, y));
        walls.add(pair(11, y));
        walls.add(pair(13, y));
        walls.add(pair(17, y));
        
        y++; //row 5: 8 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(4, y));
        walls.add(pair(7, y));
        walls.add(pair(13, y));
        walls.add(pair(15, y));
        walls.add(pair(16, y));
        walls.add(pair(17, y));
        
        y++; //row 6: 8 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(7, y));
        walls.add(pair(9, y));
        walls.add(pair(10, y));
        walls.add(pair(11, y));
        walls.add(pair(13, y));
        walls.add(pair(17, y));
        
        y++; //row 7: 11 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(4, y));
        walls.add(pair(5, y));
        walls.add(pair(6, y));
        walls.add(pair(7, y));
        walls.add(pair(11, y));
        walls.add(pair(13, y));
        walls.add(pair(14, y));
        walls.add(pair(15, y));
        walls.add(pair(17, y));
        
        y++; //row 8: 5 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(11, y));
        walls.add(pair(15, y));
        walls.add(pair(17, y));
        
        y++; //row 9: 13 walls
        walls.add(pair(0, y));
        walls.add(pair(2, y));
        walls.add(pair(3, y));
        walls.add(pair(4, y));
        walls.add(pair(6, y));
        walls.add(pair(7, y));
        walls.add(pair(8, y));
        walls.add(pair(9, y));
        walls.add(pair(10, y));
        walls.add(pair(11, y));
        walls.add(pair(13, y));
        walls.add(pair(15, y));
        walls.add(pair(17, y));
        
        y++; //row 10: 4 walls
        walls.add(pair(0, y));
        walls.add(pair(4, y));
        walls.add(pair(13, y));
        walls.add(pair(17, y));
        
        //row 11: 18 walls
        y++;
        walls.add(pair(0,y));
        walls.add(pair(1,y));
        walls.add(pair(2,y));
        walls.add(pair(3,y));
        walls.add(pair(4,y));
        walls.add(pair(5,y));
        walls.add(pair(6,y));
        walls.add(pair(7,y));
        walls.add(pair(8,y));
        walls.add(pair(9,y));
        walls.add(pair(10,y));
        walls.add(pair(11,y));
        walls.add(pair(12,y));
        walls.add(pair(13,y));
        walls.add(pair(14,y));
        walls.add(pair(15,y));
        walls.add(pair(16,y));
        walls.add(pair(17,y));
        walls.add(pair(18,y));
    }
    
    //for drawing the walls on the canvas
    public void render(GraphicsContext gc) {
        Iterator<Pair<Integer, Integer>> witer = walls.iterator();
        while (witer.hasNext()) {
            Pair<Integer, Integer> pair = witer.next();
            int x = pair.first() * 50;
            int y = pair.second() * 50;
            gc.drawImage(WALL_IMAGE, x, y);
        }
    }
    
    //to help make code shorter
    public Pair<Integer, Integer> pair(int x, int y) {
        return new Pair<Integer, Integer>(x, y);
    }
}
