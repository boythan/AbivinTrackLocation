package com.dxv.abivinTracklocationModule.model;

//import android.support.annotation.Keep;

import java.util.ArrayList;

import androidx.annotation.Keep;

@Keep
public class LocationBody {
    public ArrayList<String> organizationId;
    public ArrayList<LocationAbi> locations;
    public String shipmentId;
    public String routeId;
    public Boolean isGPSStopped;
    public Long timestamp;

    public LocationBody(Boolean isGPSStopped, Long timestamp) {
        this.isGPSStopped = isGPSStopped;
        this.timestamp = timestamp;
    }


    public LocationBody(ArrayList<String> organizationId, ArrayList<LocationAbi> locations, String shipmentId, Boolean isGPSStopped, Long timestamp, String routeId) {
        this.organizationId = organizationId;
        this.locations = locations;
        this.shipmentId = shipmentId;
        this.isGPSStopped = isGPSStopped;
        this.timestamp = timestamp;
        this.routeId = routeId;

    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<String> getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(ArrayList<String> organizationId) {
        this.organizationId = organizationId;
    }

    public ArrayList<LocationAbi> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LocationAbi> locations) {
        this.locations = locations;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Boolean getGPSStopped() {
        return isGPSStopped;
    }

    public void setGPSStopped(Boolean GPSStopped) {
        isGPSStopped = GPSStopped;
    }
}
