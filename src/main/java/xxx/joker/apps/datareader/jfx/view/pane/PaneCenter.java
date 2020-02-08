package xxx.joker.apps.datareader.jfx.view.pane;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
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
import xxx.joker.libs.core.cache.JkCache;
import xxx.joker.libs.javafx.tableview.JfxTable;
import xxx.joker.libs.javafx.tableview.JfxTableCol;
import xxx.joker.libs.javafx.util.JfxControls;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static xxx.joker.libs.core.util.JkStrings.strf;
import static xxx.joker.libs.javafx.util.JfxControls.createHBox;

@Component
public class PaneCenter extends BorderPane {

    private static final Logger LOG = LoggerFactory.getLogger(PaneCenter.class);

    @Autowired
    private GuiModel guiModel;
    @Autowired
    private FilterObj filterObj;

    private final Label lblFileName = new Label();
    private final Label lblItemSize = new Label();
    private Pane dataPane;
    private AtomicReference<JfxTable> dataTable = new AtomicReference<>();
    private final JkCache<Path, List<ObsItem>> cacheData = new JkCache<>();


    @PostConstruct
    public void init() {
        Pane headerPane = createHBox("caption", lblFileName, lblItemSize);
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
            dataTable.set(null);
            lblItemSize.setText("");
            if(n != null) {
                JfxTable<ObsItem> table = createTable(n);
                dataTable.set(table);
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

        JfxTableCol<ObsItem, Boolean> colLineNum = JfxTableCol.createCol("*", ObsItem::getValue, b -> b ? "*" : "", "centered");
        table.addColumn(0, colLineNum);
        colLineNum.setCellFactory(param -> new TableCell<ObsItem, Boolean>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : getIndex()+"");
            }
        });

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
        Consumer<FilteredList<ObsItem>> setterLblItemSize = list -> lblItemSize.setText(strf("({})", list.size()));
        setterLblItemSize.accept(filteredList);
        filteredList.addListener((InvalidationListener)l -> setterLblItemSize.accept(filteredList));

        SortedList<ObsItem> tableItems = new SortedList<>(filteredList);
        tableItems.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(tableItems);

        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> guiModel.selectedTableItemSet(n));

        return table;
    }

    public void setColumnVisible(String header, boolean visible) {
        if(dataTable.get() != null) {
            ObservableList<TableColumn> columns = dataTable.get().getColumns();
            List<TableColumn> res = columns.stream().filter(col -> header.equals(col.getText())).collect(Collectors.toList());
            res.forEach(tc -> tc.setVisible(visible));
        }
    }
}
