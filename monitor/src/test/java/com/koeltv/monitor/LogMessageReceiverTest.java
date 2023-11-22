package com.koeltv.monitor;

import com.koeltv.monitor.file.FileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class LogMessageReceiverTest {
    FileHandler logHandler = Mockito.mock(FileHandler.class);
    LogMessageReceiver receiver;

    @BeforeEach
    void setup() {
        when(logHandler.readLinesFromFile()).thenReturn(Collections.emptyList());
        receiver = new LogMessageReceiver(null, logHandler);
    }

    @Test
    void testInitialState() {
        assertEquals(Collections.emptyList(), receiver.getReceivedMessages());
    }

    @Test
    void testFirstLogEvent() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        receiver.addListener(evt -> {
            if (evt.getPropertyName().equals("FIRST_VALUE_RECEIVED")) {
                eventReceived.set(true);
            }
        });
        receiver.receiveMessage("log");

        List<String> logs = receiver.getReceivedMessages();
        assertEquals(1, logs.size());
        assertTrue(eventReceived.get());
    }

    @Test
    void testRegisterLog() {
        receiver.receiveMessage("log 1");
        receiver.receiveMessage("log 2");
        List<String> logs = receiver.getReceivedMessages();
        assertEquals(2, logs.size());
        assertEquals("log 1", logs.get(0));
        assertEquals("log 2", logs.get(1));
    }
}
