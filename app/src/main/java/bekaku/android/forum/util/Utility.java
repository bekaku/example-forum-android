package bekaku.android.forum.util;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Utility {

    public static boolean isEmpty(Object s) {
        if (s==null) {
            return true;

        } else if (s instanceof String) {
            return (((String) s).length()==0) || ((String) s).equalsIgnoreCase("null");

        } else {
            return false;
        }
    }
    public static boolean pingToHost(String ipUrl){
        Log.i("pingToHost", ipUrl);
        HttpURLConnection connection = null;
        try {
            URL u = new URL(ipUrl);//http://www.google.com/
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("HEAD");
            // Set timeouts in milliseconds
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty( "Accept-Encoding", "" );

            int code = connection.getResponseCode();
            return code==HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
//            e.printStackTrace();
            Log.e("pingToHost","Offline : ");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }
    public static String hashSHA512(String password)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-512");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }
    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
