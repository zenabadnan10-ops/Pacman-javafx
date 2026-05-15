import javafx.scene.paint.Color;

public class HardLevel extends Level {

    private static final String[] MAP = {
        "XXXXXXXXXXXXXXXXXXX",
        "X X   X   X   X   X",
        "X X X X X X X X X X",
        "X   X   X   X b X X",
        "XXX X XXXXX X XXXXX",
        "X   X       X     X",
        "X XXXXX X XXXXX X X",
        "X X   X X X   X X X",
        "X X X   r   X X X X",
        "X X XXXXXXX X X   X",
        "X           X     X",
        "X XXXXXXXXX X XXXXX",
        "X X       X       X",
        "X X XXX o X   XXX X",
        "X X X   X X X   X X",
        "X X X X   X X   X X",
        "X   X XXXXX   X   X",
        "X P X         X   X",
        "XXX X  XX  XX p XXX",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    private static final int ESCALATION_THRESHOLD = 50;
    private boolean escalated = false;

    public HardLevel(int tileSize) {
        super(tileSize,
              Math.max(tileSize / 2, 4), 
              1,                        
              9_000_000_000L,           
              Color.web("#7a1a1a"),
              Color.web("#E24B4A"),
              "LVL 3  HARD",
              MAP);
    }

    @Override public int dotScore()   { return 20; }  
    @Override public int ghostScore() { return 200; }

    @Override
    public void onDotEaten(PacMan game, int totalEaten) {
        if (!escalated && totalEaten >= ESCALATION_THRESHOLD) {
            escalated = true;
            int boosted = ghostSpeed + 3;
            for (Block g : game.ghosts) g.setSpeed(boosted);
        }
    }

    @Override
    public void onPowerUpCollected(PacMan game, PowerUpManager powerUps) {
        if (powerUps.isInvincible) {
            game.score += game.ghostsEaten * 50;
        }
    }

    @Override public int    startingLives()   { return 3; }
    @Override public String difficultyName()  { return "HARD"; }
}
