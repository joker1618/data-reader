package xxx.joker.apps.reporeader.jfx.model;

import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsItem;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

public interface GuiModel {

    void csvPathsSet(Collection<Path> paths);
    void csvPathsOnChange(Consumer<Collection<Path>> onChange);

//    ObservableList<Path> getCsvPaths();
//    SimpleListProperty<Path> csvPathsProperty();
//    void setCsvPaths(List<Path> csvPaths);

//    ObsCsv getSelectedPath();
//    SimpleObjectProperty<ObsCsv> selectedPathProperty();
//    void setSelectedPath(Path path);
    void selectedPathSet(Path path);
    void selectedPathOnChange(Consumer<ObsCsv> onChange);

//    ObsObject getSelectedTableItem();
//    SimpleObjectProperty<ObsObject> selectedTableItemOnChange();
    void selectedTableItemSet(ObsItem selectedTableItem);
    void selectedTableItemOnChange(Consumer<ObsItem> onChange);



    ObservableMap<Path, ObsCsv> getObsCsvMap();
    ObservableSet<ObsItem> getChangedObsObjects();

    boolean rollback();
    boolean commit();
}

