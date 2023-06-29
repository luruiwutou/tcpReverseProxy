package com.forward.core.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理接收到的消息
        String payload = message.getPayload();
        System.out.println("Received message: " + payload);

        // 发送回复消息
        session.sendMessage(new TextMessage("Reply: " + payload));
    }
}
