package com.bits.medalt.app.com.bits.medalt.db;

/**
 * Created by ayush on 16/3/14.
 * @author Ayush Kumar
 */
public class Places {

    private String id;
    private String name;
    private double lat;
    private double lng;
    private String[] types;
    private String reference;


    public Places(String id, String name, double lat, double lng, String[] types) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.types = types;
    }

    public Places(String name, double lat, double lng, String[] types) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.types = types;
    }

    public Places(String name, double lat, double lng, String[] types, String reference) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.types = types;
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}