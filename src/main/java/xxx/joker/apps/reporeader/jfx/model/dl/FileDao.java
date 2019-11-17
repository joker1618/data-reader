package xxx.joker.apps.reporeader.jfx.model.dl;

import javafx.collections.ObservableMap;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsCsv;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface FileDao {

    static FileDao getDao() {
        return SimpleFileDao.getInstance();
    }

    ObsCsv load(Path path);
    List<ObsCsv> load(Collection<Path> paths);

    void persist(ObservableMap<Path, ObsCsv> csvData);

//    List<ObsCsv> load();



}
