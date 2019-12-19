package pages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geobox_mapfinal.R;
import com.google.android.gms.maps.MapFragment;

import java.util.Locale;

import location.LocationHelper;
import location.database.CompassPref;
import location.model.LocationData;
import location.model.Sunshine;
import sensor.SensorListener;
import utils.Utility;
import utils.util;
import view.AccelerometerView;
import view.CompassView2;
import view.inclinoView2;

import static utils.Utility.getDirectionText;

public class CompassFragmentt extends BaseFragment implements SensorListener.OnValueChangedListener,
        LocationHelper.LocationDataChangeListener, MappFragment.OnFragmentInteractionListener {
    public static final String TAG = "CompassFragmentt";
    private static final int REQUEST_ENABLE_GPS = 1002;
    private TextView mTxtAddress;
    private TextView mTxtSunrise, mTxtSunset;
    private TextView mTxtPitch, mTxtRoll;
    private TextView mTxtLonLat, mTxtAltitude;
    private TextView mTxtPressure, mTxtHumidity, mTxtTemp;
    private ImageView mImgWeather;
    private LocationHelper mLocationHelper;
    private CompassView2 mCompassView;
    private AccelerometerView mAccelerometerView;
    private SensorListener mSensorListener;
    private CompassPref mCompassPref;
    private LinearLayout location_button;
    private inclinoView2 minclinoview;
    private String longitude=new String();
    private String latitude=new String();

    public static CompassFragmentt newInstance() {

        Bundle args = new Bundle();

        CompassFragmentt fragment = new CompassFragmentt();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView();
        this.getView().setBackgroundColor(getResources().getColor(R.color.compass_background_color));

        mLocationHelper = new LocationHelper(this);
        mLocationHelper.setLocationValueListener(this);
        mLocationHelper.onCreate();
        mSensorListener = new SensorListener(getContext());
        mSensorListener.setOnValueChangedListener(this);
        if (!Utility.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "No hay acceso a internet", Toast.LENGTH_SHORT).show();
        } else {
            LocationManager manager = (LocationManager) getContext()
                    .getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }
        }

        onUpdateLocationData(null);
    }

    private void bindView() {
        mTxtAddress = (TextView) findViewById(R.id.txt_address);
        mTxtAddress.setSelected(true);

        mTxtSunrise = (TextView) findViewById(R.id.txt_sunrise);
        mTxtSunset = (TextView) findViewById(R.id.txt_sunset);

        mTxtLonLat = (TextView) findViewById(R.id.txt_lon_lat);
        mTxtAltitude = (TextView) findViewById(R.id.txt_altitude);
        //eventoclick en compas
        mCompassView = (CompassView2) findViewById(R.id.compass_view);
        mCompassView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("----->CLICK EN COMPAS");

            }
        });

        mAccelerometerView = (AccelerometerView) findViewById(R.id.accelerometer_view);
        mTxtPressure = (TextView) findViewById(R.id.txt_pressure);
       mTxtHumidity = (TextView) findViewById(R.id.txt_humidity);
        mImgWeather = (ImageView) findViewById(R.id.img_weather);
        mTxtTemp = (TextView) findViewById(R.id.txt_temp);

        //botones
        location_button = (LinearLayout) findViewById(R.id.location_button);
        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MappFragment mapg=new MappFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_view, mapg);
                transaction.addToBackStack(null);
                transaction.commit();
                System.out.println("----->CLICK EN UBICACION");

                System.out.println("UBICACION DESDE TEXTVIEW: "+mTxtLonLat.getText().toString());
                System.out.println("UBICACION DESDE GPS:  Longitud: "+longitude
                        +" Latitud: "+latitude);
            }
        });
//inclinometro
     //minclinoview= (inclinoView2) findViewById(R.id.inclino_view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSensorListener != null) {
            mSensorListener.start();
        }
    }

    @Override
    public void onStop() {
        if (mSensorListener != null) {
            mSensorListener.stop();
        }
        super.onStop();

    }


    @Override
    public void onRotationChanged(float azimuth, float roll, float pitch) {
        String str = ((int) azimuth) + "° " + getDirectionText(azimuth);
        mCompassView.getSensorValue().setRotation(azimuth, roll, pitch);

        mAccelerometerView.getSensorValue().setRotation(azimuth, roll, pitch);
//        minclinoview.getSensorValue().setRotation(azimuth,roll,pitch);
    }

    @Override
    public void onMagneticFieldChanged(float value) {
        mCompassView.getSensorValue().setMagneticField(value);
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_compasss;
    }

    //https://stackoverflow.com/questions/39336461/how-can-i-enable-or-disable-the-gps-programmatically-on-android-6-x
    private void buildAlertMessageNoGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Tu gps está desactivado, quieres activarlo?")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, REQUEST_ENABLE_GPS);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_GPS:
                mLocationHelper.onCreate();
                break;
        }
    }


    @Override
    public void onUpdateLocationData(@Nullable LocationData locationData) {
        if (locationData == null) {
            mCompassPref= new CompassPref(getContext());
            locationData = mCompassPref.getLastedLocationData();
        }
        if (locationData != null) {
            util.setLocationData(locationData);
            //sunshine
            Sunshine sunshine = locationData.getSunshine();
            if (sunshine != null) {
                mTxtSunrise.setText(sunshine.getReadableSunriseTime());
                mTxtSunset.setText(sunshine.getReadableSunsetTime());

                //weather
                int resId = Utility.getIconResourceForWeatherCondition(locationData.getId());
                if (resId != -1) mImgWeather.setImageResource(resId);
                mTxtPressure.setText(String.format(Locale.US, "%s hPa", locationData.getPressure()));
                mTxtHumidity.setText(String.format(Locale.US, "%s %%", locationData.getHumidity()));
                mTxtTemp.setText(Utility.formatTemperature(getContext(), locationData.getTemp()));

                //address
                mTxtAddress.setText(locationData.getAddressLine());

                //location
                float longitude = (float) locationData.getLongitude();

                float latitude = (float) locationData.getLatitude();

                String lonStr = Utility.formatDms(longitude) + " " + getDirectionText(longitude);
                this.longitude=""+lonStr;
                String latStr = Utility.formatDms(latitude) + " " + getDirectionText(latitude);
                this.latitude=latStr+"";
                mTxtLonLat.setText(String.format("%s\n%s", lonStr, latStr));

                //altitude
                double altitude = locationData.getAltitude();
                mTxtAltitude.setText(String.format(Locale.US, "%d m", (long) altitude));
            }
        }
    }

    public void onFragmentInteraction(Uri uri) {

    }
}
