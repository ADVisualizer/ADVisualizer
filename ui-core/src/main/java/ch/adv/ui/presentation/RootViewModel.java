package ch.adv.ui.presentation;

import ch.adv.ui.logic.model.Session;
import ch.adv.ui.logic.SessionStore;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

public class RootViewModel {

    private final ObservableList<Session> availableSessions;
    private final ObjectProperty<Session> currentSession;
    private final SessionStore sessionStore;

    private static final Logger logger = LoggerFactory.getLogger
            (RootViewModel.class);

    @Inject
    public RootViewModel(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
        this.availableSessions = FXCollections.observableArrayList();
        this.currentSession = new SimpleObjectProperty<>();

        sessionStore.addPropertyChangeListener(new
                SessionPropertyChangeListener());
    }

    public ObservableList<Session> getAvailableSessions() {
        return availableSessions;
    }

    public ObjectProperty<Session> currentSessionProperty() {
        return currentSession;
    }

    /**
     * Delegates removing sessions to the business logic
     *
     * @param session to be removed
     */
    public void removeSession(final Session session) {
        sessionStore.deleteSession(session);
    }

    /**
     * Delegates saving sessions to the business logic
     *
     * @param file    to be saved to
     * @param session to be saved
     */
    public void saveSession(final File file, final Session session) {
        String json = session.getModule().getStringifyer().stringify(session);
        session.getModule().getDatastore().write(file, json);
    }

    private class SessionPropertyChangeListener implements
            PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            logger.debug("SessionStore has updated. Update ListView");
            logger.info(evt.getPropertyName());
            List<Session> sessions = sessionStore.getSessions();

            Platform.runLater(() -> {
                currentSession.setValue(sessionStore.getCurrentSession());
                availableSessions.clear();
                availableSessions.addAll(sessions);
            });
        }
    }
}