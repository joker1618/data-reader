package xxx.joker.apps.datareader.jfx.view.pane;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xxx.joker.apps.datareader.jfx.model.GuiModel;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.datareader.jfx.view.beans.FilterObj;
import xxx.joker.libs.core.adapter.JkProcess;
import xxx.joker.libs.javafx.builder.JfxGridPaneBuilder;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static xxx.joker.libs.core.lambda.JkStreams.filter;
import static xxx.joker.libs.core.util.JkConvert.toList;
import static xxx.joker.libs.core.util.JkStrings.strf;
import static xxx.joker.libs.javafx.util.JfxControls.createHBox;
import static xxx.joker.libs.javafx.util.JfxControls.createVBox;

@Component
public class PaneLeft extends VBox {

    private static final Logger LOG = LoggerFactory.getLogger(PaneLeft.class);

    @Autowired
    private GuiModel guiModel;
    @Autowired
    private FilterObj filterObj;

    private final ListView<Path> lvPaths = new ListView<>();
    private BiConsumer<String, Boolean> consumerColumnVisible;

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
                    ObsCsv obsCsv = guiModel.getObsCsvMap().get(item);
                    Runnable runSetText = () -> setText(strf("{} ({})", item.getFileName().toString(), obsCsv.getDataList().size()));
                    runSetText.run();
                    obsCsv.changedProperty().addListener((obs,o,n) -> {
                        setTextFill(n ? Color.RED : Color.BLACK);
                        runSetText.run();
                    });
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

        Button btnExpand = new Button(">");
        Button btnCollapse = new Button("<");
        SimpleObjectProperty<List<Node>> childrenList = new SimpleObjectProperty<>();
        VBox vboxExpand = createVBox("centered", btnExpand);
        btnCollapse.setOnAction(e -> {
            if(childrenList.get() == null)  childrenList.set(toList(getChildren()));
            getStyleClass().add("pad0");
            getChildren().setAll(vboxExpand);
        });
        btnExpand.setOnAction(e -> {
            getStyleClass().remove("pad0");
            getChildren().setAll(childrenList.get());
        });

        BorderPane bpButtons = new BorderPane();
        bpButtons.setCenter(boxButtons);
        bpButtons.setRight(createVBox("top-center", btnCollapse));

        getChildren().add(bpButtons);
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
        JfxGridPaneBuilder gpBuilder = new JfxGridPaneBuilder();
        for (int nrow = 0; nrow < obsCsv.getHeader().size(); nrow++) {
            CheckBox cbEnable = new CheckBox();
            TextField tf = new TextField();
            tflist.add(tf);
            String fname = obsCsv.getHeader().get(nrow);
            StringBinding filterStrBinding = Bindings.createStringBinding(() -> tf.isDisable() ? "" : tf.getText(), tf.textProperty(), tf.disableProperty());
            filterObj.bindValue(fname, filterStrBinding);
            tf.disableProperty().bind(Bindings.createBooleanBinding(() -> !cbEnable.isSelected(), cbEnable.selectedProperty()));
            Button btnShowHide = new Button("H");
            btnShowHide.setOnAction(e -> {
                boolean isHide = btnShowHide.getText().equals("H");
                btnShowHide.setText(isHide ? "S" : "H");
                consumerColumnVisible.accept(fname, !isHide);
            });

            cbEnable.setSelected(true);
            gpBuilder.add(nrow, 0, cbEnable);
            gpBuilder.add(nrow, 1, fname);
            gpBuilder.add(nrow, 2, tf);
            gpBuilder.add(nrow, 3, btnShowHide);
        }
        Button btnClear = new Button("CLEAR");
        btnClear.setOnAction(e -> filter(tflist, tf -> !tf.isDisable()).forEach(tf -> tf.setText("")));
        HBox boxButtons = createHBox("boxClear", btnClear);
        return createVBox("filters", gpBuilder.createGridPane(), boxButtons);
    }

    public void setConsumerColumnVisible(BiConsumer<String, Boolean> consumer) {
        this.consumerColumnVisible = consumer;
    }


}
