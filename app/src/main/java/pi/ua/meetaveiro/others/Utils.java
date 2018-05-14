package pi.ua.meetaveiro.others;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import pi.ua.meetaveiro.interfaces.NetworkCheckResponse;

public class Utils {

    public static final String KEY_ROUTE_STATE = "route_state";

    /**
     * Returns the route state from shared preferences.
     * @param context The {@link Context}.
     */
    public static Constants.ROUTE_STATE getRouteState(Context context) {
        String stringValue = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_ROUTE_STATE, Constants.ROUTE_STATE.STOPPED.toString());
        return Constants.ROUTE_STATE.toMyEnum(stringValue);
    }

    /**
     * Stores the route state in SharedPreferences.
     * @param route_state The route state.
     */
    public static void setRouteState(Context context, Constants.ROUTE_STATE route_state) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_ROUTE_STATE, route_state.toString())
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" : "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    /**
     * @param bitmap {@link Bitmap}
     * @return {@link String} object of the {@code location}
     */
    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static class NetworkCheckTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        NetworkCheckResponse response;

        public NetworkCheckTask(Context context, NetworkCheckResponse response){
            this.context = context;
            this.response = response;
        }

        protected Boolean doInBackground(String... params) {
            return hasActiveInternetConnection(this.context, params[0]);
        }
        protected void onPostExecute(Boolean hasActiveConnection) {
            Log.d("hasActiveConnection","Success=" + hasActiveConnection);
            if (!hasActiveConnection)
                Toast.makeText(context, "Can't reach Server! Check your internet connection or try again later.", Toast.LENGTH_SHORT).show();
            response.onProcessFinished(hasActiveConnection);
        }

        private boolean hasActiveInternetConnection(Context context, String URL) {
            if (isNetworkAvailable(context)) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) (new URL(URL).openConnection());
                    urlc.setConnectTimeout(750);
                    urlc.connect();
                    return (urlc.getResponseCode() == 200);
                } catch (Exception e) {
                    Log.e("ERROR", "Error checking internet connection", e);
                }
            } else {
                Log.d("ERROR", "No network available!");
            }
            return false;
        }

        private static boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null;
        }
    }
}
