package by.timofey.testtask.repository;

import by.timofey.testtask.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
            SELECT t FROM Task t
            LEFT JOIN FETCH t.user u
            WHERE t.id = :taskId
            """)
    Optional<Task> findFullTaskById(@Param("taskId") UUID taskId);
}
