package xxx.joker.apps.reporeader.jfx.model;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsObject;
import xxx.joker.apps.reporeader.jfx.model.dl.FileDao;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

import static xxx.joker.libs.core.lambda.JkStreams.filter;

@Service
public class GuiModelImpl implements GuiModel {

    private static final Logger LOG = LoggerFactory.getLogger(GuiModelImpl.class);

    private final SimpleListProperty<Path> csvPaths = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleObjectProperty<ObsCsv> selCsv = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ObsObject> selTableItem = new SimpleObjectProperty<>();

    private final ObservableMap<Path, ObsCsv> cacheData = FXCollections.observableHashMap();
    private final ObservableSet<ObsObject> changedObjs = FXCollections.observableSet(new LinkedHashSet<>());

    @Autowired
    private FileDao dao;

    public GuiModelImpl() {
        initBindings();
    }

    private void initBindings() {
        cacheData.addListener((MapChangeListener<Path, ObsCsv>) chmap -> {
            if(chmap.wasAdded()) {
                List<ObsObject> ooList = chmap.getValueAdded().getDataList();
                ooList.forEach(oo -> oo.addListener((obs, o, n) -> {
                    if(n)   changedObjs.add(oo);
                    else    changedObjs.remove(oo);
                }));
                changedObjs.addAll(filter(ooList, ObsObject::isChanged));
            } else if(chmap.wasRemoved()) {
                changedObjs.addAll(chmap.getValueRemoved().getDataList());
            }
        });
    }

    @Override
    public ObservableList<Path> getCsvPaths() {
        return csvPaths.get();
    }
    @Override
    public SimpleListProperty<Path> csvPathsProperty() {
        return csvPaths;
    }
    @Override
    public void setCsvPaths(List<Path> csvPaths) {
        this.csvPaths.setAll(csvPaths);
    }

    @Override
    public ObsCsv getSelCsv() {
        return selCsv.get();
    }
    @Override
    public SimpleObjectProperty<ObsCsv> selCsvProperty() {
        return selCsv;
    }
    @Override
    public void setSelCsv(Path path) {
        if(path == null) {
            selCsv.set(null);
        } else {
            this.cacheData.putIfAbsent(path, dao.readCsvFile(path));
            this.selCsv.set(cacheData.get(path));
        }
    }

    @Override
    public ObsObject getSelTableItem() {
        return selTableItem.get();
    }
    @Override
    public SimpleObjectProperty<ObsObject> selTableItemProperty() {
        return selTableItem;
    }
    @Override
    public void setSelTableItem(ObsObject selTableItem) {
        this.selTableItem.set(selTableItem);
    }

    @Override
    public ObservableMap<Path, ObsCsv> getCacheData() {
        return cacheData;
    }

    @Override
    public ObservableSet<ObsObject> getChangedObsObjects() {
        return changedObjs;
    }

    @Override
    public boolean rollback() {
        if(changedObjs.isEmpty()) {
            return false;
        } else {
            cacheData.values().forEach(ObsCsv::rollback);
            changedObjs.clear();
            return true;
        }
    }

    @Override
    public boolean commit() {
        if(changedObjs.isEmpty()) {
            return false;
        } else {
            cacheData.values().forEach(ObsCsv::commit);
            changedObjs.clear();
            dao.persistCsvFiles(cacheData);
            return true;
        }
    }

}

