package pages;
import android.support.v7.preference.PreferenceScreen;

import com.example.geobox_mapfinal.R;
import com.nextgis.maplib.util.AccountUtil;
import com.nextgis.maplibui.fragment.NGPreferenceHeaderFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;


public class SettingsHeaderFragment
        extends NGPreferenceHeaderFragment {
    @Override
    protected void createPreferences(PreferenceScreen screen) {
        addPreferencesFromResource(R.xml.preference_headers);
    }
}