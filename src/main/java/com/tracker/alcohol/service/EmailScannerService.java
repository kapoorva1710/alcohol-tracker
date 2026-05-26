package com.tracker.alcohol.service;

import com.tracker.alcohol.model.DrinkEvent;
import com.tracker.alcohol.model.User;
import com.tracker.alcohol.repository.DrinkEventRepository;
import com.tracker.alcohol.repository.UserRepository;
import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;

@Service
public class EmailScannerService {

    @Value("${tracker.mail.host}")
    private String host;

    @Value("${tracker.mail.username}")
    private String username;

    @Value("${tracker.mail.password}")
    private String password;

    private final DrinkEventRepository drinkEventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EmailScannerService(DrinkEventRepository drinkEventRepository, UserRepository userRepository) {
        this.drinkEventRepository = drinkEventRepository;
        this.userRepository = userRepository;
    }

    // Runs every 24 hours (86400000 ms)
    @Scheduled(fixedRate = 86400000)
    public void scanEmails() {
        System.out.println("Starting Email Scan...");

        // Skip if no users registered yet
        Optional<User> userOpt = userRepository.findById(1L);
        if (userOpt.isEmpty()) {
            System.out.println("No registered user found. Skipping email scan.");
            return;
        }

        User user = userOpt.get();

        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", host);
            properties.put("mail.imaps.port", "993");

            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Fetch unseen emails
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message message : messages) {
                String subject = message.getSubject() != null ? message.getSubject() : "";

                // Check if email looks like a receipt/order
                if (subject.toLowerCase().contains("receipt") || subject.toLowerCase().contains("order")) {
                    String content = getMessageContent(message);
                    if (content.toLowerCase().contains("beer") || content.toLowerCase().contains("wine")
                            || content.toLowerCase().contains("whiskey") || content.toLowerCase().contains("vodka")) {
                        System.out.println("Found an alcohol receipt: " + subject);

                        DrinkEvent event = new DrinkEvent();
                        event.setUser(user);
                        event.setBeverageName("Auto-Detected via Email");
                        event.setQuantity(1.0);
                        event.setSourceType("EMAIL_RECEIPT");
                        event.setConsumedAt(LocalDateTime.now());
                        event.setNotes("From receipt: " + subject);

                        drinkEventRepository.save(event);

                        // Mark as seen so we don't process it again
                        message.setFlag(Flags.Flag.SEEN, true);
                    }
                }
            }

            inbox.close(false);
            store.close();
            System.out.println("Email Scan Complete.");
        } catch (Exception e) {
            System.err.println("Error scanning emails: " + e.getMessage());
        }
    }

    private String getMessageContent(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContent() instanceof String) {
                    result.append((String) bodyPart.getContent());
                }
            }
            return result.toString();
        }
        return "";
    }
}
