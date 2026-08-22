package com.stg.szp.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);

    Optional<Message> findFirstByChatIdOrderByCreatedAtAsc(Long chatId);

    int countByChatIdAndIsReadFalseAndSenderIdNot(Long chatId, Long userId);
}
