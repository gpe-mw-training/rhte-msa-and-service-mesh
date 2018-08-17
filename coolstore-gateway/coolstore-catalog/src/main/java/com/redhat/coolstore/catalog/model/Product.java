package com.redhat.coolstore.catalog.model;

import java.io.Serializable;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class Product implements Serializable {

    private static final long serialVersionUID = -6994655395272795259L;

    private String itemId;
    private String name;
    private String desc;
    private double price;

    public Product() {

    }

    public Product(JsonObject json) {
        this.itemId = json.getString("itemId");
        this.name = json.getString("name");
        this.desc = json.getString("desc");
        this.price = json.getDouble("price");
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();
        json.put("itemId", this.itemId);
        json.put("name", this.name);
        json.put("desc", this.desc);
        json.put("price", this.price);
        return json;
    }

}
