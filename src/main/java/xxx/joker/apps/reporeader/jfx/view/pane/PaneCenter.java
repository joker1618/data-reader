package xxx.joker.apps.reporeader.jfx.view.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xxx.joker.apps.reporeader.jfx.model.GuiModel;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsObjField;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsObject;
import xxx.joker.apps.reporeader.jfx.view.controls.JfxTable;
import xxx.joker.apps.reporeader.jfx.view.controls.JfxTableCol;
import xxx.joker.libs.core.cache.JkCache;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;

import static xxx.joker.libs.core.javafx.JfxControls.createHBox;

@Component
public class PaneCenter extends BorderPane {

    @Autowired
    private GuiModel guiModel;

    private final Label lblFileName = new Label();
    private Pane dataPane;
    private final JkCache<Path, List<ObsObject>> cacheData = new JkCache<>();


    @PostConstruct
    public void init() {
    // public PaneCenter() {
        Pane headerPane = createHBox("caption", lblFileName);
        setTop(headerPane);

        dataPane = createHBox("data-view");
        setCenter(dataPane);

        initBindings();

        getStyleClass().add("rootCenter");
    }

    private void initBindings() {
        guiModel.selCsvProperty().addListener((obs,o,n) -> {
            lblFileName.setText(n == null || n.getCsvPath() == null ? "" : "File:  " + n.getCsvPath().getFileName().toString());
            dataPane.getChildren().clear();
            if(n != null) {
                JfxTable<ObsObject> table = createTable(n);
                dataPane.getChildren().setAll(table);
                table.prefWidthProperty().bind(dataPane.widthProperty());
            }
        });
    }

    private JfxTable<ObsObject> createTable(ObsCsv csv) {
        JfxTable<ObsObject> table = new JfxTable<>();

        for (int i = 0; i < csv.getHeader().size(); i++) {
            String hcol = csv.getHeader().get(i);
            int idx = i;
            JfxTableCol<ObsObject, ObsObjField> col = JfxTableCol.createCol(hcol, o -> o.getObsFields().get(idx), ObsObjField::getCurrentValue);
            table.addColumn(col);
        }

        JfxTableCol<ObsObject, Boolean> col = JfxTableCol.createCol("*", ObsObject::getValue, b -> b ? "*" : "", "centered");
        table.addColumn(0, col);

        ObservableList<ObsObject> items = FXCollections.observableArrayList(csv.getDataList());
        items.forEach(oo -> oo.addListener((obs,o,n) -> table.refresh()));
        table.setItems(items);

        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> guiModel.setSelTableItem(n));

        return table;
    }

}
