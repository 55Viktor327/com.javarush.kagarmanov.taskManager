package com.javarush.model.repository;

import com.javarush.model.entity.Task;
import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.TaskPriority;
import com.javarush.model.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"assignees", "owner"})
    @Query("select t from Task t where t.deleted = false")
    List<Task> findAllActive();

    @EntityGraph(attributePaths = {"assignees", "owner"})
    @Query("select t from Task t where t.deleted = false")
    List<Task> findAllActiveWithDetails();

    @EntityGraph(attributePaths = {"assignees", "owner"})
    @Query("select t from Task t where t.deleted = false and t.id = :id")
    Optional<Task> findActiveById(@Param("id") Long id);

    @Query("select t from Task t where t.deleted = false and t.title = :title")
    Optional<Task> findActiveByTitle(@Param("title") String title);

    @Query("select t from Task t where t.deleted = false and t.priority = :priority")
    List<Task> findActiveByPriority(@Param("priority") TaskPriority taskPriority);

    @Query("select t from Task t where t.deleted = false and t.status = :status")
    List<Task> findActiveByStatus(@Param("status") TaskStatus taskStatus);

    @Query("select t from Task t where t.deleted = false and t.createdAt = :createdAt")
    List<Task> findActiveByCreatedAt(@Param("createdAt") LocalDateTime createdAt);

    @Query("select t from Task t where t.deleted = false and t.deadline = :deadline")
    List<Task> findActiveByDeadline(@Param("deadline") LocalDateTime deadline);

    @EntityGraph(attributePaths = {"assignees", "owner"})
    @Query("select t from Task t where t.deleted = false and t.owner.id = :ownerId")
    List<Task> findActiveByOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"assignees", "owner"})
    @Query("select distinct t from Task t join t.assignees a " +
            "where t.deleted = false and a.id = :userId")
    List<Task> findActiveByAssigneeId(@Param("userId") Long userId);

    @Query("select count(t) > 0 from Task t " +
            "where t.deleted = false and t.title = :title and t.id <> :id")
    boolean existsTaskByTitleAndIdNot(@Param("title") String title, @Param("id") Long id);

    @Query("select t from Task t where t.deleted = true")
    List<Task> findAllDeleted();

    @Query("select t from Task t where t.deleted = true and t.id = :id")
    Optional<Task> findDeletedById(@Param("id") Long id);

    boolean existsTaskByTitle(String title);
}
