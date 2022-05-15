package model;

import java.time.LocalDateTime;

public record User(long id, String username, LocalDateTime creation) {
}