package xxx.joker.apps.reporeader.jfx.model.dl;

import javafx.collections.ObservableMap;
import xxx.joker.apps.reporeader.common.AppCtx;
import xxx.joker.apps.reporeader.jfx.model.beans.ObsCsv;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.JkCsv;
import xxx.joker.libs.core.lambda.JkStreams;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static xxx.joker.libs.core.lambda.JkStreams.map;

public class SimpleFileDao implements FileDao {

    private static final SimpleFileDao instance = new SimpleFileDao();

    private SimpleFileDao() {

    }

    public static FileDao getInstance() {
        return instance;
    }

    @Override
    public ObsCsv load(Path path) {
        return new ObsCsv(JkCsv.readFile(path));
    }

    @Override
    public List<ObsCsv> load(Collection<Path> paths) {
        return map(paths, this::load);
    }

    @Override
    public void persist(ObservableMap<Path, ObsCsv> csvData) {
        // Backup original files
        csvData.keySet().forEach(p -> JkFiles.copy(p, AppCtx.getFileBackupPath(p)));
        // Create new files
        csvData.forEach((path,obsCsv) -> JkFiles.writeFile(path, obsCsv.toStrCsv()));
        // Remove backup folder
        JkStreams.map(csvData.keySet(), AppCtx::getFileBackupFolder).forEach(JkFiles::delete);

    }


}
