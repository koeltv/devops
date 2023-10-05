package com.koeltv.monitor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Component
public class MessageReceiver {
    private final List<String> receivedMessages = new ArrayList<>();

    public void receiveMessage(String message) {
        receivedMessages.add(message);
    }

    public void receiveMessage(byte[] bytes) {
        receivedMessages.add(new String(bytes));
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }
}
