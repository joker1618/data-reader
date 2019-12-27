package xxx.joker.apps.datareader.jfx.view.pane;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import xxx.joker.apps.datareader.jfx.model.GuiModel;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.datareader.jfx.view.beans.FilterObj;
import xxx.joker.apps.datareader.jfx.view.controls.GridPaneBuilder;
import xxx.joker.libs.core.adapter.JkProcess;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static xxx.joker.libs.core.javafx.JfxControls.createHBox;
import static xxx.joker.libs.core.javafx.JfxControls.createVBox;
import static xxx.joker.libs.core.lambda.JkStreams.filter;

@Component
public class PaneLeft extends VBox {

    private static final Logger LOG = LoggerFactory.getLogger(PaneLeft.class);

    @Autowired
    private GuiModel guiModel;
    @Autowired
    private FilterObj filterObj;

    private final ListView<Path> lvPaths = new ListView<>();

    @PostConstruct
    public void init() {
        getStyleClass().add("rootLeft");

        // Config list view
        lvPaths.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        lvPaths.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> {
            guiModel.selectedPathSet(n);
            guiModel.selectedTableItemSet(null);
        });
        SimpleBooleanProperty flagBlack = new SimpleBooleanProperty(true);
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
                    flagBlack.addListener((obs,o,n) -> setTextFill(Color.BLACK));
                }
            }
        });
        guiModel.csvPathsOnChange(plist -> lvPaths.getItems().setAll(plist));

        // Buttons
        Button btnCommit = new Button("COMMIT");
        Button btnRollback = new Button("ROLLBACK");
        HBox boxButtons = createHBox("buttons", btnCommit, btnRollback);

        btnCommit.setOnAction(e -> {guiModel.commit();flagBlack.set(!flagBlack.get());});
        btnRollback.setOnAction(e -> {guiModel.rollback();flagBlack.set(!flagBlack.get());});

        Arrays.asList(btnCommit, btnRollback).forEach(btn -> btn.disableProperty().bind(createBooleanBinding(
                () -> guiModel.getChangedItemMap().isEmpty(),
                guiModel.getChangedItemMap()
        )));

        getChildren().add(boxButtons);
        getChildren().add(lvPaths);

        Button btnOpenExplorer = new Button("explorer");
        btnOpenExplorer.disableProperty().bind(guiModel.selectedPathProperty().isNull());
        btnOpenExplorer.setOnAction(e -> JkProcess.execute("explorer.exe {}", guiModel.getSelectedPath().getCsvPath().getParent()));
        getChildren().add(createHBox("", btnOpenExplorer));

        AtomicReference<Pane> prevFilterPane = new AtomicReference<>();
        guiModel.selectedPathOnChange(obsCsv -> {
            getChildren().remove(prevFilterPane.get());
            if(obsCsv != null) {
                Pane filterPane = createFilterPane(obsCsv);
                prevFilterPane.set(filterPane);
                getChildren().add(filterPane);
            }
        });
    }

    private Pane createFilterPane(ObsCsv obsCsv) {
        filterObj.reset(obsCsv.getHeader());
        List<TextField> tflist = new ArrayList<>();
        GridPaneBuilder gpBuilder = new GridPaneBuilder();
        for (int nrow = 0; nrow < obsCsv.getHeader().size(); nrow++) {
            CheckBox cbEnable = new CheckBox();
            TextField tf = new TextField();
            tflist.add(tf);
            String fname = obsCsv.getHeader().get(nrow);
            StringBinding filterStrBinding = Bindings.createStringBinding(() -> tf.isDisable() ? "" : tf.getText(), tf.textProperty(), tf.disableProperty());
            filterObj.bindValue(fname, filterStrBinding);
//            cbEnable.selectedProperty().addListener((obs,o,n) -> {
//                if(n) {
//                    tf.setDisable(false);
//                } else {
//                    tf.setDisable(true);
//                    filterObj.unbindValue(fname);
//
//                }
//            });
            tf.disableProperty().bind(Bindings.createBooleanBinding(() -> !cbEnable.isSelected(), cbEnable.selectedProperty()));
            Button btnShowHide = new Button("H");
            btnShowHide.setOnAction(e -> {
                btnShowHide.setText(btnShowHide.getText().equals("H") ? "S" : "H");
            });

            cbEnable.setSelected(true);
            gpBuilder.addNode(nrow, 0, cbEnable);
            gpBuilder.addLabel(nrow, 1, fname);
            gpBuilder.addNode(nrow, 2, tf);
//            filterObj.bindValue(obsCsv.getHeader().get(nrow), tf.textProperty());
        }
        Button btnClear = new Button("CLEAR");
        btnClear.setOnAction(e -> filter(tflist, tf -> !tf.isDisable()).forEach(tf -> tf.setText("")));
        HBox boxButtons = createHBox("boxClear", btnClear);
        return createVBox("filters", gpBuilder.createGridPane(), boxButtons);
    }


}
