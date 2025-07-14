package lsfzk.notiservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    // Inject the path to your service account key file from application.yml
    @Value("${fcm.file-path}")
    private String serviceAccountFilePath;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        // Load the service account key file from the classpath
        ClassPathResource resource = new ClassPathResource(serviceAccountFilePath);
        InputStream serviceAccountStream = resource.getInputStream();

        // Build the FirebaseOptions object with your credentials
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .build();

        // Initialize the FirebaseApp if it hasn't been initialized yet.
        // This check is important to prevent errors on hot reloads.
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        // Return the FirebaseMessaging instance so it can be injected elsewhere
        return FirebaseMessaging.getInstance();
    }
//
//    @PostConstruct
//    public void initialize() throws IOException {
//        if (FirebaseApp.getApps().isEmpty()) {
//            InputStream serviceAccount = new ClassPathResource("lsfzkFirebaseKey.json").getInputStream();
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//
//            FirebaseApp.initializeApp(options);
//        }
//    }
}
