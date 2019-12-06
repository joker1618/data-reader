package xxx.joker.apps.datareader.jfx.model.dl;

import javafx.collections.ObservableMap;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface FileDao {

    ObsCsv readCsvFile(Path path);
    List<ObsCsv> readCsvFile(Collection<Path> paths);

    void persistCsvFiles(ObservableMap<Path, ObsCsv> csvData);

    static FileDao createDao() {
        return new SimpleFileDao();
    }

}
