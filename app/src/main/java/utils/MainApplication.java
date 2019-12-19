
package utils;

import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.geobox_mapfinal.BuildConfig;
import com.example.geobox_mapfinal.R;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.joshdholtz.sentry.Sentry;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Field;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AccountUtil;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.NGException;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.GISApplication;
import com.nextgis.maplibui.mapui.LayerFactoryUI;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.maplibui.mapui.TrackLayerUI;
import com.nextgis.maplibui.mapui.VectorLayerUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import activities.SettingsActivity;

import static com.nextgis.maplib.util.Constants.DEBUG_MODE;
import static com.nextgis.maplib.util.Constants.MAP_EXT;
import static com.nextgis.maplib.util.GeoConstants.TMSTYPE_OSM;
import static com.nextgis.maplibui.fragment.NGWSettingsFragment.setAccountSyncEnabled;
import static utils.AppSettingsConstants.AUTHORITY;
import static utils.AppSettingsConstants.KEY_PREF_APP_VERSION;
import static utils.AppSettingsConstants.KEY_PREF_GA;


/**
 * Main application class
 * The initial layers create here. Also upgrade db from previous version is here too.
 */
public class MainApplication extends GISApplication
{
    public static final String LAYER_OSM = "osm";
    public static final String LAYER_A = "vector_a";
    public static final String LAYER_B = "vector_b";
    public static final String LAYER_C = "vector_c";
    public static final String LAYER_TRACKS = "tracks";

    private Tracker mTracker;

    @Override
    public void onCreate() {
        Sentry.init(this, BuildConfig.SENTRY_DSN);
//        Sentry.captureMessage("NGM2 Sentry is init.", Sentry.SentryEventLevel.DEBUG);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        GoogleAnalytics.getInstance(this).setAppOptOut(!mSharedPreferences.getBoolean(KEY_PREF_GA, true));
        GoogleAnalytics.getInstance(this).setDryRun(DEBUG_MODE);
        getTracker();
        setExceptionHandler();

        super.onCreate();
        updateFromOldVersion();
    }

    private void setExceptionHandler() {
        ExceptionReporter handler = new ExceptionReporter(getTracker(), Thread.getDefaultUncaughtExceptionHandler(), this);
        StandardExceptionParser exceptionParser =
                new StandardExceptionParser(getApplicationContext(), null) {
                    @Override
                    public String getDescription(String threadName, Throwable t) {
                        return "{" + threadName + "} " + Log.getStackTraceString(t);
                    }
                };

        handler.setExceptionParser(exceptionParser);
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    public synchronized Tracker getTracker() {
        if (mTracker == null)
            mTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.app_tracker);

        return mTracker;
    }

    @Override
    public void sendScreen(String name) {
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public String getAccountsType() {
        return Constants.NGW_ACCOUNT_TYPE;
    }

    @Override
    public void sendEvent(String category, String action, String label) {
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label);

        getTracker().send(event.build());
    }

    private void updateFromOldVersion() {
        try {
            int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            int savedVersionCode = mSharedPreferences.getInt(KEY_PREF_APP_VERSION, 0);

            switch (savedVersionCode) {
                case 0:
                    int source;
                    if (mSharedPreferences.contains(SettingsConstants.KEY_PREF_LOCATION_SOURCE)) {
                        try {
                            source = mSharedPreferences.getInt(SettingsConstants.KEY_PREF_LOCATION_SOURCE, 3);
                        } catch (ClassCastException e) {
                            source = 3;
                        }
                        mSharedPreferences.edit()
                                .remove(SettingsConstants.KEY_PREF_LOCATION_SOURCE)
                                .remove(SettingsConstants.KEY_PREF_LOCATION_SOURCE + "_str")
                                .putString(SettingsConstants.KEY_PREF_LOCATION_SOURCE, source + "").apply();
                    }
                    if (mSharedPreferences.contains(SettingsConstants.KEY_PREF_TRACKS_SOURCE)) {
                        try {
                            source = mSharedPreferences.getInt(SettingsConstants.KEY_PREF_TRACKS_SOURCE, 1);
                        } catch (ClassCastException e) {
                            source = 3;
                        }
                        mSharedPreferences.edit()
                                .remove(SettingsConstants.KEY_PREF_TRACKS_SOURCE)
                                .remove(SettingsConstants.KEY_PREF_TRACKS_SOURCE + "_str")
                                .putString(SettingsConstants.KEY_PREF_TRACKS_SOURCE, source + "").apply();
                    }
                case 13:
                case 14:
                case 15:
                    mSharedPreferences.edit().remove(SettingsConstantsUI.KEY_PREF_SHOW_STATUS_PANEL)
                            .remove(SettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int")
                            .remove(SettingsConstantsUI.KEY_PREF_COORD_FORMAT).apply();
                default:
                    break;
            }

            if (savedVersionCode < 44) {
                if (!AccountUtil.isProUser(this)) {
                    if (isAccountManagerValid())
                        for (final Account account : mAccountManager.getAccountsByType(getAccountsType()))
                            setAccountSyncEnabled(account, getAuthority(), false);

                    for (int i = 0; i < mMap.getLayerCount(); i++) {
                        ILayer layer = mMap.getLayer(i);
                        if (layer instanceof NGWVectorLayer) {
                            NGWVectorLayer ngwLayer = (NGWVectorLayer) layer;
                            ngwLayer.setSyncType(Constants.SYNC_NONE);
                            ngwLayer.save();
                        }
                    }
                }
            }

            if(savedVersionCode < currentVersionCode) {
                mSharedPreferences.edit().putInt(KEY_PREF_APP_VERSION, currentVersionCode).apply();
            }
        } catch (PackageManager.NameNotFoundException ignored) { }
    }

    @Override
    public MapBase getMap()
    {
        if (null != mMap) {
            return mMap;
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File defaultPath = getExternalFilesDir(SettingsConstants.KEY_PREF_MAP);
        if (defaultPath == null) {
            defaultPath = new File(getFilesDir(), SettingsConstants.KEY_PREF_MAP);
        }

        String mapPath = mSharedPreferences.getString(SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
        String mapName = mSharedPreferences.getString(SettingsConstantsUI.KEY_PREF_MAP_NAME, "default");

        File mapFullPath = new File(mapPath, mapName + MAP_EXT);

        final Bitmap bkBitmap = getMapBackground();
        mMap = new MapDrawable(bkBitmap, this, mapFullPath, new LayerFactoryUI());
        mMap.setName(mapName);
        mMap.load();

        checkTracksLayerExist();

        return mMap;
    }


    protected void checkTracksLayerExist()
    {
        List<ILayer> tracks = new ArrayList<>();
        LayerGroup.getLayersByType(mMap, Constants.LAYERTYPE_TRACKS, tracks);
        if (tracks.isEmpty()) {
            String trackLayerName = getString(R.string.tracks);
            TrackLayerUI trackLayer =
                    new TrackLayerUI(getApplicationContext(), mMap.createLayerStorage(LAYER_TRACKS));
            trackLayer.setName(trackLayerName);
            trackLayer.setVisible(true);
            mMap.addLayer(trackLayer);
            mMap.save();
        }
    }


    @Override
    public String getAuthority()
    {
        return AUTHORITY;
    }

    @Override
    public void showSettings(String settings)
    {
        if(TextUtils.isEmpty(settings)) {
            settings = SettingsConstantsUI.ACTION_PREFS_GENERAL;
        }

        switch (settings) {
            case SettingsConstantsUI.ACTION_PREFS_GENERAL:
            case SettingsConstantsUI.ACTION_PREFS_LOCATION:
            case SettingsConstantsUI.ACTION_PREFS_TRACKING:
                break;
            default:
                return;
        }

        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setAction(settings);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onFirstRun()
    {
        initBaseLayers();
    }


    public void initBaseLayers() {
        if (mMap.getLayerByPathName(LAYER_OSM) == null) {
            //add OpenStreetMap layer
            String layerName = getString(R.string.osm);
            String layerURL = SettingsConstantsUI.OSM_URL;
            final RemoteTMSLayerUI layer = new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage(LAYER_OSM));
            layer.setName(layerName);
            layer.setURL(layerURL);
            layer.setTMSType(TMSTYPE_OSM);
            layer.setVisible(true);
            layer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
            layer.setMaxZoom(19);

            mMap.addLayer(layer);
            mMap.moveLayer(0, layer);

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        layer.fillFromZip(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mapnik), null);
                    } catch (IOException | NGException | RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // create empty layers for first experimental editing
        List<Field> fields = new ArrayList<>(2);
        fields.add(new Field(GeoConstants.FTInteger, "FID", "FID"));
        fields.add(new Field(GeoConstants.FTString, "TEXT", "TEXT"));

        if (mMap.getLayerByPathName(LAYER_A) == null)
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.points_for_edit), LAYER_A, GeoConstants.GTPoint, fields));
        if (mMap.getLayerByPathName(LAYER_B) == null)
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.lines_for_edit), LAYER_B, GeoConstants.GTLineString, fields));
        if (mMap.getLayerByPathName(LAYER_C) == null)
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.polygons_for_edit), LAYER_C, GeoConstants.GTPolygon, fields));

        mMap.save();
    }


    public VectorLayer createEmptyVectorLayer(
            String layerName,
            String layerPath,
            int layerType,
            List<Field> fields)
    {
        VectorLayerUI vectorLayer = new VectorLayerUI(this, mMap.createLayerStorage(layerPath));
        vectorLayer.setName(layerName);
        vectorLayer.setVisible(true);
        vectorLayer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
        vectorLayer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);

        vectorLayer.create(layerType, fields);
        return vectorLayer;
    }
}
