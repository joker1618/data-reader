package xxx.joker.apps.datareader.jfx.view.pane;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import xxx.joker.apps.datareader.jfx.model.GuiModel;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;
import xxx.joker.libs.core.adapter.JkProcess;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Arrays;

import static xxx.joker.libs.core.javafx.JfxControls.createHBox;

@Component
public class PaneLeft extends VBox {

    private static final Logger LOG = LoggerFactory.getLogger(PaneLeft.class);

    @Autowired
    private GuiModel guiModel;

    private final ListView<Path> lvPaths = new ListView<>();

    @PostConstruct
    public void init() {
        // Config list view
        lvPaths.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        lvPaths.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> {
            guiModel.selectedPathSet(n);
            guiModel.selectedTableItemSet(null);
        });
        lvPaths.setCellFactory(param -> new ListCell<Path>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getFileName().toString());
                    ObsCsv obsCsv = guiModel.getObsCsvMap().get(item);
                    obsCsv.changedProperty().addListener((obs,o,n) -> setTextFill(n ? Color.RED : Color.BLACK));
                }
            }
        });
        guiModel.csvPathsOnChange(plist -> lvPaths.getItems().setAll(plist));

        // Buttons
        Button btnCommit = new Button("COMMIT");
        Button btnRollback = new Button("ROLLBACK");
        HBox boxButtons = createHBox("buttons", btnCommit, btnRollback);

        btnCommit.setOnAction(e -> guiModel.commit());
        btnRollback.setOnAction(e -> guiModel.rollback());

        Arrays.asList(btnCommit, btnRollback).forEach(btn -> btn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> guiModel.getChangedItemMap().isEmpty(),
                guiModel.getChangedItemMap()
        )));

        getChildren().add(boxButtons);
        getChildren().add(lvPaths);

        // todo delete
        Button btnTmp = new Button("__TMP__");
        btnTmp.setOnAction(e -> doWork("from button tmp"));
        getChildren().add(createHBox("temp", btnTmp));

        getStyleClass().add("rootLeft");
    }


    // todo delete
    public void doWork(String logString) {
        LOG.debug(logString);
        String folderName = guiModel.getSelectedPath() == null ? "" : guiModel.getSelectedPath().getCsvPath().getParent().toString();
        JkProcess.execute("explorer.exe {}", folderName);
    }

}
