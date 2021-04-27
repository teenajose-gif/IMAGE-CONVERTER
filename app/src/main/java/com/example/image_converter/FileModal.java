package com.example.image_converter;

public class FileModal {
    String path, name, size, dateCreated;

    public FileModal(String path, String name, String size, String dateCreated) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.dateCreated = dateCreated;
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
