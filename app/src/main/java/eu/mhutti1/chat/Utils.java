package eu.mhutti1.chat;

import android.content.Context;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

/**
 * Created by Isaac on 06/06/2017.
 */

public class Utils {

  public static String myId(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.USERIDPREF, "----");
  }
}
