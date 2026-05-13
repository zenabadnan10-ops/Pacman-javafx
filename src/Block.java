import javafx.scene.image.Image;
import java.util.HashSet;

/** A single tile-aligned entity: wall, dot, ghost, pacman, or power-up. */
public class Block {
    public int x, y, width, height, startX, startY;
    public Image image;
    public char direction = 'U';
    public int velocityX, velocityY;

    private final int tileSize;
    private final HashSet<Block> walls;
    private int speed;

    /** Moving entity (ghost or pacman). */
    public Block(Image image, int x, int y, int size,
                 int tileSize, HashSet<Block> walls, int speed) {
        this.image = image;
        this.x = this.startX = x;
        this.y = this.startY = y;
        this.width = this.height = size;
        this.tileSize = tileSize;
        this.walls = walls;
        this.speed = speed;
    }

    /** Static entity (wall, food, power-up). */
    public Block(Image image, int x, int y, int w, int h) {
        this.image = image;
        this.x = this.startX = x;
        this.y = this.startY = y;
        this.width = w; this.height = h;
        this.tileSize = 0; this.walls = null;
    }

    public void setSpeed(int s) { speed = s; }

    public void updateDirection(char dir) {
        char prev = direction;
        direction = dir;
        updateVelocity();
        x += velocityX; y += velocityY;
        for (Block w : walls) {
            if (collides(w)) {
                x -= velocityX; y -= velocityY;
                direction = prev;
                updateVelocity();
                return;
            }
        }
    }

    public void updateVelocity() {
        velocityX = direction == 'L' ? -speed : direction == 'R' ? speed : 0;
        velocityY = direction == 'U' ? -speed : direction == 'D' ? speed : 0;
    }

    public void reset() {
        x = startX; y = startY;
        velocityX = velocityY = 0;
        direction = 'U';
    }

    public boolean collides(Block b) {
        return x < b.x + b.width  && x + width  > b.x
            && y < b.y + b.height && y + height > b.y;
    }
}
