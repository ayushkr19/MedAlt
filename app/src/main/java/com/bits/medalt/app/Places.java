package com.bits.medalt.app;

/**
 * Created by ayush on 16/3/14.
 * @author Ayush Kumar
 */
public class Places {

    public String id;
    public String name;
    public double lat;
    public double lng;
    public String[] types;


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