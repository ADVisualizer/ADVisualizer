package ch.hsr.adv.ui.core.presentation;

import ch.hsr.adv.commons.core.logic.domain.Session;
import ch.hsr.adv.ui.core.access.DatastoreAccess;
import ch.hsr.adv.ui.core.logic.FlowControl;
import ch.hsr.adv.ui.core.logic.events.ADVEvent;
import ch.hsr.adv.ui.core.logic.events.EventManager;
import ch.hsr.adv.ui.core.logic.stores.LayoutedSnapshotStore;
import ch.hsr.adv.ui.core.logic.stores.SessionStore;
import ch.hsr.adv.ui.core.presentation.util.I18n;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles presentation logic for the {@link RootView}. Delegates tasks to
 * the business logic layer.
 *
 * @author mtrentini, mwieland
 */
@Singleton
class RootViewModel {

    private static final Logger logger = LoggerFactory.getLogger(
            RootViewModel.class);

    private static final int NOTIFICATION_FADE_DELAY = 3_000;

    private final ObservableList<Session> availableSessions = FXCollections
            .observableArrayList();
    private final ObjectProperty<Session> currentSessionProperty = new
            SimpleObjectProperty<>();
    private final BooleanProperty noSessionsProperty = new
            SimpleBooleanProperty(true);
    private final StringProperty notificationMessageProperty = new
            SimpleStringProperty("");

    private final DatastoreAccess fileAccess;
    private final SessionStore sessionStore;
    private final FlowControl flowControl;
    private final LayoutedSnapshotStore layoutedSnapshotStore;

    @Inject
    RootViewModel(SessionStore sessionStore,
                  FlowControl flowControl,
                  LayoutedSnapshotStore layoutedSnapshotStore,
                  EventManager eventManager,
                  DatastoreAccess fileAccess) {

        this.sessionStore = sessionStore;
        this.flowControl = flowControl;
        this.layoutedSnapshotStore = layoutedSnapshotStore;
        this.fileAccess = fileAccess;

        eventManager.subscribe(new SessionStoreListener(),
                List.of(ADVEvent.SESSION_ADDED)
        );

        eventManager.subscribe((e) -> {
            String i18nKey = (String) e.getNewValue();
            showNotification(i18nKey);
        }, ADVEvent.NOTIFICATION);
    }

    ObservableList<Session> getAvailableSessions() {
        return availableSessions;
    }

    ObjectProperty<Session> getCurrentSessionProperty() {
        return currentSessionProperty;
    }

    BooleanProperty getNoSessionsProperty() {
        return noSessionsProperty;
    }

    StringProperty getNotificationMessageProperty() {
        return notificationMessageProperty;
    }

    /**
     * Delegates removing the current session to the business logic
     */
    void removeCurrentSession() {
        Session current = currentSessionProperty.get();
        if (current != null) {
            logger.info("Removing session {} ({})", current.getSessionName(),
                    current.getSessionId());
            removeSession(current);
        }
    }

    private void removeSession(Session session) {
        try {
            sessionStore.delete(session.getSessionId());
            layoutedSnapshotStore.deleteAll(session.getSessionId());
            logger.info("Deleting session {} ({})", session
                    .getSessionName(), session.getSessionId());
            availableSessions.remove(session);
            if (availableSessions.isEmpty()) {
                currentSessionProperty.setValue(null);
                noSessionsProperty.set(true);
            } else {
                int size = availableSessions.size();
                currentSessionProperty
                        .setValue(availableSessions.get(size - 1));
            }
            showNotification(I18n.NOTIFICATION_SESSION_CLOSE_SUCCESSFUL);
        } catch (Exception e) {
            showNotification(I18n.NOTIFICATION_SESSION_CLOSE_UNSUCCESSFUL);
        }
    }

    /**
     * Delegates removing all sessions to the business logic
     */
    void clearAllSessions() {
        List<Session> sessionsToRemove = new ArrayList<>(availableSessions);
        sessionsToRemove.forEach(this::removeSession);
        showNotification(I18n.NOTIFICATION_SESSION_CLOSE_ALL);
    }

    /**
     * Delegates saving the current session to the access layer
     *
     * @param file to be saved to
     */
    void saveSession(File file) {
        Session session = currentSessionProperty.get();
        flowControl.save(session, file);
    }

    /**
     * Loads an existing session from the data store.
     *
     * @param file file to open
     */
    void loadSession(File file) {
        try {
            String json = fileAccess.read(file);
            flowControl.load(json);
        } catch (IOException e) {
            showNotification(I18n.NOTIFICATION_SESSION_LOAD_UNSUCCESSFUL);
        }
    }

    private void showNotification(String i18nKey) {
        Platform.runLater(() -> {

            // show notification
            notificationMessageProperty.set(I18n.get(i18nKey));

            // fade notification after delay
            Task<Void> resetTask = new Task<>() {
                @Override
                public Void call() {
                    try {
                        Thread.sleep(NOTIFICATION_FADE_DELAY);
                    } catch (InterruptedException e) {
                        logger.error("Notification reset task failed", e);
                    }
                    return null;
                }
            };

            resetTask.setOnSucceeded(taskFinishEvent ->
                    notificationMessageProperty.set(""));

            new Thread(resetTask).start();
        });
    }

    /**
     * Updates ui if session store has changed.
     */
    private class SessionStoreListener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            logger.info("SessionStore has updated: Session was added. "
                    + "Update ListView");
            Platform.runLater(() -> {
                // current needs to be set first! Otherwise NullPointer when
                // creating SessionView
                Session newSession = (Session) event.getNewValue();
                currentSessionProperty.setValue(newSession);
                availableSessions.add(newSession);
                if (noSessionsProperty.get()) {
                    noSessionsProperty.set(false);
                }
            });
        }
    }
}

