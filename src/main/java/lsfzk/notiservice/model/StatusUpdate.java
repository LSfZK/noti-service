package lsfzk.notiservice.model;

public record StatusUpdate(
        String orderId,
        String status,
        String message,
        String timestamp
) {
    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
