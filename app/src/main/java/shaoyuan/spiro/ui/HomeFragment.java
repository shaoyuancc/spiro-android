package shaoyuan.spiro.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import shaoyuan.spiro.R;

import shaoyuan.spiro.service.ServiceCallbacks;
import shaoyuan.spiro.service.SpfService;


public class HomeFragment extends Fragment implements ServiceCallbacks {
    private TextView startTextView;
    private TextView stopTextView;
    private TextView calibrateTextView;
    private TextView lastRecordTextView;
    private TextView intensityThresholdTextView;
    private TextView isConnectedTextView;
    private Button calibrateButton;
    private Button startMeasureButton;
    private Button stopMeasureButton;

    SpfService mService;
    boolean mBound = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("SpfService", "onCreateView");

        View v = inflater.inflate(R.layout.home_fragment, null);

        startTextView = v.findViewById(R.id.startTextView);
        stopTextView = v.findViewById(R.id.stopTextView);
        calibrateTextView = v.findViewById(R.id.calibrateTextView);
        lastRecordTextView = v.findViewById(R.id.lastRecordTextView);
        intensityThresholdTextView = v.findViewById(R.id.intensityThresholdTextView);
        isConnectedTextView = v.findViewById(R.id.isConnectedTextView);

        calibrateButton = v.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(createCalibrateButtonListener());

        startMeasureButton = v.findViewById(R.id.startMeasureButton);
        startMeasureButton.setOnClickListener(createStartButtonListener());
        startMeasureButton.setVisibility(View.INVISIBLE);

        stopMeasureButton = v.findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(createStopButtonListener());
        stopMeasureButton.setVisibility(View.INVISIBLE);

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
            lastRecordTextView.setText(mService.getLastRecordValue());
            isConnectedTextView.setText(String.valueOf(mService.getIsConnected()));
            intensityThresholdTextView.setText(mService.getIntensityThreshold().toString());
            if (mService.getIsCalibrating()){
                mService.startCalibration();
            }
            Log.d("SpfService", "Service connected");
            updateUI();
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
                Log.d("SpfService", "calibratebuttonpressed");
                Intent intent = new Intent(getActivity(), SpfService.class);
                intent.setAction(SpfService.ACTION_START_FOREGROUND_SERVICE);
                getActivity().startService(intent);
                getActivity().bindService(intent, mConnection, getContext().BIND_IMPORTANT);
                calibrateButton.setVisibility(View.INVISIBLE);
                startMeasureButton.setVisibility(View.INVISIBLE);
                stopMeasureButton.setVisibility(View.VISIBLE);
                startTextView.setText("");
                stopTextView.setText("");
                calibrateTextView.setText("Calibrating...");
            }
        };
    }

    private View.OnClickListener createStartButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("SpfService", "start button pressed");
                if (mBound){
                    Log.d("SpfService", "bound to service in start button pressed");
                    mService.setIsMeasuring(true);
                    mService.startMeasurement();
                    updateUI();
                }
            }
        };
    }

    private View.OnClickListener createStopButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("SpfService", "stop button pressed");
                if (mBound) {
                    mService.setIsStopped(true);
                    mService.setIsMeasuring(false);
                    mService.setIsCalibrated(false);
                    updateUI();
                    mService.stopMeasurement();
                }
                Intent intent = new Intent(getActivity(), SpfService.class);
                intent.setAction(SpfService.ACTION_STOP_FOREGROUND_SERVICE);
                getActivity().startService(intent);


                }
            };
    }

    private void updateUI(){
        Log.d("SpfService", "Enter Update UI: mBound "+ mBound +
                " |isCalibrated " + mService.getIsCalibrated() +
                " |isCalibrating " + mService.getIsCalibrating() +
                " |isMeasuring " + mService.getIsMeasuring() +
                " |isStopped " + mService.getIsStopped());
        if (mBound){
            // Reset state
            if (mService.getIsStopped()){
                Log.d("SpfService", "RESET STATE");
                calibrateButton.setVisibility(View.VISIBLE);
                startMeasureButton.setVisibility(View.INVISIBLE);
                stopMeasureButton.setVisibility(View.INVISIBLE);
                startTextView.setText("");
                stopTextView.setText("");
                calibrateTextView.setText("Not Calibrated");
            }
            // Calibrate Button Pressed
            else if (!mService.getIsCalibrated() && mService.getIsCalibrating() &&
                    !mService.getIsMeasuring() && !mService.getIsStopped()) {
                Log.d("SpfService", "CALIBRATE BUTTON PRESSED STATE");
                calibrateButton.setVisibility(View.INVISIBLE);
                startMeasureButton.setVisibility(View.INVISIBLE);
                stopMeasureButton.setVisibility(View.VISIBLE);
                startTextView.setText("");
                stopTextView.setText("");
                calibrateTextView.setText("Calibrating...");
            }
            // Calibration Complete
            else if (mService.getIsCalibrated() && !mService.getIsCalibrating() &&
                    !mService.getIsMeasuring() && !mService.getIsStopped()) {
                Log.d("SpfService", "CALIBRATION COMPLETE STATE");
                calibrateButton.setVisibility(View.INVISIBLE);
                startMeasureButton.setVisibility(View.VISIBLE);
                stopMeasureButton.setVisibility(View.VISIBLE);
                startTextView.setText("Ready to Start");
                stopTextView.setText("");
                calibrateTextView.setText("Calibration Complete");
            }
            // Measuring State
            else if (mService.getIsCalibrated() && !mService.getIsCalibrating() &&
                    mService.getIsMeasuring() && !mService.getIsStopped()) {
                Log.d("SpfService", "MEASURING STATE");
                calibrateButton.setVisibility(View.INVISIBLE);
                startMeasureButton.setVisibility(View.INVISIBLE);
                stopMeasureButton.setVisibility(View.VISIBLE);
                startTextView.setText("Measuring...");
                stopTextView.setText("");
                calibrateTextView.setText("Calibration Complete");
            }else {
                Log.d("SpfService", "UNDEFINED STATE");
            }
        }

    }

    @Override
    public void showCalibrated() {
        Log.d("SpfService", "Done Calibrating");
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    calibrateButton.setVisibility(View.INVISIBLE);
                    startMeasureButton.setVisibility(View.VISIBLE);
                    stopMeasureButton.setVisibility(View.VISIBLE);
                    startTextView.setText("Ready to Start");
                    stopTextView.setText("");
                    calibrateTextView.setText("Calibration Complete");
                }
            });
        }

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

    @Override
    public void setIsConnectedTextView(Boolean isConnected){
        Log.d("SpfService", "setIsConnectedTextView received: " + isConnected);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isConnectedTextView.setText(isConnected.toString());
            }
        });
    }


}