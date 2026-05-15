import javafx.scene.paint.Color;

public abstract class Level {

    public final int    tileSize;
    public final int    ghostSpeed;
    public final int    ghostTurnNumerator;
    public final long   spawnIntervalNs;
    public final Color  wallTint;
    public final Color  accent;
    public final String label;
    public final String[] map;

    protected Level(int tileSize, int ghostSpeed, int ghostTurn,
                    long spawnNs, Color wallTint, Color accent,
                    String label, String[] map) {
        this.tileSize           = tileSize;
        this.ghostSpeed         = ghostSpeed;
        this.ghostTurnNumerator = ghostTurn;
        this.spawnIntervalNs    = spawnNs;
        this.wallTint           = wallTint;
        this.accent             = accent;
        this.label              = label;
        this.map                = map;
    }

    public abstract int dotScore();

    public abstract int ghostScore();

    public abstract void onDotEaten(PacMan game, int totalEaten);

    public abstract void onPowerUpCollected(PacMan game, PowerUpManager powerUps);

    public abstract int startingLives();

    public abstract String difficultyName();
}
