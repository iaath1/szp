package com.stg.szp.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.CalendarEvent;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    
    @Query(
        "SELECT DISTINCT c FROM CalendarEvent c " +
        "LEFT JOIN c.project p " +
        "LEFT JOIN p.members m " +
        "WHERE (c.owner.id = :userId " +
        "   OR p.owner.id = :userId " +
        "   OR m.id = :userId) " +
        "AND (c.startTime >= :monthStart AND c.endTime <= :monthEnd)"
    )
    List<CalendarEvent> findEventsForUserInMonth(
        @Param("userId") Long userId,
        @Param("monthStart") LocalDateTime monthStart,
        @Param("monthEnd") LocalDateTime monthEnd
    );
}
