// ===================================
//   Introduction to (Web) Programming
// ===================================

// --------------------------------------------
//   Final Program
//   Creative RPG - Dungeon Crawler
//   By Leah Behr
// --------------------------------------------


var CELL_SIZE = 12;
var DEFAULT_DELAY = 50;
var NEW_GLITCH_TIME = 100;
var NEW_CHASER_TIME = 200;
var ENEMY_MOVE_TIME = 10;
var BORDER_COLOR = "black";
var GOAL_COLOR = "gold";
var WALL_DENSITY = 0.25; //should be reduced
var WALL_COLOR = "rgb(132, 68, 0)";
var PLAYER_COLOR = "blue";
var GLITCH_COLOR = "rgba(201, 8, 219, 150)";
var CHASER_COLOR = "rgba(221, 51, 51, 150)";



var G = {};


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


// -----------------------------------------------------------------

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



// -----------------------------------------------------------------
// added makeEnemy;
function makePlayer(pt, color) {
    var obj = {
        x: pt.x,
        y: pt.y,
        color: color,

        move: function(dx, dy) {
            this.moveTo(makePoint(this.x + dx, this.y + dy));
        },

        moveTo: function(pt) {
            if (pt.x >= 1 && pt.x < G.columns - 1 && pt.y >= 1 && pt.y < G.rows - 1
               && !G.walls.has(pt)) {
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
    return obj;
}

function makeGlitch(color) {
    var spawnPt = makeRandomPoint();
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

function moveChasers() {
    var i = 0;
    while (i < G.chasers.length) {
        if (G.player.x < G.chasers[i].x) {
            var dx = -1;
        } else if (G.player.x > G.chasers[i].x) {
            dx = 1;
        } else {
            dx = 0;
        }
        if (G.player.y < G.chasers[i].y) {
            var dy = -1;
        } else if (G.player.y > G.chasers[i].y) {
            dy = 1;
        } else {
            dy = 0;
        }
        var destination1 = makePoint(G.chasers[i].x + dx, G.chasers[i].y + dy);
        var destination2 = null;
        var destinationFinal = null;
        var dySave = dy;
        if (G.walls.has(destination1)) {
            if (dx !== 0 && dy !==0) {
                dy = 0;
                destination2 = makePoint(G.chasers[i].x + dx, G.chasers[i].y + dy);
                if (G.walls.has(destination2)) {
                    dy = dySave;
                    dx = 0;
                    destinationFinal = makePoint(G.chasers[i].x + dx, G.chasers[i].y + dy);
                } else {
                    destinationFinal = destination2;
                }
            } else if (dy === 0) {
                dy = -1;
                destination2 = makePoint(G.chasers[i].x + dx, G.chasers[i].y + dy);
                if (G.walls.has(destination2)) {
                    dy = 1;
                    destinationFinal = makePoint(G.chasers[i].x + dx, G.chasers[i].y + dy);
                } else {
                    destinationFinal = destination2;
                }
            } else if (dx === 0) {
                dx = -1;
                destination2 = makePoint(G.chasers[i].x + dx, G.chasers[i].y + dy);
                if (G.walls.has(destination2)) {
                    dx = 1;
                    destinationFinal = makePoint(G.chasers[i].x + dx, G.chasers[i].y + dy);
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

function makeEnemy(color) {
    var spawnPt = makeRandomPoint();
    var enemy = makePlayer(spawnPt, color);
    return enemy;
}

// -----------------------------------------------------------------

function addGoal() {
    var x = 20;
    var y = 15;
    var goal = makePoint(x, y);
    while ((goal.x > 5 && goal.x < 35) && (goal.y > 5 && goal.y < 25)) {
        x = Math.floor(Math.random() * (G.columns - 2)) + 1;
        y = Math.floor(Math.random() * (G.rows - 2)) + 1;
        goal = makePoint(x, y);
    }
    G.goalPt = goal;
    //G.goalShow = false;
}



// -----------------------------------------------------------------

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



function fillCell(gamePt, color) {
    G.context.fillStyle = color;
    var cvsPt = toCanvasCoords(gamePt);
    G.context.fillRect(cvsPt.x - CELL_SIZE/2, cvsPt.y - CELL_SIZE/2,
                       CELL_SIZE, CELL_SIZE);
}



// -----------------------------------------------------------------
// CHANGES HERE!! to gameStep, showTime
function gameStep() {
    var i = 0;
    while (i < G.glitches.length) {
        if (G.player.y === G.glitches[i].y && G.player.x === G.glitches[i].x) {
            setStatus("You were killed by a glitch.");
            G.gameOn = false;
        } else if (G.player.y === G.goalPt.y && G.player.x === G.goalPt.x) {
            setStatus("You win!");
            G.gameOn = false;
        } else if (G.player.y === G.chasers[i].y && G.player.x === G.chasers[i].x) {
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


function gameLoop() {
    showTime();
    if (G.gameOn) {
        gameStep();
    }
    setTimeout(gameLoop, G.delay);
}


function setStatus(message) {
    var statusElement = get("status");
    statusElement.innerHTML = message;
}


function showTime()  {
    var seconds = Math.floor(((Date.now() - G.t0)) / 1000);
    var timerElement = get("timer");
    timerElement.innerHTML = "time passed: " + seconds;
}



// -----------------------------------------------------------------

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



function drawWalls() {
    var points = G.walls.toArray();
    var i = 0;
    while (i < points.length) {
        fillCell(points[i], WALL_COLOR);
        i = i + 1;
    }
}



// -----------------------------------------------------------------
// CHANGES HERE to add space as "pause"; add enemy movement
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


// -----------------------------------------------------------------
// Because I won't be using teleportation, fromCanvasCoords has been commented out.
//function fromCanvasCoords(canvasPt) {
//    var row = 0; // ...
//    var column = 0; // ...
//    return makePoint(column, row);
//}


function toCanvasCoords(gamePt) {
    return makePoint(gamePt.x * CELL_SIZE + CELL_SIZE/2,
                     gamePt.y* CELL_SIZE + CELL_SIZE/2);
}



// =======================================================================

function makePoint(x, y) {
    var obj = { x: x, y: y };
    return obj;
}

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
            var rowArray = Object.keys(this.rows);
            var colArray = [];
            while (i < rowArray.length) {
                j = 0;
                colArray = Object.keys(this.rows[rowArray[i]]);
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



// -----------------------------------------------------------------

//function manhattanDistance(p0, p1) {
//    var dx = Math.abs(p0.x - p1.x);
//    var dy = Math.abs(p0.y - p1.y);
//    return (dx + dy);
//}



// -----------------------------------------------------------------

function get(id) {
    var element = document.getElementById(id);
    if (element === null) {
        console.error("DOM id " + id + " not found!");
    }
    return element;
}
