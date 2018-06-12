package pi.ua.meetaveiro.others;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

import pi.ua.meetaveiro.interfaces.NetworkCheckResponse;

public class Utils {

    public static final String KEY_ROUTE_STATE = "route_state";

    public static boolean uncompletedCameraRequest = true;

    /**
     * Returns the route state from shared preferences.
     *
     * @param context The {@link Context}.
     */
    public static Constants.ROUTE_STATE getRouteState(Context context) {
        String stringValue = context.getSharedPreferences("route_state", Context.MODE_PRIVATE).getString(KEY_ROUTE_STATE, Constants.ROUTE_STATE.STOPPED.toString());
        Log.i("stringvalue", stringValue);
        return Constants.ROUTE_STATE.toMyEnum(stringValue);
    }

    /**
     * Stores the route state in SharedPreferences.
     *
     * @param route_state The route state.
     */
    public static void setRouteState(Context context, Constants.ROUTE_STATE route_state) {
        SharedPreferences settings = context.getSharedPreferences("route_state", Context.MODE_PRIVATE);
        settings.edit().putString(KEY_ROUTE_STATE, route_state.toString()).apply();
    }


    /**
     * @param bitmap {@link Bitmap}
     * @return {@link String} object of the {@code location}
     */
    public static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }


    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    /**
     * Opens the file with the route and reconctructs it
     * File name format: route+++.json
     * +++ = route name
     *
     * @param filename
     * @return String (json format) with all the information
     */
    public static String getRouteFromFile(String filename, Context ctx) {

        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = ctx.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);
            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }
            isr.close();

        } catch (IOException e) {
            Log.e("GetRoute", e.getMessage());
        }

        return datax.toString();

    }


    public static Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        InputStream stream = null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;

        try {
            stream = getHttpConnection(url);
            bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
            stream.close();
        } catch (IOException e1) {
            Log.e("ERROR", e1.toString());
        }
        return bitmap;
    }


    public static Bitmap createSmallMarker(Bitmap image){
        int height = 132;
        int width = 132;
        return Bitmap.createScaledBitmap(image, width, height, false);
    }



    public static InputStream getHttpConnection(String urlString) throws IOException {

        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("downloadImage" + ex.toString());
        }
        return stream;
    }


    public static class NetworkCheckTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        NetworkCheckResponse response;

        public NetworkCheckTask(Context context, NetworkCheckResponse response) {
            this.context = context;
            this.response = response;
        }

        protected Boolean doInBackground(String... params) {
            return hasActiveInternetConnection(this.context, params[0]);
        }

        protected void onPostExecute(Boolean hasActiveConnection) {
            Log.d("hasActiveConnection", "Success=" + hasActiveConnection);
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

    public static String convertTimeInMilisAndFormat(long timeInMilis){
        Date d = new Date(timeInMilis);
        Calendar cl = Calendar.getInstance();
        cl.setTime(d);
        String photoDate = cl.get(Calendar.YEAR)+"-"+
                cl.get(Calendar.MONTH)+"-"+cl.get(Calendar.DAY_OF_MONTH)+" " +cl.get(Calendar.HOUR_OF_DAY)+
                ":"+cl.get(Calendar.MINUTE)+":"+cl.get(Calendar.SECOND);
        return photoDate;
    }

    public static String convertTimeInMilisAndFormatPretty(long timeInMilis){
        Date d = new Date(timeInMilis);
        Calendar cl = Calendar.getInstance();
        cl.setTime(d);
        String photoDate = cl.get(Calendar.DAY_OF_MONTH)+"/"+
                cl.get(Calendar.MONTH)+"/"+cl.get(Calendar.YEAR)+", " +cl.get(Calendar.HOUR_OF_DAY)+
                ":"+cl.get(Calendar.MINUTE)+":"+cl.get(Calendar.SECOND);
        return photoDate;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth){
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Converts bitmap to the byte array without compression
     * @param bitmap source bitmap
     * @return result byte array
     */
    public static String convertBitmapToByteArrayUncompressed(Bitmap bitmap){
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();
        String temp=Base64.encodeToString(byteBuffer.array(), Base64.DEFAULT);
        return temp;
    }

    public static String bitMapToBase64 (Bitmap image){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap correctOrientation(Bitmap bitmap, String photoPath) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = Utils.rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = Utils.rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = Utils.rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

}
