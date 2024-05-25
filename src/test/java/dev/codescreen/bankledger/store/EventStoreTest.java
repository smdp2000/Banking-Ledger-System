package dev.codescreen.bankledger.store;

import dev.codescreen.bankledger.enums.DebitCredit;
import dev.codescreen.bankledger.enums.ResponseCode;
import dev.codescreen.bankledger.event.AuthorizationEvent;
import dev.codescreen.bankledger.event.Event;
import dev.codescreen.bankledger.event.LoadEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class EventStoreTest {
    private EventStore eventStore;

    @BeforeEach
    void setUp() {
        eventStore = new EventStore();
    }

    @Test
    void addEventAndRetrieveEvents() {
        eventStore.addEvent("user1", new LoadEvent("user1", new BigDecimal("100.00"), "USD"));
        eventStore.addEvent("user1", new AuthorizationEvent("user1", new BigDecimal("50"), "USD", ResponseCode.APPROVED, DebitCredit.DEBIT));

        List<Event> events = eventStore.getEventsOfUser("user1");
        assertEquals(2, events.size(), "Should have two events for user1");
        assertTrue(events.stream().anyMatch(e -> e.getAmount().equals(new BigDecimal("100.00"))));
        assertTrue(events.stream().anyMatch(e -> e.getAmount().equals(new BigDecimal("50"))));
    }

    @Test
    void testLatestSnapshotBalance() {
        eventStore.updateSnapshot("user1", new BigDecimal("150.00"), LocalDateTime.now());
        assertEquals(new BigDecimal("150.00"), eventStore.getLatestSnapshotBalance("user1"), "Snapshot balance should be updated to 150.00");
    }

    @Test
    void testEventsSinceLastSnapshot() throws InterruptedException {
        LocalDateTime snapshotTime = LocalDateTime.now().minusMinutes(1);
        eventStore.updateSnapshot("user1", new BigDecimal("100.00"), snapshotTime);
        Thread.sleep(1000);  // Ensure the timestamp is later

        eventStore.addEvent("user1", new AuthorizationEvent("user1", new BigDecimal("50"), "USD", ResponseCode.APPROVED, DebitCredit.DEBIT));
        List<Event> eventsSinceSnapshot = eventStore.getEventsSinceLastSnapshot("user1");
        assertEquals(1, eventsSinceSnapshot.size(), "Only events after the last snapshot should be retrieved");
    }

    @Test
    void testConcurrency() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        BigDecimal amount = new BigDecimal("10.00");

        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            executorService.submit(() -> {
                eventStore.addEvent("user1", new LoadEvent("user" + finalI, amount, "USD"));
                latch.countDown();
            });
        }

        latch.await(); // Wait for all threads to finish
        executorService.shutdown();

        assertEquals(numberOfThreads, eventStore.getEventsOfUser("user1").size(), "Should have " + numberOfThreads + " events for user1");
    }

    @Test
    void testConcurrencyOnUpdateSnapshot() throws InterruptedException {
        int numberOfThreads = 50; // Number of threads to use for the test
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        LocalDateTime startTime = LocalDateTime.now();

        for (int i = 0; i < numberOfThreads; i++) {
            final int finalI = i;
            executorService.submit(() -> {
                BigDecimal newBalance = new BigDecimal("100.00").add(new BigDecimal(finalI));
                LocalDateTime newTimestamp = startTime.plusSeconds(finalI); // Ensuring distinct timestamps
                eventStore.updateSnapshot("user1", newBalance, newTimestamp);
                latch.countDown();
            });
        }

        latch.await(); // Wait for all threads to complete
        executorService.shutdown();

        BigDecimal expectedBalance = new BigDecimal("100.00").add(new BigDecimal(numberOfThreads - 1));
        LocalDateTime expectedTimestamp = startTime.plusSeconds(numberOfThreads - 1);

        assertEquals(expectedBalance, eventStore.getLatestSnapshotBalance("user1"), "Latest snapshot balance should match the last update");
    }
}
