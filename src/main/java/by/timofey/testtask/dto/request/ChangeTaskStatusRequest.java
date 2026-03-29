package by.timofey.testtask.dto.request;

import by.timofey.testtask.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO for change a task status")
public record ChangeTaskStatusRequest(
        @Schema(
                description = "Task status",
                example = "IN_PROGRESS",
                implementation = TaskStatus.class
        )
        @NotNull(message = "status cannot be null")
        TaskStatus status) {
}
