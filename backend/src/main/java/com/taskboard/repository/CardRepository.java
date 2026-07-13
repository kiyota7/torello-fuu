package com.taskboard.repository;

import com.taskboard.entity.Card;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByListIdOrderByPosition(Long listId);
}
