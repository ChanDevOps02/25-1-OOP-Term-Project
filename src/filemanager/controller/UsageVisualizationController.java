package filemanager.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class UsageVisualizationController {

    @FXML private TextField directoryField;
    @FXML private TextField daysField;
    @FXML private BarChart<String, Number> barChart;

    private Window ownerWindow;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd");

    public void setOwnerWindow(Window window) {
        this.ownerWindow = window;
    }

    @FXML
    public void initialize() {
        barChart.getData().clear();
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
    private void onVisualize() {
        barChart.getData().clear();

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

        int days;
        try {
            days = Integer.parseInt(daysField.getText().trim());
            if (days <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("유효한 기간(정수)을 입력하세요.");
            return;
        }

        // 지난 N일 날짜별 초기화
        Map<String, Integer> countMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            String key = today.minusDays(i).format(dtf);
            countMap.put(key, 0);
        }

        // 파일 수정일 집계
        try (Stream<Path> stream = Files.walk(dir.toPath())) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                try {
                    BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
                    LocalDate lm = Instant.ofEpochMilli(attr.lastModifiedTime().toMillis())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    String key = lm.format(dtf);
                    if (countMap.containsKey(key)) {
                        countMap.put(key, countMap.get(key) + 1);
                    }
                } catch (IOException ignored) {}
            });
        } catch (IOException e) {
            showAlert("시각화 중 오류가 발생했습니다.");
            return;
        }

        // 차트에 데이터 추가
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("수정 건수");
        countMap.forEach((date, cnt) ->
                series.getData().add(new XYChart.Data<>(date, cnt))
        );
        barChart.getData().add(series);
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.initOwner(ownerWindow);
        a.showAndWait();
    }
}
