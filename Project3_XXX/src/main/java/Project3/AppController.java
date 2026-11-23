/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

/** Connects screen actions to services and switches the visible screen. */
public class AppController {
    private final MainApplication application;
    private final AccountService accountService;
    private final PostService postService;
    private final MessageService messageService;
    private final VoiceNoteService voiceNoteService;
    private final ProfileImageService profileImageService;
    private LanMessageService lanMessageService;
    private ChatFrame activeChatFrame;
    private int localTcpPort;

    private String currentUser = "";
    private MainFrame mainFrame;
    private LanDiscoveryService lanDiscovery;

    public AppController(MainApplication application) {
        this.application = application;
        this.profileImageService = new ProfileImageService();
        this.accountService = new AccountService();
        this.postService = new PostService();
        this.messageService = new MessageService();
        this.voiceNoteService = new VoiceNoteService();
    }

    public void start() {
        showLogin();
    }

    public void showLogin() {
        application.setTitle("CHATGAE - Login");
        application.showScreen(new LoginPanel(this));
    }

    public void openRegister() {
        new RegisterFrame(this).setVisible(true);
    }

    public boolean register(String username, String password, String interests) {
        try {
            boolean created = accountService.register(username, password, interests);

            if (!created) {
                JOptionPane.showMessageDialog(
                        application,
                        "Username is empty or already in use.",
                        "Registration",
                        JOptionPane.WARNING_MESSAGE
                );
                return false;
            }

            JOptionPane.showMessageDialog(
                    application,
                    "Account created. You can now log in."
            );
            return true;

        } catch (IOException ex) {
            showError("Could not save the account", ex);
            return false;
        }
    }

    public void login(String username, String password) {
        try {
            if (!accountService.login(username, password)) {
                JOptionPane.showMessageDialog(
                        application,
                        "Username or password is incorrect.",
                        "Login",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            currentUser = username.trim();
            mainFrame = new MainFrame(this, currentUser);

            application.setTitle("CHATGAE - " + currentUser);
            application.showScreen(mainFrame);
            
            startLanMessageService();
            startLanDiscovery();

        } catch (IOException ex) {
            showError("Could not read the account file", ex);
        }
    }

    public void logout() {
        stopLanDiscovery();

        if (mainFrame != null) {
            mainFrame.stop();
        }

        voiceNoteService.stopPlayback();

        try {
            voiceNoteService.stopRecording();
        } catch (Exception ex) {
            showError("Could not stop the voice recording", ex);
        }

        currentUser = "";
        mainFrame = null;
        showLogin();
        
        stopLanMessageService();
        activeChatFrame = null;
    }

    public void openHome() {
        if (mainFrame != null) {
            mainFrame.showPanel(new PostPanel(this));
        }
    }

    public void openProfile() {
        if (mainFrame != null) {
            mainFrame.showPanel(new ProfilePanel(this, currentUser));
        }
    }

    public void openFriends() {
        if (mainFrame != null) {
            mainFrame.showFriends();
        }
    }

    public void openChat() {
        openChatWith("");
    }

    public void openChatWith(String contact) {
        if (mainFrame != null) {
            activeChatFrame = new ChatFrame(this, currentUser, contact);
            mainFrame.showPanel(activeChatFrame);
        }
    }

    public void openVoice() {
        if (mainFrame != null) {
            mainFrame.showPanel(
                    new VoiceChatFrame(this, currentUser)
            );
        }
    }

    private void startLanDiscovery() {
    stopLanDiscovery();

    if (localTcpPort <= 0) {
        return;
    }

    lanDiscovery = new LanDiscoveryService(
            currentUser,
            localTcpPort,
            (username, ipAddress, tcpPort) ->
                    SwingUtilities.invokeLater(() -> {
                        if (mainFrame != null) {
                            mainFrame.peerSeen(
                                    username,
                                    ipAddress,
                                    tcpPort
                            );
                        }
                    })
    );

    try {
        lanDiscovery.start();
    } catch (IOException ex) {
        showError("Could not start LAN user discovery", ex);
    }
    }

    private void stopLanDiscovery() {
        if (lanDiscovery != null) {
            lanDiscovery.close();
            lanDiscovery = null;
        }
    }

    public boolean createPost(String text, String imagePath) {
        try {
            postService.addPost(currentUser, text, imagePath);
            return true;
        } catch (IOException ex) {
            showError("Could not save the post", ex);
            return false;
        }
    }

    public void showPosts(javax.swing.JTextArea output) {
        try {
            postService.showPosts(output);
        } catch (IOException ex) {
            showError("Could not read posts", ex);
        }
    }

    public boolean sendText(String recipient, String text) {
    if (mainFrame == null || lanMessageService == null) {
        showError(
                "LAN messaging is not available",
                new IOException("The LAN message service is not running.")
        );
        return false;
    }

    String ipAddress = mainFrame.peerIpFor(recipient);
    int recipientPort = mainFrame.peerPortFor(recipient);

    if (ipAddress.isBlank() || recipientPort <= 0) {
        showError(
                "User is not online",
                new IOException("Find the recipient on the Friends page first.")
        );
        return false;
    }

    String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter
                    .ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
    );

    try {
        lanMessageService.sendText(
                ipAddress,
                recipientPort,
                currentUser,
                recipient,
                timestamp,
                text
        );

        messageService.saveSentText(
                timestamp,
                currentUser,
                recipient,
                text
        );

        return true;

    } catch (IOException ex) {
        showError("Could not send message over LAN", ex);
        return false;
    }
}

    public boolean sendImage(String recipient, String imagePath) {
        try {
            messageService.sendImage(currentUser, recipient, imagePath);
            return true;
        } catch (IOException ex) {
            showError("Could not attach the image", ex);
            return false;
        }
    }

    public void showConversation(
            String contact,
            javax.swing.JTextArea output
    ) {
        try {
            messageService.showConversation(
                    currentUser,
                    contact,
                    output
            );
        } catch (IOException ex) {
            showError("Could not read messages", ex);
        }
    }

    public String latestVoice(String contact) {
        try {
            return messageService.latestVoice(currentUser, contact);
        } catch (IOException ex) {
            showError("Could not read voice messages", ex);
            return "";
        }
    }

    public AccountService accounts() {
        return accountService;
    }

    public MessageService messages() {
        return messageService;
    }

    public VoiceNoteService voiceNotes() {
        return voiceNoteService;
    }

    public String currentUser() {
        return currentUser;
    }

    public void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(
                application,
                message + ": " + ex.getMessage(),
                "CHATGAE",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    public String profileImagePath(String username) {
    try {
        Path image = profileImageService.find(username);
        return image == null ? "" : image.toString();
    } catch (IOException ex) {
        showError("Could not load profile picture", ex);
        return "";
    }
    }

    public boolean saveProfileImage(String username, Path source) {
        try {
            profileImageService.save(username, source);
            return true;
        } catch (IOException ex) {
            showError("Could not save profile picture", ex);
        return false;
        }
    }
    public void loadConversationMessages(
        String contact,
        MessageService.ConversationConsumer consumer
    ) {
        try {
            messageService.readConversation(currentUser, contact, consumer);
        } catch (IOException ex) {
            showError("Could not read messages", ex);
        }
    }
    private void startLanMessageService() {
    stopLanMessageService();

    lanMessageService = new LanMessageService(
            currentUser,
            this::receiveLanText
    );

    try {
        localTcpPort = lanMessageService.start();
    } catch (IOException ex) {
        lanMessageService.close();
        lanMessageService = null;
        localTcpPort = 0;
        showError("Could not start LAN message service", ex);
    }
    }

    private void stopLanMessageService() {
    if (lanMessageService != null) {
        lanMessageService.close();
        lanMessageService = null;
    }

    localTcpPort = 0;
    }

private void receiveLanText(
        String timestamp,
        String sender,
        String recipient,
        String text
    ) {
    try {
        messageService.saveReceivedText(
                timestamp,
                sender,
                recipient,
                text
        );
    } catch (IOException ex) {
        SwingUtilities.invokeLater(
                () -> showError("Could not save received message", ex)
        );
        return;
    }

    SwingUtilities.invokeLater(() -> {
        if (activeChatFrame != null
                && activeChatFrame.currentContact()
                        .equalsIgnoreCase(sender)) {
            activeChatFrame.refreshConversation();
        }
    });
    }
}