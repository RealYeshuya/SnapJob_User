package com.example.snapjob_user.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.snapjob_user.Callback.IFirebaseFailedListener;
import com.example.snapjob_user.Callback.IFirebaseWorkerInfoListener;
import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.Model.GeoQueryModel;
import com.example.snapjob_user.Model.User;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.Model.WorkerGeoModel;
import com.example.snapjob_user.Model.WorkerInfoModel;
import com.example.snapjob_user.R;
import com.example.snapjob_user.TransactionClass;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ViewWorker extends Fragment implements OnMapReadyCallback, IFirebaseFailedListener, IFirebaseWorkerInfoListener {

    String workerFullName, workerId, transactionStat, userName, phoneNumber, workerPhoneNum, workerJob, workerJobDetail, workerNumDetail;
    private EditText transDescriptionTxt;
    private TextView workerNameDetail, workerJobDet, workerPhoneNumDetail, workerDistance, etaWorker;
    private CardView confirm_pickup_layout, worker_detail_layout, moreInfo;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userNameReference, workerPhoneReference;
    private Context contextNullSafe;
    private LatLng newPosition, workerPosition;
    Polyline polylineDistance = null;

    private LocationViewModel locationViewModel;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    //Get User Id
    private FirebaseUser user;
    private String userId;

    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Load Worker
    private double distance = 100.0; // default in km
    private static final double LIMIT_RANGE = 100.0; // km
    private Location previousLocation, currentLocation; // Use to calculate distance

    private boolean firstTime = true;

    //Listener
    IFirebaseWorkerInfoListener iFirebaseWorkerInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;
    private String cityName;


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        Common.workersFound.clear();
        mMap.clear();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.location_fragment, container, false);
        if (contextNullSafe == null) getContextNullSafety();

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        Bundle data = getArguments();

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (data != null){
            workerFullName = data.getString("workerFullName");
            workerId = data.getString("workerId");
            transactionStat = data.getString("transactionStatus");
            workerPhoneNum = data.getString("workerPhoneNum");
            workerJob = data.getString("workerJob");
        }

        //Get Single Value for UserName
        userNameReference = FirebaseDatabase.getInstance().getReference(Common.USER_INFO_REFERENCE);
        userNameReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if(userProfile != null){
                    userName = userProfile.fullName;
                    phoneNumber = userProfile.phoneNumber;
                } else if(userProfile.fullName == null){
                    userName = "Unavailable";
                } else if(userProfile.phoneNumber == null){
                    phoneNumber = "Unavailable";
                } else {
                    userName = "Unavailable";
                    phoneNumber = "Unavailable";
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(),"Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });

        confirm_pickup_layout = (CardView) root.findViewById(R.id.confirm_pickup_layout);
        worker_detail_layout = (CardView) root.findViewById(R.id.worker_detail_layout);
        moreInfo = (CardView) root.findViewById(R.id.moreInfo);
        transDescriptionTxt = (EditText) root.findViewById(R.id.transDescriptionTxt);
        workerNameDetail = (TextView) root.findViewById(R.id.workerName_detail);
        workerJobDet = (TextView) root.findViewById(R.id.workerJob_detail);
        workerJobDet = (TextView) root.findViewById(R.id.workerJob_detail);
        workerDistance = (TextView) root.findViewById(R.id.workerDistance);
        etaWorker = (TextView) root.findViewById(R.id.etaWorker);
        workerPhoneNumDetail = (TextView) root.findViewById(R.id.workerNum_detail);

        confirm_pickup_layout.setVisibility(View.GONE);
        worker_detail_layout.setVisibility(View.VISIBLE);
        moreInfo.setVisibility(View.VISIBLE);

        workerPhoneReference = FirebaseDatabase.getInstance().getReference(Common.WORKER_INFO_REFERENCE);
        workerPhoneReference.child(workerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Worker workerDetail = snapshot.getValue(Worker.class);
                workerJobDetail = workerDetail.job;
                workerNumDetail = workerDetail.phoneNum;

                workerNameDetail.setText("Worker Name: " + workerFullName);
                workerJobDet.setText("Job: " + workerJobDetail);
                workerPhoneNumDetail.setText("Phone Number: " + workerNumDetail);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                   Toast.makeText(getContext(),"Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });

        init();

        return root;
    }

    private void init() {

        iFirebaseFailedListener = this;
        iFirebaseWorkerInfoListener = this;

        if(ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
        }

        buildLocationRequest();
        buildLocationCallback();
        updateLocation();
        loadAvailableWorkers();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        contextNullSafe = context;
    }

    /**CALL THIS IF YOU NEED CONTEXT*/
    public Context getContextNullSafety() {
        if (getContext() != null) return getContext();
        if (getActivity() != null) return getActivity();
        if (contextNullSafe != null) return contextNullSafe;
        if (getView() != null && getView().getContext() != null) return getView().getContext();
        if (requireContext() != null) return requireContext();
        if (requireActivity() != null) return requireActivity();
        if (requireView() != null && requireView().getContext() != null)
            return requireView().getContext();

        return null;
    }


    private void buildLocationRequest() {
        if(locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(50f);
            locationRequest.setInterval(15000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    private void buildLocationCallback() {
        if(locationCallback == null){
            locationCallback = new LocationCallback() {
                //Ctrl+O
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 16f));

                    if (firstTime) {
                        previousLocation = currentLocation = locationResult.getLastLocation();
                        firstTime = false;
                    } else {
                        previousLocation = currentLocation;
                        currentLocation = locationResult.getLastLocation();
                    }
                    if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE) {
                        loadAvailableWorkers();
                    } else {
                        //Do nothing
                    }
                }
            };
        }
    }


    private void updateLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContextNullSafety());
        if (ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }


    private void loadAvailableWorkers() {

        if(ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {

                    //Load worker in the city
                    Geocoder geocoder = new Geocoder(getContextNullSafety(), Locale.getDefault());
                    List<Address> addressList;
                    try{
                        addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if(addressList.size() > 0)
                            cityName = addressList.get(0).getLocality();
                        if(!TextUtils.isEmpty(cityName)) {

                            //Try dason kung magwork ang ID nga query
                            //Query
                            DatabaseReference worker_location_ref = FirebaseDatabase.getInstance()
                                    .getReference(Common.WORKER_LOCATION_REFERENCE)
                                    .child(cityName);
                            GeoFire gf = new GeoFire(worker_location_ref);
                            GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.getLatitude(),
                                    location.getLongitude()), distance);

                            geoQuery.removeAllListeners();

                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    if(key.equals(workerId)){
                                        Common.workersFound.add(new WorkerGeoModel(key, location));
                                        /*
                                        if(!Common.workersFound.contains(workerId)){
                                            Common.workersFound.add(new WorkerGeoModel(key, location));
                                        }else if(Common.workersFound.contains(workerId)){
                                            Common.workersFound.clear();
                                            Common.workersFound.add(new WorkerGeoModel(key, location));
                                        }
                                         */
                                    }
                                }

                                @Override
                                public void onKeyExited(String key) {

                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {

                                }

                                @Override
                                public void onGeoQueryReady() {


                                    //Listen to worker within range
                                    worker_location_ref.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                            //Have new worker
                                            GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                                            GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                                    geoQueryModel.getL().get(1));
                                            WorkerGeoModel workerGeoModel = new WorkerGeoModel(snapshot.getKey(), geoLocation);
                                            Location newWorkerLocation = new Location("");
                                            newWorkerLocation.setLatitude(geoLocation.latitude);
                                            newWorkerLocation.setLongitude(geoLocation.longitude);
                                            float newDistance = location.distanceTo(newWorkerLocation) / 100;
                                            if (newDistance <= LIMIT_RANGE) {
                                                addWorkerMarker();
                                            }
                                        }

                                        @Override
                                        public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });

                                    if (distance <= LIMIT_RANGE) {
                                        addWorkerMarker();
                                    }

                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });

                            /*
                            //Listen to worker within range
                            worker_location_ref.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    //Have new worker

                                    GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                                    GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                            geoQueryModel.getL().get(1));
                                    WorkerGeoModel workerGeoModel = new WorkerGeoModel(snapshot.getKey(), geoLocation);
                                    Location newWorkerLocation = new Location("");
                                    newWorkerLocation.setLatitude(geoLocation.latitude);
                                    newWorkerLocation.setLongitude(geoLocation.longitude);
                                    float newDistance = location.distanceTo(newWorkerLocation) / 1000; // in km
                                    if (newDistance <= LIMIT_RANGE) {
                                        findWorkerByKey(workerGeoModel); // if worker in range, add to map
                                    }
                                }

                                @Override
                                public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                             */
                        } else {
                            Snackbar.make(getView(),getString(R.string.city_name_empty), Snackbar.LENGTH_LONG).show();
                        }

                    }catch (IOException e){
                        e.printStackTrace();
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }

                });

    }

    private void addWorkerMarker() {
        if(Common.workersFound.size() > 0){
            Observable.fromIterable(Common.workersFound)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(workerGeoModel -> {
                        //On next
                        findWorkerByKey(workerGeoModel);
                    }, throwable -> {
                        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }, () -> {});
        } else {
            //Snackbar.make(getView(), getString(R.string.workers_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void findWorkerByKey(WorkerGeoModel workerGeoModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.WORKER_INFO_REFERENCE)
                .child(workerGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()){
                            try {
                                workerGeoModel.setWorkerInfoModel(snapshot.getValue(WorkerInfoModel.class));
                                iFirebaseWorkerInfoListener.onWorkerInfoLoadSuccess(workerGeoModel);
                            }catch (Exception e){
                                //Toast.makeText(getContext(), "Error retrieving Data", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            iFirebaseFailedListener.onFirebaseLoadFailed(getString(R.string.not_found_key) + workerGeoModel.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        iFirebaseFailedListener.onFirebaseLoadFailed(error.getMessage());
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        //Request permission to add current location

        Dexter.withContext(getContextNullSafety())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                if (ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                                        (getContextNullSafety(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return false;
                                }
                                fusedLocationProviderClient.getLastLocation()
                                        .addOnFailureListener(e -> Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT)
                                                .show())
                                        .addOnSuccessListener(location -> {

                                            LatLng userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f));
                                        });
                                return true;
                            }
                        });

                        //Layout Button
                        View locationButton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

                        //Right Bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0,0,0,350);

                        //Update Location
                        buildLocationRequest();
                        buildLocationCallback();
                        updateLocation();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(getView(), permissionDeniedResponse.getPermissionName()+ " need enable",
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();

        mMap.getUiSettings().setZoomControlsEnabled(true);
        try{
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContextNullSafety(),
                    R.raw.uber_maps_style));
            if(!success)
                Snackbar.make(getView(), "Load map style failed", Snackbar.LENGTH_SHORT).show();
        }catch (Exception e){
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make(getView(),message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onWorkerInfoLoadSuccess(WorkerGeoModel workerGeoModel) {
        if (Common.markerList.containsKey(workerGeoModel.getKey())) {
            Common.markerList.clear();
            mMap.clear();
        }
        Common.markerList.put(workerGeoModel.getKey(),
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(workerGeoModel.getGeoLocation().latitude,
                                workerGeoModel.getGeoLocation().longitude))
                        .flat(true)
                        .title(Common.buildName(workerGeoModel.getWorkerInfoModel().getFullName(),
                                workerGeoModel.getWorkerInfoModel().getEmail()))
                        .snippet(workerGeoModel.getWorkerInfoModel().getJob())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))));
        workerPosition = new LatLng(workerGeoModel.getGeoLocation().latitude,
                workerGeoModel.getGeoLocation().longitude);
        getDistance(newPosition, workerPosition);
        if(!TextUtils.isEmpty(cityName)){
            DatabaseReference workerLocation = FirebaseDatabase.getInstance()
                    .getReference(Common.WORKER_LOCATION_REFERENCE)
                    .child(cityName)
                    .child(workerGeoModel.getKey());
            workerLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.hasChildren()){
                        if(Common.markerList.get(workerGeoModel.getKey()) != null)
                            Common.markerList.get(workerGeoModel.getKey()).remove(); // Remove Marker
                        Common.markerList.remove(workerGeoModel.getKey()); // Remove marker info from hash map
                        workerLocation.removeEventListener(this); // Remove event Listener
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getDistance(LatLng newPosition, LatLng workerPosition) {

        //No regards to traffic and route since unavailable ang routing nga feature kay bayad
        String wDist;

        Location l1=new Location("One");
        l1.setLatitude(newPosition.latitude);
        l1.setLongitude(newPosition.longitude);

        Location l2=new Location("Two");
        l2.setLatitude(workerPosition.latitude);
        l2.setLongitude(workerPosition.longitude);

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double distance=l1.distanceTo(l2);
        wDist = decimalFormat.format(distance);
        int speedIs1000MeterPerHour = 1000;
        double etaDriveTimeMinutes = distance / speedIs1000MeterPerHour;
        String wEta = decimalFormat.format(etaDriveTimeMinutes);
        String dist= wDist+" M";

        if(distance>1000.0f)
        {
            distance=distance/1000.0f;
            wDist = decimalFormat.format(distance);
            dist=wDist+" KM";
        }

        workerDistance.setText(dist);
        etaWorker.setText("ETA: " + wEta + " min.");

        //Polyline Distance
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(newPosition, workerPosition);
        polylineDistance = mMap.addPolyline(polylineOptions);
        polylineDistance.setColor(getContextNullSafety().getResources().getColor(R.color.colorPrimary));
    }
}
