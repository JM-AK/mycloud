package com.geekbrains.gb.mycloud.util;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbrains.gb.mycloud.data.ClientSettings;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WindowManager {
    private static Stage stage = ClientSettings.getInstance().getStage();

    public static void showLogin() {
            try {
                Parent root = FXMLLoader.load(WindowManager.class.getResource("/client_login.fxml"));
                stage.setTitle("MyCloud - Authorization");
                Scene scene = new Scene(root, 400, 150);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void showRegister() {
        try {
            Parent root = FXMLLoader.load(WindowManager.class.getResource("/client_Register.fxml"));
            stage.setTitle("MyCloud - Register new user");
            stage.setScene(new Scene(root, 400, 250));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showChangePassword() {
            try {
                Parent root = FXMLLoader.load(WindowManager.class.getResource("/client_changepass.fxml"));
                stage.setTitle("MyCloud - Change password");
                stage.setScene(new Scene(root, 400, 250));
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void showMain() {
            try {
                Parent root = FXMLLoader.load(WindowManager.class.getResource("/client_main.fxml"));
                stage.setTitle("MyCloud client");
                stage.setScene(new Scene(root, 1024, 768));
                stage.setResizable(false);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static List<Path> addFilesDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select files to copy to Local Storage");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        List<File> files = fileChooser.showOpenMultipleDialog(ClientSettings.getInstance().getStage());
        List<Path> list = new ArrayList<>();
        for (File file : files) {
            list.add(file.toPath());
        }
        return list;
    }

    public static Optional<ButtonType> showDeleteConfirmation(Path file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        if (Files.isDirectory(file)) {
            alert.setContentText("Do you really want to delete directory '" + file.getFileName() + "'?");
        } else {
            alert.setContentText("Do you really want to delete file '" + file.getFileName() + "'?");
        }
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showLogOutConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Do you really want to log out?");
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showOverwriteConfirmation(Path file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Overwrite");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to overwrite destination file '" + file.getFileName() + "'?");
        return alert.showAndWait();
    }

    public static Optional<String> showInputRename(Path file) {
        TextInputDialog dialog = new TextInputDialog(file.getFileName().toString());
        dialog.setTitle("Rename file");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new filename:");
        return dialog.showAndWait();
    }

    public static Optional<String> showInputCreateDir(Path file) {
        TextInputDialog dialog = new TextInputDialog(file.getFileName().toString());
        dialog.setTitle("Creation of new directory");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter the directory name:");
        return dialog.showAndWait();
    }

    public static void showWarningAlert(String msg) {
        Platform.runLater(() -> {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Warning");
            warning.setHeaderText(null);
            warning.setContentText(msg);
            warning.showAndWait();
        });
    }

    public static void showInfoAlert(String msg) {
        Platform.runLater(() -> {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Information");
            info.setHeaderText(null);
            info.setContentText(msg);
            info.showAndWait();
        });
    }

    public static void showErrorAlert(String msg) {
        Platform.runLater(() -> {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText(null);
            error.setContentText(msg);
            error.showAndWait();
        });
    }
}
