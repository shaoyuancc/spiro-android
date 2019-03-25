package shaoyuan.spiro.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private boolean isCalibrated;
    private boolean isCalibrating;
    private boolean isMeasuring;
    private boolean isStopped;
    private boolean isConnected;
    private String lastRecordValue;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Registered callbacks
    private ServiceCallbacks serviceCallbacks;

    private SharedPreferences preferences;

    private static final String TAG = "AUDIOTAG";
    private MusicIntentReceiver myReceiver;

    /**
     * Getters and setters
     */
    public boolean getIsCalibrating(){ return isCalibrating; }
    public boolean getIsCalibrated(){ return isCalibrated; }
    public boolean getIsMeasuring(){ return isMeasuring; }
    public boolean getIsStopped(){ return isStopped; }
    public String getLastRecordValue(){ return lastRecordValue; }
    public Double getIntensityThreshold(){ return intensityThreshold; }
    public boolean getIsConnected() { return isConnected; }

    public void setIsCalibrated(Boolean input){ isCalibrated = input; }
    public void setIsCalibrating(Boolean input){ isCalibrating = input; }
    public void setIsMeasuring(Boolean input){ isMeasuring = input; }
    public void setIsStopped(Boolean input){ isStopped = input; }
    public void setLastRecordValue(String input){ lastRecordValue = input; }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        isConnected = false;
                        if (serviceCallbacks != null) {
                            serviceCallbacks.setIsConnectedTextView(isConnected);
                        }
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        isConnected = true;
                        if (serviceCallbacks != null) {
                            serviceCallbacks.setIsConnectedTextView(isConnected);
                        }
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                        isConnected = false;
                        if (serviceCallbacks != null) {
                            serviceCallbacks.setIsConnectedTextView(isConnected);
                        }
                }
            }
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("intensityThreshold")){
                intensityThreshold = getIntensityThresholdFromPref();
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
        // Calibrate Button Pressed State
        intensityThreshold = getIntensityThresholdFromPref();
        isCalibrated = false;
        isCalibrating = true;
        isMeasuring = false;
        isStopped = false;
        lastRecordValue = getString(R.string.empty_last_record_value);

        myReceiver = new MusicIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);

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

        unregisterReceiver(myReceiver);

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    /** method for clients */

    public void startCalibration() {
        Log.d(TAG_FOREGROUND_SERVICE, "startCalibration Called");
        /*like
        String filename = DataOutput.generateFileName(".wav");

        MicrophoneSignalProcess.getInstance()
                .setRecordFile(new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), filename));
        */

        MicrophoneSignalProcess.getInstance().startCalibration(new SignalProcess.OnCalibrated() {
            @Override
            public void onCalibrated(int status) {
                MicrophoneSignalProcess.getInstance().stopCalibration();
                Log.d(TAG_FOREGROUND_SERVICE, "onCalibrated Called");
                if (serviceCallbacks != null) {
                    setIsCalibrating(false);
                    setIsCalibrated(true);
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
                if (magnitude > intensityThreshold && isConnected){
                    Log.d(TAG_FOREGROUND_SERVICE,"Flow Rate: " + flowRate + " Magnitude: " + magnitude);
                    String data = DataOutput.createStringFromValue(flowRate);
                    DataOutput.writeFileExternalStorage(filename, data);
                    if (serviceCallbacks != null) {
                        serviceCallbacks.showResult(data);
                        setLastRecordValue(data);
                    }else{
                        setLastRecordValue(data);
                    }
                }
            }
        });
    }

    public void stopMeasurement(){
        MicrophoneSignalProcess.getInstance().stopAnalyze();
        MicrophoneSignalProcess.getInstance().close();
    }

    public double getIntensityThresholdFromPref(){
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
