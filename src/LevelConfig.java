public class LevelConfig {

    public static Level forLevel(int level, int tileSize) {
        return switch (level) {
            case 2  -> new MediumLevel(tileSize);
            case 3  -> new HardLevel(tileSize);
            default -> new EasyLevel(tileSize);
        };
    }
}
