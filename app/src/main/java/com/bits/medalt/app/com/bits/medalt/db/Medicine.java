package com.bits.medalt.app.com.bits.medalt.db;

/**
 * Created by ayush on 21/4/14.
 * @author Ayush Kumar
 */
public class Medicine {

    private String id;
    private String trade_name;
    private String api;
    private String dosage;
    private String category;

    public Medicine(String id, String trade_name, String api, String dosage, String category) {
        this.id = id;
        this.trade_name = trade_name;
        this.api = api;
        this.dosage = dosage;
        this.category = category;
    }

    public Medicine(String trade_name, String api, String dosage, String category) {
        this.trade_name = trade_name;
        this.api = api;
        this.dosage = dosage;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrade_name() {
        return trade_name;
    }

    public void setTrade_name(String trade_name) {
        this.trade_name = trade_name;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
