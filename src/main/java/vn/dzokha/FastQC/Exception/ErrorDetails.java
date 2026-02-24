package vn.dzokha.FastQC.Exception;

import java.time.LocalDateTime;

public record ErrorDetails(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}