package ru.prusakova.otp.listener;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.dto.KafkaSendOtpOutResponse;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaMessageContext {

    private final Cache<String, CompletableFuture<KafkaSendOtpOutResponse>> messageContext = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();

    public CompletableFuture<KafkaSendOtpOutResponse> createMessageCompletableFuture(String id) {
        CompletableFuture<KafkaSendOtpOutResponse> completableFuture = new CompletableFuture<>();
        messageContext.put(id, completableFuture);

        return completableFuture;
    }

    public CompletableFuture<KafkaSendOtpOutResponse> findById(String id) {
        return messageContext.getIfPresent(id);
    }

    public void removeById(String id) {
        messageContext.invalidate(id);
    }
}
