package by.timofey.testtask.dto;

import java.util.UUID;

public record AssignUserToTaskEvent(UUID taskId, UUID userId) {
}
