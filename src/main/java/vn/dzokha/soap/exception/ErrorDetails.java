package vn.dzokha.soap.Exception;

import java.time.LocalDateTime;

public record ErrorDetails(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}