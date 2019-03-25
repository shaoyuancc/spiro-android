package shaoyuan.spiro.service;

public interface ServiceCallbacks {
    void showCalibrated();
    void showResult(String result);
    void setIntensityThresholdTextView(String intensityText);
    void setIsConnectedTextView(Boolean isConnected);
}
