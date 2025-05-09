package filemanager.controller;

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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InactiveFileCleanupController {

    @FXML private TextField directoryField;
    @FXML private TextField daysField;
    @FXML private TableView<FileInfo> tableView;
    @FXML private TableColumn<FileInfo, String> colFilePath;
    @FXML private TableColumn<FileInfo, String> colLastAccess;

    private Window ownerWindow;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        colFilePath.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPath()));
        colLastAccess.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getLastAccess()));
        tableView.getItems().clear();
    }

    // MainWindowController 에서 호출
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
    private void onScanInactiveFiles() {
        tableView.getItems().clear();

        String path = directoryField.getText();
        if (path == null || path.isEmpty()) {
            showAlert("폴더를 먼저 선택하세요.");
            return;
        }
        File dir = new File(path);
        if (!dir.isDirectory()) {
            showAlert("유효한 폴더를 선택하세요.");
            return;
        }

        int days;
        try {
            days = Integer.parseInt(daysField.getText().trim());
            if (days < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("유효한 일(Days) 수를 입력하세요.");
            return;
        }

        Instant cutoff = Instant.now().minus(Duration.ofDays(days));
        List<FileInfo> list = new ArrayList<>();

        // 재귀적으로 파일 탐색
        try (Stream<Path> stream = Files.walk(dir.toPath())) {
            stream.filter(Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
                            Instant lastAccess = attr.lastAccessTime().toInstant();
                            if (lastAccess.isBefore(cutoff)) {
                                String formatted = LocalDateTime.ofInstant(lastAccess, ZoneId.systemDefault())
                                        .format(dtf);
                                list.add(new FileInfo(p.toString(), formatted));
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("파일 검색 중 오류가 발생했습니다.");
            return;
        }

        if (list.isEmpty()) {
            showAlert(days + "일 이상 미사용된 파일이 없습니다.");
        } else {
            tableView.getItems().setAll(list);
        }
    }

    @FXML
    private void onDeleteAll() {
        List<FileInfo> items = new ArrayList<>(tableView.getItems());
        if (items.isEmpty()) {
            showAlert("삭제할 파일이 없습니다.");
            return;
        }

        items.forEach(fi -> {
            try {
                Files.deleteIfExists(Paths.get(fi.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        tableView.getItems().clear();
        showAlert("선택된 모든 파일을 삭제했습니다.");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.initOwner(ownerWindow);
        alert.showAndWait();
    }

    // 내부 모델 클래스 (간단 재사용)
    public static class FileInfo {
        private final String path;
        private final String lastAccess;

        public FileInfo(String path, String lastAccess) {
            this.path = path;
            this.lastAccess = lastAccess;
        }
        public String getPath() { return path; }
        public String getLastAccess() { return lastAccess; }
    }
}
