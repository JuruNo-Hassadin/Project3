package Project3_6813256;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Reads and appends every finished run to a portable TSV file. */
public final class LeaderboardStore {
    private static final String PATH = "src/main/java/Project3_6813256/resources/";
    private static final String SEPARATOR = "\t";
    private final Path file;

    public LeaderboardStore() {
        this(Path.of(PATH + "leaderboard.txt"));
    }

    public LeaderboardStore(Path file) {
        this.file = file;
    }

    public synchronized void save(LeaderboardEntry entry) throws IOException {
        String row = String.join(SEPARATOR,
                entry.playedAt().toString(),
                clean(entry.playerName()),
                entry.difficulty().name(),
                Integer.toString(entry.floorsClimbed()),
                Long.toString(entry.durationSeconds()));
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);   // update parent back to file object
        }
        Files.writeString(file, row + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);    // save to file
    }

    public synchronized List<LeaderboardEntry> loadAll() throws IOException {
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (!Files.exists(file)) {
            return entries;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length != 5) {
                continue;
            }
            try {
                entries.add(new LeaderboardEntry(    // load all lines from storage file back to arraylist
                        LocalDateTime.parse(fields[0]),
                        fields[1],
                        Difficulty.valueOf(fields[2]),
                        Integer.parseInt(fields[3]),
                        Long.parseLong(fields[4])));
            } catch (RuntimeException ignored) {
                // Ignore malformed rows so one bad record does not hide the rest.
            }
        }
        entries.sort(Comparator.comparing(LeaderboardEntry::playedAt).reversed());  // sort and return arraylist
        return entries;
    }

    public Path getFile() {
        return file;
    }

    private String clean(String text) {   // formatting string  * method is used in this file(private)
        return text.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }
}
