package shaoyuan.spiro;

import android.content.res.Resources;

import java.io.IOException;
import java.io.InputStream;

public class AppUtil {

    public static String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = Resources.getSystem().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
