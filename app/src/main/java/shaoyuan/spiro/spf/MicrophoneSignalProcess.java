//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package shaoyuan.spiro.spf;

import android.media.AudioRecord;
import android.util.Log;

import com.synthnet.spf.SignalProcess;
import java.io.File;
import java.io.IOException;

public class MicrophoneSignalProcess extends SignalProcess {
    private static MicrophoneSignalProcess instance = null;
    private static final int RATE = 44100; //44100
    private WaveFileWriter waveFileWriter;
    private boolean isProcesing;
    AudioRecord recorder;

    private BufferCallback bufferCallback = new BufferCallback() {
        short[] tempBuffer;

        public double[] getBuffer(int size) {
            if (this.tempBuffer == null || this.tempBuffer.length != size) {
                this.tempBuffer = new short[size];
            }

            int read = 0;
            while(read < size && MicrophoneSignalProcess.this.isProcesing) {
                Log.d("SPF-Lib-Tests", "size: " + size + "| read: " + read);
                read += MicrophoneSignalProcess.this.recorder.read(this.tempBuffer, read, size - read);
                if (read >= size) {
                    break;
                }

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException var7) {
                    var7.printStackTrace();
                }
            }

            if (!MicrophoneSignalProcess.this.isProcesing) {
                return null;
            } else {
                double[] ret = new double[size];
                byte[] wavData = new byte[size * 2];

                for(int i = 0; i < size; ++i) {
                    ret[i] = (double)((float)this.tempBuffer[i] / 32768.0F);
                    if (MicrophoneSignalProcess.this.waveFileWriter != null) {
                        wavData[i * 2] = (byte)(this.tempBuffer[i] & 255);
                        wavData[i * 2 + 1] = (byte)(this.tempBuffer[i] >> 8);
                    }
                }

                if (MicrophoneSignalProcess.this.waveFileWriter != null) {
                    try {
                        MicrophoneSignalProcess.this.waveFileWriter.write(wavData);
                    } catch (IOException var6) {
                        var6.printStackTrace();
                    }
                }

                return ret;
            }
        }
    };

    public static MicrophoneSignalProcess getInstance() {
        if (instance == null) {
            instance = new MicrophoneSignalProcess();
        }

        return instance;
    }

    private MicrophoneSignalProcess() {
    }

    public synchronized void startAnalyze(OnPeakFound listener) {
        Log.d("SPF-Lib", "Start Analyze");
        if (!this.isProcesing) {
            this.isProcesing = true;
            if (this.recorder == null) {
                int minBufferSize = AudioRecord.getMinBufferSize(RATE, 16, 2);
                this.recorder = new AudioRecord(1, RATE, 2, 2, minBufferSize * 3);
            }

            if (this.waveFileWriter != null && this.waveFileWriter.isClosed()) {
                this.waveFileWriter = null;
            }

            this.recorder.startRecording();
            this.start(this.bufferCallback, listener);
        }
    }

    public synchronized void stopAnalyze() {
        this.stopAnalyze(true);
    }

    public synchronized void stopAnalyze(boolean closeWaveFileWriter) {
        Log.d("SPF-Lib", "Stop Analyze");
        if (this.isProcesing) {
            this.logToFile((String)null);
            this.recorder.stop();
            if (closeWaveFileWriter && this.waveFileWriter != null) {
                try {
                    this.waveFileWriter.close();
                } catch (IOException var3) {
                    var3.printStackTrace();
                }

                this.waveFileWriter = null;
            }

            this.isProcesing = false;
        }
    }

    public synchronized void startCalibration(OnCalibrated listener) {
        Log.d("SPF-Lib", "Start Calibration");
        if (!this.isProcesing) {
            this.isProcesing = true;
            if (this.recorder == null) {
                int minBufferSize = AudioRecord.getMinBufferSize(RATE, 16, 2);
                this.recorder = new AudioRecord(1, RATE, 2, 2, minBufferSize * 3);
            }

            if (this.waveFileWriter != null && this.waveFileWriter.isClosed()) {
                this.waveFileWriter = null;
            }

            this.recorder.startRecording();
            this.startCalibration(this.bufferCallback, listener);
        }
    }

    public synchronized void stopCalibration() {
        this.stopCalibration(false);
    }

    public synchronized void stopCalibration(boolean closeWaveFileWriter) {
        Log.d("SPF-Lib", "Stop Calibration");
        if (this.isProcesing) {
            this.logToFile((String)null);
            this.recorder.stop();
            if (closeWaveFileWriter && this.waveFileWriter != null) {
                try {
                    this.waveFileWriter.close();
                } catch (IOException var3) {
                    var3.printStackTrace();
                }

                this.waveFileWriter = null;
            }

            this.isProcesing = false;
        }
    }

    public boolean isProcesing() {
        return this.isProcesing;
    }

    public void setRecordFile(File wavFile) {
        if (this.waveFileWriter != null) {
            try {
                this.waveFileWriter.close();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

        if (wavFile == null) {
            this.waveFileWriter = null;
        } else {
            try {
                this.waveFileWriter = new WaveFileWriter(wavFile, RATE, (short)1, (short)16);
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

    }

    public WaveFileWriter getWaveFileWriter() {
        return this.waveFileWriter;
    }

    public void logToFile(String fileName) {
        super.logToFile(fileName);
    }

    public synchronized void close() {
        super.close();
        if (this.recorder != null) {
            this.recorder.release();
        }

        instance = null;
    }

    public synchronized void debugStartContinuous(OnPeakFound listener) {
        Log.d("SPF-Lib", "--DEBUG-- Start Analyze");
        if (!this.isProcesing) {
            this.isProcesing = true;
            if (this.recorder == null) {
                int minBufferSize = AudioRecord.getMinBufferSize(RATE, 16, 2);
                this.recorder = new AudioRecord(1, RATE, 2, 2, minBufferSize * 3);
            }

            if (this.waveFileWriter != null && this.waveFileWriter.isClosed()) {
                this.waveFileWriter = null;
            }

            this.recorder.startRecording();
            this.debugContinuousStart(this.bufferCallback, listener);
        }
    }
}
