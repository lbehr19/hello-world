PROJECT TITLE: "Conference Game"
PURPOSE OF PROJECT: for class credit
VERSION or DATE: 5.12.18
HOW TO START THIS PROJECT: Run the JavaFX application to begin
AUTHORS: Leah Behr (inspired by an example from an online tutorial - URL below); Pair class contributed by Michael Siff
USER INSTRUCTIONS: Have fun!

https://gamedevelopment.tutsplus.com/tutorials/introduction-to-javafx-for-game-development--cms-23835

Notes: (what works, what doesn't work)
Player presses arrow keys to move; space bar is an attack, with range 1 (range includes diagonals). Pressing R resets the game. 
There are bugs in the enemy movement: I had intended for them to move only in 1 direction at a time, but they're moving diagonally.
Plus, sometimes they move away from the player when they should be moving towards player (which is a minor problem). The game 
ends when either the player successfully reaches the unlocked door, or player runs into an enemy and dies. The gameOver
function finishes the game and reports the score; however, the text only shows up when the player presses another button (this is 
an unintended bug). Another problem is that the score should be showing up while the game is running, but it's not. 

What I wish I had time for:
I included some small variables in the Main class to support future implementation of levels - the reason why I don't have more
than one level is because I didn't have time to hard-code additional wall maps. Something I could've done to make that easier
would be to have a 2D array to keep track of wall positions, rather than list of coordinate pairs. Anyway, to make the levels
actually work, I'd have to write a new constructor for Walls - Walls(int l) where l is the level. 
I also regret not having more javadoc comments. 

A few other ideas to improve the game:
Include 'treasure' that drops when enemies die, or spawn at random points on the map. 
Add scoring leaderboards? So that the player can compare to the top ten scores. 
Add new types of enemies, i.e. sentries, ghosts, things like that. 
Add in bonus point rewards for finishing levels quickly (timed aspect)
Add in visibility moderator - player can only see a set radius (optional - can't see around walls). 
    If visibility is added, consider altering Chaser movement so that they only chase when the player sees them. 