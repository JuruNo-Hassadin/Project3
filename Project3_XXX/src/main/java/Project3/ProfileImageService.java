/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class ProfileImageService {
    private final Path profileDirectory = Paths.get("data", "profiles");

    public Path save(String username, Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IOException("Image file was not found.");
        }

        String fileName = source.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String extension = dot >= 0
                ? fileName.substring(dot).toLowerCase(Locale.ROOT)
                : "";

        if (!extension.equals(".png")
                && !extension.equals(".jpg")
                && !extension.equals(".jpeg")) {
            throw new IOException("Choose a PNG or JPG image.");
        }

        Files.createDirectories(profileDirectory);

        String fileKey = makeFileKey(username);
        Path target = profileDirectory.resolve(fileKey + extension);

        Path sourceAbsolute = source.toAbsolutePath().normalize();
        Path targetAbsolute = target.toAbsolutePath().normalize();

        if (!sourceAbsolute.equals(targetAbsolute)) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }

        deleteOtherFormats(fileKey, target);
        return target;
    }

    public Path find(String username) throws IOException {
        Files.createDirectories(profileDirectory);

        String fileKey = makeFileKey(username);

        Path png = profileDirectory.resolve(fileKey + ".png");
        if (Files.isRegularFile(png)) return png;

        Path jpg = profileDirectory.resolve(fileKey + ".jpg");
        if (Files.isRegularFile(jpg)) return jpg;

        Path jpeg = profileDirectory.resolve(fileKey + ".jpeg");
        if (Files.isRegularFile(jpeg)) return jpeg;

        return null;
    }

    private void deleteOtherFormats(String fileKey, Path keep) throws IOException {
        deleteUnlessKept(profileDirectory.resolve(fileKey + ".png"), keep);
        deleteUnlessKept(profileDirectory.resolve(fileKey + ".jpg"), keep);
        deleteUnlessKept(profileDirectory.resolve(fileKey + ".jpeg"), keep);
    }

    private void deleteUnlessKept(Path candidate, Path keep) throws IOException {
        Path candidateAbsolute = candidate.toAbsolutePath().normalize();
        Path keepAbsolute = keep.toAbsolutePath().normalize();

        if (!candidateAbsolute.equals(keepAbsolute)) {
            Files.deleteIfExists(candidate);
        }
    }

    private String makeFileKey(String username) {
        String lowerName = username.trim().toLowerCase(Locale.ROOT);
        String safeName = lowerName.replaceAll("[^a-z0-9_-]", "_");

        if (safeName.isBlank()) {
            safeName = "user";
        }

        return safeName + "_" + Integer.toHexString(lowerName.hashCode());
    }
}