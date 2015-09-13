package de.windowsfreak.nistreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import de.windowsfreak.testjni.Config;
import de.windowsfreak.testjni.Main;
import de.windowsfreak.testjni.SourceFactory;
import de.windowsfreak.testjni.codec.CharLSCodec;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    private static Thread mainThread;

    public SettingsActivity() {
        SourceFactory.activity = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(de.windowsfreak.nistreamer.R.xml.pref_headers, target);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("silent"/*R.string.pref_ringtone_silent*/);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Main.pf = this;
            new CharLSCodec();
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(de.windowsfreak.nistreamer.R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            String[] editableFields = new String[] {"source", "source_url", "source_port", "compression", "threads", "sink", "sink_url", "sink_port", "x", "y", "depth", "mode", "fps"};
            for (String s : editableFields) {
                bindPreferenceSummaryToValue(findPreference(s));
            }
            //bindPreferenceSummaryToValue(findPreference("example_text"));
            //bindPreferenceSummaryToValue(findPreference("example_list"));

            final Preference button = (Preference)findPreference("start");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    final Config config = new Config();
                    config.source = prefs.getString("source", "");
                    config.sourceUrl = prefs.getString("source_url", "");
                    config.sourcePort = Integer.parseInt(prefs.getString("source_port", "9999"));
                    config.compression = prefs.getString("compression", "");
                    config.compress = prefs.getBoolean("compress", false);
                    config.decompress = prefs.getBoolean("decompress", false);
                    config.sink = prefs.getString("sink", "");
                    config.sinkUrl = prefs.getString("sink_url", "");
                    config.sinkPort = Integer.parseInt(prefs.getString("sink_port", "9999"));
                    config.sinkControlled = prefs.getBoolean("sink_controlled", false);
                    config.x = (short)Integer.parseInt(prefs.getString("x", "640"));
                    config.y = (short)Integer.parseInt(prefs.getString("y", "480"));
                    config.depth = (byte)Integer.parseInt(prefs.getString("depth", "1"));
                    config.mode = (byte)Integer.parseInt(prefs.getString("mode", "1"));
                    config.fps = (byte)Integer.parseInt(prefs.getString("fps", "30"));

                    WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

                    Log.d("NETWORK", "IP address: " + ip);

                    button.setSummary("IP address: " + ip);

                    if (mainThread != null) {
                        mainThread.interrupt();
                        mainThread = null;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    config.condition = true;
                    mainThread = new Thread() {
                        Main main;
                        public void run() {
                            try {
                                main = new Main(config, "TestJNI");
                                //Main4.main(config1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        public void interrupt() {
                            config.condition = false;
                            if (main != null) {
                                if (main.schedulerThread != null)
                                    main.schedulerThread.interrupt();
                                if (main.deliveryThread != null) main.deliveryThread.interrupt();
                                if (main.workers != null) {
                                    for (Thread worker : main.workers) {
                                        worker.interrupt();
                                    }
                                }
                                if (main.sif != null) main.sif.stop(main.config);
                                if (main.sink != null) try {
                                    main.sink.shutDown();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            super.interrupt();
                        }
                    };
                    mainThread.start();

                    //MyToaster.toast("IP address: " + ip);

                    return true;
                }
            });

        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }*/

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }*/
}
