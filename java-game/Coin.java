package game;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Coin {

	ArrayList<Sprite> coins = new ArrayList<Sprite>();
    private Image GOLD_COIN = new Image("coin_sprite.png", 50, 50, false, false);
    private Walls w;
    private Pair<Integer, Integer> door;
    
    public Coin(Walls wallset, Pair<Integer, Integer> d) {
    	w = wallset;
    	door = d;
    }
    
    public boolean coinsPresent() {
    	return !coins.isEmpty();
    }
    
    //modifies and returns list containing positions of all coins on the field
    public ArrayList<Pair<Integer, Integer>> getCoins(ArrayList<Pair<Integer, Integer>> target) {
    	Iterator<Sprite> beanCounter = coins.iterator();
    	target.clear();
    	while (beanCounter.hasNext()) {
    		Pair<Integer, Integer> pos = beanCounter.next().getPos();
    		target.add(pos);
    	}
    	return target;
    }
    
    //should be given the dead enemy's location
    public void dropCoin(Pair<Integer, Integer> loc) {
    	Sprite newCoin = new Sprite(w, door);
    	newCoin.setPos(loc);
    	newCoin.setImage(GOLD_COIN);
    	coins.add(newCoin);
    }
    
    //should be given the player's current location
    public boolean takeCoin(Pair<Integer, Integer> loc) {
    	for (Sprite coin : coins) {
    		Pair<Integer, Integer> coinPos = coin.getPos();
    		if (coinPos.equals(loc)) {
    			return coins.remove(coin);
    		}
    	}
    	return false;
    }
    
    public void render(GraphicsContext gc) {
        Iterator<Sprite> beanCounter = coins.iterator();
        while(beanCounter.hasNext()) {
            beanCounter.next().render(gc);
        }
    }
}
