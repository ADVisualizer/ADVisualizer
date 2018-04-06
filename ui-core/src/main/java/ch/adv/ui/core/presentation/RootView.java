package ch.adv.ui.core.presentation;

import ch.adv.ui.core.domain.Session;
import ch.adv.ui.core.util.ResourceLocator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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
    private final RootViewModel rootViewModel;
    private final FileChooser fileChooser = new FileChooser();
    @FXML
    private Menu menuADV;
    @FXML
    private Menu menuHelp;
    @FXML
    private MenuItem menuItemClose;
    @FXML
    private MenuItem menuItemHelp;
    @FXML
    private Button loadSessionButton;
    @FXML
    private Button clearAllSessionsButton;
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
     * Will be called once on an controller when the contents of
     * its associated document have been completely loaded
     */
    @FXML
    public void initialize() {
        bindStrings();
        menuItemClose.setOnAction(e -> handleCloseMenuItemClicked());
        loadSessionButton.setOnAction(e -> handleLoadSessionClicked());
        clearAllSessionsButton.setOnAction(event ->
                handleClearAllSessionsClicked());
        sessionListView.setItems(rootViewModel.getAvailableSessions());
        sessionListView.setCellFactory(lv -> new CustomListCell());

        handleLogoVisibility();
        openNewTab();
        initLanguageButtons();
        setToolTips();
    }

    private void bindStrings() {
        menuItemClose.textProperty()
                .bind(I18n.createStringBinding("menu.item.close"));
        menuItemHelp.textProperty()
                .bind(I18n.createStringBinding("menu.item.help"));
        menuADV.textProperty()
                .bind(I18n.createStringBinding("menu.adv"));
        menuHelp.textProperty()
                .bind(I18n.createStringBinding("menu.help"));
        sessionListViewTitle.textProperty()
                .bind(I18n.createStringBinding("title.session_list"));
        english.textProperty().bind(I18n.createStringBinding("tooltip"
                + ".session-bar.english"));
        german.textProperty().bind(I18n.createStringBinding("tooltip"
                + ".session-bar.german"));
    }


    private void handleCloseMenuItemClicked() {
        Platform.exit();
        System.exit(0);
    }

    private void handleLogoVisibility() {
        sessionTabPane.getStyleClass().add("logo");
        sessionTabPane.getTabs().addListener((ListChangeListener<? super Tab>)
                c -> {
                    int tabNumber = sessionTabPane.getTabs().size();
                    if (tabNumber == 0) {
                        sessionTabPane.getStyleClass().add("logo");
                    } else {
                        sessionTabPane.getStyleClass().remove("logo");
                    }
                });
    }

    private void openNewTab() {
        rootViewModel.getCurrentSessionPropertyProperty().addListener(
                this::openTabAction);
        sessionListView.setOnMouseClicked(e -> {
            int selectedItem = sessionListView.getSelectionModel()
                    .getSelectedIndex();
            if (sessionListView.getFocusModel().isFocused(selectedItem)) {
                sessionListView.getSelectionModel().select(-1);
                sessionListView.getSelectionModel().select(selectedItem);
            }
        });
        sessionListView.getSelectionModel().selectedItemProperty().addListener(
                this::openTabAction);
    }

    private void openTabAction(ObservableValue<? extends Session>
                                       observableValue, Session oldSession,
                               Session session) {
        if (session != null) {

            Optional<Tab> existingTab = getExistingTab(session);

            if (!existingTab.isPresent()) {
                Node sessionView = resourceLocator.loadFXML(ResourceLocator
                        .Resource.SESSION_VIEW_FXML);
                Tab newTab = new Tab(session.toString(), sessionView);
                sessionTabPane.getTabs().add(newTab);
                sessionTabPane.getSelectionModel().select(newTab);
            } else {
                sessionTabPane.getSelectionModel().select(existingTab.get());
            }
        }
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

    private void initLanguageButtons() {
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
        clearAllSessionsButton.setTooltip(I18n
                .tooltipForKey("tooltip.session-bar.delete_sessions"));
        english.setTooltip(I18n.tooltipForKey("tooltip.session-bar.english"));
        german.setTooltip(I18n.tooltipForKey("tooltip.session-bar.german"));
    }

    private void handleRemoveSessionClicked(final Session session, final
    MouseEvent event) {
        logger.info("Removing session {} ({})", session.getSessionName(),
                session.getSessionId());

        Optional<Tab> existingTab = getExistingTab(session);

        if (existingTab.isPresent()) {
            sessionTabPane.getTabs().remove(existingTab.get());
        }

        rootViewModel.removeSession(session);

        if (event != null) {
            event.consume();
        }
    }


    private void handleClearAllSessionsClicked() {
        sessionListView.getItems().forEach(session -> {
            handleRemoveSessionClicked(session, null);
        });
    }


    private void handleLoadSessionClicked() {
        Window stage = sessionTabPane.getScene().getWindow();
        fileChooser.setTitle("Load Session File");
        File file = fileChooser.showOpenDialog(stage);

        if (file != null && file.exists()) {
            rootViewModel.loadSession(file);
        }
    }

    private void handleSaveSessionClicked(final Session session) {
        Window stage = sessionTabPane.getScene().getWindow();
        fileChooser.setTitle("Save Session File");
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

            rootViewModel.saveSession(file, session);
        }
    }

    /**
     * Represents a ListCell in the {@link ListView}.
     * <p>
     * It contains the label and a save and remove button.
     */
    private class CustomListCell extends ListCell<Session> {

        private static final int ICON_SIZE = 16;
        private static final int SPACING = 12;
        private final FontAwesomeIconView saveIcon;
        private final FontAwesomeIconView removeIcon;
        private HBox hbox = new HBox();
        private Label label = new Label("(empty)");
        private Pane pane = new Pane();
        private Label removeButton = new Label();
        private Label saveButton = new Label();

        CustomListCell() {
            super();

            this.removeIcon = new FontAwesomeIconView();
            removeIcon.setIcon(FontAwesomeIcon.TRASH_ALT);
            removeIcon.setGlyphSize(ICON_SIZE);
            removeButton.setGraphic(removeIcon);
            removeButton.setTooltip(I18n
                    .tooltipForKey("tooltip.session-list.remove_session"));
            removeButton.setOnMouseClicked(event -> handleRemoveSessionClicked(
                    getItem(), event));

            this.saveIcon = new FontAwesomeIconView();
            saveIcon.setIcon(FontAwesomeIcon.FLOPPY_ALT);
            saveIcon.setGlyphSize(ICON_SIZE);
            saveButton.setGraphic(saveIcon);
            saveButton.setTooltip(I18n
                    .tooltipForKey("tooltip.session-list.save_session"));
            saveButton.setOnMouseClicked(e -> handleSaveSessionClicked(
                    getItem()));

            hbox.getChildren().addAll(label, pane, saveButton, removeButton);
            hbox.setSpacing(SPACING);
            hbox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pane, Priority.ALWAYS);
        }


        /**
         * {@inheritDoc}
         *
         * @param session the new session to be displayed
         */
        @Override
        protected void updateItem(Session session, boolean empty) {
            super.updateItem(session, empty);
            setText(null);  // No text in label of super class
            if (empty || session == null) {
                label.setText("null");
                setGraphic(null);
            } else {
                label.setText(session.toString());
                setGraphic(hbox);
            }
        }
    }

}
