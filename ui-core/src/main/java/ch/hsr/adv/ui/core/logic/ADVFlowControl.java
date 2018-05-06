package ch.hsr.adv.ui.core.logic;

import ch.hsr.adv.ui.core.logic.domain.LayoutedSnapshot;
import ch.hsr.adv.ui.core.logic.domain.Session;
import ch.hsr.adv.ui.core.logic.domain.Snapshot;
import ch.hsr.adv.ui.core.logic.util.ADVParseException;
import ch.hsr.adv.ui.core.presentation.util.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Framework component which controls the default visualization flow.
 */
@Singleton
public class ADVFlowControl implements FlowControl {

    private static final Logger logger = LoggerFactory.getLogger(
            ADVFlowControl.class);

    private final SessionStore sessionStore;
    private final ServiceProvider serviceProvider;
    private final LayoutedSnapshotStore layoutedSnapshotStore;
    private final EventManager eventManager;
    private final CoreParser coreParser;
    private final CoreLayouter coreLayouter;

    @Inject
    public ADVFlowControl(SessionStore sessionStore,
                          ServiceProvider serviceProvider,
                          LayoutedSnapshotStore layoutedSnapshotStore,
                          EventManager eventManager,
                          CoreParser coreParser, CoreLayouter coreLayouter) {
        this.sessionStore = sessionStore;
        this.serviceProvider = serviceProvider;
        this.layoutedSnapshotStore = layoutedSnapshotStore;
        this.eventManager = eventManager;
        this.coreParser = coreParser;
        this.coreLayouter = coreLayouter;
    }

    /**
     * Parses and stores incoming json either from the socket or the file
     * system. Kicks off the layouting process.
     *
     * @param sessionJSON json
     */
    public void process(String sessionJSON) {
        try {
            logger.info("Processing JSON...");

            // parse session
            Session session = coreParser.parse(sessionJSON);
            long sessionId = session.getSessionId();

            // filter new snapshots
            List<Snapshot> newSnapshots = session.getSnapshots().stream()
                    .filter(s -> !layoutedSnapshotStore.contains(sessionId,
                            s.getSnapshotId()))
                    .collect(Collectors.toList());

            // Layout only snapshots that have not yet been layouted
            newSnapshots.forEach(snapshot -> {

                // layout
                List<Pane> panes = new ArrayList<>();
                snapshot.getModuleGroups().forEach(group -> {
                    String moduleName = group.getModuleName();
                    Layouter layouter = serviceProvider.getLayouter(moduleName);
                    Pane pane = layouter.layout(group, session.getFlags());
                    panes.add(pane);
                });

                Region parent = coreLayouter.layout(panes);
                LayoutedSnapshot layoutedSnapshot = new LayoutedSnapshot(
                        snapshot.getSnapshotId(), parent);

                // store layouted snapshot
                layoutedSnapshotStore.add(sessionId, layoutedSnapshot);

            });

            if (!newSnapshots.isEmpty()) {
                sessionStore.add(session);
                eventManager.fire(ADVEvent.NOTIFICATION, null,
                        I18n.NOTIFICATION_SESSION_LOAD_SUCCESSFUL);
            } else {
                sessionStore.setCurrent(sessionId);
                eventManager.fire(ADVEvent.NOTIFICATION, null,
                        I18n.NOTIFICATION_SESSION_LOAD_EXISTING);
            }
            logger.info("Process finished: delegated session and snapshot "
                    + "creation.");
        } catch (ADVParseException e) {
            eventManager.fire(ADVEvent.NOTIFICATION, null,
                    I18n.NOTIFICATION_SESSION_LOAD_UNSUCCESSFUL);
        }
    }

}
