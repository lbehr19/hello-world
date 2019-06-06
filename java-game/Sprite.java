import javafx.application.Application;//yes 
import javafx.event.ActionEvent;//yes
import javafx.event.EventHandler;//yes
import javafx.scene.Scene;//yes
import javafx.stage.Stage;//yes?
import javafx.animation.AnimationTimer;//yes
import javafx.scene.canvas.Canvas;//yes
import javafx.scene.image.Image; //yes
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext; //yes*
import javafx.geometry.Rectangle2D; //yes*
//* - these classes may be removed in the future
/**
 * for making enemies and players.
 *
 * @author Leah Behr
 * @version (a version number or a date)
 */
public class Sprite extends Main
{
    private Image image;
    private int posX, posY;
    private int width, height;

    public Sprite() {
        posX = 0;
        posY = 0;
        width = 10;
        height = 10;
    }

    public void setPos(Pair<Integer, Integer> pair) {
        posX = pair.first();
        posY = pair.second();
    }

    public void setImage(Image i) {
        image = i;
    }

    public void update(int dx, int dy) {
        posX += dx;
        posY += dy;
    }

    public void render(GraphicsContext gc) {
        gc.drawImage(image, posX*50, posY*50);
    }

    //getBoundary and intersects will probably be unnecessary, since this
    //should be a cell-based game. 
    public Rectangle2D getBoundary() {
        return new Rectangle2D(posX, posY, width, height);
    }

    public boolean intersects(Sprite s) {
        return s.getBoundary().intersects(this.getBoundary());
    }

}
