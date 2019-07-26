package game;
// Data Structures & Algorithms
// Spring 2018
// HW2: bfs/dfs traversal


/** 
 * generic immutable pairs (solution)
 *
 * @author Michael Siff
 */
public class Pair<T, U> {

    private T _first;
    private U _second;

    public Pair(T first, U second) {
        _first = first;
        _second = second;
    }
    
    public T first() { return _first; }
    public U second() { return _second; }

	@Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        Pair<T, U> otherPair = (Pair<T,U>)o;
    	return _first.equals(otherPair.first()) &&
            _second.equals(otherPair.second());
    }

    @Override
    public int hashCode() {
    	return _first.hashCode() + _second.hashCode();
    }
    
    public String toString() {
        return "<" + _first + ", " + _second + ">";
    }
      
}
