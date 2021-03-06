package ch.hsr.adv.ui.core.logic.events;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * This class is responsible for the event dispatching of all ADVEvents
 * It uses Java's PropertyChangeSupport internally.
 * <p>
 * Modules can register PropertyChangeListeners to listen
 * for specific Events which are fired by the ADV framework.
 *
 * @author mwieland
 */
@Singleton
public class EventManager {

    private static final Logger logger = LoggerFactory.getLogger(
            EventManager.class);

    private final PropertyChangeSupport eventStore = new
            PropertyChangeSupport(this);

    /**
     * Registers a listener for a specific event
     * <p>
     * If filter arguments are provided, they are added to the event handle.
     * This can be handy to narrow the context of an event (e.g. only
     * listen for added snapshots in the session with id XY).
     * <p>
     * usage example:
     * <code>
     *     subscribe(listener, ADVEvent.SESSION_REMOVED, sessionId + "")
     * </code>
     *
     * @param listener   listener
     * @param event      ADV event handle
     * @param filterArgs optional filter arguments
     */
    public void subscribe(PropertyChangeListener listener, ADVEvent event,
                          String... filterArgs) {
        String handle = event.toString() + String.join("-", filterArgs);
        eventStore.addPropertyChangeListener(handle, listener);
    }

    /**
     * Registers a listener for multiple events
     * <p>
     * If filter arguments are provided, they are added to the event handle.
     * This can be handy to narrow the context of an event (e.g.
     * only listen for added snapshots in the session with id XY).
     * <p>
     * usage example:
     * <code>
     *     subscribe(listener, eventlist, sessionId + "")
     * </code>
     *
     * @param listener   listener
     * @param events     ADV events handle
     * @param filterArgs optional filter arguments
     */
    public void subscribe(PropertyChangeListener listener,
                          List<ADVEvent> events, String... filterArgs) {
        events.forEach(e -> subscribe(listener, e, filterArgs));
    }

    /**
     * Removes the listener for a specific event
     * <p>
     * If filter arguments are provided, they are added to the event handle.
     * This can be handy to narrow the context of an event (e.g. only
     * listen for added snapshots in the session with id XY).
     * <p>
     * usage example:
     * <code>
     *     unsubscribe(listener, ADVEvent.SESSION_REMOVED, sessionId + "")
     * </code>
     *
     * @param listener   listener
     * @param event      ADV event handle
     * @param filterArgs optional filter arguments
     */
    public void unsubscribe(PropertyChangeListener listener, ADVEvent event,
                            String... filterArgs) {
        String handle = event.toString() + String.join("-", filterArgs);
        eventStore.removePropertyChangeListener(handle, listener);
    }

    /**
     * Removes the listener for multiple events
     * <p>
     * usage example:
     * <code>
     *     unsubscribe(listener, eventlist, sessionId + "")
     * </code>
     *
     * @param listener   listener
     * @param events     ADV events
     * @param filterArgs optional filter arguments
     */
    public void unsubscribe(PropertyChangeListener listener,
                            List<ADVEvent> events, String... filterArgs) {
        events.forEach(e -> unsubscribe(listener, e, filterArgs));
    }

    /**
     * Executes an ADV event and transmits the old and the new
     * value to all subscribers.
     * <p>
     * If filter arguments are provided, they are added to the event handle.
     * This can be handy to narrow the context of an event (e.g. only
     * listen for added snapshots in the session with id XY).
     * <p>
     * usage example:
     * <code>
     *     unsubscribe(ADVEvent.SESSION_REMOVED, session, null, sessionId + "")
     * </code>
     *
     * @param event      event handle
     * @param oldVal     value before change
     * @param newVal     value after change
     * @param filterArgs optional filter arguments
     */
    public void fire(ADVEvent event, Object oldVal, Object newVal,
                     String... filterArgs) {
        String handle = event.toString();
        eventStore.firePropertyChange(handle, oldVal, newVal);
        logger.debug("Fired event {}", handle);
        if (filterArgs.length > 0) {
            handle += String.join("-", filterArgs);
            eventStore.firePropertyChange(handle, oldVal, newVal);
            logger.debug("Fired event {}", handle);
        }
    }
}
