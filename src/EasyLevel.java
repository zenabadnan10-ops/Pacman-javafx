import javafx.scene.paint.Color;

public class EasyLevel extends Level {

    private static final String[] MAP = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X        X   X    X",
        "XXXX XX  X  XX XXXX",
        "OOOX X         XOOO",
        "XXXX X XXXXX X XXXX",
        "O      O r O      O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX XX  P  XX XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X         X    X",
        "XX X X XXXXX X X XX",
        "X    X   b   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    public EasyLevel(int tileSize) {
        super(tileSize,
              tileSize / 4,          
              3,                      
              5_000_000_000L,         
              Color.web("#1a3a9a"),
              Color.web("#FFE000"),
              "LVL 1  EASY",
              MAP);
    }

    @Override public int dotScore()   { return 10; }
    @Override public int ghostScore() { return 100; }

    @Override
    public void onDotEaten(PacMan game, int totalEaten) {
        
    }

    @Override
    public void onPowerUpCollected(PacMan game, PowerUpManager powerUps) {
        
    }

    @Override public int    startingLives()   { return 1; }
    @Override public String difficultyName()  { return "EASY"; }
}
