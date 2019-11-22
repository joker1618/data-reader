package xxx.joker.apps.reporeader.jfx.model.beans;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.format.JkCsv;
import xxx.joker.libs.core.lambda.JkStreams;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static xxx.joker.libs.core.lambda.JkStreams.count;
import static xxx.joker.libs.core.lambda.JkStreams.map;

public class ObsCsv {

    private static final Logger LOG = LoggerFactory.getLogger(ObsCsv.class);

    private final Path csvPath;
    private final List<String> header = new ArrayList<>();
    private final SimpleListProperty<ObsItem> dataList = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleBooleanProperty changedProperty = new SimpleBooleanProperty(false);

    public ObsCsv(JkCsv csv) {
        this.csvPath = csv.getCsvPath();
        this.header.addAll(csv.getHeader());

        List<ObsItem> list = map(csv.getCurrentData(true), row -> new ObsItem(csv.getHeader(), map(row, ObsField::new)));
        this.dataList.setAll(list);
        dataList.forEach(oi -> oi.addListener((obs,o,n) -> changedProperty.set(isChanged())));
        changedProperty.addListener((obs,o,n) -> LOG.debug("diocane {}", n));
    }

    public void rollback() {
        dataList.forEach(ObsItem::rollback);
    }
    public void commit() {
        dataList.forEach(ObsItem::commit);
    }

    public List<String> getHeader() {
        return header;
    }

    public Path getCsvPath() {
        return csvPath;
    }

    public SimpleListProperty<ObsItem> getDataList() {
        return dataList;
    }

    public String toStrCsv() {
        JkCsv csv = new JkCsv(header, map(dataList, ObsItem::strFields));
        return JkStreams.joinLines(csv.strLines());
    }

    public boolean isChanged() {
        return count(getDataList(), ObsItem::isChanged) > 0;
    }

    public ObservableBooleanValue changedProperty() {
        return changedProperty;
    }
    public void onChange(Consumer<ObsCsv> consumer) {
        changedProperty.addListener((obs,o,n) -> consumer.accept(this));
    }

}
