package com.sburato.navegacao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView;
    private LocationDisplay mLocationDisplay;
    private static final String PROVIDER = LocationManager.GPS_PROVIDER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapView);
    }

    @Override
    protected void onPause() {
        if (mMapView != null) mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) mMapView.dispose();
        super.onDestroy();
    }

    private void setupMap(double latitude, double longitude) {
        if (mMapView != null) {
            Basemap.Type basemapType = Basemap.Type.OPEN_STREET_MAP;
            int levelOfDetail = 20;
            ArcGISMap map = new ArcGISMap(basemapType, latitude, longitude, levelOfDetail);
            mMapView.setMap(map);
        }
    }

    private void setupGPS() {
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }
            checkSelfPermission();
        });
    }

    private void setupLocationDisplay() {
        mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
        mLocationDisplay.startAsync();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mLocationDisplay != null) mLocationDisplay.startAsync();
        } else {
            Toast.makeText(MainActivity.this, "Permissão Recusada", Toast.LENGTH_LONG).show();
        }
    }

    public void buscarCoordenadas(View view) {
        if (checkSelfPermission()) return;

        LocationManager mLocManager   = (LocationManager) getSystemService(MainActivity.this.LOCATION_SERVICE);
        LocationListener mLocListener = new MinhaLocalizacaoListener();

        mLocManager.requestLocationUpdates(PROVIDER, 0, 0, mLocListener);

        if (!mLocManager.isProviderEnabled(PROVIDER)) {
            Toast.makeText(this, "GPS desabilitado.", Toast.LENGTH_LONG).show();

            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(getApplicationContext(), "Para este aplicativo é necessário habilitar o GPS", Toast.LENGTH_LONG).show();

            return;
        }

        Location localAtual = mLocManager.getLastKnownLocation(PROVIDER);

        setupMap(localAtual.getLatitude(), localAtual.getLongitude());
        setupLocationDisplay();
        setupGPS();
    }

    private boolean checkSelfPermission() {
        int requestPermissionsCode = 2;

        String[] requestPermissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION };

        if (ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[0])
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[1])
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, requestPermissions,
                    requestPermissionsCode);

            return true;
        }

        return false;
    }
}