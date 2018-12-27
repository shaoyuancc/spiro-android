package shaoyuan.spiro.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.synthnet.spf.MicrophoneSignalProcess;
import com.synthnet.spf.SignalProcess;

import java.io.File;

import shaoyuan.spiro.R;

import shaoyuan.spiro.feature.DataOutput;

import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment {
    private TextView startTextView;
    private TextView stopTextView;
    private TextView calibrateTextView;
    private TextView lastRecordTextView;
    private Button calibrateButton;
    private Button startMeasureButton;
    private Button stopMeasureButton;

    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_fragment, null);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


        startTextView = v.findViewById(R.id.startTextView);
        stopTextView = v.findViewById(R.id.stopTextView);
        calibrateTextView = v.findViewById(R.id.calibrateTextView);
        lastRecordTextView = v.findViewById(R.id.lastRecordTextView);

        calibrateButton = v.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(createCalibrateButtonListener());
        calibrateButton.setVisibility(preferences.getBoolean("isMeasuring", true) ? View.INVISIBLE : View.VISIBLE);

        startMeasureButton = v.findViewById(R.id.startMeasureButton);
        startMeasureButton.setOnClickListener(createStartButtonListener());
        startMeasureButton.setVisibility(preferences.getBoolean("isMeasuring", true) ? View.INVISIBLE : View.VISIBLE);

        stopMeasureButton = v.findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(createStopButtonListener());
        stopMeasureButton.setVisibility(preferences.getBoolean("isMeasuring", true) ? View.VISIBLE : View.INVISIBLE);

        return v;
    }

    private View.OnClickListener createCalibrateButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {

                String filename = DataOutput.generateFileName(".wav");

                MicrophoneSignalProcess.getInstance()
                        .setRecordFile(new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS), filename));

                MicrophoneSignalProcess.getInstance().startCalibration(new SignalProcess.OnCalibrated() {
                    @Override
                    public void onCalibrated(int status) {
                        MicrophoneSignalProcess.getInstance().stopCalibration();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                calibrateButtonPressed();
                            }
                        });
                    }
                });

            }
        };
    }

    private View.OnClickListener createStartButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                startButtonPressed();
                String filename = DataOutput.generateFileName(".csv");
                DataOutput.writeFileExternalStorage(filename, preferencesToString());

                MicrophoneSignalProcess.getInstance().debugStartContinuous(new SignalProcess.OnPeakFound() {
                    @Override
                    public void onResult(int flowRate, double magnitude) {
                        if (magnitude > 0.1){
                            Log.d("SPF-Lib","Flow Rate: " + flowRate + " Magnitude: " + magnitude);
                            String data = DataOutput.createStringFromValue(flowRate);
                            DataOutput.writeFileExternalStorage(filename, data);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resultReturned(data);
                                }
                            });
                        }
                    }
                });

            }


        };
    }

    private View.OnClickListener createStopButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                MicrophoneSignalProcess.getInstance().stopAnalyze();
                MicrophoneSignalProcess.getInstance().close();
                stopButtonPressed();
                }
            };
    }

    private void stopButtonPressed() {
        startTextView.setText("Stopped");
        stopTextView.setText("Stopped");
        calibrateTextView.setText("Not Calibrated");
        preferences.edit().putBoolean("isMeasuring", false).apply();
        calibrateButton.setVisibility(View.VISIBLE);
        startMeasureButton.setVisibility(View.VISIBLE);
        stopMeasureButton.setVisibility(View.INVISIBLE);

    }

    private void startButtonPressed() {
        startTextView.setText("Measuring...");
        stopTextView.setText("");
        preferences.edit().putBoolean("isMeasuring", true).apply();
        calibrateButton.setVisibility(View.INVISIBLE);
        startMeasureButton.setVisibility(View.INVISIBLE);
        stopMeasureButton.setVisibility(View.VISIBLE);
    }

    private void calibrateButtonPressed() {
        calibrateTextView.setText("Complete");
    }

    private void resultReturned(String data) {
        lastRecordTextView.setText(data);
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