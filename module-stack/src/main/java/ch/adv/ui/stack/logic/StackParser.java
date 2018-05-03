package ch.adv.ui.stack.logic;

import ch.adv.ui.core.logic.InterfaceAdapter;
import ch.adv.ui.core.logic.Parser;
import ch.adv.ui.core.logic.domain.ADVElement;
import ch.adv.ui.core.logic.domain.Module;
import ch.adv.ui.core.logic.domain.Session;
import ch.adv.ui.core.logic.domain.styles.ADVStyle;
import ch.adv.ui.core.logic.domain.styles.ADVValueStyle;
import ch.adv.ui.core.logic.util.ADVParseException;
import ch.adv.ui.stack.logic.domain.StackElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a json of a stack session to a Session object.
 */
@Singleton
@Module("stack")
public class StackParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(StackParser
            .class);
    private final Gson gson;

    /**
     * Registers Stack specific types to the GsonBuilder
     */
    public StackParser() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ADVElement.class, new
                InterfaceAdapter(StackElement.class));
        gsonBuilder.registerTypeAdapter(ADVStyle.class, new
                InterfaceAdapter(ADVValueStyle.class));
        gson = gsonBuilder.create();
    }
    
    @Override
    public Session parse(String json) throws ADVParseException {
        logger.debug("Parsing json: \n {}", json);
        Session session = gson.fromJson(json, Session.class);
        // sessionId wasn't found in json, so id is default initialized
        if (session.getSessionId() == 0) {
            throw new ADVParseException("No SessionId found.");
        }
        return session;
    }

}