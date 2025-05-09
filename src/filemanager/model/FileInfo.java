package filemanager.model;

public class FileInfo {
    private final String oldPath;
    private final String newPath;

    public FileInfo(String oldPath, String newPath) {
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    public String getOldPath() {
        return oldPath;
    }

    public String getNewPath() {
        return newPath;
    }
}
