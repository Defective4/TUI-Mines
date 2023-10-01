package io.github.defective4.javajam.tuisweeper.core.replay;

import io.github.defective4.javajam.tuisweeper.core.Difficulty;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class ReplayIO {
    private ReplayIO() {
    }

    public static void write(Replay replay, File output) throws IOException {
        try (DataOutputStream os = new DataOutputStream(new DeflaterOutputStream(Files.newOutputStream(output.toPath())))) {
            byte[] header = "TUIRPL1".getBytes();
            os.writeInt(header.length);
            os.write(header); // Write header
            os.write(Arrays.copyOf(replay.getMetadata().getIdentifier().getBytes(StandardCharsets.UTF_8),
                                   10)); // Write identifier
            os.writeLong(replay.getStartTime()); // Write start time
            replay.getMetadata().setCreatedDate(System.currentTimeMillis()); // Update and write creation date
            os.writeLong(replay.getMetadata().getCreatedDate());
            os.writeByte(replay.getMetadata().getDifficulty().getId()); // Write difficulty

            os.writeInt(replay.getWidth());
            os.writeInt(replay.getHeight()); // Write WidthxHeight

            List<Replay.CoordPair> bombs = replay.getBombs(); // Write bombs
            os.writeInt(bombs.size());
            for (Replay.CoordPair bomb : bombs) {
                os.writeInt(bomb.getX());
                os.writeInt(bomb.getY());
            }

            List<Replay.Action> actions = replay.getActions(); // Write actions
            os.writeInt(actions.size());
            for (Replay.Action act : actions) {
                os.writeByte(act.getAction().getId());
                os.writeInt(act.getX());
                os.writeInt(act.getY());
                os.writeLong(act.getTimestamp());
            }
        }
    }

    public static Replay read(File in) throws IOException {
        try (InputStream is = Files.newInputStream(in.toPath())) {
            Replay rpl = read(is);
            rpl.getMetadata().setOrigin(in);
            return rpl;
        }
    }

    public static Replay read(InputStream input) throws IOException {
        try (DataInputStream is = new DataInputStream(new InflaterInputStream(input))) {
            byte[] header = new byte[is.readInt()];
            is.read(header);
            if (!"TUIRPL1".equals(new String(header))) {
                throw new IOException("Invalid file header!");
            }
            byte[] id = new byte[10];
            is.read(id);
            String identifier = new String(id, StandardCharsets.UTF_8).trim();
            long startTime = is.readLong();
            long createTime = is.readLong();
            Difficulty diff = Difficulty.getByID(is.readByte());
            int width = is.readInt();
            int height = is.readInt();
            int count;
            count = is.readInt();
            Replay rp = new Replay(startTime);
            List<Replay.CoordPair> bombs = rp.getBombs();
            for (int x = 0; x < count; x++)
                bombs.add(new Replay.CoordPair(is.readInt(), is.readInt()));
            count = is.readInt();
            List<Replay.Action> actions = rp.getActions();
            for (int x = 0; x < count; x++) {
                Replay.ActionType type = Replay.ActionType.getByID(is.readByte());
                if (type != null)
                    actions.add(new Replay.Action(type, is.readInt(), is.readInt(), is.readLong()));
            }

            rp.setHeight(height);
            rp.setWidth(width);
            rp.getMetadata().setIdentifier(identifier);
            rp.getMetadata().setDifficulty(diff);
            rp.getMetadata().setCreatedDate(createTime);

            return rp;
        }
    }
}
