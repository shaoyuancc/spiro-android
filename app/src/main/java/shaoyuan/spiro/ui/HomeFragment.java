package shaoyuan.spiro.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.synthnet.spf.MicrophoneSignalProcess;
import com.synthnet.spf.SignalProcess;

import java.io.File;

import shaoyuan.spiro.AppUtil;
import shaoyuan.spiro.R;

import shaoyuan.spiro.feature.DataOutput;
import shaoyuan.spiro.service.ServiceCallbacks;
import shaoyuan.spiro.service.SpfService;


public class HomeFragment extends Fragment implements ServiceCallbacks {
    private TextView startTextView;
    private TextView stopTextView;
    private TextView calibrateTextView;
    private TextView lastRecordTextView;
    private TextView intensityThresholdTextView;
    private Button calibrateButton;
    private Button startMeasureButton;
    private Button stopMeasureButton;


    private SharedPreferences preferences;

    SpfService mService;
    boolean mBound = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_fragment, null);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        startTextView = v.findViewById(R.id.startTextView);
        stopTextView = v.findViewById(R.id.stopTextView);
        calibrateTextView = v.findViewById(R.id.calibrateTextView);
        lastRecordTextView = v.findViewById(R.id.lastRecordTextView);
        intensityThresholdTextView = v.findViewById(R.id.intensityThresholdTextView);

        calibrateButton = v.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(createCalibrateButtonListener());
        // calibrateButton.setVisibility(preferences.getBoolean("isCalibrated", true) ? View.INVISIBLE : View.VISIBLE);

        startMeasureButton = v.findViewById(R.id.startMeasureButton);
        startMeasureButton.setOnClickListener(createStartButtonListener());
        startMeasureButton.setVisibility(
                (preferences.getBoolean("isMeasuring", false) && preferences.getBoolean("isCalibrated", true)) ? View.VISIBLE : View.INVISIBLE);

        stopMeasureButton = v.findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(createStopButtonListener());
        stopMeasureButton.setVisibility(preferences.getBoolean("isMeasuring", true) ? View.VISIBLE : View.INVISIBLE);

        Button startServiceButton = v.findViewById(R.id.start_foreground_service_button);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SpfService", "Start Foreground Service Pressed");
                Intent intent = new Intent(getActivity(), SpfService.class);
                intent.setAction(SpfService.ACTION_START_FOREGROUND_SERVICE);
                getActivity().startService(intent);
                getActivity().bindService(intent, mConnection, getContext().BIND_IMPORTANT);
                Log.d("SpfService", "Bound to service");
            }
        });

        Button stopServiceButton = v.findViewById(R.id.stop_foreground_service_button);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SpfService", "Stop Foreground Service Pressed");
                Intent intent = new Intent(getActivity(), SpfService.class);
                intent.setAction(SpfService.ACTION_STOP_FOREGROUND_SERVICE);
                getActivity().startService(intent);
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(getActivity(), SpfService.class);
        getActivity().bindService(intent, mConnection, getContext().BIND_IMPORTANT);
        Log.d("SpfService", "Bound to service");

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound){
            mService.setCallbacks(null); // unregister
            getActivity().unbindService(mConnection);
            mBound = false;
        }

    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpfService.LocalBinder binder = (SpfService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setCallbacks(HomeFragment.this); // register
            Log.d("SpfService", "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.d("SpfService", "service disconnected");
        }
    };

    private View.OnClickListener createCalibrateButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                if (mBound){
                    mService.startCalibration();
                }
            }
        };
    }

    private View.OnClickListener createStartButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                startButtonPressed();
                mService.startMeasurement();
            }
        };
    }

    private View.OnClickListener createStopButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                stopButtonPressed();
                }
            };
    }

    private void stopButtonPressed() {
        startTextView.setText("Stopped");
        stopTextView.setText("Stopped");
        calibrateTextView.setText("Not Calibrated");
        preferences.edit().putBoolean("isMeasuring", false).apply();
        preferences.edit().putBoolean("isCalibrated", false).apply();
        calibrateButton.setVisibility(View.VISIBLE);
        startMeasureButton.setVisibility(View.INVISIBLE);
        stopMeasureButton.setVisibility(View.INVISIBLE);
        mService.stopMeasurement();
    }

    private void startButtonPressed() {
        startTextView.setText("Measuring...");
        stopTextView.setText("");
        preferences.edit().putBoolean("isMeasuring", true).apply();
        calibrateButton.setVisibility(View.INVISIBLE);
        startMeasureButton.setVisibility(View.INVISIBLE);
        stopMeasureButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showCalibrated() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calibrateTextView.setText("Calibration Complete");
                preferences.edit().putBoolean("isCalibrated", true).apply();
                calibrateButton.setVisibility(View.INVISIBLE);
                startMeasureButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showResult(String result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lastRecordTextView.setText(result);
            }
        });
    }

    @Override
    public void setIntensityThresholdTextView(String intensityText){
        Log.d("SpfService", "setIntensityThresholdTextView received: " + intensityText);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                intensityThresholdTextView.setText(intensityText);
            }
        });
    }

}