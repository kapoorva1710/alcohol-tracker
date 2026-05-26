package com.tracker.alcohol.repository;

import com.tracker.alcohol.model.DrinkEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DrinkEventRepository extends JpaRepository<DrinkEvent, Long> {

    List<DrinkEvent> findByUserIdOrderByConsumedAtDesc(Long userId);

    List<DrinkEvent> findByUserIdAndConsumedAtBetweenOrderByConsumedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<DrinkEvent> findByUserIdAndBeverageNameContainingIgnoreCaseOrderByConsumedAtDesc(
            Long userId, String beverageName);

    List<DrinkEvent> findByUserIdAndConsumedAtBetweenAndBeverageNameContainingIgnoreCaseOrderByConsumedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end, String beverageName);
}
