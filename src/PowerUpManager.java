import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Random;

/** Manages power-up spawning and active-effect timers. */
public class PowerUpManager {

    public static final int EFFECT_TICKS = 200;

    public Block  active;
    public int    displayTimer;
    public int    effectTimer;
    public boolean isFast, isFrozen, isInvincible;

    private final Image speedImg, freezeImg, scaredImg;
    private final Image[] options;
    private final Random  random;

    public PowerUpManager(Image speedImg, Image freezeImg, Image scaredImg) {
        this.speedImg  = speedImg;
        this.freezeImg = freezeImg;
        this.scaredImg = scaredImg;
        this.options   = new Image[]{speedImg, freezeImg, scaredImg};
        this.random    = new Random();
    }

    /** Try to spawn a power-up at a random open tile. */
    public void trySpawn(String[] map, int tileSize) {
        if (active != null) return;
        int attempts = 0, r, c;
        do {
            r = random.nextInt(map.length);
            c = random.nextInt(map[0].length());
            if (++attempts > 200) return;
        } while (map[r].charAt(c) != ' ');
        active       = new Block(options[random.nextInt(3)],
                                 c * tileSize, r * tileSize, tileSize, tileSize);
        displayTimer = EFFECT_TICKS;
    }

    /** Tick down timers each game step. */
    public void tick() {
        if (effectTimer > 0 && --effectTimer == 0)
            isFast = isFrozen = isInvincible = false;
        if (displayTimer > 0 && --displayTimer == 0)
            active = null;
    }

    /** Apply the collected power-up effect. */
    public void collect() {
        isFast = isFrozen = isInvincible = false;
        if      (active.image == speedImg)  isFast       = true;
        else if (active.image == freezeImg) isFrozen     = true;
        else if (active.image == scaredImg) isInvincible = true;
        effectTimer  = EFFECT_TICKS;
        active       = null;
        displayTimer = 0;
    }

    public void reset() {
        active = null;
        effectTimer = displayTimer = 0;
        isFast = isFrozen = isInvincible = false;
    }

    /** Label for the HUD timer bar. */
    public String effectLabel() {
        return isFast ? "SPEED" : isFrozen ? "FREEZE" : "POWER";
    }
}
