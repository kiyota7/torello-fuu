package com.taskboard.repository;

import com.taskboard.entity.TaskList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    List<TaskList> findByUserIdOrderByPosition(Long userId);
}
