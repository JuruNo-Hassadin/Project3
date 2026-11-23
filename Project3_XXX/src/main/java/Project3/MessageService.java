package Project3;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JTextArea;

public class MessageService {
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

    @FunctionalInterface
    public interface ConversationConsumer {
        void accept(
                String timestamp,
                String sender,
                String kind,
                String content
        );
    }

    public void sendText(String sender, String recipient, String text)
            throws IOException {
        String timestamp = LocalDateTime.now().format(TIME);
        saveSentText(timestamp, sender, recipient, text);
    }

    public void saveSentText(
            String timestamp,
            String sender,
            String recipient,
            String text
    ) throws IOException {
        appendMessage(timestamp, sender, recipient, "T", text);
    }

    public void saveReceivedText(
            String timestamp,
            String sender,
            String recipient,
            String text
    ) throws IOException {
        appendMessage(timestamp, sender, recipient, "T", text);
    }

    public void sendImage(
            String sender,
            String recipient,
            String imageSource
    ) throws IOException {
        if (imageSource == null || imageSource.isBlank()) {
            throw new IOException("No image was selected.");
        }

        Path source = Path.of(imageSource);

        if (!Files.isRegularFile(source)) {
            throw new IOException("The selected image file does not exist.");
        }

        String fileName = source.getFileName().toString();
        int dot = fileName.lastIndexOf('.');

        String extension = dot >= 0
                ? fileName.substring(dot).toLowerCase(Locale.ROOT)
                : "";

        if (!extension.equals(".png")
                && !extension.equals(".jpg")
                && !extension.equals(".jpeg")) {
            throw new IOException("Only PNG and JPG images are supported.");
        }

        DataFiles.prepare();

        Path target = DataFiles.IMAGES.resolve(
                "dm_" + System.currentTimeMillis() + extension
        );

        Files.copy(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING
        );

        String timestamp = LocalDateTime.now().format(TIME);

        appendMessage(
                timestamp,
                sender,
                recipient,
                "I",
                target.toString().replace('\\', '/')
        );
    }

    public void sendVoice(
            String sender,
            String recipient,
            String voicePath
    ) throws IOException {
        if (voicePath == null || voicePath.isBlank()) {
            throw new IOException("No voice message was selected.");
        }

        Path voiceFile = Path.of(voicePath);

        if (!Files.isRegularFile(voiceFile)) {
            throw new IOException("The voice message file does not exist.");
        }

        String timestamp = LocalDateTime.now().format(TIME);

        appendMessage(
                timestamp,
                sender,
                recipient,
                "V",
                voiceFile.toString().replace('\\', '/')
        );
    }

    public void readConversation(
            String currentUser,
            String contact,
            ConversationConsumer consumer
    ) throws IOException {
        DataFiles.prepare();

        try (BufferedReader reader = Files.newBufferedReader(
                DataFiles.MESSAGES,
                StandardCharsets.UTF_8
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                int firstTab = line.indexOf('\t');
                int secondTab = firstTab < 0
                        ? -1
                        : line.indexOf('\t', firstTab + 1);
                int thirdTab = secondTab < 0
                        ? -1
                        : line.indexOf('\t', secondTab + 1);
                int fourthTab = thirdTab < 0
                        ? -1
                        : line.indexOf('\t', thirdTab + 1);

                if (fourthTab < 0) {
                    continue;
                }

                String timestamp = line.substring(0, firstTab);
                String sender = line.substring(firstTab + 1, secondTab);
                String recipient = line.substring(secondTab + 1, thirdTab);
                String kind = line.substring(thirdTab + 1, fourthTab);
                String content = line.substring(fourthTab + 1);

                boolean belongsToConversation =
                        (sender.equalsIgnoreCase(currentUser)
                                && recipient.equalsIgnoreCase(contact))
                        || (sender.equalsIgnoreCase(contact)
                                && recipient.equalsIgnoreCase(currentUser));

                if (belongsToConversation) {
                    consumer.accept(timestamp, sender, kind, content);
                }
            }
        }
    }

    public void showConversation(
            String currentUser,
            String contact,
            JTextArea output
    ) throws IOException {
        output.setText("");

        readConversation(
                currentUser,
                contact,
                (timestamp, sender, kind, content) -> {
                    String label = "";

                    if ("I".equals(kind)) {
                        label = "[Image] ";
                    } else if ("V".equals(kind)) {
                        label = "[Voice] ";
                    }

                    output.append(
                            timestamp + "  "
                                    + sender + ": "
                                    + label + content
                                    + System.lineSeparator()
                    );
                }
        );
    }

    public String latestVoice(String currentUser, String contact)
            throws IOException {
        DataFiles.prepare();

        String latestVoicePath = "";

        try (BufferedReader reader = Files.newBufferedReader(
                DataFiles.MESSAGES,
                StandardCharsets.UTF_8
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                int firstTab = line.indexOf('\t');
                int secondTab = firstTab < 0
                        ? -1
                        : line.indexOf('\t', firstTab + 1);
                int thirdTab = secondTab < 0
                        ? -1
                        : line.indexOf('\t', secondTab + 1);
                int fourthTab = thirdTab < 0
                        ? -1
                        : line.indexOf('\t', thirdTab + 1);

                if (fourthTab < 0) {
                    continue;
                }

                String sender = line.substring(firstTab + 1, secondTab);
                String recipient = line.substring(secondTab + 1, thirdTab);
                String kind = line.substring(thirdTab + 1, fourthTab);
                String content = line.substring(fourthTab + 1);

                boolean belongsToConversation =
                        (sender.equalsIgnoreCase(currentUser)
                                && recipient.equalsIgnoreCase(contact))
                        || (sender.equalsIgnoreCase(contact)
                                && recipient.equalsIgnoreCase(currentUser));

                if (belongsToConversation && "V".equals(kind)) {
                    latestVoicePath = content;
                }
            }
        }

        return latestVoicePath;
    }

    private static synchronized void appendMessage(
            String timestamp,
            String sender,
            String recipient,
            String kind,
            String content
    ) throws IOException {
        DataFiles.prepare();

        String row = DataFiles.cleanField(timestamp) + "\t"
                + DataFiles.cleanField(sender) + "\t"
                + DataFiles.cleanField(recipient) + "\t"
                + DataFiles.cleanField(kind) + "\t"
                + DataFiles.cleanField(content);

        try (FileChannel channel = FileChannel.open(
                DataFiles.MESSAGES,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
        ); FileLock fileLock = channel.lock()) {

            if (containsLine(channel, row)) {
                return;
            }

            channel.position(channel.size());

            String rowWithNewline = row + System.lineSeparator();
            ByteBuffer buffer =
                    StandardCharsets.UTF_8.encode(rowWithNewline);

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            channel.force(true);
        }
    }

    private static boolean containsLine(
            FileChannel channel,
            String wantedLine
    ) throws IOException {
        long fileSize = channel.size();

        if (fileSize > Integer.MAX_VALUE) {
            throw new IOException("The messages file is too large to read.");
        }

        ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
        channel.position(0);

        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) {
                break;
            }
        }

        buffer.flip();

        String existingText = StandardCharsets.UTF_8
                .decode(buffer)
                .toString();

        int start = 0;

        while (start < existingText.length()) {
            int end = existingText.indexOf('\n', start);

            if (end < 0) {
                end = existingText.length();
            }

            int lineEnd = end;

            if (lineEnd > start
                    && existingText.charAt(lineEnd - 1) == '\r') {
                lineEnd--;
            }

            String existingLine =
                    existingText.substring(start, lineEnd);

            if (existingLine.equals(wantedLine)) {
                return true;
            }

            start = end + 1;
        }

        return false;
    }
}