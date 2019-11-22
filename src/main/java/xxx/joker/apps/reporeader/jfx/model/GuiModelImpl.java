package xxx.joker.apps.reporeader.jfx.model;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsItem;
import xxx.joker.apps.reporeader.jfx.model.dl.FileDao;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

import static xxx.joker.libs.core.lambda.JkStreams.filter;

@Repository
class GuiModelImpl implements GuiModel {

    private static final Logger LOG = LoggerFactory.getLogger(GuiModelImpl.class);

    private final SimpleListProperty<Path> csvPaths = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleObjectProperty<ObsCsv> selectedPath = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ObsItem> selectedTableItem = new SimpleObjectProperty<>();

    private final ObservableMap<Path, ObsCsv> obsCsvMap = FXCollections.observableHashMap();
    private final ObservableSet<ObsItem> changedObjs = FXCollections.observableSet(new LinkedHashSet<>());

    private final FileDao dao;

    public GuiModelImpl() {
        LOG.debug("Creating bean 'guiModel'");
        this.dao = FileDao.createDao();
        initBindings();
    }

    private void initBindings() {
        obsCsvMap.addListener((MapChangeListener<Path, ObsCsv>) chmap -> {
            if(chmap.wasAdded()) {
                SimpleListProperty<ObsItem> ooList = chmap.getValueAdded().getDataList();
                Consumer<ObsItem> ooCons = oo -> oo.addListener((obs, o, n) -> {
                    if(n)   changedObjs.add(oo);
                    else    changedObjs.remove(oo);
                });
                ooList.addListener((ListChangeListener<ObsItem>) ch -> {
                    if(ch.wasAdded()) {
                        ch.getAddedSubList().forEach(ooCons::accept);
                    } else if(ch.wasRemoved()) {
                        ch.getRemoved().forEach(changedObjs::add);
                    }
                });
                ooList.forEach(ooCons::accept);
                changedObjs.addAll(filter(ooList, ObsItem::isChanged));
            } else if(chmap.wasRemoved()) {
                changedObjs.addAll(chmap.getValueRemoved().getDataList());
            }
        });
    }

    @Override
    public void csvPathsOnChange(Consumer<Collection<Path>> onChange) {
        csvPaths.addListener((ListChangeListener<Path>)lch -> onChange.accept(csvPaths.get()));
    }
    @Override
    public void csvPathsSet(Collection<Path> csvPaths) {
        this.csvPaths.setAll(csvPaths);
        csvPaths.forEach(p -> this.obsCsvMap.putIfAbsent(p, dao.readCsvFile(p)));
        selectedPath.set(null);
    }

//    @Override
//    public ObservableList<Path> getCsvPaths() {
//        return csvPaths.get();
//    }
//    @Override
//    public SimpleListProperty<Path> csvPathsProperty() {
//        return csvPaths;
//    }
//    @Override
//    public void setCsvPaths(List<Path> csvPaths) {
//        this.csvPaths.setAll(csvPaths);
//    }

//    @Override
//    public ObsCsv getSelectedPath() {
//        return selectedPath.get();
//    }
//    @Override
//    public SimpleObjectProperty<ObsCsv> selectedPathProperty() {
//        return selectedPath;
//    }
//    @Override
//    public void setSelectedPath(Path path) {
//        if(path == null) {
//            selectedPath.set(null);
//        } else {
//            this.cacheObsCsv.putIfAbsent(path, dao.readCsvFile(path));
//            selectedPath.set(cacheObsCsv.get(path));
//        }
//    }

    @Override
    public void selectedPathSet(Path path) {
        selectedPath.set(path == null ? null : obsCsvMap.get(path));
    }

    @Override
    public void selectedPathOnChange(Consumer<ObsCsv> onChange) {
        selectedPath.addListener((obs, o, n) -> onChange.accept(n));
    }

//    @Override
//    public ObsObject getSelectedTableItem() {
//        return selectedTableItem.get();
//    }
    @Override
    public void selectedTableItemOnChange(Consumer<ObsItem> onChange) {
        selectedTableItem.addListener((obs,o,n) -> onChange.accept(n));
    }
    @Override
    public void selectedTableItemSet(ObsItem selectedTableItem) {
        this.selectedTableItem.set(selectedTableItem);
    }

    @Override
    public ObservableMap<Path, ObsCsv> getObsCsvMap() {
        return obsCsvMap;
    }

    @Override
    public ObservableSet<ObsItem> getChangedObsObjects() {
        return changedObjs;
    }

    @Override
    public boolean rollback() {
        if(changedObjs.isEmpty()) {
            return false;
        } else {
            obsCsvMap.values().forEach(ObsCsv::rollback);
            changedObjs.clear();
            return true;
        }
    }

    @Override
    public boolean commit() {
        if(changedObjs.isEmpty()) {
            return false;
        } else {
            obsCsvMap.values().forEach(ObsCsv::commit);
            changedObjs.clear();
            dao.persistCsvFiles(obsCsvMap);
            return true;
        }
    }

}

