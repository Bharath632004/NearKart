package in.nearkart.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Initialises the Firebase Admin SDK.
 *
 * The service-account JSON can be supplied in two ways:
 *  1. Raw JSON string  (firebase.service-account-json starts with '{')
 *  2. Base64-encoded JSON (useful for env-var injection without newline issues)
 *
 * Example Base64 encoding:
 *   base64 -w0 service-account.json
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @PostConstruct
    public void initialize() {
        if (!StringUtils.hasText(serviceAccountJson) || serviceAccountJson.equals("{}")) {
            log.warn("Firebase service account JSON not configured — push notifications disabled.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try {
                byte[] jsonBytes = isBase64(serviceAccountJson)
                        ? Base64.getDecoder().decode(serviceAccountJson)
                        : serviceAccountJson.getBytes(StandardCharsets.UTF_8);

                try (InputStream is = new ByteArrayInputStream(jsonBytes)) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK initialised successfully.");
                }
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to initialise Firebase Admin SDK: " + e.getMessage(), e);
            }
        }
    }

    /** Heuristic: if the value doesn’t start with '{' it’s treated as Base64. */
    private boolean isBase64(String value) {
        return !value.trim().startsWith("{");
    }
}
