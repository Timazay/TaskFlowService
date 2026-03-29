package by.timofey.testtask.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignUserRequest(
        @NotNull(message = "User userId is required")
        @Schema(description = "User userId", example = "33815145-ce49-450f-995d-33e84553ff77")
        UUID userId
) {
}
