package com.example.xaimy.locator;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FloatingActionButton fab;
    private String location,DevIDNode,myDevId,updated_email,convertedLongi,getConvertedLati,savedDate,savedTime;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG1 = MainActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private DatabaseReference usersNode;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused TimeStamp Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 16;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused TimeStamp Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //Get Device ID

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        updated_email = preferences.getString("EmailID", "");
        //to save the value of email locally
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("savedDate",getDate());
        editor.putString("savedTime",getTime());
        editor.apply();
        savedDate = preferences.getString("savedDate", "");
        savedTime = preferences.getString("savedTime", "");
        //firebase work
        // get reference to 'users' node
        usersNode = FirebaseDatabase.getInstance().getReference("Users");
        /*childref.setValue("Calllist");*/
        // store app title to 'app_title' node
        FirebaseDatabase.getInstance().getReference("App Title").setValue("Registration");
        //google Maps functionality
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null)
        {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getDevId();

                try {
                    AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                    ab.setIcon(R.drawable.current_location);
                    ab.setTitle("Current Location");
                    ab.setMessage("Longitude : "+ mLastKnownLocation.getLatitude() + "\n" +"Latitude : "+ mLastKnownLocation.getLongitude());
                    ab.setCancelable(true);
                    ab.setNeutralButton("Send Location", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getDeviceLocation();
                            String userLocation = getCordinates();
                            String time = getTime();
                            String email = updated_email;
                            // Check for already existed userId
                            if (TextUtils.isEmpty(myDevId))
                            {
                                createUser(email,convertedLongi,getConvertedLati);
                            } else
                            {
                                updateUser(email,convertedLongi,getConvertedLati,getDate(),getTime());
                            }
                            Toast.makeText(getApplicationContext(), "Location Sent", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                    ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
                    ab.create();
                    ab.show();
                    //function to be added
                }
                catch (Exception e)
                {
                    Log.e("Exception: %s", e.getMessage());
                }
            }
        });


        authListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }

    //Saves the state of the map when the activity is paused.
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.btn_signout)
        {
            signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        if (item.getItemId() == R.id.send_one_click)
        {
            //to be added
        }
        if (item.getItemId() == R.id.option_get_place)
        {
            showCurrentPlace();
        }
        if (item.getItemId() == R.id.send_via)
        {
            sendEmail();
            Toast.makeText(getApplicationContext(),"Opening Email Client..", Toast.LENGTH_LONG).show();
        }
        if (item.getItemId() == R.id.enquiry)
        {
            //function to be added

        }
        if(item.getItemId() == R.id.quick_send)
        {
            //function to be added
        }
        return true;
    }


    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My TimeStamp layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                final Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            location = "Current Location : \n" + "Longitude : "+ mLastKnownLocation.getLongitude() + " " +"\nLatitude : "+ mLastKnownLocation.getLatitude();
                            convertedLongi = ""+mLastKnownLocation.getLongitude();
                            getConvertedLati = ""+mLastKnownLocation.getLatitude();
                        } else
                            {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)
        {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }




    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null)
        {
            return;
        }

        if (mLocationPermissionGranted) {
            //get the best matches (places) from the current location
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");


            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }
    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }
    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    protected void sendEmail() {
        Log.i("Send email", "");

        String[] TO = {"amar.bisht220dtsfi@gmail.com"};
        String[] CC = {"amar.asb09@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Current TimeStamp");
        emailIntent.putExtra(Intent.EXTRA_TEXT,location);

        try
        {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            Log.i("Mail Sent....", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
    private void signOut()
    {
        auth.signOut();
    }

    private void updateUser(String email,String longi,String lati, String date, String time)
    {
        User user = new User(email);
        if (!TextUtils.isEmpty(longi))
        {
            usersNode.child(myDevId).child("Email").setValue(user);
            TimeStamp timeStamp = new TimeStamp(longi,lati);
            DatabaseReference locationNode = usersNode.child(myDevId).child("Locations");
            DatabaseReference dateNode = locationNode.child(getDate());
            DatabaseReference timeNode = dateNode.child(getTime());
            String tNode = locationNode.push().getKey();
            DatabaseReference Current = timeNode.child(tNode);
            Current.setValue(timeStamp);
        }




    }

    /**
     * Creating new user node under 'users'
     */
    private void createUser(String email,String longi, String lati )
    {
        User user = new User(email);
        TimeStamp timeStamp = new TimeStamp(longi,lati);
        usersNode.child(myDevId).setValue(user);
        DatabaseReference locationNode = usersNode.child(myDevId).child("Locations");
        DatabaseReference dateNode = locationNode.child(getDate());
        DatabaseReference timeNode = dateNode.child(getTime());
        timeNode.setValue(timeStamp);
    }

    private String getDate()
    {
        String dateStamp = (DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        return dateStamp;
    }
    private String getTime()
    {
        String timeStamp = (DateFormat.format("hh:mm:ss", new java.util.Date()).toString());
        return timeStamp;
    }
    private String getCordinates()
    {
        return "Longitude :"+String.valueOf(mLastKnownLocation.getLongitude())+ "  Latitude :" + String.valueOf(mLastKnownLocation.getLatitude());
    }

    @Override
    public void onBackPressed()
    {AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        ab.setIcon(R.drawable.exit);
        ab.setTitle("Exit Application?");  //To Set The Titile Like <Exit Application>
        ab.setMessage("Do You want To Exit Application?"); //To Show The Messege
        ab.setCancelable(false); // To Enable the outside click, i.e not closed by user on clicking outside the dialogue area.
        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //to exit the application without going to previos activitis
                moveTaskToBack(true);
                finish();
            }
        });
        ab.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                dialog.dismiss();

            }
        });
        ab.create();
        ab.show();
    }
    public void getDevId()
    {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        myDevId = deviceUuid.toString();
    }
}
