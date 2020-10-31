package com.geekbrains.gb.mycloud.controller;

import com.geekbraind.gb.mycloud.dictionary.Command;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbrains.gb.mycloud.data.ClientSettings;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import com.geekbrains.gb.mycloud.util.StoragePath;
import com.geekbrains.gb.mycloud.util.TableEntry;
import com.geekbrains.gb.mycloud.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {
    private ObservableList<TableEntry> listLocal = FXCollections.observableArrayList();
    private ObservableList<TableEntry> listServer = FXCollections.observableArrayList();
    private StoragePath spLocal = ClientSettings.getInstance().getLocalPath();
    private StoragePath spServer = ClientSettings.getInstance().getServerPath();

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
            fillFileTable(listLocal, spLocal, pathLocalText);
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
                fillFileTable(listLocal, spLocal, pathLocalText);
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
                    fillFileTable(listLocal, spLocal, pathLocalText);
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
                deleteFileDIR (path);
                try {
                    fillFileTable(listLocal, spLocal, pathLocalText);
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
                if (Files.isDirectory(path)) cmdMsg = new CommandMsg(Command.DELETE_DIR,path.toString());
                if (!Files.isDirectory(path)) cmdMsg = new CommandMsg(Command.DELETE_FILE,path.toString());
                ClientNetwork.getInstance().sendObject(cmdMsg);
            }
        }
    }

    @FXML
    public void openDir(ActionEvent actionEvent) {
    }

    @FXML
    public void addLocal(ActionEvent actionEvent) {
    }
    @FXML
    public void uploadToServer(ActionEvent actionEvent) {
    }
    @FXML
    public void downloadFromServer(ActionEvent actionEvent) {
    }

    @FXML
    public void logOut(ActionEvent actionEvent) {
    }

    /*
    * Init GUI
    * */

    private void initialise () {
        try {
            fillFileTable(listLocal, spLocal, pathLocalText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fillFileTable(listServer, spServer, pathServerText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initLocalTableGUI();
        initServerTableGUI();
    }

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

    private void initServerTableGUI() {
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
            fillFileTable(listLocal, spLocal, pathLocalText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.updateText(spLocal, pathLocalText, true);
    }

    private void insideDirLocal(Path path) {
        spLocal.inside(path);
        try {
            fillFileTable(listLocal, spLocal, pathLocalText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.updateText(spLocal, pathLocalText, true);
    }

    private void fillFileTable (ObservableList<TableEntry> list, StoragePath sp, Text pathText) throws IOException {
        list.clear();
        List<Path> files = null;
        if (list.equals(localTable)) files = Files.list(sp.getFullPath()).collect(Collectors.toList());
        if (list.equals(listServer)) files = ClientSettings.getInstance().getClientFileList();
        int rowsCount = (files != null) ? files.size() : 0;
        if (!sp.isRoot()) list.add(new TableEntry());
        for (Path file : files) {
            rowsCount++;
            list.add(new TableEntry(rowsCount, file));
        }
        sortList(list, sp);
        updateText(sp, pathText, true);
    }

    private void updateText (StoragePath sp, Text pathText , boolean isAbsPath) {
        if (isAbsPath) {
            pathText.setText(sp.toAbsString());
        } else {
            pathText.setText(sp.toRelString());
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

    //ToDo
    private void deleteFileDIR(Path path) {
    }

}



