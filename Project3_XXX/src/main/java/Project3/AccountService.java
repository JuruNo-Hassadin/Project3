/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class AccountService {
    public AccountService() {
        try {
            DataFiles.prepare();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not prepare the data folder.", ex);
        }
    }

    public boolean register(String username, String password, String interests) throws IOException {
        String user = DataFiles.cleanField(username);
        String pass = DataFiles.cleanField(password);
        String tags = DataFiles.cleanField(interests);
        if (user.isEmpty() || pass.isEmpty()) return false;
        if (usernameExists(user)) return false;
        String line = user + "\t" + pass + "\t" + tags + System.lineSeparator();
        Files.writeString(DataFiles.USERS, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return true;
    }

    public boolean login(String username, String password) throws IOException {
        String user = DataFiles.cleanField(username);
        String pass = DataFiles.cleanField(password);
        try (BufferedReader reader = Files.newBufferedReader(DataFiles.USERS, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstTab = line.indexOf('\t');
                if (firstTab < 0) continue;
                int secondTab = line.indexOf('\t', firstTab + 1);
                String savedUser = line.substring(0, firstTab);
                String savedPass = secondTab < 0
                        ? line.substring(firstTab + 1)
                        : line.substring(firstTab + 1, secondTab);
                if (savedUser.equals(user) && savedPass.equals(pass)) return true;
            }
        }
        return false;
    }

    public String interestsFor(String username) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(DataFiles.USERS, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstTab = line.indexOf('\t');
                int secondTab = firstTab < 0 ? -1 : line.indexOf('\t', firstTab + 1);
                if (firstTab >= 0 && secondTab >= 0
                        && line.substring(0, firstTab).equals(username)) {
                    return line.substring(secondTab + 1);
                }
            }
        }
        return "No interests selected.";
    }

    private boolean usernameExists(String username) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(DataFiles.USERS, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int tab = line.indexOf('\t');
                if (tab >= 0 && line.substring(0, tab).equalsIgnoreCase(username)) return true;
            }
        }
        return false;
    }
}
