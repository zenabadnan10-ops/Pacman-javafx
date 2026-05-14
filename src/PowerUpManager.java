import javafx.scene.image.Image;

import java.util.Random;

public class PowerUpManager {

    public static final int EFFECT_TICKS   = 200; 
    public static final int DISPLAY_TICKS  = 140;
    public static final int COOLDOWN_TICKS = 100; 
    public Block   active;
    public int     displayTimer;
    public int     effectTimer;
    public int     cooldownTimer;
    public boolean waitingForEffectEnd; 
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

    public void trySpawn(String[] map, int tileSize) {
        if (active != null || cooldownTimer > 0 || waitingForEffectEnd) return;
        int attempts = 0, r, c;
        do {
            r = random.nextInt(map.length);
            c = random.nextInt(map[0].length());
            if (++attempts > 200) return;
        } while (map[r].charAt(c) != ' ');
        active       = new Block(options[random.nextInt(3)],
                c * tileSize, r * tileSize, tileSize, tileSize);
        displayTimer = DISPLAY_TICKS;
    }

    public void tick() {
        if (effectTimer > 0 && --effectTimer == 0) {
            isFast = isFrozen = isInvincible = false;
            waitingForEffectEnd = false;
            cooldownTimer = COOLDOWN_TICKS; 
        }
        if (displayTimer > 0 && --displayTimer == 0) {
            active = null;
            cooldownTimer = COOLDOWN_TICKS; 
        }
        if (cooldownTimer > 0) cooldownTimer--;
    }

    public void collect() {
        isFast = isFrozen = isInvincible = false;
        if      (active.image == speedImg)  isFast       = true;
        else if (active.image == freezeImg) isFrozen     = true;
        else if (active.image == scaredImg) isInvincible = true;
        effectTimer         = EFFECT_TICKS;
        waitingForEffectEnd = true;  
        active              = null;
        displayTimer        = 0;
        cooldownTimer       = 0;    
    }

    public void reset() {
        active              = null;
        effectTimer         = 0;
        displayTimer        = 0;
        cooldownTimer       = 0;
        waitingForEffectEnd = false;
        isFast = isFrozen = isInvincible = false;
    }

    public String effectLabel() {
        return isFast ? "SPEED" : isFrozen ? "FREEZE" : isInvincible ? "INVINCIBLE" : "NONE";
    }
}
