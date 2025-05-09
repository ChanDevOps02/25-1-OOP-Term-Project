package filemanager.controller;

import filemanager.model.FileInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.*;
import java.util.*;

public class SortByExtensionController {

    @FXML private TextField directoryField;
    @FXML private TableView<FileInfo> resultTable;
    @FXML private TableColumn<FileInfo, String> colOldPath;
    @FXML private TableColumn<FileInfo, String> colNewPath;

    private Window ownerWindow;

    @FXML
    public void initialize() {
        colOldPath.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOldPath()));
        colNewPath.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNewPath()));
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
    private void onExecuteSort() {
        resultTable.getItems().clear();
        String path = directoryField.getText();
        if (path == null || path.isEmpty()) {
            showAlert("폴더를 먼저 선택하세요.");
            return;
        }

        File dir = new File(path);
        if (!dir.isDirectory()) {
            showAlert("유효한 폴더가 아닙니다.");
            return;
        }

        Map<String, List<File>> map = new HashMap<>();
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isFile()) {
                String ext = getExtension(f);
                map.computeIfAbsent(ext, k -> new ArrayList<>()).add(f);
            }
        }

        map.forEach((ext, files) -> {
            Path targetDir = Paths.get(path, ext.toUpperCase());
            try { Files.createDirectories(targetDir); }
            catch (Exception e) { e.printStackTrace(); }

            for (File f : files) {
                try {
                    Path src = f.toPath();
                    Path dst = targetDir.resolve(f.getName());
                    Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
                    resultTable.getItems().add(new FileInfo(src.toString(), dst.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void onUndoSort() {
        List<FileInfo> items = new ArrayList<>(resultTable.getItems());
        if (items.isEmpty()) {
            showAlert("복원할 항목이 없습니다.");
            return;
        }

        for (FileInfo fi : items) {
            try {
                Path src = Paths.get(fi.getNewPath());
                Path dst = Paths.get(fi.getOldPath());
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("파일 복원 중 오류: " + fi.getNewPath());
            }
        }

        resultTable.getItems().clear();
        showAlert("정리 취소(복원)가 완료되었습니다.");
    }

    private String getExtension(File f) {
        String name = f.getName();
        int idx = name.lastIndexOf('.');
        return (idx == -1) ? "UNKNOWN" : name.substring(idx + 1).toLowerCase();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.initOwner(ownerWindow);
        alert.showAndWait();
    }
}
