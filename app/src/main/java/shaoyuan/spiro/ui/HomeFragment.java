package shaoyuan.spiro.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.synthnet.spf.MicrophoneSignalProcess;
import com.synthnet.spf.SignalProcess;

import shaoyuan.spiro.R;


public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_fragment, null);

        final Button calibrateButton = v.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(createCalibrateButtonListener());

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

}