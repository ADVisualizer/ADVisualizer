package ch.hsr.adv.ui.tree.logic;

import ch.hsr.adv.commons.core.access.GsonProvider;
import ch.hsr.adv.commons.core.logic.domain.ModuleGroup;
import ch.hsr.adv.ui.core.logic.Stringifyer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes a tree ModuleGroup to json
 */
public abstract class TreeStringifyer implements Stringifyer {

    private static final Logger logger = LoggerFactory
            .getLogger(TreeStringifyer.class);

    private final Gson gson;

    protected TreeStringifyer(GsonProvider gsonProvider) {
        this.gson = gsonProvider.getPrettifyer().create();
    }

    /**
     * Builds a json string from a tree module group.
     *
     * @param moduleGroup the moduleGroup to be transmitted
     * @return json string representation of the session
     */
    @Override
    public JsonElement stringify(ModuleGroup moduleGroup) {
        logger.info("Serialize stack group");
        String json = gson.toJson(moduleGroup);
        return gson.fromJson(json, JsonElement.class);
    }

}
