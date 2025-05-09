package filemanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AboutController {

    @FXML private TextArea aboutTextArea;
    private Window ownerWindow;

    /** MainWindowController에서 호출하여 ownerWindow를 설정 */
    public void setOwnerWindow(Window window) {
        this.ownerWindow = window;
    }

    @FXML
    public void initialize() {
        loadAboutText();
    }

    private void loadAboutText() {
        InputStream in = getClass().getResourceAsStream("/about.txt");
        if (in == null) {
            aboutTextArea.setText("개발자 소개 파일을 찾을 수 없습니다.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            aboutTextArea.setText(sb.toString());
        } catch (Exception e) {
            aboutTextArea.setText("개발자 소개 로드 중 오류:\n" + e.getMessage());
        }
    }
}
