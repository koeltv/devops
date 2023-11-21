package com.koeltv.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MessageController {
    private final LogMessageReceiver logReceiver;
    private final StateMessageReceiver stateReceiver;

    MessageController(
            @Autowired LogMessageReceiver logReceiver,
            @Autowired StateMessageReceiver stateReceiver
    ) {
        this.logReceiver = logReceiver;
        this.stateReceiver = stateReceiver;
    }

    @GetMapping("")
    public String getLogMessages() {
        return String.join("\n", logReceiver.getReceivedMessages());
    }

    @GetMapping("/state")
    public String getState() {
        return stateReceiver.getLastState();
    }
}
