package com.stg.szp.controllers;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.ChatResponseDTO;
import com.stg.szp.DTO.MessageResponseDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.ChatService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatResponseDTO>> getChats(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(chatService.getUserCharts(user), HttpStatus.OK);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable Long chatId, @AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(chatService.getChatMessages(chatId, user), HttpStatus.OK);
    }

    @PostMapping("/user/{targetUserId}")
    public ResponseEntity<ChatResponseDTO> createChat(@PathVariable Long targetUserId, @AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        ChatResponseDTO response = chatService.getOrCreatePrivateChat(user, targetUserId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
