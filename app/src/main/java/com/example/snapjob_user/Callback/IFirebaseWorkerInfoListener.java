package com.example.snapjob_user.Callback;

import com.example.snapjob_user.Model.WorkerGeoModel;

public interface IFirebaseWorkerInfoListener {
    void onWorkerInfoLoadSuccess(WorkerGeoModel workerGeoModel);
}
