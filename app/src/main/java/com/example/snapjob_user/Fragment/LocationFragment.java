package com.example.snapjob_user.Fragment;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import com.example.snapjob_user.Callback.IFirebaseFailedListener;
import com.example.snapjob_user.Callback.IFirebaseWorkerInfoListener;
import com.example.snapjob_user.Common.Common;
import com.example.snapjob_user.HomePage;
import com.example.snapjob_user.Model.User;
import com.example.snapjob_user.Model.Worker;
import com.example.snapjob_user.Model.WorkerGeoModel;
import com.example.snapjob_user.Model.WorkerInfoModel;
import com.example.snapjob_user.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LocationFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, IFirebaseFailedListener, IFirebaseWorkerInfoListener {

    String workerFullName, workerId, transactionStat, userName, transactionId, phoneNumber, workerPhoneNum, workerJob, workerJobDetail, workerNumDetail;
    private String userAddress;
    private Button btn_confirm_pickup_spot;
    private EditText transDescriptionTxt;
    private TextView workerNameDetail, workerJobDet, workerPhoneNumDetail;
    private CardView confirm_pickup_layout, worker_detail_layout;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userNameReference, transactionReference, workerPhoneReference;
    private Context contextNullSafe;

    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    String strDate = sdf.format(c.getTime());

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    //Get User Id
    private FirebaseUser user;
    private String userId;

    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Online System
    DatabaseReference onlineRef, currentUserRef, userLocation;
    GeoFire geofire;
    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
            if (snapshot.exists() && currentUserRef != null) {
                currentUserRef.onDisconnect().removeValue();
            }
        }

        @Override
        public void onCancelled(@NonNull @NotNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    //Load Driver
    private double distance = 1.0; // default in km
    private static final double LIMIT_RANGE = 100.0; // km
    private Location previousLocation, currentLocation; // Use to calculate distance

    private boolean firstTime = true;

    //Listener
    IFirebaseWorkerInfoListener iFirebaseWorkerInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;
    private String cityName;

    @Override
    public void onDestroy() {
        /*
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        geofire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);
         */
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment, container, false);
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

        btn_confirm_pickup_spot = (Button) view.findViewById(R.id.btn_confirm_pickup_spot);
        confirm_pickup_layout = (CardView) view.findViewById(R.id.confirm_pickup_layout);
        worker_detail_layout = (CardView) view.findViewById(R.id.worker_detail_layout);
        transDescriptionTxt = (EditText) view.findViewById(R.id.transDescriptionTxt);
        workerNameDetail = (TextView) view.findViewById(R.id.workerName_detail);
        workerJobDet = (TextView) view.findViewById(R.id.workerJob_detail);
        workerPhoneNumDetail = (TextView) view.findViewById(R.id.workerNum_detail);

        btn_confirm_pickup_spot.setOnClickListener(this);

        init();


        return view;
    }

    private void init() {

        iFirebaseFailedListener = this;
        iFirebaseWorkerInfoListener = this;

        //Add location to database
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");

        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
        }

        //registerOnline System ang gindugang ko kag gindelete ang onStop
        buildLocationRequest();
        buildLocationCallBack();
        updateLocation();

        if(transactionStat == null){

        }else if(transactionStat.equals("Ongoing")){
            loadAvailableWorkers();
        }
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
        if(locationRequest == null){
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(10f);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            /*
            locationRequest = LocationRequest.create()
                    .setSmallestDisplacement(10f)
                    .setInterval(5000)
                    .setFastestInterval(3000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
             */
        }
    }

    private void buildLocationCallBack() {
        if(locationCallback == null){
            locationCallback = new LocationCallback() {
                //Ctrl+O
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                    Geocoder geocoder = new Geocoder(getContextNullSafety(), Locale.getDefault());
                    List<Address> addressList;
                    try {
                        addressList = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(),
                                locationResult.getLastLocation().getLongitude(), 1);
                        String cityName = addressList.get(0).getLocality();
                        userAddress = addressList.get(0).getAddressLine(0);

                        userLocation = FirebaseDatabase.getInstance().getReference(Common.USER_LOCATION_REFERENCE).child(cityName);
                        currentUserRef = userLocation.child(userId);
                        geofire = new GeoFire(userLocation);

                        //Update Location
                        geofire.setLocation(userId, new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude()),
                                (key, error) -> {
                                    if(error != null){
                                        Snackbar.make(mapFragment.getView(),error.getMessage(),Snackbar.LENGTH_LONG)
                                                .show();
                                    } else {
                                    }
                                });

                    } catch (IOException e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }

                    //If user has change location, calculate and load worker again
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_confirm_pickup_spot:
                transaction();
                break;
        }
    }

    private void transaction() {
        String description = transDescriptionTxt.getText().toString().trim();

        transactionReference = FirebaseDatabase.getInstance().getReference("Transactions");
        transactionId = transactionReference.push().getKey();

        if(description.isEmpty()){
            transDescriptionTxt.setError("Please enter Description");
            transDescriptionTxt.requestFocus();
            return;
        } else {
            confirm_pickup_layout.setVisibility(View.GONE);
        }

        transactionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                HashMap transaction = new HashMap<>();
                transaction.put("transId", transactionId);
                transaction.put("userId", userId);
                transaction.put("userName", userName);
                transaction.put("workerId", workerId);
                transaction.put("workerName", workerFullName);
                transaction.put("userPhoneNumber", phoneNumber);
                transaction.put("address", userAddress);
                transaction.put("transactionStatus", "Waiting");
                transaction.put("workerArrived", "No");
                transaction.put("transactionDescription", description);
                transaction.put("transactionDate", strDate);
                transaction.put("jobType", workerJob);

                transactionReference.child(transactionId).setValue(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Request has been sent!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getContext(), HomePage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().finish();
                        getActivity().overridePendingTransition(0,0);
                        startActivity(intent);
                        getActivity().overridePendingTransition(0,0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                //DO NOTHING
            }
        });
    }

    private void loadAvailableWorkers() {

        if (ActivityCompat.checkSelfPermission(getContextNullSafety(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
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
                                    if (distance <= LIMIT_RANGE) {
                                        distance++;
                                        loadAvailableWorkers(); //Continue search in new distance
                                    } else {
                                        distance = 1.0;
                                        addWorkerMarker();
                                    }

                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                    Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });

                            //Listen to new worker within range
                            /*
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
            //Snackbar.make(mapFragment.getView(), getString(R.string.workers_not_found), Snackbar.LENGTH_SHORT).show();
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
                            workerGeoModel.setWorkerInfoModel(snapshot.getValue(WorkerInfoModel.class));
                            iFirebaseWorkerInfoListener.onWorkerInfoLoadSuccess(workerGeoModel);
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

                        /*
                        //Right Bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0,0,0,250);
                         */

                        //UpdateLocation
                        buildLocationRequest();
                        buildLocationCallBack();
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
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),
                    R.raw.uber_maps_style));
            if(!success)
                Snackbar.make(getView(), "Load map style failed", Snackbar.LENGTH_SHORT).show();
        }catch (Exception e){
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make(mapFragment.getView(),message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onWorkerInfoLoadSuccess(WorkerGeoModel workerGeoModel) {
        //If already have marker with this key, doesn't set again
        if(!Common.markerList.containsKey(workerGeoModel.getKey())){
            Common.markerList.put(workerGeoModel.getKey(),
                    mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(workerGeoModel.getGeoLocation().latitude,
                            workerGeoModel.getGeoLocation().longitude))
                    .flat(true)
                    .title(Common.buildName(workerGeoModel.getWorkerInfoModel().getFullName(),
                            workerGeoModel.getWorkerInfoModel().getEmail()))
                    .snippet(workerGeoModel.getWorkerInfoModel().getJob())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))));
        }
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
}