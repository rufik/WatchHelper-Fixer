package pl.rufik.watchhelperfixer;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class MainActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Setup a non-default and world readable shared preferences, so that:
            // 1 - we know the name (necessary for XSharedPreferences()),
            // 2 - the preferences are accessible from inside the hook.
            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName("prefs_whfixer");
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
