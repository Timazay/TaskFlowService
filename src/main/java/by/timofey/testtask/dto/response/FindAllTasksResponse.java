package by.timofey.testtask.dto.response;

import by.timofey.testtask.entity.enums.TaskStatus;

import java.util.UUID;

public record FindAllTasksResponse(
        UUID taskId,
        String name,
        String description,
        TaskStatus status
){
}
