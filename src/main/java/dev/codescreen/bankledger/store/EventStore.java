package dev.codescreen.bankledger.store;

import dev.codescreen.bankledger.event.Event;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class EventStore {

    private final ConcurrentHashMap<String, List<Event>> eventsByUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BigDecimal> lastSnapshotBalance = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lastSnapshotTimestamp = new ConcurrentHashMap<>();



    public void addEvent(String userId, Event event) {
        eventsByUser.computeIfAbsent(userId, k -> Collections.synchronizedList(new LinkedList<>())).add(event);
    }

    public List<Event> getEventsOfUser(String userId) {
        return eventsByUser.getOrDefault(userId, Collections.emptyList());
    }

    public BigDecimal getLatestSnapshotBalance(String userId) {
        return lastSnapshotBalance.getOrDefault(userId, BigDecimal.ZERO);
    }

    public void updateSnapshot(String userId, BigDecimal balance, LocalDateTime timestamp) {
        synchronized (this) {
            lastSnapshotBalance.put(userId, balance);
            lastSnapshotTimestamp.put(userId, timestamp);
        }
    }

    public List<Event> getEventsSinceLastSnapshot(String userId) {
        LocalDateTime snapshotTimestamp = lastSnapshotTimestamp.getOrDefault(userId, LocalDateTime.MIN);
        return getEventsOfUser(userId).stream()
                .filter(event -> event.getTimestamp().isAfter(snapshotTimestamp))
                .collect(Collectors.toList());
    }


}
