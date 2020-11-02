package com.geekbrains.gb.mycloud.controller;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.message.FileMsg;
import com.geekbrains.gb.mycloud.data.ClientSettings;
import com.geekbrains.gb.mycloud.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainController implements Initializable, FileListReceiverCallback {
    private ObservableList<TableEntry> listLocal = FXCollections.observableArrayList();
    private ObservableList<TableEntry> listServer = FXCollections.observableArrayList();
    private StoragePath spLocal = ClientSettings.getInstance().getLocalPath();
    private StoragePath spServer = ClientSettings.getInstance().getServerPath();
    private Queue<FileMsg> uploadQueue;

    private static MainController instance = new MainController();

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public Text pathLocalText;
    @FXML
    public Text pathServerText;
    @FXML
    public TableView<TableEntry> localTable;
    @FXML
    public TableView<TableEntry> serverTable;
    @FXML
    public TableColumn<TableEntry, Integer> idColLocal;
    @FXML
    public TableColumn<TableEntry, Integer> idColServer;
    @FXML
    public TableColumn<TableEntry, String> nameColLocal;
    @FXML
    public TableColumn<TableEntry, String> nameColServer;
    @FXML
    public TableColumn<TableEntry, String> typeColLocal;
    @FXML
    public TableColumn<TableEntry, String> typeColServer;
    @FXML
    public TableColumn<TableEntry, String> sizeColLocal;
    @FXML
    public TableColumn<TableEntry, String> sizeColServer;
    @FXML
    public Button btnDownload;
    @FXML
    public Button btnUpload;

    /*
    * Management GUI
    * */

    @FXML
    public void refreshLocal(ActionEvent actionEvent) {
        try {
            fillFileTable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void refreshServer(ActionEvent actionEvent) {
        CommandMsg cmdMsg = new CommandMsg(Command.REFRESH_FILELIST, spServer.toString());
        ClientNetwork.getInstance().sendObject(cmdMsg);
    }

    @FXML
    public void createDirLocal(ActionEvent actionEvent) {
        Path path = spLocal.getFullPath();
        Optional<String> btn = WindowManager.showInputCreateDir(path);
        if (btn.isPresent()) {
            try {
                String newDirName = btn.get();
                Files.createDirectory(Paths.get(path.toString(), newDirName));
                fillFileTable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void createDirServer(ActionEvent actionEvent) {
        Path path = spServer.getFullPath();
        Optional<String> btn = WindowManager.showInputCreateDir(path);
        if (btn.isPresent()) {
            String newDirName = btn.get();
            Path newFilePath = Paths.get(path.toString(), newDirName);
            CommandMsg cmdMsg = new CommandMsg(Command.CREATE_DIR, newFilePath.toString());
            ClientNetwork.getInstance().sendObject(cmdMsg);
        }
    }

    @FXML
    public void renameLocal(ActionEvent actionEvent) {
        TableEntry selectedEntry = localTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            Path file = selectedEntry.getFullPath();
            try {
                Optional<String> btn = WindowManager.showInputRename(file);
                if (btn.isPresent()) {
                    String newName = btn.get();
                    Files.move(file, file.resolveSibling(newName), StandardCopyOption.REPLACE_EXISTING);
                    fillFileTable(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void renameAtServer(ActionEvent actionEvent) {
        TableEntry selectedEntry = serverTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            Path file = selectedEntry.getFullPath();
            Optional<String> btn = WindowManager.showInputRename(file);
            if (btn.isPresent()) {
                String newName = btn.get();
                CommandMsg cmdMsg = new CommandMsg(Command.RENAME_FILE_DIR, file.toString(), newName);
                ClientNetwork.getInstance().sendObject(cmdMsg);
            }
        }
    }

    @FXML
    public void deleteLocal(ActionEvent actionEvent) {
        TableEntry selectedEntry = localTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            Optional<ButtonType> btn = WindowManager.showDeleteConfirmation(selectedEntry.getFullPath());
            if (btn.isPresent() && btn.get() == ButtonType.OK) {
                Path path = selectedEntry.getFullPath();
                if (Files.isDirectory(path)) {
                    deleteDirectory(path);
                } else {
                    deleteFile(path);
                }
                try {
                    fillFileTable(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @FXML
    public void deleteAtServer(ActionEvent actionEvent) {
        TableEntry selectedEntry = serverTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            Optional<ButtonType> btn = WindowManager.showDeleteConfirmation(selectedEntry.getFullPath());
            if (btn.isPresent() && btn.get() == ButtonType.OK) {
                Path path = selectedEntry.getFullPath();
                CommandMsg cmdMsg = null;
                if (Files.isDirectory(path)) {
                    cmdMsg = new CommandMsg(Command.DELETE_DIR,path.toString());
                } else {
                    cmdMsg = new CommandMsg(Command.DELETE_FILE,path.toString());
                }
                ClientNetwork.getInstance().sendObject(cmdMsg);
            }
        }
    }

    @FXML
    public void openDir(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().browse(new URI(spLocal.getFullPath().toString()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void addLocal(ActionEvent actionEvent) {
        List<Path> files = WindowManager.addFilesDialog();
        if (files != null) {
            for (Path src : files) {
                if (Files.exists(src)) {
                    Path dst = Paths.get(spLocal.getFullPath().toString(), src.getFileName().toString());
                    try {
                        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                        fillFileTable(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @FXML
    public void uploadToServer(ActionEvent actionEvent) {
        TableEntry selectedEntry = localTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            btnUpload.setDisable(true);
            Path src = selectedEntry.getFullPath();
            Path dst = spServer.getFullPath();
            if (!Files.exists(src)) return;

            if (Files.isDirectory(src)) {
                uploadDirectory(src, dst);
            } else {
                uploadFile(src, dst);
            }
            btnUpload.setDisable(false);
        }
    }

    @FXML
    public void downloadFromServer(ActionEvent actionEvent) {
        TableEntry selectedEntry = serverTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            btnDownload.setDisable(true);
            Path src = selectedEntry.getFullPath();
            Path dst = spLocal.getFullPath();

            boolean isDirectory = false;
            if (!Files.exists(src)) return;
            if (Files.isDirectory(src)) isDirectory = true;

            CommandMsg cmdMsg = new CommandMsg(Command.DOWNLOAD_FILEDIR, src.toString(), dst.toString(), isDirectory);
            ClientNetwork.getInstance().sendObject(cmdMsg);
            btnDownload.setDisable(false);
        }
    }

    @FXML
    public void logOut(ActionEvent actionEvent) {
        Optional<ButtonType> btn = WindowManager.showLogOutConfirmation();
        if (btn.isPresent()) {
            CommandMsg cmdMsg = new CommandMsg(Command.LOGOUT, true);
        }
    }

    /*
    * Init GUI
    * */

    private void initLocalTableGUI() {
        //set column name
        idColLocal.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColLocal.setCellValueFactory(new PropertyValueFactory<>("shortFileName"));
        typeColLocal.setCellValueFactory(new PropertyValueFactory<>("type"));
        sizeColLocal.setCellValueFactory(new PropertyValueFactory<>("size"));
        //set col styles
        idColLocal.setStyle("-fx-alignment: CENTER;");
        nameColLocal.setStyle("-fx-alignment: LEFT;");
        typeColLocal.setStyle("-fx-alignment: CENTER;");
        sizeColLocal.setStyle("-fx-alignment: RIGHT;");
        //fill column
        localTable.setItems(listLocal);
        //manage local table
        localTable.setRowFactory(tr -> {
            TableRow<TableEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    TableEntry entry = row.getItem();
                    if (entry.isDirectory()) {
                        this.insideDirLocal(entry.getFullPath());
                    } else if (entry.isMove()) {
                        this.outsideDirLocal();
                    } else {
                        try {
                            Desktop.getDesktop().open(new File(entry.getFullPath().toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return row;
        });
    }

    private void initServerTableGUI () {
        //set column name
        idColServer.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColServer.setCellValueFactory(new PropertyValueFactory<>("shortFileName"));
        typeColServer.setCellValueFactory(new PropertyValueFactory<>("type"));
        sizeColServer.setCellValueFactory(new PropertyValueFactory<>("size"));
        //set col styles
        idColServer.setStyle("-fx-alignment: CENTER;");
        nameColServer.setStyle("-fx-alignment: LEFT;");
        typeColServer.setStyle("-fx-alignment: CENTER;");
        sizeColServer.setStyle("-fx-alignment: RIGHT;");
        //fill column
        serverTable.setItems(listServer);

        //manage server table
        serverTable.setRowFactory(tr -> {
            TableRow<TableEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    TableEntry entry = row.getItem();
                    if (entry.isDirectory()) {
                        String folderOpen = entry.getShortFileName();
                        Path path = Paths.get(spServer.getFullPath().toString(), folderOpen);
                        CommandMsg cmdMsg = new CommandMsg(Command.OPEN_DIR, path.toString());
                        ClientNetwork.getInstance().sendObject(cmdMsg);
                    } else if (entry.isMove()) {
                        Path path = spServer.outside();
                        CommandMsg cmdMsg = new CommandMsg(Command.OPEN_DIR, path.toString());
                        ClientNetwork.getInstance().sendObject(cmdMsg);
                    }
                }
            });
            return row;
        });
    }

    /*
    * Support methods
    * */

    private void outsideDirLocal() {
        spLocal.outside();
        try {
            fillFileTable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateText(true, spLocal, true);
    }

    private void insideDirLocal(Path path) {
        spLocal.inside(path);
        try {
            fillFileTable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateText(true, spLocal, true);
    }

    private void fillFileTable(boolean isLocal) throws IOException {
        ObservableList<TableEntry> list = FXCollections.observableArrayList();;
        StoragePath sp = isLocal ? this.spLocal : this.spServer;

        List<Path> files = null;
        if (isLocal) files = Files.list(sp.getFullPath()).collect(Collectors.toList());
        if (!isLocal) files = ClientSettings.getInstance().getServerFileList();
        int rowsCount = (files != null) ? files.size() : 0;
        if (!sp.isRoot()) list.add(new TableEntry());
        for (Path file : files) {
            rowsCount++;
            list.add(new TableEntry(rowsCount, file));
        }
        sortList(list, sp);
        if (isLocal) listLocal = list;
        if (!isLocal) listServer = list;
        updateText(isLocal, sp, true);
    }

    private void updateText (boolean isLocal, StoragePath sp, boolean isAbsPath) {
        if (isAbsPath) {
            if ((isLocal)) {
                this.pathLocalText.setText(sp.toAbsString());
            } else {
                this.pathServerText.setText(sp.toAbsString());
            }
        } else {
            if ((isLocal)) {
                this.pathLocalText.setText(sp.toRelString());
            } else {
                this.pathServerText.setText(sp.toRelString());
            }
        }
    }

    private void sortList(ObservableList<TableEntry> list, StoragePath sp) {
        Comparator<TableEntry> comp = (o1, o2) -> {
            if (o1.isDirectory() && o2.isFile()) return -1;
            if (o1.isFile() && o2.isDirectory()) return 1;
            if (o1.isFile() && o2.isFile()) return o1.getShortFileName().compareTo(o2.getShortFileName());
            if (o1.isDirectory() && o2.isDirectory()) return o1.getShortFileName().compareTo(o2.getShortFileName());
            return 0;
        };
        list.sort(comp);
        for (int i = 0; i < list.size(); i++) {
            TableEntry entry = list.get(i);
            if (!sp.isRoot() && entry.isMove())
                entry.setId(null);
            else if (sp.isRoot() && !entry.isMove())
                entry.setId(i + 1);
            else entry.setId(i);
        }
    }

    //ToDo mb move to FileService utility

    private void deleteFile(Path path) {
        try {
            DosFileAttributeView attr = Files.getFileAttributeView(path, DosFileAttributeView.class);
            attr.setReadOnly(false);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    DosFileAttributeView attr = Files.getFileAttributeView(dir, DosFileAttributeView.class);
                    attr.setReadOnly(false);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    DosFileAttributeView attr = Files.getFileAttributeView(file, DosFileAttributeView.class);
                    attr.setReadOnly(false);
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    DosFileAttributeView attr = Files.getFileAttributeView(dir, DosFileAttributeView.class);
                    attr.setReadOnly(false);
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadFile(Path src, Path dst) {
        Thread uploadFileThread = new Thread(() -> {
            FileMsg fileMsg = new FileMsg(src, dst, false);
            ClientNetwork.getInstance().sendObject(fileMsg);
        });
        uploadFileThread.start();

    }

    private void uploadDirectory(Path src, Path dst) {
        Thread uploadDirThread = new Thread(() -> {
            try {
                Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path subDir = spLocal.getFullPath().relativize(dir);
                        Path dst = Paths.get(spServer.getFullPath().toString(), subDir.toString());

                        CommandMsg cmdMsg = new CommandMsg(Command.CREATE_DIR, dst.toString());
                        ClientNetwork.getInstance().sendObject(cmdMsg);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path subFile = spLocal.getFullPath().relativize(file);
                        Path relFolder = subFile.subpath(0, subFile.getNameCount() - 1);
                        Path newPath = Paths.get(dst.toString(), relFolder.toString());
                        FileMsg fileMsg = new FileMsg(file, newPath, false);
                        uploadQueue.offer(fileMsg);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (!uploadQueue.isEmpty()) {
                    FileMsg fileMsg = uploadQueue.poll();
                    ClientNetwork.getInstance().sendObject(fileMsg);
                    //ToDo - add listener
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CommandMsg cmdMsg = new CommandMsg(Command.GETFILELIST, spServer.toAbsString());
            ClientNetwork.getInstance().sendObject(cmdMsg);
        });
        uploadDirThread.start();
        btnUpload.setDisable(false);
    }

    @Override
    public void receiveFileListCallback() {
        try {
            fillFileTable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.fillFileTable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.fillFileTable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initLocalTableGUI();
          initServerTableGUI();
    }
}



