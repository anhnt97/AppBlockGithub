package com.example.ngothanh.appblock.sqlite;

public class Locution {
    private String vieValue;
    private String engValue;
    private String author;

    public Locution(String vieValue, String engValue, String author) {
        this.vieValue = vieValue;
        this.engValue = engValue;
        this.author = author;
    }

    public String getVieValue() {
        return vieValue;
    }

    public String getEngValue() {
        return engValue;
    }

    public String getAuthor() {
        return author;
    }
}
