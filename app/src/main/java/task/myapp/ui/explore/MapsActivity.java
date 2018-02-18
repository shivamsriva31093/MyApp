package task.myapp.ui.explore;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ramotion.garlandview.TailLayoutManager;
import com.ramotion.garlandview.TailRecyclerView;
import com.ramotion.garlandview.TailSnapHelper;
import com.ramotion.garlandview.header.HeaderTransformer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.bloco.faker.Faker;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import task.myapp.AppClass;
import task.myapp.R;
import task.myapp.navigation.BaseFragmentActivity;
import task.myapp.navigation.NavigationModel;
import task.myapp.ui.explore.inner.InnerData;
import task.myapp.ui.explore.inner.InnerItem;
import task.myapp.ui.explore.outer.OuterAdapter;
import task.myapp.ui.explore.outer.OuterItem;

import static task.myapp.Config.RC_LOCATION;
import static task.myapp.utils.LogUtils.LOGD;
import static task.myapp.utils.LogUtils.LOGE;
import static task.myapp.utils.LogUtils.LOGI;
import static task.myapp.utils.LogUtils.makeLogTag;

public class MapsActivity extends BaseFragmentActivity implements OnMapReadyCallback,
        EasyPermissions.PermissionCallbacks, AppClass.FakerReadyListener, OuterItem.BottomSheetStateListener {

    private static final String TAG = makeLogTag(MapsActivity.class);
    private static final int DEFAULT_ZOOM = 15;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final int REQUEST_CHECK_SETTINGS = 29;
    private final static int OUTER_COUNT = 10;
    private final static int INNER_COUNT = 20;
    @BindView(R.id.bottomsheet_container)
    CardView bottomSheetLayout;
    private int currentBottomSheetBehaviour;
    private BottomSheetBehavior mBottomSheetBehavior;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 8 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 1000; /* 2 sec */
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private CameraPosition mCameraPosition;
    private String[] perms = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private LatLng mDefaultLocation = new LatLng(34.25f, 0);


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null) {
                mLastKnownLocation = locationResult.getLastLocation();
                LOGD(TAG, "Retrieved current location." + mLastKnownLocation.toString());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                        .zoom(15)
                        .bearing(0)
                        .tilt(45)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                        .title("You are here.")
                );
                mFusedLocationProviderClient.removeLocationUpdates(this);

            } else {
                LOGD(TAG, "Current location is null. Using defaults.");
//                LOGE(TAG, "Exception: %s", task.getException());
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }

        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelfNavDrawerItem(NavigationModel.NavigationItemEnum.EXPLORE);
        setNavigationTitleId(R.string.title_activity_maps);
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        mFusedLocationProviderClient = new FusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initBottomSheet();

    }

    private void initBottomSheet() {
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                currentBottomSheetBehaviour = newState;
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        ((AppClass) getApplication()).addListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onFakerReady(final Faker faker) {
        Single.create((SingleOnSubscribe<List<List<InnerData>>>) e -> {
            final List<List<InnerData>> outerData = new ArrayList<>();
            for (int i = 0; i < OUTER_COUNT && !e.isDisposed(); i++) {
                final List<InnerData> innerData = new ArrayList<>();
                for (int j = 0; j < INNER_COUNT && !e.isDisposed(); j++) {
                    innerData.add(createInnerData(faker));
                }
                outerData.add(innerData);
            }

            if (!e.isDisposed()) {
                e.onSuccess(outerData);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::initRecyclerView);
    }

    private void initRecyclerView(List<List<InnerData>> data) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);

        final TailRecyclerView rv = (TailRecyclerView) findViewById(R.id.recycler_view);
        ((TailLayoutManager) rv.getLayoutManager()).setPageTransformer(new HeaderTransformer());
        rv.setAdapter(new OuterAdapter(data, this));

        new TailSnapHelper().attachToRecyclerView(rv);
    }

    private InnerData createInnerData(Faker faker) {
        return new InnerData(
                faker.book.title(),
                faker.name.name(),
                faker.address.city() + ", " + faker.address.stateAbbr(),
                faker.avatar.image(faker.internet.email(), "150x150", "jpg", "set1", "bg1"),
                faker.number.between(20, 50)
        );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnInnerItemClick(InnerItem item) {
        final InnerData itemData = item.getItemData();
        if (itemData == null) {
            return;
        }
/*
        DetailsActivity.start(this,
                item.getItemData().name, item.mAddress.getText().toString(),
                item.getItemData().avatarUrl, item.itemView, item.mAvatarBorder);*/
    }

    private void getDeviceLocation() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            updateLocationUI();
            getCurrentLocation();
            addMarkersToMap();
        } else {
            getLocationPermission();
        }

    }

    private void addMarkersToMap() {

    }

    @AfterPermissionGranted(RC_LOCATION)
    private void getCurrentLocation() {
        if (EasyPermissions.hasPermissions(this, perms)) {


            try {
                LOGI(TAG, "Changed location settings");
                mLocationRequest = new LocationRequest();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                builder.addLocationRequest(mLocationRequest);
                builder.setAlwaysShow(true);
                LocationSettingsRequest locationSettingsRequest = builder.build();

                SettingsClient settingsClient = LocationServices.getSettingsClient(this);
                settingsClient.checkLocationSettings(locationSettingsRequest).addOnCompleteListener(task -> {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        LOGI(TAG, "Changed location settings");
                        getLastKnownLocationResult();
                    } catch (ApiException e) {
                        e.printStackTrace();
                        switch (e.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) e;
                                    resolvable.startResolutionForResult(
                                            MapsActivity.this,
                                            REQUEST_CHECK_SETTINGS
                                    );
                                } catch (IntentSender.SendIntentException | ClassCastException e1) {
                                    LOGE(TAG, e.getMessage());
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                LOGE(TAG, "Unable to change location settings");
                        }
                    }
                });

            } catch (SecurityException ex) {
                LOGE(TAG, ex.getMessage());
            }
        } else {
            getLocationPermission();
        }
    }

    private void getLastKnownLocationResult() throws SecurityException {
        mFusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                locationCallback,
                Looper.myLooper()
        );

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_map));

            if (!success) {
                LOGE(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            LOGE(TAG, "Can't find style. Error: ", e);
        }*/
        getDeviceLocation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (EasyPermissions.hasPermissions(this, perms)) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            LOGE(TAG, e.getMessage());
        }
    }

    private void getLocationPermission() {
        EasyPermissions.requestPermissions(
                this,
                getString(R.string.location_rationale),
                RC_LOCATION,
                perms
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (data != null) {
                    final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
                    if (resultCode == Activity.RESULT_OK) {
                        getCurrentLocation();
                    }
                }
                break;

            case AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE:
                String yes = getString(R.string.yes);
                String no = getString(R.string.no);

                // Do something after user returned from app settings screen, like showing a Toast.
                Toast.makeText(
                        this,
                        getString(R.string.returned_from_app_settings_to_activity,
                                EasyPermissions.hasPermissions(this, perms) ? yes : no),
                        Toast.LENGTH_LONG)
                        .show();
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        LOGI(TAG, "Permissions granted.");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        LOGI(TAG, "Permissions denied.");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public int getCurrenBottomSheetState() {
        return currentBottomSheetBehaviour;
    }

    @Override
    public void setSheetState(int newState) {
        mBottomSheetBehavior.setState(newState);
    }

    @Override
    public void onBackPressed() {
        if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
