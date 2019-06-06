PROJECT TITLE: "Conference Game"
PURPOSE OF PROJECT: for class credit
VERSION or DATE: 3.27.18
HOW TO START THIS PROJECT: Run the JavaFX application to begin
AUTHORS: Leah Behr (inspired by an example from an online tutorial - URL below); Pair class contributed by Michael Siff
USER INSTRUCTIONS: Have fun!

https://gamedevelopment.tutsplus.com/tutorials/introduction-to-javafx-for-game-development--cms-23835

Notes: 
Current version includes canvas, event handlers. Need to be able to animate the game itself. 
The kind of animation I want to do is not continuous movement, so I don't want to use the last example from the tutorial above - I don't want to update the game board constantly, I want to update only when the player gives it input. As such, I'm not sure I should be using AnimationTimer. It did talk about frame-based animation, but the way it's set up in the tutorial appears to be better suited for short, finite animations, not a game. 

As of right now, the Sprite class does not interact with Main class. Next step is to code the enemy movements based on type, probably using wrappers around the Sprite class. 
        Enemy types: Glitch - moves randomly, can move through walls. 
        Chaser - moves towards player, cannot move through walls - knows to move around walls when possible. 

After Sprites, work on the same thing for the player character; movement pattern for the player is based solely on key input, so this is the step where I add in the keys that are being handled by the KeyEventHandlers. In addition to coding player movement, don't forget to add keys to pause or quit the game. 

After movement is down pat for players and enemies, add in door and key: player must reach multiple points of the map in order to win. 

The last step will be adding in the actual game mechanics: This game is point-based, rather than simply objective-based. Points are gained by picking up treasure, reaching new levels, (possibly killing enemies?), with the most points awarded for reaching the end of Level 3. 

Options for improving the game, once those steps are finished:
Add in bonus point rewards for finishing levels quickly. 
Add in visibility moderator - player can only see a set radius (optional - can't see around walls). 
    If visibility is added, consider altering Chaser movement so that they move like Sentries when not visible. 