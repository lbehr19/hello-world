// ===================================
//   Introduction to (Web) Programming
// ===================================

// --------------------------------------------
//   Final Program
//   Creative RPG - Dungeon Crawler
//   By Leah Behr
//   Created 05/17
// --------------------------------------------

// Static Definitions
// pixel size of each cell within the grid map
var CELL_SIZE = 12;
// amount of time (nanoseconds) between calls to gameLoop()
var DEFAULT_DELAY = 50;
// number of gamesteps (calls to gameLoop) before creating new Glitch
var NEW_GLITCH_TIME = 100;
// number of gamesteps before creating new Chaser
var NEW_CHASER_TIME = 200;
// number of gamesteps between enemy movement functions
var ENEMY_MOVE_TIME = 10;
// percent likelihood that a wall will spawn in any one cell in the grid
var WALL_DENSITY = 0.25;
// max distance (in cell position) from goal to border wall
var GOAL_MARGIN = 5;
// Colors for objects: 
var WALL_COLOR = "rgb(132, 68, 0)"; // custom brown color
var BORDER_COLOR = "black";
var GOAL_COLOR = "gold";
var PLAYER_COLOR = "blue";
var GLITCH_COLOR = "rgba(201, 8, 219, 150)"; // custom purple
var CHASER_COLOR = "rgba(221, 51, 51, 150)"; // custom red


// global object: keeps track of time, gamesteps, grid state, etc.
var G = {};

/**
 * Creates and sets canvas, handlers/event listeners; makes calls to render functions, and begins the game loop.
 */
function initialize() {
    G.canvas = get("canvas");
    G.context = G.canvas.getContext("2d");
    // determine game-board size and resize canvas accordingly
    G.columns = Math.floor(G.canvas.width / CELL_SIZE);
    G.rows = Math.floor(G.canvas.height / CELL_SIZE);
    G.canvas.width = G.columns * CELL_SIZE;
    G.canvas.height = G.rows * CELL_SIZE;

    // to enable quick repainting of stationary elements
    G.auxCanvas = document.createElement("canvas");
    G.auxCanvas.width = G.canvas.width;
    G.auxCanvas.height = G.canvas.height;
    G.auxContext = G.auxCanvas.getContext("2d");

    document.addEventListener("keydown", keydownHandler);

    var center = makePoint(Math.floor(G.columns/2), Math.floor(G.rows/2));
    G.player = makePlayer(center, PLAYER_COLOR);

    G.glitches = [];
    G.chasers = [];

    addGoal();
    addWalls();

    drawBorder();
    drawWalls();
    G.auxContext.drawImage(G.canvas, 0, 0);   // save copy of stationary board

    G.delay = DEFAULT_DELAY;
    G.steps = 0;
    G.gameOn = true;
    G.t0 = Date.now();

    render();
    gameLoop();
}


// -------------------------= stationary object creation =-------------------------------------

/**
 * Creates the goal spot in a random location close to the grid's border 
 * and adds it to the global object. 
 */
function addGoal() {
    // convenience variables to avoid hard-coding goal positions
    var margin = GOAL_MARGIN
    var maxX = G.columns - margin
    var maxY = G.rows - margin
    // start out in the center
    var goal = makePoint(Math.floor(G.columns/2), Math.floor(G.rows/2));
    while ((goal.x > margin && goal.x < maxX) && (goal.y > margin && goal.y < maxY)) {
        x = Math.floor(Math.random() * (G.columns - 2)) + 1;
        y = Math.floor(Math.random() * (G.rows - 2)) + 1;
        goal = makePoint(x, y);
    }
    G.goalPt = goal;
    //G.goalShow = false;
}

/**
 * Uses a convenience function to make an object representing an empty set 
 * of coordinate objects, then iterates through the grid coordinates to decide 
 * whether there should be a wall at any point. If so, add the point to the set. 
 * At the end, take out the goal position and the player's spawn position. 
 */
function addWalls() {
    G.walls = makePointCollection();
    var i = 1;
    while (i < G.rows - 1) {   // loop over game-board rows
        var j = 1;
        while (j < G.columns - 1) {   // loop over game-board columns
            if (Math.random() < WALL_DENSITY) {
                G.walls.add(makePoint(j, i));
            }
            j = j + 1;
        }
        i = i + 1;
    }
    G.walls.remove(G.goalPt);
    G.walls.remove(makePoint(Math.floor(G.columns / 2), Math.floor(G.rows / 2)));
}


// -----------------------= dynamic object creation =------------------------------------------

/**
 * Essentially the same as a class definition for dynamic objects. 
 * @todo add ignoreWalls param so that this can be used to create glitches as well
 * @param {coordinate object} pt object's 'spawn point' on the grid
 * @param {string} color style string describing object color
 * @return {object}  dynamic object; keeps track of position/color, contains functions for movement/rendering
 */
function makePlayer(pt, color) {
    var obj = {
        x: pt.x,
        y: pt.y,
        color: color,

        // change this object's position based on given x & y speeds. 
        move: function(dx, dy) {
            this.moveTo(makePoint(this.x + dx, this.y + dy));
        },

        // Given an (x,y) grid position, check if that pos is 'legal'; if so, set this object's position to the one given.
        moveTo: function(pt) {
            if (pt.x >= 1 && pt.x < G.columns - 1 && pt.y >= 1 && pt.y < G.rows - 1
               && !G.walls.has(pt)) {
                this.x = pt.x;
                this.y = pt.y;
            }
        },

        // draw this object as a circle on the global canvas. 
        draw: function() {
            G.context.fillStyle = this.color;
            G.context.beginPath();
            var canvasPt = toCanvasCoords(this);
            G.context.arc(canvasPt.x, canvasPt.y, CELL_SIZE/2, 0, 2 * Math.PI);
            G.context.fill();
        }
    };
    return obj;
}

/**
 * Creates a new Glitch-type enemy using makePlayer with a random spawn point, 
 * then pushes it onto the global list of glitches.
 * @todo if I can do the ignoreWalls thing, this function becomes obselete. 
 * @param {string} color style string describing the glitch's color
 */
function makeGlitch(color) {
    var spawnPt = makeRandomPoint();
    // var glitch = makePlayer(spawnPt, color, True);
    var glitch = {
        x: spawnPt.x,
        y: spawnPt.y,
        color: color,

        move: function(dx, dy) {
            this.moveTo(makePoint(this.x + dx, this.y + dy));
        },

        moveTo: function(pt) {
            if (pt.x >= 1 && pt.x < G.columns - 1 && pt.y >= 1 && pt.y < G.rows - 1) {
                this.x = pt.x;
                this.y = pt.y;
            }
        },

        draw: function() {
            G.context.fillStyle = this.color;
            G.context.beginPath();
            var canvasPt = toCanvasCoords(this);
            G.context.arc(canvasPt.x, canvasPt.y, CELL_SIZE/2, 0, 2 * Math.PI);
            G.context.fill();
        }
    };
    G.glitches.push(glitch);
}

/**
 * Creates and returns an enemy object using makePlayer with a randomized spawn point. 
 * @param {string} color style string to describe enemy color
 * @returns {object} the enemy object which keeps track of position, movement/drawing functions, and color.
 */
function makeEnemy(color) {
    var spawnPt = makeRandomPoint();
    var enemy = makePlayer(spawnPt, color);
    return enemy;
}


//-----------------------------= dynamic object movement =------------------------------------------------------
/**
 * Iterates through the global list of glitches, assigns each a random velocity, then moves it accordingly. 
 * These randomly assigned velocities are temporary, to create erratic enemy movement.
 */
function moveGlitches() {
    var i = 0;
    while (i < G.glitches.length) {
        var dx = Math.floor(Math.random()*8) - 4;
        var dy = Math.floor(Math.random()*8) - 4;
        if (dx >= 0) { // making sure that the glitches never stay in one place
            dx = dx + 1;
        }
        if (dy >= 0) {
            dy = dy + 1;
        }
        G.glitches[i].move(dx, dy);
        i = i + 1;
    }
}

/**
 * Iterates through the global list of chasers, calculates its velocity so that 
 * it is moving towards the player (even if it has to move around a wall),  
 * and then moves it accordingly. 
 */
function moveChasers() {
    var i = 0;
    while (i < G.chasers.length) {
        var cx = G.chasers[i].x;
        var cy = G.chasers[i].y;
        if (G.player.x < cx) {
            var dx = -1;
        } else if (G.player.x > cx) {
            dx = 1;
        } else {
            dx = 0;
        }
        if (G.player.y < cy) {
            var dy = -1;
        } else if (G.player.y > cy) {
            dy = 1;
        } else {
            dy = 0;
        }
        var destination1 = makePoint(cx + dx, cy + dy);
        var destination2 = null;
        var destinationFinal = null;
        var dySave = dy; // saves the vertical direction 
        if (G.walls.has(destination1)) { // if there's a wall where we need to go:
            if (dx !== 0 && dy !==0) { 
                // if we're moving diagonally, try moving just horizontally
                dy = 0;
                destination2 = makePoint(cx + dx, cy + dy); 
                if (G.walls.has(destination2)) {
                    // if horizontal movement didn't work, use vertical movement.
                    dy = dySave;
                    dx = 0;
                    destinationFinal = makePoint(cx + dx, cy + dy);
                    // note: if vertical only movement doesn't work, we'll just bump into a wall, but that's okay.
                } else {
                    destinationFinal = destination2;
                }
            } else if (dy === 0) {
                // if we were moving horizontally and we couldn't, we'll try moving diagonally to go around
                dy = -1;
                destination2 = makePoint(cx + dx, cy + dy);
                if (G.walls.has(destination2)) {
                    dy = 1;
                    destinationFinal = makePoint(cx + dx, cy + dy);
                    // note that here, again, we may end up bumping into a wall, but that's okay. 
                } else {
                    destinationFinal = destination2;
                }
            } else if (dx === 0) {
                // like above, if we were only moving vertically, try moving diagonally around the wall. 
                dx = -1;
                destination2 = makePoint(cx + dx, cy + dy);
                if (G.walls.has(destination2)) {
                    dx = 1;
                    destinationFinal = makePoint(cx + dx, cy + dy);
                } else {
                    destinationFinal = destination2;
                }
            }
        } else {
            destinationFinal = destination1;
        }
        G.chasers[i].moveTo(destinationFinal);
        i = i + 1;
    }
}


// ----------------------------= drawing functions =-------------------------------------

/**
 * Clears then re-draws the grid. This function should be called any time 
 * one of the dynamic objects (player or enemies) moves, or after each game step. 
 */
function render() {
    var i = 0;
    var j = 0;
    G.context.clearRect(0, 0, G.canvas.width, G.canvas.height);
    G.context.drawImage(G.auxCanvas, 0, 0);   // (re)paint stationary objects
    fillCell(G.goalPt, GOAL_COLOR);
    G.player.draw();
    while (i < G.glitches.length) {
        G.glitches[i].draw();
        i = i + 1;
    }
    while (j < G.chasers.length) {
        G.chasers[j].draw();
        j = j + 1;
    }
}

/**
 * Fills the grid cell at position gamePt with the specified color. Used for drawing stationary squares on the grid.
 * @param {coordinate object} gamePt tuple-like object representing an (x,y) coordinate on the grid
 * @param {string} color the color used to fill the object
 */
function fillCell(gamePt, color) {
    G.context.fillStyle = color;
    var cvsPt = toCanvasCoords(gamePt);
    G.context.fillRect(cvsPt.x - CELL_SIZE/2, cvsPt.y - CELL_SIZE/2,
                       CELL_SIZE, CELL_SIZE);
}

/**
 * Turns the global set of walls to an array of coordinate objects, then uses those
 * coordinate objects to color in the walls with fillCell. 
 */
function drawWalls() {
    var points = G.walls.toArray();
    var i = 0;
    while (i < points.length) {
        fillCell(points[i], WALL_COLOR);
        i = i + 1;
    }
}

/**
 * Draws the initial border around the grid using global values stored in G
 */
function drawBorder() {
    var i = 0;
    var j = 0;
    while (i < G.columns) {
        fillCell(makePoint(i, 0), BORDER_COLOR);
        fillCell(makePoint(i, G.rows - 1), BORDER_COLOR);
        i = i + 1;
    }
    while (j < G.rows) {
        fillCell(makePoint(0, j), BORDER_COLOR);
        fillCell(makePoint(G.columns - 1, j), BORDER_COLOR);
        j = j + 1;
    }
}


// -------------------------= gameplay functions =--------------------------------------------

/**
 * This function manages the actions that must be completed during each game step: 
 * checking to see if the player has either been killed or reached the goal, and
 * managing enemy movement and creation. 
 * @todo optimize the way it checks player status?
 */
function gameStep() {
    var i = 0;
    while (i < G.glitches.length) { // we only check glitches because glitches are created first
        if (G.player.y === G.glitches[i].y && G.player.x === G.glitches[i].x) {
            setStatus("You were killed by a glitch.");
            G.gameOn = false;
        } else if (G.player.y === G.goalPt.y && G.player.x === G.goalPt.x) {
            setStatus("You win!");
            G.gameOn = false;
        } else if (G.player.y === G.chasers[i].y && G.player.x === G.chasers[i].x) {
            // because JS is silly, there's no need to check whether G.chasers[i] really exists before accessing it
            setStatus("You were killed by a chaser.");
            G.gameOn = false;
        }
        i = i + 1;
    }
    if (G.steps % NEW_CHASER_TIME === 0) {
        G.chasers.push(makeEnemy(CHASER_COLOR));
    } else if (G.steps % NEW_GLITCH_TIME === 0) {
        makeGlitch(GLITCH_COLOR);
    } else if (G.steps % ENEMY_MOVE_TIME === 0) {
        moveGlitches();
        moveChasers();
    }
    G.steps = G.steps + 1;
    render();
}

/**
 * Keeps the timer going and, if there's an active game happening, call gameStep
 */
function gameLoop() {
    showTime();
    if (G.gameOn) {
        gameStep();
    }
    setTimeout(gameLoop, G.delay);
}

/**
 * Upon game completion, retrieves and changes the status element of the HTML page accordingly. 
 * @param {string} message what should be shown to the player when game ends
 */
function setStatus(message) {
    var statusElement = get("status");
    statusElement.innerHTML = message;
}

/**
 * Calculates the timer for the current match, then sets the timer element of the HTML page accordingly.
 */
function showTime()  {
    var seconds = Math.floor(((Date.now() - G.t0)) / 1000);
    var timerElement = get("timer");
    timerElement.innerHTML = "time passed: " + seconds;
}


// -------------------------------= event handlers =----------------------------------

/**
 * Key handler for player movement, pausing the game. 
 * @param {event object} event Key Event which triggers the handler
 */
function keydownHandler(event) {
    if (event.key === "ArrowLeft") {
        G.player.move(-1, 0);
    } else if (event.key === "ArrowRight") {
        G.player.move(1, 0);
    } else if (event.key === "ArrowUp") {
        G.player.move(0, -1);
    } else if (event.key === "ArrowDown") {
        G.player.move(0, 1);
    } else if (event.key === " ") {
        G.gameOn = !G.gameOn;
        if (!G.gameOn) {
          setStatus("Game Paused");
        } else {
          setStatus("");
        }
    }
}


// ----------------------= Coordinate-based functions =--------------------------------------------------

/**
 * Takes an x,y coordinate object and calculates where that object appears on the canvas.
 * Specifically, it calculates the position of the pixel at the center of the object. 
 * @param {coordinate object} gamePt the grid representation of the x,y coordinate
 * @returns {coordinate object} a new (x,y) coordinate representing pixel position on the canvas.
 */
function toCanvasCoords(gamePt) {
    return makePoint(gamePt.x * CELL_SIZE + CELL_SIZE/2,
                     gamePt.y* CELL_SIZE + CELL_SIZE/2);
}

/**
 * Turns two integers into an object representing a coordinate pair. 
 * @param {integer} x The x value of the coordinate.
 * @param {integer} y The y value of the coordinate.
 * @returns {coordinate object} the new object with x & y properties. 
 */
function makePoint(x, y) {
    var obj = { x: x, y: y };
    return obj;
}

/**
 * Calculates a random point on the grid which is not contained within a wall
 * and is not the same as the player's position. 
 * @returns {coordinate object} the new randomly generated coordinate
 */
function makeRandomPoint() {
    var x = Math.floor(Math.random() * (G.columns - 2)) + 1;
    var y = Math.floor(Math.random() * (G.rows - 2)) + 1;
    var obj = {x: x, y: y};
    while (G.walls.has(obj) && G.player.x === obj.x && G.player.y === obj.y) {
        x = Math.floor(Math.random() * (G.columns - 2)) + 1;
        y = Math.floor(Math.random() * (G.rows - 2)) + 1;
        obj = {x: x, y: y};
    }
    return obj;
}

/**
 * Creates an object to represent an easily accessible set of coordinate pairs.
 * The object contains 'rows', which is used like a Python dictionary to contain 
 * y-values as their own dictionaries. Each y-value dictionary contains x-value attributes. 
 * This makes it simple to check whether the collection contains a specific (x,y) coordinate pair.
 */
function makePointCollection() {
    var obj = {
        rows: {},
        add: function(pt) {
            if (!this.rows.hasOwnProperty(pt.y)) {
                this.rows[pt.y] = {};
            }
            this.rows[pt.y][pt.x] = true;
        },
        has: function(pt) {
            return (this.rows.hasOwnProperty(pt.y) &&
                    this.rows[pt.y].hasOwnProperty(pt.x));
        },
        remove: function(pt) {
            if (this.rows.hasOwnProperty(pt.y)) {
                delete this.rows[pt.y][pt.x];
            }
        },
        toArray: function () {
            var a = [];
            var i = 0;
            var j = 0;
            var rowArray = Object.keys(this.rows); //pull all the y-values from the rows
            var colArray = [];
            while (i < rowArray.length) {
                j = 0;
                colArray = Object.keys(this.rows[rowArray[i]]); //pull x-values from each y-value in rows
                while (j < colArray.length) {
                    a.push(makePoint(colArray[j], rowArray[i]));
                    j = j + 1;
                }
                i = i + 1;
            }
            return a;
        }
    };
    return obj;
}


//--------------------------= HTML functions =--------------------------------------------------------------

/**
 * Pulls an HTML element from the HTML page so that it can be used. 
 * @param {string} id the name of the HTML element
 * @returns the HTML element, if it exists in the document
 */
function get(id) {
    var element = document.getElementById(id);
    if (element === null) {
        console.error("DOM id " + id + " not found!");
    }
    return element;
}
