package xxx.joker.apps.datareader.jfx.model;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;
import xxx.joker.apps.datareader.jfx.model.beans.ObsItem;
import xxx.joker.apps.datareader.jfx.model.dl.FileDao;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static xxx.joker.libs.core.lambda.JkStreams.*;

@Repository
class GuiModelImpl implements GuiModel {

    private static final Logger LOG = LoggerFactory.getLogger(GuiModelImpl.class);

    private final SimpleListProperty<Path> csvPaths = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleObjectProperty<ObsCsv> selectedPath = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ObsItem> selectedTableItem = new SimpleObjectProperty<>();

    private final ObservableMap<Path, ObsCsv> obsCsvMap = FXCollections.observableHashMap();
    private final ObservableMap<ObsItem, ObsCsv> changedItemMap = FXCollections.observableHashMap();

    private final FileDao dao;

    public GuiModelImpl() {
        LOG.debug("Creating bean 'guiModel'");
        this.dao = FileDao.createDao();
        initBindings();
    }

    private void initBindings() {
        obsCsvMap.addListener((MapChangeListener<Path, ObsCsv>) chmap -> {
            if(chmap.wasAdded()) {
                SimpleListProperty<ObsItem> oiList = chmap.getValueAdded().getDataList();
                Consumer<ObsItem> oiCons = oi -> oi.addListener((obs, o, n) -> {
                    if(n)   changedItemMap.put(oi, chmap.getValueAdded());
                    else    changedItemMap.remove(oi);
                });
                oiList.addListener((ListChangeListener<ObsItem>) ch -> {
                    if(ch.next()) {
                        if(ch.getAddedSize() != ch.getRemovedSize() && (ch.getAddedSize() == 0 || ch.getRemovedSize() == 0)) {
                            ch.getAddedSubList().forEach(oiCons::accept);
                            ch.getRemoved().forEach(oi -> changedItemMap.put(oi, chmap.getValueRemoved()));
                        }
                    }
                });
                oiList.forEach(oiCons::accept);
                filter(oiList, ObsItem::isChanged).forEach(oi -> changedItemMap.put(oi, chmap.getValueAdded()));
            } else if(chmap.wasRemoved()) {
//                if(chmap.getValueRemoved() != chmap.getValueRemoved() && (chmap.getAddedSize() == 0 || chmap.getRemovedSize() == 0)) {
                filter(chmap.getValueRemoved().getDataList(), ObsItem::isChanged).forEach(oi -> changedItemMap.put(oi, chmap.getValueRemoved()));
                List<ObsItem> unchanged = filter(changedItemMap.keySet(), o -> !o.isChanged());
                unchanged.forEach(changedItemMap::remove);
//                }
                
            }
            obsCsvMap.values().forEach(ObsCsv::checkIfChanged);


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

    @Override
    public void selectedPathSet(Path path) {
        selectedPath.set(path == null ? null : obsCsvMap.get(path));
    }

    @Override
    public void selectedPathOnChange(Consumer<ObsCsv> onChange) {
        selectedPath.addListener((obs, o, n) -> onChange.accept(n));
    }

    @Override
    public ObsCsv getSelectedPath() {
        return selectedPath.get();
    }

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
    public ObservableMap<ObsItem, ObsCsv> getChangedItemMap() {
        return changedItemMap;
    }

    @Override
    public List<ObsCsv> getChangedObsCsv() {
        return distinct(changedItemMap.values());
    }

    @Override
    public boolean rollback() {
        if(changedItemMap.isEmpty()) {
            return false;
        } else {
            obsCsvMap.clear();
            csvPaths.forEach(p -> obsCsvMap.put(p, dao.readCsvFile(p)));
//            obsCsvMap.values().forEach(ObsCsv::rollback);
            changedItemMap.clear();
            return true;
        }
    }

    @Override
    public boolean commit() {
        if(changedItemMap.isEmpty()) {
            return false;
        } else {
            obsCsvMap.values().forEach(ObsCsv::commit);
            changedItemMap.clear();
            dao.persistCsvFiles(obsCsvMap);
            return true;
        }
    }

}

