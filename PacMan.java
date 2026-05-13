import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import java.util.HashSet;
import java.util.Random;

/**
 * Main game pane. Delegates rendering to GameRenderer, power-ups to
 * PowerUpManager, and level data to LevelConfig.
 */
public class PacMan extends Pane {

    // ------------------------------------------------------------------
    //  CONSTANTS
    // ------------------------------------------------------------------
    private static final long MOVE_NS = 50_000_000L;
    private static final char[] DIRS = {'U', 'D', 'L', 'R'};

    // ------------------------------------------------------------------
    //  STATE
    // ------------------------------------------------------------------
    private final int boardWidth, boardHeight, tileSize;
    private final App app;
    private final Random rng = new Random();

    // Game state
    int score = 0, lives = 3;
    boolean gameOver = false;
    int overlay = GameRenderer.NONE; // NONE / GAME_OVER / LEVEL_COMPLETE / PAUSED
    int blinkTick, dotsEaten, totalDots, ghostsEaten;
    int currentLevel = 1, completedLevel = 1;
    int ghostCombo, dotsSinceWaka;
    char requestedDir = ' ';

    // Entity sets
    HashSet<Block> walls = new HashSet<>(), foods = new HashSet<>(), ghosts = new HashSet<>();
    Block pacman;

    // Sub-systems
    private LevelConfig     cfg;
    private PowerUpManager  powerUps;
    private GameRenderer    renderer;
    private AnimationTimer  gameLoop;
    private long lastSpawnTime, lastMoveTime;

    // Images
    private Image wallImg, blueGhost, orangeGhost, pinkGhost, redGhost;
    private Image pacUp, pacDown, pacLeft, pacRight;
    private Image speedImg, freezeImg, scaredImg;

    // ------------------------------------------------------------------
    //  CONSTRUCTOR
    // ------------------------------------------------------------------
    public PacMan(int boardWidth, int boardHeight, int tileSize,
                  int rowCount, int columnCount, App app) {
        this.boardWidth = boardWidth; this.boardHeight = boardHeight;
        this.tileSize   = tileSize;  this.app         = app;

        var canvas = new Canvas(boardWidth, boardHeight);
        getChildren().add(canvas);
        setStyle("-fx-background-color: black;");

        loadImages();
        powerUps = new PowerUpManager(speedImg, freezeImg, scaredImg);
        renderer = new GameRenderer(canvas.getGraphicsContext2D(), boardWidth, boardHeight);

        applyLevel(currentLevel);
        loadMap();
        startLoop();
    }

    public int getCurrentLevel() { return currentLevel; }

    // ------------------------------------------------------------------
    //  SETUP
    // ------------------------------------------------------------------
    private void applyLevel(int level) {
        cfg = LevelConfig.forLevel(level, tileSize);
    }

    private void loadImages() {
        wallImg     = img("wall.png");
        blueGhost   = img("blueGhost.png");   orangeGhost = img("orangeGhost.png");
        pinkGhost   = img("pinkGhost.png");   redGhost    = img("redGhost.png");
        pacUp       = img("pacmanUp.png");    pacDown     = img("pacmanDown.png");
        pacLeft     = img("pacmanLeft.png");  pacRight    = img("pacmanRight.png");
        speedImg    = img("speed.png");       freezeImg   = img("freeze.png");
        scaredImg   = img("scaredGhost.png");
    }

    private Image img(String name) {
        // Filesystem: images/ folder at project root (next to src/)
        java.io.File f = new java.io.File("images/" + name);
        if (f.exists()) {
            try { return new Image(new java.io.FileInputStream(f)); }
            catch (java.io.FileNotFoundException ignored) {}
        }
        // Fallback: classpath (when packaged into a JAR with images/ inside)
        var s = getClass().getResourceAsStream("/images/" + name);
        if (s != null) return new Image(s);
        System.err.println("Missing image: " + name);
        return null;
    }

    public void loadMap() {
        walls.clear(); foods.clear(); ghosts.clear();
        powerUps.reset();
        for (int r = 0; r < cfg.map.length; r++) {
            String row = cfg.map[r];
            for (int c = 0; c < row.length(); c++) {
                char ch = row.charAt(c);
                int x = c * tileSize, y = r * tileSize;
                switch (ch) {
                    case 'X' -> walls.add(new Block(wallImg, x, y, tileSize, tileSize));
                    case 'b' -> addGhost(blueGhost,   x, y);
                    case 'o' -> addGhost(orangeGhost, x, y);
                    case 'p' -> addGhost(pinkGhost,   x, y);
                    case 'r' -> addGhost(redGhost,    x, y);
                    case 'P' -> pacman = new Block(pacRight, x, y,
                                    tileSize, tileSize, walls, tileSize / 4);
                    case ' ' -> foods.add(new Block(null, x + 14, y + 14, 4, 4));
                }
            }
        }
        totalDots = foods.size(); dotsEaten = ghostsEaten = 0;
        for (Block g : ghosts) g.updateDirection(DIRS[rng.nextInt(4)]);
    }

    private void addGhost(Image img, int x, int y) {
        ghosts.add(new Block(img, x, y, tileSize, tileSize, walls, cfg.ghostSpeed));
    }

    // ------------------------------------------------------------------
    //  GAME LOOP
    // ------------------------------------------------------------------
    private void startLoop() {
        gameLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (overlay != GameRenderer.NONE) { blinkTick++; draw(); return; }
                if (lastSpawnTime == 0) lastSpawnTime = now;
                if (now - lastSpawnTime >= cfg.spawnIntervalNs) {
                    powerUps.trySpawn(cfg.map, tileSize);
                    lastSpawnTime = now;
                }
                if (lastMoveTime == 0) lastMoveTime = now;
                if (now - lastMoveTime >= MOVE_NS) { move(); draw(); lastMoveTime = now; }
            }
        };
        gameLoop.start();
    }

    // ------------------------------------------------------------------
    //  MOVE
    // ------------------------------------------------------------------
    private void move() {
        if (gameOver || overlay != GameRenderer.NONE) return;
        powerUps.tick();

        // Pacman (2 steps when fast)
        for (int i = 0, steps = powerUps.isFast ? 2 : 1; i < steps; i++)
            movePacman();

        // Ghosts
        if (!powerUps.isFrozen)
            for (Block g : ghosts) moveGhost(g);

        // Power-up collection
        if (powerUps.active != null && pacman.collides(powerUps.active)) {
            powerUps.collect();
            score += 50;
            PacmanAudio.powerPellet();
        }

        // Ghost collision
        for (Block g : ghosts) {
            if (!g.collides(pacman)) continue;
            if (powerUps.isInvincible) {
                g.reset(); g.updateDirection(DIRS[rng.nextInt(4)]);
                score += 100 * (ghostCombo + 1); ghostsEaten++;
                PacmanAudio.ghostEaten(ghostCombo++);
            } else if (!powerUps.isFrozen) {
                loseLife(); return;
            }
        }

        // Dot collection
        int before = foods.size();
        foods.removeIf(f -> { if (pacman.collides(f)) { score += 10; return true; } return false; });
        int eaten = before - foods.size();
        if (eaten > 0) {
            dotsEaten += eaten;
            if ((dotsSinceWaka += eaten) >= 2) { PacmanAudio.waka(); dotsSinceWaka = 0; }
        }

        if (foods.isEmpty()) {
            PacmanAudio.stopMusic(); PacmanAudio.levelComplete();
            completedLevel = currentLevel;
            overlay = GameRenderer.LEVEL_COMPLETE;
        }
    }

    private void movePacman() {
        if (requestedDir != ' ' && pacman.x % tileSize == 0 && pacman.y % tileSize == 0) {
            char prev = pacman.direction;
            int pvx = pacman.velocityX, pvy = pacman.velocityY;
            pacman.direction = requestedDir;
            pacman.updateVelocity();
            pacman.x += pacman.velocityX; pacman.y += pacman.velocityY;
            boolean hit = walls.stream().anyMatch(pacman::collides);
            pacman.x -= pacman.velocityX; pacman.y -= pacman.velocityY;
            if (!hit) { requestedDir = ' '; updatePacImg(); }
            else { pacman.direction = prev; pacman.velocityX = pvx; pacman.velocityY = pvy; }
        }
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        if      (pacman.x < 0)                        pacman.x = boardWidth - pacman.width;
        else if (pacman.x + pacman.width > boardWidth) pacman.x = 0;
        for (Block w : walls) {
            if (pacman.collides(w)) { pacman.x -= pacman.velocityX; pacman.y -= pacman.velocityY; break; }
        }
    }

    private void moveGhost(Block g) {
        g.x += g.velocityX; g.y += g.velocityY;
        if      (g.x < 0)                    g.x = boardWidth - g.width;
        else if (g.x + g.width > boardWidth) g.x = 0;
        if (g.x % tileSize == 0 && g.y % tileSize == 0
                && rng.nextInt(10) < cfg.ghostTurnNumerator)
            g.updateDirection(DIRS[rng.nextInt(4)]);
        for (Block w : walls) {
            if (g.collides(w)) {
                g.x -= g.velocityX; g.y -= g.velocityY;
                g.updateDirection(DIRS[rng.nextInt(4)]);
                break;
            }
        }
    }

    private void loseLife() {
        lives--;
        ghostCombo = 0;
        PacmanAudio.stopMusic(); PacmanAudio.death();
        if (lives <= 0) { gameOver = true; overlay = GameRenderer.GAME_OVER; return; }
        resetPositions();
        delayedMusic(1200);
    }

    // ------------------------------------------------------------------
    //  HELPERS
    // ------------------------------------------------------------------
    public void resetPositions() {
        pacman.reset(); pacman.updateVelocity(); requestedDir = ' ';
        for (Block g : ghosts) { g.reset(); g.updateDirection(DIRS[rng.nextInt(4)]); }
        powerUps.reset();
    }

    private void updatePacImg() {
        pacman.image = switch (pacman.direction) {
            case 'U' -> pacUp; case 'D' -> pacDown;
            case 'L' -> pacLeft; default -> pacRight;
        };
    }

    private void delayedMusic(long delayMs) {
        int lvl = currentLevel;
        new Thread(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) { return; }
            if (!gameOver && overlay == GameRenderer.NONE) PacmanAudio.playLevelMusic(lvl);
        }, "music-delay").start();
    }

    // ------------------------------------------------------------------
    //  DRAW
    // ------------------------------------------------------------------
    private void draw() {
        renderer.drawWorld(walls, foods, powerUps.active, ghosts, pacman,
                           powerUps.isInvincible, powerUps.isFrozen,
                           scaredImg, cfg.wallTint);
        renderer.drawHUD(score, lives, currentLevel, powerUps.effectTimer,
                         powerUps.effectLabel(), cfg.accent);
        if (overlay != GameRenderer.NONE)
            renderer.drawOverlay(overlay, blinkTick, score, lives, currentLevel,
                                 completedLevel, dotsEaten, totalDots, ghostsEaten);
    }

    // ------------------------------------------------------------------
    //  INPUT
    // ------------------------------------------------------------------
    public void handleKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case UP,    W -> { if (overlay == GameRenderer.NONE) requestedDir = 'U'; }
            case DOWN,  S -> { if (overlay == GameRenderer.NONE) requestedDir = 'D'; }
            case LEFT,  A -> { if (overlay == GameRenderer.NONE) requestedDir = 'L'; }
            case RIGHT, D -> { if (overlay == GameRenderer.NONE) requestedDir = 'R'; }

            case P -> togglePause();
            case M -> { if (overlay == GameRenderer.PAUSED) exitToMenu(); }
            case ESCAPE -> {
                if      (overlay == GameRenderer.PAUSED)         { togglePause(); }
                else if (overlay == GameRenderer.GAME_OVER
                      || overlay == GameRenderer.LEVEL_COMPLETE) { exitToMenu(); }
                else if (overlay == GameRenderer.NONE)            { togglePause(); }
            }
            default -> handleOverlayAction();
        }
    }

    private void togglePause() {
        if (overlay == GameRenderer.NONE) {
            overlay = GameRenderer.PAUSED;
            PacmanAudio.stopMusic(); PacmanAudio.playPauseMusic();
        } else if (overlay == GameRenderer.PAUSED) {
            overlay = GameRenderer.NONE;
            PacmanAudio.stopMusic(); PacmanAudio.playLevelMusic(currentLevel);
        }
    }

    private void handleOverlayAction() {
        if (overlay == GameRenderer.GAME_OVER) { fullRestart(); }
        else if (overlay == GameRenderer.LEVEL_COMPLETE) {
            if (completedLevel >= 3) { fullRestart(); }
            else {
                currentLevel = completedLevel + 1;
                applyLevel(currentLevel);
                loadMap(); resetPositions();
                overlay = GameRenderer.NONE;
                lastSpawnTime = 0;
                for (Block g : ghosts) g.setSpeed(cfg.ghostSpeed);
                delayedMusic(1400);
            }
        }
    }

    private void fullRestart() {
        PacmanAudio.stopMusic(); PacmanAudio.intro();
        score = 0; lives = 3; gameOver = false;
        currentLevel = 1; ghostCombo = 0; dotsSinceWaka = 0;
        overlay = GameRenderer.NONE; lastSpawnTime = 0;
        applyLevel(1);
        loadMap(); resetPositions();
        delayedMusic(1800);
    }

    private void exitToMenu() {
        gameLoop.stop(); PacmanAudio.stopMusic();
        Platform.runLater(app::showMainMenu);
    }
}