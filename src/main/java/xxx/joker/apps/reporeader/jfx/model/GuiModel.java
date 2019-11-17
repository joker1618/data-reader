package xxx.joker.apps.reporeader.jfx.model;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsObject;

import java.nio.file.Path;
import java.util.List;

public interface GuiModel {

    ObservableList<Path> getCsvPaths();
    SimpleListProperty<Path> csvPathsProperty();
    void setCsvPaths(List<Path> csvPaths);

    ObsCsv getSelCsv();
    SimpleObjectProperty<ObsCsv> selCsvProperty();
    void setSelCsv(Path path);

    ObsObject getSelTableItem();
    SimpleObjectProperty<ObsObject> selTableItemProperty();
    void setSelTableItem(ObsObject selTableItem);

    ObservableMap<Path, ObsCsv> getCacheData();
    ObservableSet<ObsObject> getChangedObsObjects();

    boolean rollback();
    boolean commit();
}

