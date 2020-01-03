package com.dxv.abivinTracklocationModule.model;

import java.util.ArrayList;

public class TrackLocationConfig {
    private ArrayList<String> organizationId;
    private String token;
    private String URL;
    private String shipmentId;
    private String routeDetailId;

    public TrackLocationConfig(ArrayList<String> organizationId, String token, String URL, String shimentId, String routeDetailId) {
        this.organizationId = organizationId;
        this.token = token;
        this.URL = URL;
        this.shipmentId = shimentId;
        this.routeDetailId = routeDetailId;
    }

    public ArrayList<String> getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(ArrayList<String> organizationId) {
        this.organizationId = organizationId;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getRouteDetailId(){
        return routeDetailId;
    }
}
