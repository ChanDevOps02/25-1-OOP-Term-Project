package filemanager.model;

public class SearchResult {
    private final String path;
    private final String size;
    private final String modified;

    public SearchResult(String path, String size, String modified) {
        this.path = path;
        this.size = size;
        this.modified = modified;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }

    public String getModified() {
        return modified;
    }
}
