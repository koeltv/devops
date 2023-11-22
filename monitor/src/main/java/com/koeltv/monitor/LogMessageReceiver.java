package com.koeltv.monitor;

import com.koeltv.monitor.file.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Component
public class LogMessageReceiver {
    private final ApplicationContext context;
    private final FileHandler logHandler;
    private final List<String> receivedMessages = new ArrayList<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final int startingLogCount;

    public LogMessageReceiver(
            @Autowired ApplicationContext context,
            @Qualifier("logHandler") FileHandler logHandler
            ) {
        this.context = context;
        this.logHandler = logHandler;
        receivedMessages.addAll(logHandler.readLinesFromFile());
        startingLogCount = receivedMessages.size();
    }

    public void receiveMessage(String message) {
        if (receivedMessages.size() == startingLogCount) {
            pcs.firePropertyChange("FIRST_VALUE_RECEIVED", false, true);
        }

        receivedMessages.add(message);
        logHandler.appendLineToFile(message);

        // Stop the service when receiving stop log message
        if (message.equals("SND STOP")) SpringApplication.exit(context, () -> 0);
    }

    public void receiveMessage(byte[] bytes) {
        receiveMessage(new String(bytes));
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }

    public void addListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
}
