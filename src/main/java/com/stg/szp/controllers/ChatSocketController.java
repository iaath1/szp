package com.stg.szp.controllers;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.stg.szp.DTO.ChatMessageRequest;
import com.stg.szp.DTO.MessageResponseDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.ChatService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class ChatSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{chatId}/sendMessage")
    public void processMessage(@DestinationVariable Long chatId, @Payload ChatMessageRequest request, Authentication auth) {
        SZP_User sender = (SZP_User) auth.getPrincipal();

        MessageResponseDTO savedMessage = chatService.saveMessage(chatId, sender, request.getText());
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessage);
    }

    @MessageMapping("/chat/{chatId}/signal")
    public void sendSignal(@DestinationVariable Long chatId, @Payload String signalData) {
        messagingTemplate.convertAndSend("/topic/call/" + chatId, signalData);
    }
}
