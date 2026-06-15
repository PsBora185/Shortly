package com.urlShortner.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUrlRequest(
        @NotBlank(message = "Original URL is required") String originalUrl
) {
}
