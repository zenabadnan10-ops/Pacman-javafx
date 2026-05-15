import javafx.scene.paint.Color;

public class MediumLevel extends Level {

    private static final String[] MAP = {
        "XXXXXXXXXXXXXXXXXXX",
        "X  X     X     X  X",
        "X  X XXX X XXX X  X",
        "X              r  X",
        "XXXX X X   X X XXXX",
        "X    X XX XX X    X",
        "X XX X       X XX X",
        "X XX X       X XX X",
        "X      XXXXX      X",
        "X X            X  X",
        "X X XXXXXXXXX  X  X",
        "X X O    p  O  X  X",
        "X   O       O     X",
        "XXXX XXX XXX  XXXXX",
        "X                 X",
        "X  XX X P X XX    X",
        "X  XX X   X XX    X",
        "X      X X       bX",
        "XXXXXX X X XXXXXXXX",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    private static final int GHOST_SURGE_EVERY = 30;

    public MediumLevel(int tileSize) {
        super(tileSize,
              tileSize / 3,          
              2,                     
              7_000_000_000L,        
              Color.web("#1a6a4a"),
              Color.web("#5DCAA5"),
              "LVL 2  MEDIUM",
              MAP);
    }

    @Override public int dotScore()   { return 15; }  
    @Override public int ghostScore() { return 150; }

    @Override
    public void onDotEaten(PacMan game, int totalEaten) {
        
        if (totalEaten % GHOST_SURGE_EVERY == 0) {
            for (Block g : game.ghosts) {
                g.setSpeed(ghostSpeed + 2);
            }
            
            int normalSpeed = ghostSpeed;
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                for (Block g : game.ghosts) g.setSpeed(normalSpeed);
            }, "ghost-surge").start();
        }
    }

    @Override
    public void onPowerUpCollected(PacMan game, PowerUpManager powerUps) {
        
        if (powerUps.isFrozen) {
            game.score += 25;
        }
    }

    @Override public int    startingLives()   { return 2; }
    @Override public String difficultyName()  { return "MEDIUM"; }
}
