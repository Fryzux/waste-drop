package ru.mirea.wastemanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestStatusTest {

    @Test
    @DisplayName("Проверка допустимых переходов из статуса NEW")
    void testTransitionsFromNew() {
        RequestStatus status = RequestStatus.NEW;
        assertTrue(status.canTransitionTo(RequestStatus.IN_PROGRESS), "NEW должен переходить в IN_PROGRESS");
        assertTrue(status.canTransitionTo(RequestStatus.CANCELLED), "NEW должен переходить в CANCELLED");
        assertFalse(status.canTransitionTo(RequestStatus.COMPLETED), "NEW не должен сразу переходить в COMPLETED");
        assertFalse(status.canTransitionTo(RequestStatus.NEW), "Переход в самого себя запрещен");
    }

    @Test
    @DisplayName("Проверка допустимых переходов из статуса IN_PROGRESS")
    void testTransitionsFromInProgress() {
        RequestStatus status = RequestStatus.IN_PROGRESS;
        assertTrue(status.canTransitionTo(RequestStatus.COMPLETED), "IN_PROGRESS должен переходить в COMPLETED");
        assertTrue(status.canTransitionTo(RequestStatus.CANCELLED), "IN_PROGRESS должен переходить в CANCELLED");
        assertFalse(status.canTransitionTo(RequestStatus.NEW), "IN_PROGRESS не может откатываться в NEW");
    }

    @Test
    @DisplayName("Проверка терминальности статуса COMPLETED")
    void testCompletedIsTerminal() {
        RequestStatus status = RequestStatus.COMPLETED;
        for (RequestStatus next : RequestStatus.values()) {
            assertFalse(status.canTransitionTo(next), "Из COMPLETED запрещены любые переходы в " + next);
        }
    }

    @Test
    @DisplayName("Проверка терминальности статуса CANCELLED")
    void testCancelledIsTerminal() {
        RequestStatus status = RequestStatus.CANCELLED;
        for (RequestStatus next : RequestStatus.values()) {
            assertFalse(status.canTransitionTo(next), "Из CANCELLED запрещены любые переходы в " + next);
        }
    }

    @Test
    @DisplayName("Защита от null при проверке перехода")
    void testNullSafety() {
        assertFalse(RequestStatus.NEW.canTransitionTo(null), "Переход в null должен возвращать false");
    }
}
