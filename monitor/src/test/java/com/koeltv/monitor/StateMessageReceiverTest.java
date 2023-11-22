package com.koeltv.monitor;

import com.koeltv.monitor.file.FileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class StateMessageReceiverTest {
    FileHandler logHandler = Mockito.mock(FileHandler.class);
    FileHandler stateHandler = Mockito.mock(FileHandler.class);
    LogMessageReceiver logReceiver;
    StateMessageReceiver receiver;

    @BeforeEach
    void setup() {
        when(logHandler.readLinesFromFile()).thenReturn(Collections.emptyList());
        when(stateHandler.readLinesFromFile()).thenReturn(Collections.emptyList());
        logReceiver = new LogMessageReceiver(null, logHandler);
        receiver = new StateMessageReceiver(logReceiver, stateHandler);
    }

    @Test
    void testInitialState() {
        assertEquals("INIT", receiver.getLastState());
    }

    @Test
    void testFirstLogEvent() {
        logReceiver.receiveMessage("log");
        assertEquals("RUNNING", receiver.getLastState());
    }

    @Test
    void testRegisterStateChanges() {
        receiver.receiveMessage("RUNNING");
        receiver.receiveMessage("PAUSED");
        List<String[]> stateTransitions = receiver.getStateTransitions().stream().map(s -> s.split(" ")).toList();
        assertEquals(2, stateTransitions.size());
        assertEquals("INIT->RUNNING", stateTransitions.get(0)[1]);
        assertEquals("RUNNING->PAUSED", stateTransitions.get(1)[1]);
    }
}
