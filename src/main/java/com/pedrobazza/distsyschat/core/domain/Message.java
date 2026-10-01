package com.pedrobazza.distsyschat.core.domain;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final String sender;
    private final String content;
    private final long timestamp;
    private final String recipient;

    public Message(String sender, String content) {
        this(sender, content, System.currentTimeMillis(), "ALL");
    }

    public Message(String sender, String content, long timestamp, String recipient) {
        this.sender = sender != null ? sender.trim() : "Desconhecido";
        this.content = content != null ? content : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.recipient = (recipient != null && !recipient.isBlank()) ? recipient.trim() : "ALL";
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getFormattedTime() {
        return TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    public String formatDisplay() {
        return String.format("[%s] %s: %s", getFormattedTime(), sender, content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return timestamp == message.timestamp &&
                Objects.equals(sender, message.sender) &&
                Objects.equals(content, message.content) &&
                Objects.equals(recipient, message.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, content, timestamp, recipient);
    }

    @Override
    public String toString() {
        return formatDisplay();
    }
}
