package ch.adv.ui.core.presentation;

import ch.adv.ui.core.domain.Session;
import ch.adv.ui.core.util.ResourceLocator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.controlsfx.control.SegmentedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.Locale;
import java.util.Optional;

/**
 * Main UI View
 */
public class RootView {

    private static final Logger logger = LoggerFactory.getLogger(RootView
            .class);
    private static final int ICON_SIZE = 16;
    private final RootViewModel rootViewModel;
    private final FileChooser fileChooser = new FileChooser();

    @FXML
    private Button loadSessionButton;
    @FXML
    private Button closeAllSessionsButton;
    @FXML
    private Button saveActiveSessionButton;
    @FXML
    private Button closeActiveSessionButton;
    @FXML
    private ListView<Session> sessionListView;
    @FXML
    private TabPane sessionTabPane;
    @FXML
    private TitledPane sessionListViewTitle;
    @FXML
    private SegmentedButton changeLanguageButton;
    @FXML
    private ToggleButton english;
    @FXML
    private ToggleButton german;
    @Inject
    private ResourceLocator resourceLocator;


    @Inject
    public RootView(RootViewModel viewModel) {
        this.rootViewModel = viewModel;
        FileChooser.ExtensionFilter extensionFilter = new FileChooser
                .ExtensionFilter("ADV files (*.adv)", "*.adv");

        fileChooser.getExtensionFilters().add(extensionFilter);
    }

    /**
     * Will be called once on a controller when the contents of
     * its associated document have been completely loaded
     */
    @FXML
    public void initialize() {
        bindI18nStrings();
        sessionListView.setItems(rootViewModel.getAvailableSessions());


        rootViewModel.getAvailableSessions()
                .addListener(handleAvailableSessionUpdate());

        sessionListView.setOnMouseClicked(e -> {
            updateSelected(sessionListView
                    .getSelectionModel(), sessionTabPane.getSelectionModel());
        });

        sessionTabPane.setOnMouseClicked(e -> {
            updateSelected(sessionTabPane.getSelectionModel(), sessionListView
                    .getSelectionModel());
        });

        handleLogoVisibility();
        bindButtonProperties();
        initButtons();
        setToolTips();
    }

    /**
     * Reacts to changes in the underlying data structure. Adding a session
     * results in creating a tab and selecting the new session. Removing a
     * session results in closing the tab and selecting another existing
     * session.
     *
     * @return a ListChangeListener
     */
    private ListChangeListener<Session> handleAvailableSessionUpdate() {
        return change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(session -> {
                    Optional<Tab> existingTab = getExistingTab(session);
                    if (!existingTab.isPresent()) {
                        Node sessionView = resourceLocator
                                .loadFXML(ResourceLocator
                                        .Resource.SESSION_VIEW_FXML);
                        Tab newTab = new Tab(session
                                .toString(), sessionView);
                        sessionTabPane.getTabs().add(newTab);
                        sessionTabPane.getSelectionModel()
                                .select(newTab);
                        sessionListView.getSelectionModel()
                                .select(session);
                    }
                });
                change.getRemoved().forEach(session -> {
                    getExistingTab(session)
                            .ifPresent(t -> sessionTabPane.getTabs().remove(t));
                });
            }
        };
    }

    private void updateSelected(SelectionModel source, SelectionModel target) {
        int selectedIndex = source.getSelectedIndex();
        target.select(selectedIndex);
        Session selectedSession = sessionListView.getItems().get(selectedIndex);
        rootViewModel.getCurrentSessionProperty().setValue(selectedSession);
    }

    private void bindI18nStrings() {
        sessionListViewTitle.textProperty()
                .bind(I18n.createStringBinding("title.session_list"));
        english.textProperty().bind(I18n.createStringBinding(
                "session-bar.english"));
        german.textProperty().bind(I18n.createStringBinding(
                "session-bar.german"));
    }

    private void handleLogoVisibility() {
        sessionTabPane.getStyleClass().add("logo");
        sessionTabPane.getTabs()
                .addListener((ListChangeListener<? super Tab>)
                        c -> {
                            int tabNumber = sessionTabPane.getTabs().size();
                            if (tabNumber == 0) {
                                sessionTabPane.getStyleClass().add("logo");
                            } else {
                                sessionTabPane.getStyleClass()
                                        .remove("logo");
                            }
                        });
    }

    private void bindButtonProperties() {
        saveActiveSessionButton.disableProperty()
                .bind(rootViewModel.getNoSessionsProperty());
        closeActiveSessionButton.disableProperty()
                .bind(rootViewModel.getNoSessionsProperty());
        closeAllSessionsButton.disableProperty()
                .bind(rootViewModel.getNoSessionsProperty());
    }


    /**
     * Checks if a Tab is already existing for the given session
     *
     * @param session session to check
     * @return optional tab
     */
    private Optional<Tab> getExistingTab(Session session) {
        return sessionTabPane.getTabs()
                .stream()
                .filter(t -> t.getText().equals(session.toString()))
                .findFirst();
    }

    private void initButtons() {
        FontAwesomeIconView loadIcon = new FontAwesomeIconView();
        loadIcon.setIcon(FontAwesomeIcon.FOLDER_OPEN);
        loadIcon.setGlyphSize(ICON_SIZE);
        loadSessionButton.setGraphic(loadIcon);
        loadSessionButton.textProperty().bind(I18n
                .createStringBinding("button.session-bar.load_session"));

        FontAwesomeIconView saveIcon = new FontAwesomeIconView();
        saveIcon.setIcon(FontAwesomeIcon.FLOPPY_ALT);
        saveIcon.setGlyphSize(ICON_SIZE);
        saveActiveSessionButton.setGraphic(saveIcon);
        saveActiveSessionButton.textProperty().bind(I18n
                .createStringBinding("button.session-bar.save_session"));


        saveActiveSessionButton.sceneProperty().addListener((e, o, n) -> {
            if (n != null) {
                logger.debug("Setting key shortcut to save a session.");
                saveActiveSessionButton.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.S, KeyCombination
                                .SHORTCUT_DOWN),
                        () -> saveActiveSessionButton.fire()
                );
            }
        });

        FontAwesomeIconView closeIcon = new FontAwesomeIconView();
        closeIcon.setIcon(FontAwesomeIcon.TIMES);
        closeIcon.setGlyphSize(ICON_SIZE);
        closeActiveSessionButton.setGraphic(closeIcon);
        closeActiveSessionButton.textProperty().bind(I18n
                .createStringBinding("button.session-bar.close_active"));

        FontAwesomeIconView closeAllIcon = new FontAwesomeIconView();
        closeAllIcon.setIcon(FontAwesomeIcon.TIMES);
        closeAllIcon.setGlyphSize(ICON_SIZE);
        closeAllSessionsButton.setGraphic(closeAllIcon);
        closeAllSessionsButton.textProperty().bind(I18n
                .createStringBinding("button.session-bar.close_all"));

        changeLanguageButton.getToggleGroup().selectToggle(english);
        changeLanguageButton.getToggleGroup().selectedToggleProperty()
                .addListener((e, oldV, newV) -> {
                    if (newV == german) {
                        I18n.setLocale(new Locale("de", "CH"));
                    } else {
                        I18n.setLocale(Locale.UK);
                    }
                });
    }

    private void setToolTips() {
        loadSessionButton.setTooltip(I18n
                .tooltipForKey("tooltip.session-bar.load_session"));
        closeAllSessionsButton.setTooltip(I18n
                .tooltipForKey("tooltip.session-bar.close_all"));
        saveActiveSessionButton.setTooltip(I18n
                .tooltipForKey("tooltip.session-bar.save_session"));
        closeActiveSessionButton.setTooltip(I18n
                .tooltipForKey("tooltip.session-bar.close_active"));
        english.setTooltip(I18n
                .tooltipForKey("tooltip.session-bar.english"));
        german.setTooltip(I18n.tooltipForKey("tooltip.session-bar.german"));
    }

    @FXML
    private void handleClearAllSessionsClicked() {
        rootViewModel.clearAllSessions();
    }

    @FXML
    private void handleLoadSessionClicked() {
        Window stage = sessionTabPane.getScene().getWindow();
        fileChooser.setTitle("Load Session File");
        File file = fileChooser.showOpenDialog(stage);

        if (file != null && file.exists()) {
            rootViewModel.loadSession(file);
        }
    }

    @FXML
    private void handleSaveSessionClicked(ActionEvent event) {
        Window stage = sessionTabPane.getScene().getWindow();
        fileChooser.setTitle("Save Session File");
        fileChooser.setInitialFileName(sessionListView.getSelectionModel()
                .getSelectedItem().getSessionName());
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            String chosenFilePath = file.getPath();
            if (!chosenFilePath.endsWith(".adv")) {
                File fileWithExtension = new File(file.getPath() + ".adv");
                if (fileWithExtension.exists()) {
                    file = new File(chosenFilePath + "_copy.adv");
                } else {
                    file = fileWithExtension;
                }
            }

            rootViewModel.saveSession(file);
        }
    }

    @FXML
    private void handleRemoveSessionClicked() {
        rootViewModel.removeCurrentSession();
    }
}
