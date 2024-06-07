package com.example.foodorderingworkplace.models;

public class AsiaFood {

    String name;
    String price;
    Integer imageUrl;

    public AsiaFood(String name, String price, Integer imageUrl, String restorantname) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.restorantname = restorantname;
    }
    String restorantname;

    public String getRestorantname() {
        return restorantname;
    }

    public void setRestorantname(String restorantname) {
        this.restorantname = restorantname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Integer imageUrl) {
        this.imageUrl = imageUrl;
    }
}
