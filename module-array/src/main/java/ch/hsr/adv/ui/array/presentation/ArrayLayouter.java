package ch.hsr.adv.ui.array.presentation;

import ch.hsr.adv.commons.array.logic.ConstantsArray;
import ch.hsr.adv.commons.core.logic.domain.Module;
import ch.hsr.adv.commons.core.logic.domain.ModuleGroup;
import ch.hsr.adv.ui.core.logic.Layouter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Creates and positions ArrayElements on a JavaFX Pane
 */
@Singleton
@Module(ConstantsArray.MODULE_NAME)
public class ArrayLayouter implements Layouter {

    private static final String SHOW_OBJECT_RELATIONS = "SHOW_OBJECT_RELATIONS";
    private static final Logger logger = LoggerFactory.getLogger(
            ArrayLayouter.class);

    private final ArrayObjectReferenceLayouter arrayObjectReferenceLayouter;
    private final ArrayDefaultLayouter arrayDefaultLayouter;

    @Inject
    public ArrayLayouter(ArrayObjectReferenceLayouter objectLayouter,
                         ArrayDefaultLayouter arrayDefaultLayouter) {

        this.arrayObjectReferenceLayouter = objectLayouter;
        this.arrayDefaultLayouter = arrayDefaultLayouter;
    }

    /**
     * Delegates the layout of an array to the correct array layouter.
     *
     * @param moduleGroup to be layouted
     * @param flags       to switch between primitive array and object
     *                    references
     * @return layouted snapshot
     */
    @Override
    public Pane layout(ModuleGroup moduleGroup, List<String> flags) {

        boolean showObjectRelations = false;
        boolean showIndices = false;
        if (flags != null) {
            showObjectRelations = flags.stream()
                    .anyMatch(f -> f.equals(SHOW_OBJECT_RELATIONS));
            showIndices = flags.stream()
                    .anyMatch(f -> f.equals(ConstantsArray.SHOW_ARRAY_INDICES));
        }

        if (showObjectRelations) {
            logger.info("Use Object Reference Array Layouter");
            return arrayObjectReferenceLayouter.layout(
                    moduleGroup, showIndices);
        } else {
            logger.info("Use Default Array Layouter");
            return arrayDefaultLayouter.layout(moduleGroup, showIndices);
        }
    }
}
