package com.koeltv.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Component
public class StateMessageReceiver implements PropertyChangeListener {
    private final List<Map.Entry<String, String>> receivedStates = new ArrayList<>();

    public StateMessageReceiver(
            @Autowired LogMessageReceiver logMessageReceiver
    ) {
        logMessageReceiver.addListener(this);

        var initState = Map.entry(getCurrentTime(), "INIT");
        receivedStates.add(initState);
    }

    private String getCurrentTime() {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss:SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }

    public void receiveMessage(String state) {
        if (!state.equals(getLastState())) {
            Map.Entry<String, String> timedState = Map.entry(getCurrentTime(), state);
            receivedStates.add(timedState);
        }
    }

    public void receiveMessage(byte[] bytes) {
        receiveMessage(new String(bytes));
    }

    public String getLastState() {
        return receivedStates.get(receivedStates.size() - 1).getValue();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("FIRST_VALUE_RECEIVED")) {
            receivedStates.add(Map.entry(getCurrentTime(), "RUNNING"));
        }
    }
}
