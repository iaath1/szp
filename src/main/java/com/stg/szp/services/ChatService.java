package com.stg.szp.services;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stg.szp.DTO.ChatResponseDTO;
import com.stg.szp.DTO.MessageResponseDTO;
import com.stg.szp.models.Chat;
import com.stg.szp.models.Message;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ChatRepository;
import com.stg.szp.repos.MessageRepository;
import com.stg.szp.repos.SZP_UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatService {

    private final SZP_UserRepository userRepository;
    private final ChatRepository chatRepo;
    private final MessageRepository messageRepo;

    public List<ChatResponseDTO> getUserCharts(SZP_User user) {
        return chatRepo.findByMembersId(user.getId()).stream().map(
            chat -> {
                Message lastMessage = messageRepo.findFirstByChatIdOrderByCreatedAtAsc(chat.getId()).orElse(null);
                int unread = messageRepo.countByChatIdAndIsReadFalseAndSenderIdNot(chat.getId(), user.getId());

                return ChatResponseDTO.builder()
                    .id(chat.getId())
                    .name(chat.getName() != null ? chat.getName() : "Chat #" + chat.getId())
                    .avatar("null")
                    .lastMessage(lastMessage != null ? lastMessage.getText() : "")
                    .time(lastMessage != null ? lastMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                    .unread(unread)
                    .build();
            }
        ).collect(Collectors.toList());
    }

    public List<MessageResponseDTO> getChatMessages(Long chatId, SZP_User user) {
        return messageRepo.findByChatIdOrderByCreatedAtAsc(chatId).stream().map(
            msg -> MessageResponseDTO.builder()
                .id(msg.getId())
                .sender(msg.getSender().getName() + " " + msg.getSender().getSurname())
                .text(msg.getText())
                .time(msg.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .isMine(msg.getSender().getId().equals(user.getId()))
                .build()    
        ).collect(Collectors.toList());
    }

    @Transactional
    public MessageResponseDTO saveMessage(Long chatId, SZP_User sender, String text) {
        Chat chat = chatRepo.findById(chatId).orElseThrow(() -> new RuntimeException("Chat was not found"));
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setText(text);
        message.setCreatedAt(LocalDateTime.now());

        message = messageRepo.save(message);

        return MessageResponseDTO.builder()
            .id(message.getId())
            .sender(sender.getName() + " " + sender.getSurname())
            .text(text)
            .time(message.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
            .isMine(false)
            .build();
    }

    @Transactional
    public ChatResponseDTO getOrCreatePrivateChat(SZP_User currentUser, Long targetUserId) {
        if(currentUser.getId().equals(targetUserId)) throw new RuntimeException("You cant message yourself");

        List<Chat> userChats = chatRepo.findByMembersId(currentUser.getId());

        for(Chat chat : userChats) {
            if(!chat.isGroup() && chat.getMembers().size() == 2) {
                boolean hasTargetUser = chat.getMembers().stream().anyMatch(m -> m.getId().equals(targetUserId));

                if(hasTargetUser) return toChatResponseDto(chat, currentUser);
            }
        }

        SZP_User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("User not found"));

        Chat newChat = new Chat();
        newChat.setGroup(false);
        newChat.getMembers().add(currentUser);
        newChat.getMembers().add(targetUser);
        
        chatRepo.save(newChat);

        return toChatResponseDto(newChat, currentUser);
    }

    private ChatResponseDTO toChatResponseDto(Chat chat, SZP_User user) {
        Message lastMessage = messageRepo.findFirstByChatIdOrderByCreatedAtAsc(chat.getId()).orElse(null);
        int unread = messageRepo.countByChatIdAndIsReadFalseAndSenderIdNot(chat.getId(), user.getId());

        String chatName = chat.getName();
        if(!chat.isGroup() && chat.getMembers().size() == 2) {
            SZP_User otherMember = chat.getMembers().stream().filter(m -> !m.getId().equals(user.getId())).findFirst().orElse(null);

            if(otherMember != null) chatName = otherMember.getName() + " " + otherMember.getSurname();
        }
        if(chatName == null) chatName = "Chat #" + chat.getId();

        return ChatResponseDTO.builder()
            .id(chat.getId())
            .name(chatName)
            .avatar(null)
            .lastMessage(lastMessage != null ? lastMessage.getText() : "")
            .time(lastMessage != null ? lastMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
            .unread(unread)
            .build();
    }
}

