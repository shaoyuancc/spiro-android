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

import shaoyuan.spiro.R;


public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_fragment, null);

        final TextView readingView = v.findViewById(R.id.textView);
        readingView.setText("hello");

        final Button calibrateButton = v.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(createCalibrateButtonListener());

        final Button startMeasureButton = v.findViewById(R.id.startMeasureButton);
        startMeasureButton.setOnClickListener(createStartButtonListener(readingView));

        final Button stopMeasureButton = v.findViewById(R.id.stopMeasureButton);
        stopMeasureButton.setOnClickListener(createStopButtonListener());



        return v;
    }

    private View.OnClickListener createCalibrateButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                MicrophoneSignalProcess.getInstance().startCalibration(new SignalProcess.OnCalibrated() {
                    @Override
                    public void onCalibrated(int status) {
                        MicrophoneSignalProcess.getInstance().stopCalibration();
                    }
                });
            }
        };
    }

    private View.OnClickListener createStartButtonListener(TextView readingView) {
        return new View.OnClickListener() {

            public void onClick(View v) {
                readingView.setText("Start Reading");


                MicrophoneSignalProcess.getInstance().startAnalyze(new SignalProcess.OnPeakFound() {
                     @Override
                     public void onResult(int peakFlowRate) {
                         Log.d("SPF-Lib","Peak Flow Rate: " + peakFlowRate);
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
                }
            };
    }

}