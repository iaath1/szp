package com.stg.szp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Notification;
import com.stg.szp.models.SZP_User;

import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    Long countByRecipientIdAndIsReadFalse(Long recipientId);
    List<Notification> findAllByRecipientIdAndIsReadFalse(Long recipientId);
}
