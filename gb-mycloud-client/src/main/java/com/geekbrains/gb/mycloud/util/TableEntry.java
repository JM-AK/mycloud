package com.geekbrains.gb.mycloud.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class TableEntry implements Comparable<TableEntry> {
    private Integer id;
    private String shortFilename;
    private String fullFilename;
    private Path fullPath;
    private String shortPath;
    private String type;
    private String size;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShortFilename() {
        return shortFilename;
    }

    public String getFullFilename() {
        return fullFilename;
    }

    public Path getFullPath() {
        return fullPath;
    }

    public String getShortPath() {
        return shortPath;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public TableEntry(Integer id, Path file) {
        this.id = id;
        this.fullPath = file.toAbsolutePath();
        this.shortPath = file.getParent().toString();
        this.fullFilename = file.getFileName().toString();
        this.shortFilename = getFilename(file);
        this.type = getFileType(file);
        this.size = getFileSize(file);
    }

    public TableEntry() {
        this.fullPath = null;
        this.shortPath = "";
        this.fullFilename = "";
        this.shortFilename = "...";
        this.type = "";
        this.size = "";
    }

    public boolean isDirectory() {
        return this.getType().equals("dir");
    }

    public boolean isFile() {
        return !this.getType().equals("dir") && !isMove();
    }

    public boolean isMove() {
        return this.getShortFilename().equals("...");
    }

    private String getFilename(Path file) {
        if (file == null) return null;
        String name = file.getFileName().toString();
        int dotPos = name.lastIndexOf(".");
        if (dotPos != -1) {
            return name.substring(0, dotPos);
        }
        return name;
    }

    private String getFileSize(Path file) {
        if (Files.isDirectory(file)) {
            return "";
        }
        long fileSize = file.toFile().length();
        if (fileSize < 0) return "";

        final long BYTES_LIMIT = 1024;
        final long K_BYTES_LIMIT = (1024 * 1024);
        final long M_BYTES_LIMIT = (1024 * 1024 * 1024);

        if (fileSize < BYTES_LIMIT) {
            return String.valueOf(fileSize) + " b";
        } else if (fileSize < K_BYTES_LIMIT) {
            return String.valueOf(fileSize / 1024) + " Kb";
        } else if (fileSize < M_BYTES_LIMIT) {
            return String.valueOf(fileSize / 1024 / 1024) + " Mb";
        } else {
            return String.valueOf(fileSize / 1024 / 1024 / 1024) + " Gb";
        }
    }

    private String getFileType(Path file) {
        if (file == null) return null;
        String name = file.getFileName().toString();
        if (Files.isDirectory(file)) {
            return "dir";
        }
        int dotPos = name.lastIndexOf(".") + 1;
        if (dotPos > 0) {
            return name.substring(dotPos).toLowerCase();
        }
        return "";
    }

    @Override
    public int compareTo(TableEntry o) {
        return this.getShortFilename().compareTo(o.getShortFilename());
    }
}

