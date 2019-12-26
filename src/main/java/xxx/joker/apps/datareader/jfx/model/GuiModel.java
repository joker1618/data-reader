package xxx.joker.apps.datareader.jfx.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.datareader.jfx.model.beans.ObsItem;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface GuiModel {

    void csvPathsSet(Collection<Path> paths);
    void csvPathsOnChange(Consumer<Collection<Path>> onChange);

    void selectedPathSet(Path path);
    void selectedPathOnChange(Consumer<ObsCsv> onChange);
    SimpleObjectProperty<ObsCsv> selectedPathProperty();
    ObsCsv getSelectedPath();

    void selectedTableItemSet(ObsItem selectedTableItem);
    void selectedTableItemOnChange(Consumer<ObsItem> onChange);

    ObservableMap<Path, ObsCsv> getObsCsvMap();
    ObservableMap<ObsItem, ObsCsv> getChangedItemMap();
    List<ObsCsv> getChangedObsCsv();

    boolean rollback();
    boolean commit();
}

