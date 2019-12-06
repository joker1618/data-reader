package xxx.joker.apps.datareader.jfx.model.dl;

import javafx.collections.ObservableMap;
import xxx.joker.apps.datareader.config.AppCtx;
import xxx.joker.apps.datareader.jfx.model.beans.ObsCsv;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.csv.JkCsv;
import xxx.joker.libs.core.lambda.JkStreams;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static xxx.joker.libs.core.lambda.JkStreams.filter;
import static xxx.joker.libs.core.lambda.JkStreams.map;

class SimpleFileDao implements FileDao {

    public SimpleFileDao() {

    }

    @Override
    public ObsCsv readCsvFile(Path path) {
        return new ObsCsv(JkCsv.readFile(path));
    }

    @Override
    public List<ObsCsv> readCsvFile(Collection<Path> paths) {
        return map(paths, this::readCsvFile);
    }

    @Override
    public void persistCsvFiles(ObservableMap<Path, ObsCsv> csvData) {
        List<ObsCsv> changedList = filter(csvData.values(), ObsCsv::isChanged);
        // Backup original files
        changedList.forEach(p -> JkFiles.copy(p.getCsvPath(), getFileBackupPath(p.getCsvPath())));
        // Create new files
        changedList.forEach(o -> JkFiles.writeFile(o.getCsvPath(), o.toStrCsv()));
        // Remove backup folder
        JkStreams.mapUniq(changedList, ch -> getFileBackupFolder(ch.getCsvPath())).forEach(JkFiles::delete);
    }


    private Path getFileBackupFolder(Path filePath) {
        return JkFiles.getParent(filePath).resolve(".data-reader-bkp");
    }
    private Path getFileBackupPath(Path filePath) {
        return getFileBackupFolder(filePath).resolve(filePath.getFileName());
    }
}
