package xxx.joker.apps.datareader.jfx.view.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xxx.joker.apps.datareader.jfx.model.GuiModel;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.datareader.jfx.model.beans.ObsField;
import xxx.joker.apps.datareader.jfx.model.beans.ObsItem;
import xxx.joker.apps.datareader.jfx.view.beans.FilterObj;
import xxx.joker.apps.datareader.jfx.view.controls.JfxTable;
import xxx.joker.apps.datareader.jfx.view.controls.JfxTableCol;
import xxx.joker.libs.core.cache.JkCache;
import xxx.joker.libs.core.javafx.JfxControls;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;

import static xxx.joker.libs.core.javafx.JfxControls.createHBox;
import static xxx.joker.libs.core.javafx.JfxControls.createVBox;

@Component
public class PaneCenter extends BorderPane {

    private static final Logger LOG = LoggerFactory.getLogger(PaneCenter.class);

    @Autowired
    private GuiModel guiModel;
    @Autowired
    private FilterObj filterObj;

    private final Label lblFileName = new Label();
    private Pane dataPane;
    private final JkCache<Path, List<ObsItem>> cacheData = new JkCache<>();


    @PostConstruct
    public void init() {
        Pane headerPane = createHBox("caption", lblFileName);
        setTop(headerPane);

        dataPane = createHBox("data-view");
        setCenter(dataPane);

        initBindings();

        getStyleClass().add("rootCenter");
    }

    private void initBindings() {
        guiModel.selectedPathOnChange(n -> {
            lblFileName.setText(n == null || n.getCsvPath() == null ? "" : "FILE:  " + n.getCsvPath().toString());
            dataPane.getChildren().clear();
            if(n != null) {
                JfxTable<ObsItem> table = createTable(n);
                dataPane.getChildren().setAll(table);
                table.prefWidthProperty().bind(dataPane.widthProperty());
            }
        });
    }

    private JfxTable<ObsItem> createTable(ObsCsv csv) {
        JfxTable<ObsItem> table = new JfxTable<>();

        for (int i = 0; i < csv.getHeader().size(); i++) {
            String hcol = csv.getHeader().get(i);
            int idx = i;
            JfxTableCol<ObsItem, ObsField> col = JfxTableCol.createCol(hcol, o -> o.getObsFields().get(idx), ObsField::getCurrentValue);
            table.addColumn(col);
        }

        JfxTableCol<ObsItem, Boolean> col = JfxTableCol.createCol("*", ObsItem::getValue, b -> b ? "*" : "", "centered");
        table.addColumn(0, col);

        JfxTableCol<ObsItem, Boolean> colDel = JfxTableCol.createCol("", ObsItem::getValue, "centered");
        table.addColumn(0, colDel);
        Image delImg = new Image(getClass().getResource("/icon/deleteRed.png").toExternalForm());
        colDel.setCellFactory(param -> new TableCell<ObsItem, Boolean>() {
            final Button btn = new Button();
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    btn.setGraphic(JfxControls.createImageView(delImg, 20, 20));
                    btn.getStyleClass().add("btnDelete");
                    setGraphic(btn);
                    btn.setOnAction(e -> {
                        ObsItem itemToDel = getTableView().getItems().get(getTableRow().getIndex());
                        csv.getDataList().remove(itemToDel);
                        LOG.debug("deleted item {}", itemToDel);
                    });
                    setText(null);
                }
            }
        });

        ObservableList<ObsItem> items = csv.getDataList();
        items.forEach(oo -> oo.addListener((obs,o,n) -> table.refresh()));

        FilteredList<ObsItem> filteredList = new FilteredList<>(items);
        filteredList.predicateProperty().bind(filterObj);

        SortedList<ObsItem> tableItems = new SortedList<>(filteredList);
        tableItems.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(tableItems);

        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> guiModel.selectedTableItemSet(n));

        return table;
    }

}
