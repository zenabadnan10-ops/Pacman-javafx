import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashSet;

/** Handles all canvas rendering: world, HUD, and overlay screens. */
public class GameRenderer {

    // Overlay state constants (shared with PacMan)
    public static final int NONE           = 0;
    public static final int GAME_OVER      = 1;
    public static final int LEVEL_COMPLETE = 2;
    public static final int PAUSED         = 3;

    private final GraphicsContext gc;
    private final int boardWidth, boardHeight;

    public GameRenderer(GraphicsContext gc, int boardWidth, int boardHeight) {
        this.gc          = gc;
        this.boardWidth  = boardWidth;
        this.boardHeight = boardHeight;
    }

    // ------------------------------------------------------------------
    //  WORLD
    // ------------------------------------------------------------------
    public void drawWorld(HashSet<Block> walls, HashSet<Block> foods,
                          Block activePowerUp, HashSet<Block> ghosts,
                          Block pacman, boolean isInvincible, boolean isFrozen,
                          javafx.scene.image.Image scaredGhostImage,
                          Color wallTint) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, boardWidth, boardHeight);

        for (Block w : walls) {
            if (w.image != null) {
                gc.drawImage(w.image, w.x, w.y, w.width, w.height);
                gc.setFill(wallTint.deriveColor(0, 1, 1, 0.35));
                gc.fillRect(w.x, w.y, w.width, w.height);
            } else {
                gc.setFill(wallTint);
                gc.fillRect(w.x, w.y, w.width, w.height);
            }
        }

        gc.setFill(Color.WHITE);
        for (Block f : foods) gc.fillRect(f.x, f.y, f.width, f.height);

        if (activePowerUp != null && activePowerUp.image != null)
            gc.drawImage(activePowerUp.image,
                         activePowerUp.x, activePowerUp.y,
                         activePowerUp.width, activePowerUp.height);

        for (Block g : ghosts) {
            var img = isInvincible ? scaredGhostImage : g.image;
            if (img != null) gc.drawImage(img, g.x, g.y, g.width, g.height);
            if (isFrozen) {
                gc.setFill(Color.color(0, 0.59, 1.0, 0.31));
                gc.fillRect(g.x, g.y, g.width, g.height);
            }
        }

        if (pacman != null && pacman.image != null)
            gc.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height);
    }

    // ------------------------------------------------------------------
    //  HUD
    // ------------------------------------------------------------------
    public void drawHUD(int score, int lives, int currentLevel,
                        int effectTimer, String effectLabel, Color levelAccent) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, boardWidth, 28);
        gc.setFill(levelAccent);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        gc.fillText(levelLabel(currentLevel) + "   SCORE " + score, 10, 18);

        double lx = boardWidth - 14;
        for (int i = 0; i < lives; i++) {
            lx -= 18;
            gc.setFill(Color.web("#FFE000"));
            gc.fillArc(lx, 5, 14, 14, 30, 300, ArcType.ROUND);
        }

        if (effectTimer > 0) {
            Color bar = effectLabel.equals("SPEED") ? Color.web("#EF9F27")
                      : effectLabel.equals("FREEZE") ? Color.web("#85B7EB")
                      : Color.web("#E24B4A");
            gc.setFill(bar.deriveColor(0, 1, 1, 0.25));
            gc.fillRect(0, boardHeight - 6, boardWidth, 6);
            gc.setFill(bar);
            gc.fillRect(0, boardHeight - 6, boardWidth * (effectTimer / 200.0), 6);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
            gc.fillText(effectLabel + " " + (effectTimer / 20) + "s",
                        boardWidth / 2.0 - 28, boardHeight - 9);
        }
    }

    // ------------------------------------------------------------------
    //  OVERLAYS
    // ------------------------------------------------------------------
    public void drawOverlay(int state, int blinkTick,
                            int score, int lives, int currentLevel,
                            int completedLevel, int dotsEaten, int totalDots,
                            int ghostsEaten) {
        switch (state) {
            case GAME_OVER      -> drawGameOver(blinkTick, score, currentLevel);
            case LEVEL_COMPLETE -> drawLevelComplete(blinkTick, score, completedLevel,
                                                     dotsEaten, totalDots, ghostsEaten);
            case PAUSED         -> drawPaused();
        }
    }

    private void drawGameOver(int blink, int score, int level) {
        dimScreen(0.82);
        double[] r = panel(320, 260);
        double px = r[0], py = r[1], pw = r[2];

        panelBox(px, py, pw, r[3], Color.web("#E24B4A"));
        centeredBigText("GAME OVER",       px, py + 50, pw, 32, Color.web("#E24B4A"));
        centeredSmallText("YOU WERE EATEN!", px, py + 72, pw, Color.web("#888888"));
        dotRow(px + 20, py + 88, pw - 40);
        statRow(px, py + 118, pw, "LEVEL REACHED", levelLabel(level), levelAccent(level));
        statRow(px, py + 148, pw, "FINAL SCORE",   String.format("%,d", score), Color.web("#FFE000"));
        dotRow(px + 20, py + 162, pw - 40);
        centeredBlinkText("PRESS ANY KEY TO PLAY AGAIN", px, py + 195, pw, 13, blink);
        centeredSmallText("ESC  →  MAIN MENU", px, py + 225, pw, Color.web("#555555"));
    }

    private void drawLevelComplete(int blink, int score, int completed,
                                   int dotsEaten, int totalDots, int ghostsEaten) {
        dimScreen(0.82);
        boolean final3 = completed == 3;
        Color accent = levelAccent(completed);
        double[] r = panel(320, final3 ? 310 : 290);
        double px = r[0], py = r[1], pw = r[2];

        panelBox(px, py, pw, r[3], accent);
        centeredSmallText(final3 ? "✦  YOU WIN!  ✦" : "✦  STAGE CLEAR  ✦",
                          px, py + 34, pw, accent);
        centeredBigText(final3 ? "COMPLETE!" : "LEVEL " + completed,
                        px, py + 78, pw, 32, accent.deriveColor(0, 0.8, 1.5, 1));
        centeredSmallText(switch (completed) {
            case 1 -> "EASY  CLEARED"; case 2 -> "MEDIUM  CLEARED"; default -> "HARD  CLEARED";
        }, px, py + 104, pw, accent);

        dotRow(px + 20, py + 118, pw - 40);
        statRow(px, py + 148, pw, "DOTS EATEN",   dotsEaten + " / " + totalDots, accent);
        statRow(px, py + 178, pw, "GHOSTS EATEN", String.valueOf(ghostsEaten),    accent);
        statRow(px, py + 208, pw, "TOTAL SCORE",  String.format("%,d", score),    Color.web("#FFE000"));
        dotRow(px + 20, py + 222, pw - 40);

        if (final3) {
            centeredBlinkText("PRESS ANY KEY TO PLAY AGAIN", px, py + 256, pw, 13, blink);
            centeredSmallText("ESC  →  MAIN MENU", px, py + 285, pw, Color.web("#555555"));
        } else {
            String next = "PRESS ANY KEY  →  "
                        + (completed == 1 ? "NEXT: LVL 2  MEDIUM" : "NEXT: LVL 3  HARD");
            if ((blink / 18) % 2 == 0) {
                gc.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
                gc.setFill(levelAccent(completed + 1));
                gc.fillText(next, px + pw / 2 - charWidth(next, 12) / 2, py + 256);
            }
            centeredSmallText("ESC  →  MAIN MENU", px, py + 278, pw, Color.web("#555555"));
        }
    }

    private void drawPaused() {
        dimScreen(0.78);
        double[] r = panel(280, 180);
        double px = r[0], py = r[1], pw = r[2];
        panelBox(px, py, pw, r[3], Color.web("#FFE000"));
        centeredBigText("PAUSED",              px, py + 58, pw, 34, Color.web("#FFE000"));
        dotRow(px + 20, py + 74, pw - 40);
        centeredSmallText("P / ESC  →  RESUME",    px, py + 112, pw, Color.web("#aaaaaa"));
        centeredSmallText("M        →  MAIN MENU", px, py + 138, pw, Color.web("#aaaaaa"));
    }

    // ------------------------------------------------------------------
    //  DRAWING PRIMITIVES
    // ------------------------------------------------------------------
    private void dimScreen(double alpha) {
        gc.setFill(Color.color(0, 0, 0, alpha));
        gc.fillRect(0, 0, boardWidth, boardHeight);
    }

    private double[] panel(double pw, double ph) {
        double px = boardWidth / 2.0 - pw / 2;
        double py = boardHeight / 2.0 - ph / 2 - 40;
        return new double[]{px, py, pw, ph};
    }

    private void panelBox(double px, double py, double pw, double ph, Color border) {
        gc.setFill(Color.color(0.04, 0.02, 0.02, 0.95));
        gc.fillRoundRect(px, py, pw, ph, 12, 12);
        gc.setStroke(border); gc.setLineWidth(2.5);
        gc.strokeRoundRect(px, py, pw, ph, 12, 12);
    }

    /** Approximate character width for Courier New (monospace): fontSize * 0.60 */
    private double charWidth(String text, double fontSize) {
        return text.length() * fontSize * 0.60;
    }

    private void centeredBigText(String t, double px, double y, double pw,
                                 double size, Color c) {
        gc.setFont(Font.font("Courier New", FontWeight.EXTRA_BOLD, size));
        gc.setFill(c);
        gc.fillText(t, px + pw / 2 - charWidth(t, size) / 2, y);
    }

    private void centeredSmallText(String t, double px, double y, double pw, Color c) {
        gc.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
        gc.setFill(c);
        gc.fillText(t, px + pw / 2 - charWidth(t, 12) / 2, y);
    }

    private void centeredBlinkText(String t, double px, double y, double pw,
                                   double size, int tick) {
        if ((tick / 18) % 2 == 0) {
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, size));
            gc.setFill(Color.web("#FFE000"));
            gc.fillText(t, px + pw / 2 - charWidth(t, size) / 2, y);
        }
    }

    private void dotRow(double x, double y, double width) {
        for (double dx = x; dx < x + width; dx += 14) {
            boolean big = ((int)((dx - x) / 14)) % 5 == 2;
            if (big) { gc.setFill(Color.web("#FFE000", 0.6)); gc.fillOval(dx - 3, y - 3, 7, 7); }
            else     { gc.setFill(Color.web("#444444"));       gc.fillOval(dx - 2, y - 2, 4, 4); }
        }
    }

    private void statRow(double px, double y, double pw,
                         String label, String value, Color vc) {
        gc.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#888888"));
        gc.fillText(label, px + 24, y);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        gc.setFill(vc);
        gc.fillText(value, px + pw - 24 - charWidth(value, 16), y);
    }

    // ------------------------------------------------------------------
    //  HELPERS
    // ------------------------------------------------------------------
    public static String levelLabel(int level) {
        return switch (level) { case 2 -> "LVL 2  MEDIUM"; case 3 -> "LVL 3  HARD"; default -> "LVL 1  EASY"; };
    }

    public static Color levelAccent(int level) {
        return switch (level) { case 2 -> Color.web("#5DCAA5"); case 3 -> Color.web("#E24B4A"); default -> Color.web("#FFE000"); };
    }
}
