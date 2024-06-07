package com.example.foodorderingworkplace.models;

public class ModelFood {
    private String foodId,foodName,foodDescription,foodCategory,foodAvailability,foodIcon,price,timestamp,uid;

    public ModelFood() {
    }

    public ModelFood(String foodId, String foodName, String foodDescription, String foodCategory, String foodAvailability, String foodIcon, String price, String timestamp, String uid) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodDescription = foodDescription;
        this.foodCategory = foodCategory;
        this.foodAvailability = foodAvailability;
        this.foodIcon = foodIcon;
        this.price = price;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public String getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(String foodCategory) {
        this.foodCategory = foodCategory;
    }

    public String getfoodAvailability() {
        return foodAvailability;
    }

    public void setfoodAvailability(String foodAvailability) {
        this.foodAvailability = foodAvailability;
    }

    public String getFoodIcon() {
        return foodIcon;
    }

    public void setFoodIcon(String foodIcon) {
        this.foodIcon = foodIcon;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
