package by.timofey.testtask.dto;


import java.util.UUID;

public record CreateTaskEvent(UUID taskId, String taskName) {
}
