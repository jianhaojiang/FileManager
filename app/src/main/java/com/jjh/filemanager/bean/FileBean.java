package com.jjh.filemanager.bean;

/**
 *
 */

public class FileBean {

    private String name;
    private String path;
    private FileType fileType = FileType.other;
    private int sonFileCount;
    private int sonFolderCount;
    private int childCount ;
    private long size ;
    private int holderType ;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getHolderType() {
        return holderType;
    }

    public void setHolderType(int holderType) {
        this.holderType = holderType;
    }

    public int getSonFileCount() { return sonFileCount; }

    public void setSonFileCount(int sonFileCount) { this.sonFileCount = sonFileCount; }

    public int getSonFolderCount() { return sonFolderCount; }

    public void setSonFolderCount(int sonFolderCount) { this.sonFolderCount = sonFolderCount; }

}