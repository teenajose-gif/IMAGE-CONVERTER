package com.example.image_converter;

import java.io.File;

public class FileModal {
    String path, name, size, dateCreated;
    File file;

    public FileModal(File file, String path, String name, String size, String dateCreated) {
        this.file = file;
        this.path = path;
        this.name = name;
        this.size = size;
        this.dateCreated = dateCreated;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
