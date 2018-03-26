package ch.adv.ui.array;

import ch.adv.ui.access.GsonProvider;
import ch.adv.ui.access.Stringifyer;
import ch.adv.ui.logic.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a json string from a given Session
 */
public class ArrayStringifyer implements Stringifyer {

    private static final String EXPECTED_MODULE = "array";
    private static final Logger logger = LoggerFactory.getLogger
            (ArrayStringifyer.class);
    private final GsonProvider gsonProvider;

    //can't be injected, because has to be instantiated with 'new' keyword in
    // Module implementations (default methods of interfaces)
    public ArrayStringifyer() {
        this.gsonProvider = new GsonProvider();
    }

    /**
     * Builds a json string from an array session.
     *
     * @param session the session to be transmitted
     * @return json string representation of the session
     */
    @Override
    public String stringify(final Session session) {
        if (EXPECTED_MODULE.equals(session.getModule().getName())) {
            logger.debug("resulting json: " + gsonProvider
                    .getPrettifyer().toJson(session));
            return gsonProvider.getMinifier().toJson
                    (session);
        } else {
            logger.error("Wrong session for this Stringifyer. Module name is " +
                            "{} but should be {}", session.getSessionName(),
                    EXPECTED_MODULE);
            return null;
        }
    }
}

