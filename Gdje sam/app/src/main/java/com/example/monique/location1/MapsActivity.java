package com.example.monique.location1;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.monique.location1.R.raw.sound;

// /*
public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        View.OnClickListener{

    private TextView tLocation;
    private Button bCamera;
    private Activity mActivity;

    //* ZA GOOGLE MAPU - PRIKAZ MAPE
    private GoogleMap mGoogleMap;
    private MapFragment mMapFragment;
    private GoogleMap.OnMapClickListener mCustomOnMapClickListener;

    // * ZA GOOGLE MAPU OZNAKA TRENUTNE LOKACIJE
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    //* ZA LOCATION OPĆENITO
    Location mLocation;
    private Geocoder mGeocoder;

    //* ZA DOPUŠTENJE
    private static final int PERMISSION_REQUEST_CODE = 1;

    //* ZA ZVUK
    private SoundPool mSoundPool;
    private boolean loaded = false;
    private int ID;

    // * ZA KAMERU
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    private  String locationPlace;

    // * ZA NOTIFIKACIJE
    public static final String MSG_KEY = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.initialize();
    }

    private void initialize() {

        mActivity = this;
        this.bCamera = (Button)findViewById(R.id.bCamera);
        this.bCamera.setOnClickListener(this);

        this.tLocation = (TextView) findViewById(R.id.tLocation);

        this.mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        this.mMapFragment.getMapAsync(this);
        this.mCustomOnMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapClickAction(latLng);
            }
        };
        if (Geocoder.isPresent()) {
            this.mGeocoder = new Geocoder(this);
        }
        //* Stvori GoogleAPI Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();

        //* Stvori LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        //* Zvuk
        this.mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        this.ID = mSoundPool.load(this, R.raw.sound, 1);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        UiSettings uiSettings = this.mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        this.mGoogleMap.setOnMapClickListener(this.mCustomOnMapClickListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //PERMISSION PROMJENE ZA ANDROID MARSHMELLOW UREĐAJE
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }
        // * Prikaz lokacije koju je dohvatio GoogleMaps sam bez dodatnog koda
        this.mGoogleMap.setMyLocationEnabled(true);
    }

    //* Klik na mapu
    private void mapClickAction(LatLng latLng) {
        //* Postavi novi marker
        MarkerOptions newMarkerOptions = new MarkerOptions();
        newMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
        newMarkerOptions.title("Moje mjesto");
        newMarkerOptions.snippet("Zauzimam ovo kao svoj teritorij!");
        newMarkerOptions.position(latLng);
        mGoogleMap.addMarker(newMarkerOptions);
        //* Reproduciraj zvuk
        if (loaded) {
            mSoundPool.play(ID, 1, 1, 1, 0, 1f);
        }
    }

    // * Prikaz Toast poruke
    private void displayToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        String locationText = "Unknown location";
        double lat = 0;
        double lng = 0;
        this.mLocation = location;
        if (null != this.mLocation) {
            lat = this.mLocation.getLatitude();
            lng = this.mLocation.getLongitude();
            locationText = "Geografska širina: " + lat + "\n" +
                    "Geografska dužina: " + lng;

            //* Prikaz kodom dohvaćene lokacije na Google mapi
            LatLng latLng = new LatLng(lat,lng);
            MarkerOptions mLocationMarker = new MarkerOptions()
                    .position(latLng)
                    .title("Ovdje sam")
                    .snippet("Wow! Konačno znam gdje se nalazim!");
            mGoogleMap.addMarker(mLocationMarker);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            if (null != this.mGeocoder) {
                try {
                    ArrayList<Address> nearbyAddresses =
                            (ArrayList<Address>) this.mGeocoder.getFromLocation(
                                    lat, lng, 1
                            );
                    if (null != nearbyAddresses && nearbyAddresses.size() > 0) {
                        Address myAddress = nearbyAddresses.get(0);
                        locationText += "\n" + "Država: " + myAddress.getCountryName()
                                + "\n" + "Mjesto: " + myAddress.getLocality() + "\n"
                                + "Adresa: " + myAddress.getAddressLine(0);
                        locationPlace = myAddress.getLocality();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.tLocation.setText(locationText);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayToast("Permission Granted, Now you can access location data.");
                } else {
                    displayToast("Permission Denied, You cannot access location data.");
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //PERMISSION PROMJENE ZA ANDROID MARSHMELLOW UREĐAJE
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    displayToast("Greška prilikom kreiranja datoteke za pohranu slike.");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
        }
    }

    private File createImageFile() throws IOException {
        // Ne radi u slučaju da uređaj nema vanjsku memoriju. Ukoliko ima, slika se sprema na
        //SD karticu s tim da se nekad ne pojavi odmah, nekad nestane, ali se onda pojavi i spremljena ih.
        //Ne znam u čemu je problem ako je napravljeno prema dokumentaciji na službenim stranicama.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = locationPlace + "_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    private void sendNotification() {
        String msgText = mCurrentPhotoPath;
        // We need a pending intent to kick off when the notification i pressed:
        //Intent notificationIntent = new Intent (Intent.ACTION_VIEW, Uri.parse(mCurrentPhotoPath));

        Intent notificationIntent = new Intent(android.content.Intent.ACTION_VIEW);
        notificationIntent.setDataAndType(Uri.parse(mCurrentPhotoPath),"image/*");
        //notificationIntent.setAction(Intent.ACTION_GET_CONTENT);

        notificationIntent.putExtra(MSG_KEY, msgText);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        // Compat builder should be used to create the notification when working
        // with api level 15 and lower
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(true)
                .setContentTitle("Spremljena je nova slika")
                .setContentText(msgText)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentIntent(notificationPendingIntent)
                .setLights(Color.BLUE, 2000, 1000)
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Notification notification = notificationBuilder.build();

        // When you have a notification, call notify on the notification manager object
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);

        // When the notification is sent, this activity is no longer neccessary
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        sendNotification();
    }
}

// */

