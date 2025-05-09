package filemanager.controller;

import filemanager.util.EncryptionUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class EncryptionController {

    @FXML private TextField fileField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea statusArea;

    private File selectedFile;
    private Window ownerWindow;

    public void setOwnerWindow(Window window) {
        this.ownerWindow = window;
    }

    @FXML
    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("암호화할 파일 선택");
        File f = chooser.showOpenDialog(ownerWindow);
        if (f != null) {
            selectedFile = f;
            fileField.setText(f.getAbsolutePath());
        }
    }

    @FXML
    private void onEncrypt() {
        if (!validatePassword()) return;
        File out = chooseOutput("암호화된 파일 저장");
        if (out == null) return;

        // 익명 Task<Void> 객체를 직접 생성
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                EncryptionUtil.encrypt(selectedFile, out, passwordField.getText());
                return null;
            }
        };

        task.setOnSucceeded(e -> appendStatus("암호화 완료: " + out.getName()));
        task.setOnFailed(e -> appendStatus("❌ 오류: " + task.getException().getMessage()));
        progressBar.progressProperty().bind(task.progressProperty());

        new Thread(task).start();
    }

    @FXML
    private void onDecrypt() {
        if (!validatePassword()) return;

        File out = chooseOutput("복호화된 파일 저장");
        if (out == null) return;

        // Task<Void>를 익명 클래스로 만들어 호출
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                EncryptionUtil.decrypt(selectedFile, out, passwordField.getText());
                return null;
            }
        };

        // 진행률 프로퍼티 바인딩
        progressBar.progressProperty().bind(task.progressProperty());

        // 성공/실패 핸들러
        task.setOnSucceeded(e -> appendStatus("🔓 복호화 완료: " + out.getName()));
        task.setOnFailed(e -> appendStatus("❌ 복호화 오류: " + task.getException().getMessage()));

        // 백그라운드 스레드에서 실행
        new Thread(task).start();
    }


    private boolean validatePassword() {
        String pw = passwordField.getText(), cpw = confirmField.getText();
        if (pw.isEmpty() || cpw.isEmpty()) {
            alert("비밀번호를 입력해주세요."); return false;
        }
        if (!pw.equals(cpw)) {
            alert("비밀번호가 일치하지 않습니다."); return false;
        }
        if (selectedFile == null) {
            alert("파일을 먼저 선택해주세요."); return false;
        }
        return true;
    }

    private File chooseOutput(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(selectedFile.getName() + (title.contains("암호화") ? ".enc" : ".dec"));
        return chooser.showSaveDialog(ownerWindow);
    }

    private void runTask(Task<Void> task, String successMsg) {
        progressBar.setProgress(0);
        statusArea.clear();
        task.setOnSucceeded(e -> appendStatus(successMsg));
        task.setOnFailed(e -> appendStatus("❌ 오류: " + task.getException().getMessage()));
        task.progressProperty().addListener((obs, o, n) -> progressBar.setProgress(n.doubleValue()));
        new Thread(task).start();
    }

    private void appendStatus(String msg) {
        Platform.runLater(() -> statusArea.appendText(msg + "\n"));
    }

    private void alert(String m) {
        Platform.runLater(() -> {
            // Alert 객체를 변수에 담고
            Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK);
            // initOwner는 따로 호출
            a.initOwner(ownerWindow);
            // showAndWait도 따로 호출
            a.showAndWait();
        });
    }

}
