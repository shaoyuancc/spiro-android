package shaoyuan.spiro.ui;

import android.os.Bundle;
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

import org.w3c.dom.Text;

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
        lastRecordTextView = v.findViewById(R.id.lastRecordLabelTextView);

        final Button calibrateButton = v.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(createCalibrateButtonListener());

        final Button startMeasureButton = v.findViewById(R.id.startMeasureButton);
        startMeasureButton.setOnClickListener(
                createStartButtonListener(startTextView, stopTextView, lastRecordTextView));

        final Button stopMeasureButton = v.findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(
                createStopButtonListener(startTextView, stopTextView, calibrateTextView));



        return v;
    }

    private View.OnClickListener createCalibrateButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
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

    private View.OnClickListener createStartButtonListener(TextView startTextView, TextView stopTextView, TextView lastRecordView) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                startTextView.setText("Measuring...");
                stopTextView.setText("");
                String filename = DataOutput.generateFileName();
                DataOutput.writeFileExternalStorage(filename, null);

                MicrophoneSignalProcess.getInstance().startAnalyze(new SignalProcess.OnPeakFound() {
                     @Override
                     public void onResult(int peakFlowRate) {
                         Log.d("SPF-Lib","Peak Flow Rate: " + peakFlowRate);
                         String data = DataOutput.createStringFromValue(peakFlowRate);
                         DataOutput.writeFileExternalStorage(filename, data);
                         lastRecordView.setText(data);
                     }
                 });
            }


        };
    }

    private View.OnClickListener createStopButtonListener(TextView startTextView, TextView stopTextView, TextView calibrationTextView) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                    MicrophoneSignalProcess.getInstance().stopAnalyze();
                    MicrophoneSignalProcess.getInstance().close();

                }
            };
    }

    private void stopButtonPressed (){
        startTextView.setText("Stopped");
        stopTextView.setText("Stopped");
        calibrateTextView.setText("Not Calibrated");
    }
    private void calibrateButtonPressed (){
        calibrateTextView.setText("Complete");
    }

}