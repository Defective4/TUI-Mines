package io.github.defective4.javajam.tuisweeper.core.sfx;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SFX {
    private final AudioFormat FORMAT = new AudioFormat(44100, 16, 1, true, false);

    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    private final Map<String, byte[]> loadedSounds = new HashMap<>();
    private final List<String> queue = Collections.synchronizedList(new ArrayList<>());
    private final boolean available = AudioSystem.isLineSupported(new DataLine.Info(DataLine.class, FORMAT));
    private boolean enabled = true;

    public SFX() {
        load();

        service.scheduleAtFixedRate(() -> {

            synchronized (queue) {
                if (!queue.isEmpty()) for (String entry : queue.toArray(new String[0])) {
                    queue.remove(entry);
                    try {
                        byte[] data = loadedSounds.get(entry);
                        if (data != null) {
                            Clip c = AudioSystem.getClip();
                            c.open(FORMAT, data, 0, data.length);
                            c.addLineListener(event -> {
                                if (event.getType() == LineEvent.Type.STOP) c.close();
                            });
                            c.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void play(String sound) {
        if (!isAvailable() || !enabled) return;
        synchronized (queue) {
            queue.add(sound.toLowerCase());
        }
    }

    public boolean isAvailable() {
        return available;
    }

    private void load() {
        if (isAvailable())
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(SFX.class.getResourceAsStream(
                    "/sfx/index")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try (InputStream is = SFX.class.getResourceAsStream("/sfx/" + line + ".wav")) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        int read;
                        byte[] tmp = new byte[1024];
                        while ((read = is.read(tmp)) > 0) {
                            buffer.write(tmp, 0, read);
                        }

                        AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(buffer.toByteArray()));
                        buffer = new ByteArrayOutputStream();
                        while ((read = ais.read(tmp)) > 0) buffer.write(tmp, 0, read);

                        loadedSounds.put(line.toLowerCase(), buffer.toByteArray());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
