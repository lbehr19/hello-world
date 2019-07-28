PROJECT TITLE: Dungeon Crawler
PURPOSE OF PROJECT: for fun!
VERSION or DATE: 6.28.19
AUTHORS: Leah Behr; Pair class contributed by Michael Siff

Notes:
This is a Java application programmed with the JavaFX library. I've been using Eclipse to edit/run it while I'm working on it. 
The project was originally created for academic credit as a Java version of my JavaScript-based game (also included in this repo - 
see js-game folder). It's also heavily inspired by Crypt of the Necrodancer. 

How to Play:
Use the arrow keys to move. Avoid running into enemies, pick up the key, and get out of the dungeon. 
Use space bar to swing your sword - but keep in mind, you might miss. 

Bugs:
-the enemies are not moving in the ways I originally intended; they occasionally stop moving as if stuck. 
-Score should be fixed so that it always updates. Also, change score modifier for winning - 
     +15 for getting the key, +15 for going through the door, rather than the straight +25 for reaching the unlocked door.
-when player and enemy overlap, the game doesn't always end. 

Planned Improvements:
-Include 'treasure' that either appears in hard-to-reach places or drops when enemies die.
-Add new types of enemies: some that move in set patterns, some that can move through walls, things like that.
-Implement 'health' as a concept for player/enemies; attacks do x damage, or player dies after three hits, something like that. 

Possible Improvements:
-Implement levels; when player wins, they move on to the next level, with a harder map, more enemies, etc.
-Add a 'scoreboard' type feature so that player can compare with the top 10 scores. 
-Add bonus points for finishing levels quickly (timed aspect)
-Implement visibility moderator - player can only see a set radius around them (optional - can't see around walls)
    If visibility is added, modify enemy movement so that they only move when visible.
