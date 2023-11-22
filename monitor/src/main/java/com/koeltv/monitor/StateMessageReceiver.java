package com.koeltv.monitor;

import com.koeltv.monitor.file.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final FileHandler stateHandler;
    private final List<Map.Entry<String, String>> receivedStates = new ArrayList<>();

    public StateMessageReceiver(
            @Autowired LogMessageReceiver logMessageReceiver,
            @Qualifier("stateHandler") FileHandler stateHandler
    ) {
        this.stateHandler = stateHandler;
        logMessageReceiver.addListener(this);

        receivedStates.addAll(stateHandler.readLinesFromFile().stream().map(line -> {
            String[] parts = line.split(" ");
            return Map.entry(parts[0], parts[1]);
        }).toList());

        var initState = Map.entry(getCurrentTime(), "INIT");
        receivedStates.add(initState);
        stateHandler.appendLineToFile(initState.getKey() + " " + initState.getValue());
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
            stateHandler.appendLineToFile(String.format("%s %s", timedState.getKey(), timedState.getValue()));
            receivedStates.add(timedState);
        }
    }

    public void receiveMessage(byte[] bytes) {
        receiveMessage(new String(bytes));
    }

    public String getLastState() {
        return receivedStates.get(receivedStates.size() - 1).getValue();
    }

    public List<String> getStateTransitions() {
        List<String> stateTransitions = new ArrayList<>(receivedStates.size() - 1);
        for (int i = 1; i < receivedStates.size(); i++) {
            stateTransitions.add(String.format(
                    "%s: %s->%s",
                    receivedStates.get(i).getKey(),
                    receivedStates.get(i - 1).getValue(),
                    receivedStates.get(i).getValue()
            ));
        }
        return stateTransitions;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("FIRST_VALUE_RECEIVED")) {
            receivedStates.add(Map.entry(getCurrentTime(), "RUNNING"));
        }
    }
}
