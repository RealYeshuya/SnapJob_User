package com.example.snapjob_user.Model;

import com.firebase.geofire.GeoLocation;

public class WorkerGeoModel {
    private String key;
    private GeoLocation geoLocation;
    private WorkerInfoModel workerInfoModel;

    public WorkerGeoModel() {
    }

    public WorkerGeoModel(String key, GeoLocation geoLocation) {
        this.key = key;
        this.geoLocation = geoLocation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public WorkerInfoModel getWorkerInfoModel() {
        return workerInfoModel;
    }

    public void setWorkerInfoModel(WorkerInfoModel workerInfoModel) {
        this.workerInfoModel = workerInfoModel;
    }
}
