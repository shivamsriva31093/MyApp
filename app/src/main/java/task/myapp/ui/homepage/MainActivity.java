package task.myapp.ui.homepage;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.samples.apps.iosched.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import task.myapp.R;
import task.myapp.navigation.NavigationModel;
import task.myapp.widgets.BadgedBottomNavigationView;

import static task.myapp.utils.LogUtils.LOGE;
import static task.myapp.utils.LogUtils.LOGI;
import static task.myapp.utils.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity {

    private static final String TAG = makeLogTag(MainActivity.class);
    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));

    protected GeoDataClient mGeoDataClient;
    @BindView(R.id.autocomplete_places)
    AutoCompleteTextView mAutocompleteView;
    @BindView(R.id.bottom_navigation)
    BadgedBottomNavigationView navigationView;
    private PlacesAutocompleteAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelfNavDrawerItem(NavigationModel.NavigationItemEnum.HOME);
        setNavigationTitleId(R.string.title_activity_homepage);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        initUiComponents(savedInstanceState);
    }

    private void initUiComponents(Bundle savedInstanceState) {
/*        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);*/
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlacesAutocompleteAdapter(this, mGeoDataClient, BOUNDS_INDIA, null);
        mAutocompleteView.setAdapter(mAdapter);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            LOGI(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */
            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            LOGI(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data Client query that shows the first place result in
     * the details view on screen.
     */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = task -> {
                try {
                    PlaceBufferResponse places = task.getResult();

                    // Get the Place object from the buffer.
                    final Place place = places.get(0);

                    // Display the third party attributions if set.
                    final CharSequence thirdPartyAttribution = places.getAttributions();

                    LOGI(TAG, "Place details received: " + place.getName());

                    places.release();
                } catch (RuntimeRemoteException e) {
                    // Request did not complete successfully
                   LOGE(TAG, "Place query did not complete.", e);
                    return;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
