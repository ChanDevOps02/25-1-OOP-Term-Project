package filemanager.controller;

import filemanager.model.SearchResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdvancedSearchController {

    @FXML private TextField directoryField;
    @FXML private TextField patternField;
    @FXML private TextField minSizeField;
    @FXML private TextField maxSizeField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<SearchResult> resultTable;
    @FXML private TableColumn<SearchResult, String> colPath;
    @FXML private TableColumn<SearchResult, String> colSize;
    @FXML private TableColumn<SearchResult, String> colModified;

    private Window ownerWindow;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        colPath.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPath()));
        colSize.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSize()));
        colModified.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModified()));
        resultTable.getItems().clear();
    }

    public void setOwnerWindow(Window window) {
        this.ownerWindow = window;
    }

    @FXML
    private void onSelectDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("폴더 선택");
        File dir = chooser.showDialog(ownerWindow);
        if (dir != null) {
            directoryField.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void onSearch() {
        resultTable.getItems().clear();

        String path = directoryField.getText();
        if (path == null || path.isEmpty()) {
            alert("폴더를 먼저 선택하세요.");
            return;
        }
        File dir = new File(path);
        if (!dir.isDirectory()) {
            alert("유효하지 않은 폴더입니다.");
            return;
        }

        String keyword = patternField.getText().trim().toLowerCase();
        long minBytes = parseSize(minSizeField.getText());
        long maxBytes = parseSize(maxSizeField.getText());
        Instant from = fromDatePicker.getValue() != null
                ? fromDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()
                : Instant.EPOCH;
        Instant to = toDatePicker.getValue() != null
                ? toDatePicker.getValue().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
                : Instant.now();

        List<SearchResult> results;
        try (Stream<Path> stream = Files.walk(dir.toPath())) {
            results = stream.filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
                            long size = attr.size();
                            Instant modified = attr.lastModifiedTime().toInstant();
                            return new Object[]{p, size, modified};
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(arr -> arr != null)
                    .filter(arr -> {
                        Path p = (Path)arr[0];
                        long size = (long)arr[1];
                        Instant modified = (Instant)arr[2];
                        boolean matchName = keyword.isEmpty() || p.getFileName().toString().toLowerCase().contains(keyword);
                        boolean matchSize = (minBytes < 0 || size >= minBytes) && (maxBytes < 0 || size <= maxBytes);
                        boolean matchDate = !modified.isBefore(from) && !modified.isAfter(to);
                        return matchName && matchSize && matchDate;
                    })
                    .map(arr -> {
                        Path p = (Path)arr[0];
                        long size = (long)arr[1];
                        Instant modified = (Instant)arr[2];
                        String sizeStr = formatSize(size);
                        String modStr = LocalDateTime.ofInstant(modified, ZoneId.systemDefault()).format(dtf);
                        return new SearchResult(p.toString(), sizeStr, modStr);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            alert("검색 중 오류가 발생했습니다.");
            return;
        }

        if (results.isEmpty()) {
            alert("조건에 맞는 파일이 없습니다.");
        } else {
            resultTable.getItems().setAll(results);
        }
    }

    @FXML
    private void onClear() {
        directoryField.clear();
        patternField.clear();
        minSizeField.clear();
        maxSizeField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        resultTable.getItems().clear();
    }

    private long parseSize(String text) {
        try {
            double kb = Double.parseDouble(text.trim());
            return (long)(kb * 1024);
        } catch (Exception e) {
            return -1;
        }
    }

    private String formatSize(long bytes) {
        String[] units = { "B","KB","MB","GB" };
        double sz = bytes;
        int idx=0;
        while(sz>=1024 && idx<units.length-1){
            sz/=1024; idx++;
        }
        return String.format("%.2f %s", sz, units[idx]);
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.initOwner(ownerWindow);
        a.showAndWait();
    }
}
