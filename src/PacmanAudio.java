import javax.sound.sampled.*;


public class PacmanAudio {

    private static final int SR = 44100; 

    private static double sine(double p)     { return Math.sin(p); }
    private static double square(double p)   { return Math.sin(p) >= 0 ? 1.0 : -1.0; }
    private static double triangle(double p) {
        double t = (p % (2*Math.PI)) / (2*Math.PI);
        return t < 0.5 ? 4*t - 1 : 3 - 4*t;
    }
    private static double sawtooth(double p) {
        return 2 * ((p % (2*Math.PI)) / (2*Math.PI)) - 1;
    }

    private static double adsr(int i, int frames, double a, double d, double s, double r) {
        double t = (double)i / SR, total = (double)frames / SR, se = total - r;
        if (t < a)      return t / a;
        if (t < a + d)  return 1.0 - (1.0 - s) * ((t - a) / d);
        if (t < se)     return s;
        if (t < total)  return s * (total - t) / r;
        return 0;
    }

    private static byte[] buildWave(double freq, double dur, double vol,
                                    int wt, double a, double d, double s, double r) {
        int frames = (int)(SR * dur);
        byte[] buf = new byte[frames * 2];
        for (int i = 0; i < frames; i++) {
            double ph  = 2 * Math.PI * freq * i / SR;
            double raw = switch (wt) { case 1 -> square(ph); case 2 -> triangle(ph);
                                       case 3 -> sawtooth(ph); default -> sine(ph); };
            short sample = (short)(raw * vol * adsr(i, frames, a, d, s, r) * Short.MAX_VALUE);
            buf[i*2]   = (byte)(sample & 0xFF);
            buf[i*2+1] = (byte)((sample >> 8) & 0xFF);
        }
        return buf;
    }

    private static byte[] buildTone(double freq, double dur, double vol) {
        return buildWave(freq, dur, vol, 0, 0.005, 0.05, 0.85, 0.1);
    }

    private static byte[] buildSweep(double f0, double f1, double dur, double vol, int wt) {
        int frames = (int)(SR * dur);
        byte[] buf = new byte[frames * 2];
        double phase = 0;
        for (int i = 0; i < frames; i++) {
            double freq = f0 + (f1 - f0) * i / frames;
            phase += 2 * Math.PI * freq / SR;
            double raw = switch (wt) { case 1 -> square(phase); case 2 -> triangle(phase);
                                       case 3 -> sawtooth(phase); default -> sine(phase); };
            short sample = (short)(raw * vol * adsr(i, frames, 0.005, 0.1, 0.7, 0.15) * Short.MAX_VALUE);
            buf[i*2]   = (byte)(sample & 0xFF);
            buf[i*2+1] = (byte)((sample >> 8) & 0xFF);
        }
        return buf;
    }

    private static byte[] mix(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        byte[] out = new byte[len];
        for (int i = 0; i < len - 1; i += 2) {
            short sa = (short)((a[i] & 0xFF) | (a[i+1] << 8));
            short sb = (short)((b[i] & 0xFF) | (b[i+1] << 8));
            int m = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sa + sb));
            out[i] = (byte)(m & 0xFF); out[i+1] = (byte)((m >> 8) & 0xFF);
        }
        return out;
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0; for (byte[] p : parts) total += p.length;
        byte[] out = new byte[total]; int pos = 0;
        for (byte[] p : parts) { System.arraycopy(p, 0, out, pos, p.length); pos += p.length; }
        return out;
    }

    private static byte[] silence(int ms) { return new byte[(int)(SR * ms / 1000.0) * 2]; }

    private static void play(byte[] buf) {
        new Thread(() -> {
            try {
                AudioFormat fmt = new AudioFormat(SR, 16, 1, true, false);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                    new DataLine.Info(SourceDataLine.class, fmt));
                line.open(fmt); line.start();
                line.write(buf, 0, buf.length);
                line.drain(); line.close();
            } catch (Exception e) { e.printStackTrace(); }
        }, "sfx").start();
    }

    private static volatile Thread  musicThread;
    private static volatile boolean musicRunning;

    public static void stopMusic() {
        musicRunning = false;
        if (musicThread != null) {
            musicThread.interrupt();
            try { musicThread.join(500); } catch (InterruptedException ignored) {}
            musicThread = null;
        }
    }

    private static void loopMelody(String name, double[][] mel, double vol) {
        stopMusic();
        musicRunning = true;
        musicThread = new Thread(() -> {
            AudioFormat fmt = new AudioFormat(SR, 16, 1, true, false);
            while (musicRunning && !Thread.currentThread().isInterrupted()) {
                for (double[] n : mel) {
                    if (!musicRunning || Thread.currentThread().isInterrupted()) break;
                    try {
                        if (n[0] > 0) {
                            byte[] buf = buildWave(n[0], n[1], vol, 2, 0.01, 0.04, 0.8, 0.15);
                            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                                new DataLine.Info(SourceDataLine.class, fmt));
                            line.open(fmt); line.start();
                            line.write(buf, 0, buf.length);
                            line.drain(); line.close();
                        }
                        if ((long)n[2] > 0) Thread.sleep((long)n[2]);
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                    catch (Exception e) { e.printStackTrace(); }
                }
            }
        }, name);
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public static void playMainMenuMusic() {
        loopMelody("music-menu", new double[][]{
            {523,.11,18},{659,.11,18},{784,.11,18},{1047,.11,18},
            {988,.11,18},{784,.11,18},{659,.11,18},{523,.11,55},
            {587,.11,18},{698,.11,18},{880,.11,18},{1047,.13,90}
        }, 0.17);
    }

    public static void playPauseMusic() {
        loopMelody("music-pause", new double[][]{
            {220,.22,50},{233,.22,50},{0,0,180},{196,.22,50},{175,.22,50},
            {0,0,260},{196,.22,50},{220,.22,80},{0,0,320}
        }, 0.15);
    }

    public static void playLevelMusic(int level) {
        switch (level) {
            case 2 -> loopMelody("music-l2", new double[][]{
                {392,.10,15},{349,.10,15},{330,.10,15},{294,.10,15},
                {311,.10,15},{349,.10,15},{392,.10,40},{440,.10,15},
                {415,.10,15},{392,.13,70}}, 0.20);
            case 3 -> loopMelody("music-l3", new double[][]{
                {523,.07,8},{554,.07,8},{587,.07,8},{622,.07,8},{659,.07,8},
                {698,.07,8},{740,.07,8},{784,.09,18},{740,.07,8},{698,.07,8},
                {659,.07,8},{622,.07,8},{587,.09,55}}, 0.22);
            default -> loopMelody("music-l1", new double[][]{
                {523,.10,15},{659,.10,15},{784,.10,15},{880,.10,15},
                {784,.10,15},{659,.10,15},{587,.10,15},{523,.10,15},
                {494,.10,15},{523,.13,65}}, 0.20);
        }
    }

    public static void intro() {
        new Thread(() -> {
            try {
                double[] scale = {262,294,330,349,392,440,494,523,659,784};
                byte[][] notes = new byte[scale.length][];
                for (int i = 0; i < scale.length; i++)
                    notes[i] = buildWave(scale[i], 0.12, 0.72, 2, 0.01, 0.03, 0.75, 0.11);
                byte[] chord = mix(mix(
                    buildWave(523, 0.40, 0.55, 2, 0.01, 0.08, 0.7, 0.20),
                    buildWave(659, 0.40, 0.45, 2, 0.01, 0.08, 0.7, 0.20)),
                    buildWave(784, 0.40, 0.38, 2, 0.01, 0.08, 0.7, 0.20));
                AudioFormat fmt = new AudioFormat(SR, 16, 1, true, false);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                    new DataLine.Info(SourceDataLine.class, fmt));
                line.open(fmt); line.start();
                for (byte[] p : notes) { line.write(p, 0, p.length); Thread.sleep(10); }
                line.write(chord, 0, chord.length);
                line.drain(); line.close();
            } catch (Exception e) { e.printStackTrace(); }
        }, "sfx-intro").start();
    }

    private static volatile boolean wakaPhase;
    public static void waka() {
        boolean ph = wakaPhase; wakaPhase = !wakaPhase;
        play(concat(buildWave(ph ? 380 : 480, 0.055, 0.78, 1, 0.002, 0.02, 0.5, 0.03),
                    silence(28),
                    buildWave(ph ? 480 : 380, 0.050, 0.72, 1, 0.002, 0.02, 0.4, 0.03)));
    }

    public static void powerPellet() {
        play(concat(buildSweep(150, 900, 0.18, 0.82, 3), silence(15),
                    buildWave(1200, 0.05, 0.65, 1, 0.002, 0.01, 0.6, 0.04), silence(15),
                    buildWave(1400, 0.05, 0.60, 1, 0.002, 0.01, 0.5, 0.04), silence(15),
                    buildWave(1600, 0.06, 0.55, 1, 0.002, 0.01, 0.4, 0.05)));
    }

    public static void ghostEaten(int combo) {
        play(switch (Math.min(combo, 3)) {
            case 0 -> concat(buildWave(300,.08,.78,0,.005,.04,.7,.03), silence(18),
                             buildWave(480,.10,.80,0,.005,.04,.7,.04));
            case 1 -> concat(buildSweep(400,700,.14,.82,2),
                             buildWave(700,.07,.75,2,.002,.03,.5,.04));
            case 2 -> concat(buildWave(600,.06,.82,1,.002,.02,.6,.02), silence(12),
                             buildWave(900,.06,.84,1,.002,.02,.6,.02), silence(12),
                             buildWave(1200,.08,.86,1,.002,.02,.6,.03));
            default -> concat(buildSweep(800,2000,.12,.88,3),
                              buildWave(2000,.10,.90,3,.001,.02,.5,.07));
        });
    }

    public static void death() {
        byte[] flutter = new byte[0];
        for (double f : new double[]{320,260,300,240,270,210,240,180})
            flutter = concat(flutter, buildWave(f,.055,.78,1,.002,.01,.6,.02), silence(8));
        play(concat(buildSweep(600,200,.30,.85,3), silence(20),
                    flutter, silence(15), buildSweep(180,50,.28,.80,0)));
    }

    public static void levelComplete() {
        byte[] arpe = concat(
            buildWave(523,.09,.78,2,.005,.03,.75,.05), buildWave(659,.09,.80,2,.005,.03,.75,.05),
            buildWave(784,.09,.82,2,.005,.03,.75,.05), buildWave(1047,.16,.85,2,.005,.05,.80,.08),
            silence(20),
            buildWave(659,.08,.80,2,.004,.03,.75,.04), buildWave(784,.08,.82,2,.004,.03,.75,.04),
            buildWave(1047,.08,.84,2,.004,.03,.75,.04), buildWave(1319,.14,.86,2,.004,.05,.78,.07),
            silence(15));
        byte[] chord = mix(mix(buildWave(523,.50,.58,2,.01,.06,.72,.22),
                               buildWave(659,.50,.50,2,.01,.06,.72,.22)),
                              buildWave(784,.50,.44,2,.01,.06,.72,.22));
        play(concat(arpe, chord));
    }

    public static void extraLife() {
        byte[] arp = new byte[0];
        for (double f : new double[]{392,523,659,784,1047,784,1047})
            arp = concat(arp, buildWave(f,.065,.76,1,.002,.02,.6,.03));
        play(concat(arp, silence(12),
                    buildWave(1568,.12,.80,0,.003,.04,.6,.07), silence(8),
                    buildWave(2093,.16,.84,0,.003,.04,.5,.10)));
    }
}
