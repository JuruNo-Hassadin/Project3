/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class DataFiles {
    static final Path ROOT = Paths.get("data");
    static final Path USERS = ROOT.resolve("users.txt");
    static final Path POSTS = ROOT.resolve("posts.txt");
    static final Path MESSAGES = ROOT.resolve("messages.txt");
    static final Path IMAGES = ROOT.resolve("images");
    static final Path VOICE = ROOT.resolve("voice");

    private DataFiles() {
    }

    static void prepare() throws IOException {
        Files.createDirectories(ROOT);
        Files.createDirectories(IMAGES);
        Files.createDirectories(VOICE);
        if (Files.notExists(USERS)) Files.createFile(USERS);
        if (Files.notExists(POSTS)) Files.createFile(POSTS);
        if (Files.notExists(MESSAGES)) Files.createFile(MESSAGES);
    }

    static String cleanField(String value) {
        if (value == null) return "";
        return value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ').trim();
    }
}
