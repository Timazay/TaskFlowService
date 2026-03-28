package by.timofey.testtask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for creating a new task")
public record CreateTaskEvent(
        @Schema(description = "Task name", example = "sum")
        @NotBlank(message = "Task name is required")
        @Size(min = 3, max = 30, message = "Task name must be between 3 and 30 characters")
        String name,
        @Schema(description = "Task description", example = "2+2=?")
        @NotBlank(message = "Task description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}
