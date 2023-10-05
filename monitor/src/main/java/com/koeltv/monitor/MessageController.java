package com.koeltv.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MessageController {
    private final MessageReceiver messageReceiver;

    MessageController(@Autowired MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @GetMapping("")
    public String getLogMessages() {
        return String.join("\n", messageReceiver.getReceivedMessages());
    }
}
