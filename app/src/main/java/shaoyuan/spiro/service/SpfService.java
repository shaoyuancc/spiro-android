package shaoyuan.spiro.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.synthnet.spf.MicrophoneSignalProcess;
import com.synthnet.spf.SignalProcess;

import java.io.File;

import shaoyuan.spiro.AppUtil;
import shaoyuan.spiro.R;
import shaoyuan.spiro.feature.DataOutput;

public class SpfService extends Service {

    private static final String TAG_FOREGROUND_SERVICE = "SpfService";//"FOREGROUND_SERVICE";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "shaoyuan.spiro.service";
    private Double intensityThreshold;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Registered callbacks
    private ServiceCallbacks serviceCallbacks;

    private SharedPreferences preferences;

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("intensityThreshold")){
                intensityThreshold = getIntensityThreshold();
                Log.d("SPF-Lib", "Intensity Threshold Changed to " + intensityThreshold.toString());
            }
        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SpfService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SpfService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public SpfService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG_FOREGROUND_SERVICE, "My foreground service onCreate().");

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        intensityThreshold = getIntensityThreshold();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_settings_black_24dp)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();

            switch (action)
            {
                case ACTION_START_FOREGROUND_SERVICE:
                    startMyOwnForeground();
                    //startForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_PLAY:
                    Toast.makeText(getApplicationContext(), "You click Play button.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_PAUSE:
                    Toast.makeText(getApplicationContext(), "You click Pause button.", Toast.LENGTH_LONG).show();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopForegroundService()
    {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    /** method for clients */

    public void startCalibration() {

        String filename = DataOutput.generateFileName(".wav");

        MicrophoneSignalProcess.getInstance()
                .setRecordFile(new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), filename));

        MicrophoneSignalProcess.getInstance().startCalibration(new SignalProcess.OnCalibrated() {
            @Override
            public void onCalibrated(int status) {
                MicrophoneSignalProcess.getInstance().stopCalibration();
                Log.d(TAG_FOREGROUND_SERVICE, "onCalibrated Called");
                if (serviceCallbacks != null) {
                    serviceCallbacks.showCalibrated();
                    Log.d(TAG_FOREGROUND_SERVICE, "showCalibrated Called");
                }
            }
        });
    }

    public void startMeasurement() {
        String filename = DataOutput.generateFileName(".csv");
        DataOutput.writeFileExternalStorage(filename, preferencesToString());

        MicrophoneSignalProcess.getInstance().debugStartContinuous(new SignalProcess.OnPeakFound() {
            @Override
            public void onResult(int flowRate, double magnitude) {
                if (magnitude > intensityThreshold){
                    Log.d(TAG_FOREGROUND_SERVICE,"Flow Rate: " + flowRate + " Magnitude: " + magnitude);
                    String data = DataOutput.createStringFromValue(flowRate);
                    DataOutput.writeFileExternalStorage(filename, data);
                    serviceCallbacks.showResult(data);
                }
            }
        });
    }

    public void stopMeasurement(){
        MicrophoneSignalProcess.getInstance().stopAnalyze();
        MicrophoneSignalProcess.getInstance().close();
    }

    public double getIntensityThreshold(){
        Double val = AppUtil.convertStringToDouble(preferences.getString("intensityThreshold", "0.1"));
        if (val == null){
            Log.d(TAG_FOREGROUND_SERVICE, "Intensity Threshold Invalid. Using default 0.1");
            if (serviceCallbacks != null) {
                serviceCallbacks.setIntensityThresholdTextView("Intensity Threshold Invalid. Using default 0.1");
            }
            return 0.1;
        }else {
            Log.d(TAG_FOREGROUND_SERVICE, "Intensity Threshold onCreate is " + val.toString());
            if (serviceCallbacks != null) {
                serviceCallbacks.setIntensityThresholdTextView("Intensity Threshold onCreate is " + val.toString());
            }
            return val;
        }

    }

    private String preferencesToString(){
        String deliminator = ",";
        String prefString =
                "usePeriodUuid" + deliminator + preferences.getString("usePeriodUuid", "") + '\n' +
                        "patientUuid" + deliminator + preferences.getString("patientUuid", "") + '\n' +
                        "patientName" + deliminator + preferences.getString("patientName", "") + '\n' +
                        "androidDeviceUuid" + deliminator + preferences.getString("androidDeviceUuid", "") + '\n' +
                        "usePeriodStart" + deliminator + preferences.getString("usePeriodStart", "") + '\n' +
                        "usePeriodEnd" + deliminator + preferences.getString("usePeriodEnd", "") + '\n' +
                        "applicationMode" + deliminator + preferences.getString("applicationMode", "") + "\n\n";
        return prefString;
    }
}
