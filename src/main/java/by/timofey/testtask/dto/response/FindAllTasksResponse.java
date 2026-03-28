package by.timofey.testtask.dto.response;

import java.util.UUID;

public record FindAllTasksResponse(
        UUID taskId,
        String name,
        String description
){
}
