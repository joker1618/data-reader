package xxx.joker.apps.reporeader.common;

import xxx.joker.libs.core.file.JkFiles;

import java.nio.file.Path;

public class AppCtx {

    private final Path folder;

    public AppCtx(Path folder) {
        this.folder = folder;
    }

    public Path getFolder() {
        return folder;
    }

    public static final String BACKUP_DAO_FOLDER_NAME = ".repo-reader-generic-bkp";
    public static Path getFileBackupFolder(Path filePath) {
        return JkFiles.getParent(filePath).resolve(BACKUP_DAO_FOLDER_NAME);
    }
    public static Path getFileBackupPath(Path filePath) {
        return getFileBackupFolder(filePath).resolve(filePath.getFileName());
    }


}
