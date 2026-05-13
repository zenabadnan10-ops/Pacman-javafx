import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    static final int ROWS = 21, COLS = 19, TILE = 32;
    static final int W = COLS * TILE, H = ROWS * TILE;

    private static final Color[] GHOST_COLORS = {
        Color.web("#E24B4A"), Color.web("#F4C0D1"),
        Color.web("#85B7EB"), Color.web("#EF9F27")
    };

    private Stage stage;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Pac-Man");
        stage.setResizable(false);
        showMainMenu();
        stage.show();
    }

    // ------------------------------------------------------------------
    //  MAIN MENU
    // ------------------------------------------------------------------
    void showMainMenu() {
        PacmanAudio.playMainMenuMusic();

        // Background canvas
        Canvas bg = new Canvas(W, H);
        drawMenuBg(bg.getGraphicsContext2D());

        // Ghost parade
        HBox ghosts = new HBox(22);
        ghosts.setAlignment(Pos.CENTER);
        for (Color c : GHOST_COLORS) {
            Canvas gc = new Canvas(28, 32);
            drawGhost(gc.getGraphicsContext2D(), c, 0, 0, 28, 32);
            ghosts.getChildren().add(gc);
        }

        // Title
        Text title = styledText("PAC-MAN", 56, FontWeight.EXTRA_BOLD, "#FFE000");
        Text sub   = styledText("J A V A F X   E D I T I O N", 13,
                                FontWeight.NORMAL, "#888888");

        // Dot divider
        Canvas div = new Canvas(W * 0.6, 12);
        drawDotRow(div.getGraphicsContext2D(), (int)(W * 0.6));

        // Buttons
        Button play = menuButton("▶   PLAY", true);
        Button quit = menuButton("✕   QUIT", false);
        play.setOnAction(e -> startGame());
        quit.setOnAction(e -> { PacmanAudio.stopMusic(); stage.close(); });

        // Blinking coin text
        Text coin = styledText("— INSERT COIN —", 13, FontWeight.BOLD, "#FFE000");
        Timeline blink = new Timeline(
            new KeyFrame(Duration.millis(550), e -> coin.setVisible(true)),
            new KeyFrame(Duration.millis(1100), e -> coin.setVisible(false))
        );
        blink.setCycleCount(Animation.INDEFINITE);
        blink.play();

        VBox content = new VBox(16, ghosts, title, sub, div, play, quit, coin);
        content.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(bg, content);
        root.setStyle("-fx-background-color: black;");
        stage.setScene(new Scene(root, W, H));
    }

    // ------------------------------------------------------------------
    //  START GAME
    // ------------------------------------------------------------------
    void startGame() {
        PacmanAudio.stopMusic();
        PacmanAudio.intro();
        PacMan game = new PacMan(W, H, TILE, ROWS, COLS, this);
        Scene scene = new Scene(game, W, H);
        scene.setOnKeyPressed(game::handleKeyPressed);
        stage.setScene(scene);
        game.requestFocus();
        int lvl = game.getCurrentLevel();
        new Thread(() -> {
            try { Thread.sleep(1800); } catch (InterruptedException e) { return; }
            PacmanAudio.playLevelMusic(lvl);
        }, "music-start").start();
    }

    // ------------------------------------------------------------------
    //  DRAWING HELPERS
    // ------------------------------------------------------------------
    private void drawMenuBg(GraphicsContext gc) {
        gc.setFill(Color.BLACK); gc.fillRect(0, 0, W, H);
        gc.setFill(Color.web("#1a1a1a"));
        for (int x = TILE; x < W; x += TILE)
            for (int y = TILE; y < H; y += TILE)
                gc.fillOval(x - 2, y - 2, 4, 4);
        gc.setStroke(Color.web("#FFE000", 0.18)); gc.setLineWidth(1);
        gc.strokeLine(0, 68, W, 68);
        gc.strokeLine(0, H - 48, W, H - 48);
    }

    private void drawDotRow(GraphicsContext gc, int width) {
        for (int x = 5; x < width - 5; x += 14) {
            boolean big = (x / 14) % 5 == 2;
            if (big) { gc.setFill(Color.web("#FFE000")); gc.fillOval(x - 4, 1, 8, 8); }
            else     { gc.setFill(Color.WHITE);           gc.fillOval(x - 2, 3, 5, 5); }
        }
    }

    static void drawGhost(GraphicsContext gc, Color c,
                          double ox, double oy, double w, double h) {
        gc.setFill(c);
        gc.fillArc(ox, oy, w, h * 0.72, 0, 180, ArcType.ROUND);
        gc.fillRect(ox, oy + h * 0.36, w, h * 0.5);
        gc.setFill(Color.BLACK);
        double tw = w / 3.0;
        for (int i = 0; i < 3; i++) gc.fillOval(ox + i * tw, oy + h * 0.78, tw, tw * 0.9);
        gc.setFill(c);
        for (int i = 0; i < 2; i++) gc.fillRect(ox + i * tw + tw * 0.5, oy + h * 0.78, tw, tw * 0.45);
        gc.setFill(Color.WHITE);
        gc.fillOval(ox + w * 0.18, oy + h * 0.22, w * 0.28, h * 0.28);
        gc.fillOval(ox + w * 0.54, oy + h * 0.22, w * 0.28, h * 0.28);
        gc.setFill(Color.web("#1149CC"));
        gc.fillOval(ox + w * 0.26, oy + h * 0.30, w * 0.14, h * 0.14);
        gc.fillOval(ox + w * 0.62, oy + h * 0.30, w * 0.14, h * 0.14);
    }

    private Text styledText(String s, double size, FontWeight w, String hex) {
        Text t = new Text(s);
        t.setFont(Font.font("Courier New", w, size));
        t.setFill(Color.web(hex));
        return t;
    }

    private Button menuButton(String label, boolean primary) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 17));
        btn.setPrefWidth(220);
        String common = "-fx-padding: 9 32 9 32; -fx-cursor: hand;";
        String base  = primary
            ? "-fx-background-color:#FFE000;-fx-text-fill:#000;-fx-border-color:#FFE000;-fx-border-width:2;" + common
            : "-fx-background-color:transparent;-fx-text-fill:#888;-fx-border-color:#444;-fx-border-width:2;" + common;
        String hover = primary
            ? "-fx-background-color:#FFEE55;-fx-text-fill:#000;-fx-border-color:#FFEE55;-fx-border-width:2;" + common
            : "-fx-background-color:#222;-fx-text-fill:#aaa;-fx-border-color:#666;-fx-border-width:2;" + common;
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited (e -> btn.setStyle(base));
        return btn;
    }
}
