import javafx.scene.paint.Color;

/** Holds all data that changes between levels: map, difficulty, and theme. */
public class LevelConfig {

    // ------------------------------------------------------------------
    //  MAZE LAYOUTS  (19 cols × 21 rows)
    //  X=wall  ' '=dot  O=open  P=pacman  r/b/o/p=ghosts
    // ------------------------------------------------------------------
    private static final String[] MAP_1 = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X   X    X   X    X",
        "XXXX XX  X  XX XXXX",
        "OOOX X         XOOO",
        "XXXX X XXXXX X XXXX",
        "O      O r O      O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX XX     XX XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X  P      X    X",
        "XX X X XXXXX X X XX",
        "X    X   b   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    private static final String[] MAP_2 = {
        "XXXXXXXXXXXXXXXXXXX",
        "X  X     X     X  X",
        "X  X XXX X XXX X  X",
        "X                 X",
        "XXXX X X   X X XXXX",
        "X    X XXXXX X    X",
        "X XX X       X XX X",
        "X XX X  r    X XX X",
        "X    XXXXXXX      X",
        "X X            X  X",
        "X X XXXXXXXXX  X  X",
        "X X O       O  X  X",
        "X   O  b p  O     X",
        "XXXX XXXXXXX  XXXXX",
        "X                 X",
        "X  XX X P X XX    X",
        "X  XX X X X XX    X",
        "X      X X        X",
        "XXXXXX X X XXXXXXXX",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    private static final String[] MAP_3 = {
        "XXXXXXXXXXXXXXXXXXX",
        "X X   X   X   X   X",
        "X X X X X X X X X X",
        "X   X   X   X   X X",
        "XXX X XXXXX X XXXXX",
        "X   X       X     X",
        "X XXXXX X XXXXX X X",
        "X X   X X X   X X X",
        "X X X   r   X X X X",
        "X X XXXXXXX X X   X",
        "X           X b   X",
        "X XXXXXXXXX X XXXXX",
        "X X       X       X",
        "X X XXX o X p XXX X",
        "X X X   X X X   X X",
        "X X X X   X X   X X",
        "X   X XXXXX   X   X",
        "X P X         X   X",
        "XXX X XXXXXXX X XXX",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    // ------------------------------------------------------------------
    //  DIFFICULTY
    // ------------------------------------------------------------------
    public final int    tileSize;
    public final int    ghostSpeed;
    public final int    ghostTurnNumerator;
    public final long   spawnIntervalNs;
    public final Color  wallTint;
    public final Color  accent;
    public final String label;
    public final String[] map;

    private LevelConfig(int tileSize, int ghostSpeed, int ghostTurn,
                        long spawnNs, Color wallTint, Color accent,
                        String label, String[] map) {
        this.tileSize          = tileSize;
        this.ghostSpeed        = ghostSpeed;
        this.ghostTurnNumerator = ghostTurn;
        this.spawnIntervalNs   = spawnNs;
        this.wallTint          = wallTint;
        this.accent            = accent;
        this.label             = label;
        this.map               = map;
    }

    public static LevelConfig forLevel(int level, int tileSize) {
        return switch (level) {
            case 2  -> new LevelConfig(tileSize, tileSize / 3, 2,
                7_000_000_000L, Color.web("#1a6a4a"), Color.web("#5DCAA5"),
                "LVL 2  MEDIUM", MAP_2);
            case 3  -> new LevelConfig(tileSize, Math.max(tileSize / 2, 4), 1,
                9_000_000_000L, Color.web("#7a1a1a"), Color.web("#E24B4A"),
                "LVL 3  HARD", MAP_3);
            default -> new LevelConfig(tileSize, tileSize / 4, 3,
                5_000_000_000L, Color.web("#1a3a9a"), Color.web("#FFE000"),
                "LVL 1  EASY", MAP_1);
        };
    }
}
