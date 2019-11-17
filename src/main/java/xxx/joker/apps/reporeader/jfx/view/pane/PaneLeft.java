package xxx.joker.apps.reporeader.jfx.view.pane;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xxx.joker.apps.reporeader.jfx.model.GenServ;
import xxx.joker.apps.reporeader.jfx.model.GuiModel;
import xxx.joker.apps.reporeader.jfx.model.ServT;
import xxx.joker.libs.core.util.JkConsole;

import java.nio.file.Path;
import java.util.Arrays;

import static xxx.joker.libs.core.javafx.JfxControls.createHBox;
import static xxx.joker.libs.core.util.JkConsole.display;

@Component
public class PaneLeft extends VBox {

    private GuiModel guiModel = GuiModel.getModel();

    @Autowired
    private ServT servT;
    @Autowired
    private GenServ genServ;

//    private final GuiModel guiModel = GuiModel.getModel();
    private final ListView<Path> lvPaths = new ListView<>();

    public PaneLeft() {

        // Config list view
        lvPaths.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        lvPaths.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> {
            if(n != null)   guiModel.setSelCsv(n);
            guiModel.setSelTableItem(null);
        });
        lvPaths.setCellFactory(param -> new ListCell<Path>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if(item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getFileName().toString());
//                    setTextFill(Color.RED);
//                    guiModel.getChangedObsObjects().addListener((SetChangeListener<ObsObject>)ch -> {
//                        ObsObject oo = ch.wasAdded() ? ch.getElementAdded() : ch.getElementRemoved();
//                        JkFiles.areEquals(oo.ge)
//                    });
                }
            }
        });
        guiModel.csvPathsProperty().addListener((InvalidationListener) lch -> lvPaths.getItems().setAll(guiModel.getCsvPaths()));

        // Buttons
        Button btnCommit = new Button("COMMIT");
        Button btnRollback = new Button("ROLLBACK");
        HBox boxButtons = createHBox("buttons", btnCommit, btnRollback);

        btnCommit.setOnAction(e -> guiModel.commit());
        btnRollback.setOnAction(e -> guiModel.rollback());

        Arrays.asList(btnCommit, btnRollback).forEach(btn -> btn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> guiModel.getChangedObsObjects().isEmpty(), guiModel.getChangedObsObjects()
        )));

        getChildren().add(boxButtons);
        getChildren().add(lvPaths);
//        final ObservableList<ObsObject> objList = FXCollections.observableArrayList();
////        BooleanBinding disabled = Bindings.createBooleanBinding(() -> JkStreams.count(objList, ObsObject::isChanged) == 0, objList);
//        BooleanBinding disabled = Bindings.createBooleanBinding(() -> {
//            int count = JkStreams.count(objList, ObsObject::isChanged);
//            display("from disabled binding {}", count);
//            return count == 0;
//        }, objList);
//        Arrays.asList(btnCommit, btnRollback).forEach(btn -> btn.disableProperty().bind(disabled));
//        guiModel.getCacheData().addListener((MapChangeListener<Path,ObsCsv>)chmap -> {
//            objList.setAll(flatMap(guiModel.getCacheData().values(), ObsCsv::getDataList));
//        });

        Button btnTmp = new Button("__TMP__");
        btnTmp.setOnAction(e -> doWork("from button tmp"));
        getChildren().add(createHBox("buttons temp", btnTmp));

        getStyleClass().add("rootLeft");

    }

    public void doWork(String prefixString) {
        display(prefixString);
        servT.doSome();
        genServ.doSomeWork();
    }

}
