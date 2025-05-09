package filemanager.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainWindowController {

    @FXML
    private Label diskUsageLabel;       // 디스크 사용량 표시 레이블

    @FXML
    private StackPane mainContentPane;

    @FXML
    private TextArea logArea;

    @FXML
    public void initialize() {
        // 애플리케이션 시작 시 디스크 사용량을 보여줍니다.
        updateDiskUsage();
    }

    /** 디스크 사용량 레이블 갱신 */
    private void updateDiskUsage() {
        // 현재 작업 디렉터리의 루트 드라이브 추출
        Path rootPath = Paths.get(".").toAbsolutePath().getRoot();
        File root = rootPath.toFile();

        long total = root.getTotalSpace();
        long free  = root.getFreeSpace();
        long used  = total - free;

        diskUsageLabel.setText(
                String.format("디스크 사용량: %s / %s",
                        formatSize(used),
                        formatSize(total)
                )
        );
    }

    /** 바이트 단위를 사람이 읽기 쉬운 형태로 변환 */
    private String formatSize(long bytes) {
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        double size = bytes;
        int idx = 0;
        while (size >= 1024 && idx < units.length - 1) {
            size /= 1024;
            idx++;
        }
        return String.format("%.2f %s", size, units[idx]);
    }

    @FXML
    private void handleSortByExtension() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SortByExtensionView.fxml"));
        Parent view = loader.load();

        // 컨트롤러에 메인 윈도우의 Window 객체 전달
        SortByExtensionController ctrl = loader.getController();
        Window window = mainContentPane.getScene().getWindow();
        ctrl.setOwnerWindow(window);

        mainContentPane.getChildren().setAll(view);
    }

    @FXML
    private void handleInactiveFileCleanup() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InactiveFileCleanupView.fxml"));
        Parent view = loader.load();

        // 컨트롤러에 메인 윈도우의 Window 객체 전달
        InactiveFileCleanupController ctrl = loader.getController();
        Window window = mainContentPane.getScene().getWindow();
        ctrl.setOwnerWindow(window);

        mainContentPane.getChildren().setAll(view);
    }

    @FXML
    private void handleAdvancedSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdvancedSearchView.fxml"));
            Parent view = loader.load();

            AdvancedSearchController ctrl = loader.getController();
            ctrl.setOwnerWindow(mainContentPane.getScene().getWindow());
            mainContentPane.getChildren().setAll(view);

        } catch (IOException e) {
            // 오류가 나도 UI 스레드를 멈추지 않습니다.
            e.printStackTrace();
            logArea.appendText("⚠ 고급 검색 화면 로드 실패: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void handleUsageVisualization() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsageVisualizationView.fxml"));
            Parent view = loader.load();

            UsageVisualizationController ctrl = loader.getController();
            ctrl.setOwnerWindow(mainContentPane.getScene().getWindow());

            mainContentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            logArea.appendText("⚠ 사용 이력 시각화 로드 실패: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void handleEncryption() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EncryptionView.fxml"));
            Parent view = loader.load();

            EncryptionController ctrl = loader.getController();
            ctrl.setOwnerWindow(mainContentPane.getScene().getWindow());
            mainContentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            logArea.appendText("⚠ 암호화 화면 로드 실패: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void handleHelp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HelpView.fxml"));
            Parent view = loader.load();
            HelpController ctrl = loader.getController();
            ctrl.setOwnerWindow(mainContentPane.getScene().getWindow());

            mainContentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            logArea.appendText("⚠ 도움말 로드 실패: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void handleAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AboutView.fxml"));
            Parent view = loader.load();

            AboutController ctrl = loader.getController();
            ctrl.setOwnerWindow(mainContentPane.getScene().getWindow());

            mainContentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            logArea.appendText("⚠ 개발자 소개 로드 실패: " + e.getMessage() + "\n");
        }
    }


}
