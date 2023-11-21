package com.koeltv.monitor;

import org.springframework.stereotype.Component;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Component
public class LogMessageReceiver {
    private final List<String> receivedMessages = new ArrayList<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void receiveMessage(String message) {
        if (receivedMessages.isEmpty()) {
            pcs.firePropertyChange("FIRST_VALUE_RECEIVED", false, true);
        }

        receivedMessages.add(message);
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
