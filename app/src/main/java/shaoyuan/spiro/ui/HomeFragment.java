package shaoyuan.spiro.ui;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import shaoyuan.spiro.R;

import shaoyuan.spiro.feature.DataOutput;


public class HomeFragment extends Fragment {
    private TextView startTextView;
    private TextView stopTextView;
    private TextView calibrateTextView;
    private TextView lastRecordTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_fragment, null);

        startTextView = v.findViewById(R.id.startTextView);
        stopTextView = v.findViewById(R.id.stopTextView);
        calibrateTextView = v.findViewById(R.id.calibrateTextView);
        lastRecordTextView = v.findViewById(R.id.lastRecordTextView);

        final Button calibrateButton = v.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(createCalibrateButtonListener());

        final Button startMeasureButton = v.findViewById(R.id.startMeasureButton);
        startMeasureButton.setOnClickListener(createStartButtonListener());

        final Button stopMeasureButton = v.findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(createStopButtonListener());

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
                    public void onResult(int flowRate) {
                        if (flowRate > 0){
                            Log.d("SPF-Lib","Flow Rate: " + flowRate);
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

                /*
                MicrophoneSignalProcess.getInstance().startAnalyze(new SignalProcess.OnPeakFound() {
                     @Override
                     public void onResult(int peakFlowRate) {
                         Log.d("SPF-Lib","Peak Flow Rate: " + peakFlowRate);
                         String data = DataOutput.createStringFromValue(peakFlowRate);
                         DataOutput.writeFileExternalStorage(filename, data);
                         getActivity().runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 resultReturned(data);
                             }
                         });

                     }
                 });
                 */
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
    }

    private void startButtonPressed() {
        startTextView.setText("Measuring...");
        stopTextView.setText("");
    }

    private void calibrateButtonPressed() {
        calibrateTextView.setText("Complete");
    }

    private void resultReturned(String data) {
        lastRecordTextView.setText(data);
    }

    private String preferencesToString(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String deliminator = ",";
        String preferences =
                "usePeriodUuid" + deliminator + sharedPref.getString("usePeriodUuid", "") + '\n' +
                "patientUuid" + deliminator + sharedPref.getString("patientUuid", "") + '\n' +
                "patientName" + deliminator + sharedPref.getString("patientName", "") + '\n' +
                "androidDeviceUuid" + deliminator + sharedPref.getString("androidDeviceUuid", "") + '\n' +
                "usePeriodStart" + deliminator + sharedPref.getString("usePeriodStart", "") + '\n' +
                "usePeriodEnd" + deliminator + sharedPref.getString("usePeriodEnd", "") + '\n' +
                "applicationMode" + deliminator + sharedPref.getString("applicationMode", "") + "\n\n";
        return preferences;
    }

}