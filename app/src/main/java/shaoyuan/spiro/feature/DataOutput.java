package shaoyuan.spiro.feature;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import shaoyuan.spiro.db.entity.DatumEntity;
import shaoyuan.spiro.model.Datum;
import shaoyuan.spiro.ui.SettingsFragment;

public  class DataOutput {

    public static void writeFileExternalStorage(String filenameExternal, String content) {
        String state = Environment.getExternalStorageState();
        //external storage availability check
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), filenameExternal);
        Log.d("SPIRO-OUTPUT", "filepath: " + file.getAbsolutePath());
        Log.d("SPIRO-OUTPUT", "Can Write: " + file.canWrite());

        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            //second argument of FileOutputStream constructor indicates whether to append or create new file if one exists
            if (content != null && !content.isEmpty()){
                outputStream = new FileOutputStream(file, true);
                outputStream.write(content.getBytes());
                outputStream.flush();
                outputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateFileName(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        Date date = new Date();
        return (dateFormat.format(date) + ".csv");
    }



    public static String createStringFromValue(int value) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        Date date = new Date();
        return (dateFormat.format(date) + "," + String.valueOf(value) + '\n');
    }
}
