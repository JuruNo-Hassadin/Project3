/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PostService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String addPost(String username, String text, String imageSource) throws IOException {
        DataFiles.prepare();
        String imagePath = "";
        if (imageSource != null && !imageSource.isBlank()) {
            Path source = Path.of(imageSource);
            String name = source.getFileName().toString();
            int dot = name.lastIndexOf('.');
            String extension = dot >= 0 ? name.substring(dot) : ".img";
            Path target = DataFiles.IMAGES.resolve("post_" + System.currentTimeMillis() + extension);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            imagePath = target.toString().replace('\\', '/');
        }
        String row = LocalDateTime.now().format(TIME) + "\t"
                + DataFiles.cleanField(username) + "\t"
                + DataFiles.cleanField(text) + "\t"
                + DataFiles.cleanField(imagePath) + System.lineSeparator();
        Files.writeString(DataFiles.POSTS, row, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return imagePath;
    }

    public void showPosts(javax.swing.JTextArea output) throws IOException {
        DataFiles.prepare();
        output.setText("");
        try (BufferedReader reader = Files.newBufferedReader(DataFiles.POSTS, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int a = line.indexOf('\t');
                int b = a < 0 ? -1 : line.indexOf('\t', a + 1);
                int c = b < 0 ? -1 : line.indexOf('\t', b + 1);
                if (c < 0) continue;
                String time = line.substring(0, a);
                String user = line.substring(a + 1, b);
                String body = line.substring(b + 1, c);
                String image = line.substring(c + 1);
                output.append("@" + user + "    " + time + System.lineSeparator());
                if (!body.isBlank()) output.append(body + System.lineSeparator());
                if (!image.isBlank()) output.append("[Image: " + image + "]" + System.lineSeparator());
                output.append(System.lineSeparator());
            }
        }
    }
}
