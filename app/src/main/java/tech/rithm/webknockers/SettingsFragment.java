package tech.rithm.webknockers;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by rithm on 3/2/2017.
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
