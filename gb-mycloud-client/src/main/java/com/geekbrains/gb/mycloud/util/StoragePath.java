package com.geekbrains.gb.mycloud.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StoragePath {
    private Path root;
    private Path fullPath;
    private Path relPath;

    public Path getRoot() {
        return root;
    }

    public Path getFullPath() {
        return fullPath;
    }

    public Path getRelPath() {
        return relPath;
    }

    public void setFullPath(Path fullPath) {
        this.fullPath = fullPath;
        this.relativize();
    }

    public void setRoot(Path root) {
        this.root = root;
        this.fullPath = root;
        this.relativize();
    }

    public StoragePath() {
        this.root = null;
        this.fullPath = null;
        this.relativize();
    }

    public Path inside(String dir) {
        Path path = Paths.get(fullPath.toString(), dir);
        if (Files.exists(path)) {
            Path newPath = fullPath.resolve(dir);
            fullPath = newPath;
            this.relativize();
            return fullPath;
        }
        return null;
    }

    public Path inside(Path path) {
        if (Files.exists(path)) {
            Path newPath = fullPath.resolve(path);
            fullPath = newPath;
            this.relativize();
            return fullPath;
        }
        return null;
    }

    public Path outside() {
        Path newPath = fullPath.resolveSibling("");
        fullPath = newPath;
        return fullPath;
    }

    private void relativize() {
        if (root != null) {
            this.relPath = root.relativize(fullPath);
        }
    }

    public String toAbsString() {
        return fullPath.toString();
    }

    public String toRelString() {
        return "\\root\\" + relPath.toString();
    }

    @Override
    public String toString() {
        return this.toAbsString();
    }

    public boolean isRoot() {
        return (this.toRelString().equals("\\root\\")) || fullPath.equals(root);
    }
}
