package filemanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HelpController {

    @FXML private TextArea helpTextArea;

    // MainWindowController로부터 전달받을 소유 윈도우
    private Window ownerWindow;

    @FXML
    public void initialize() {
        loadHelpText();
    }

    /** MainWindowController에서 호출하여 ownerWindow를 설정 */
    public void setOwnerWindow(Window window) {
        this.ownerWindow = window;
    }

    /** resources/help.txt를 읽어서 TextArea에 채워 넣음 */
    private void loadHelpText() {
        InputStream in = getClass().getResourceAsStream("/help.txt");
        if (in == null) {
            helpTextArea.setText("도움말 파일을 찾을 수 없습니다.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            helpTextArea.setText(sb.toString());
        } catch (Exception e) {
            helpTextArea.setText("도움말 로드 중 오류가 발생했습니다:\n" + e.getMessage());
        }
    }
}
