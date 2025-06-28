package lsfzk.notiservice.model;

import java.util.List;

public record StoreAddEvent(
        String userId,
        String storeId,
        String storeName,
        String deviceToken,
        List<String> adminEmails
) {
}
