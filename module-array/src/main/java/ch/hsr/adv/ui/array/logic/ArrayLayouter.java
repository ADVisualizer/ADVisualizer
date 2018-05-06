package ch.hsr.adv.ui.array.logic;

import ch.hsr.adv.ui.core.logic.Layouter;
import ch.hsr.adv.ui.core.logic.domain.LayoutedSnapshot;
import ch.hsr.adv.ui.core.logic.domain.Module;
import ch.hsr.adv.ui.core.logic.domain.ModuleGroup;
import ch.hsr.adv.ui.core.logic.domain.Snapshot;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Positions the ArrayElements on the Pane
 */
@Singleton
@Module("array")
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
     * Layouts an Array snapshot if it is not already layouted
     *
     * @param snapshot to be layouted
     * @return layouted snapshot
     */
    @Override
    public Pane layout(ModuleGroup moduleGroup, List<String> flags) {

        boolean showObjectRelations = false;
        if (flags != null) {
            showObjectRelations = flags.stream().anyMatch(
                    f -> f.equals(SHOW_OBJECT_RELATIONS));
        }

        Pane pane;
        if (showObjectRelations) {
            logger.info("Use Object Reference Array Layouter");
            pane = arrayObjectReferenceLayouter.layout(moduleGroup);
        } else {
            logger.info("Use Default Array Layouter");
            pane = arrayDefaultLayouter.layout(moduleGroup);
        }

        return pane;
    }
}
