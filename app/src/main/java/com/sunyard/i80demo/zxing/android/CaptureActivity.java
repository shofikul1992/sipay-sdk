package com.sunyard.i80demo.zxing.android;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.sunyard.i80demo.zxing.camera.CameraManager;
import com.sunyard.i80demo.zxing.view.ViewfinderView;

import java.util.Collection;
import java.util.Map;


/**
 * 这个activity打开相机，在后台线程做常规的扫描；它绘制了一个结果view来帮助正确地显示条形码，在扫描的时候显示反馈信息，
 * 然后在扫描成功的时候覆盖扫描结果
 */
public class CaptureActivity extends Activity implements
        SurfaceHolder.Callback {

    //    private static final String TAG = CaptureActivity.class.getSimpleName();
    private static final String TAG = "ReadSerialportThread";
    //	private Handler handler = null;
//    private SerialPortUtil serialPortUtil = null;
//    private Context context = null;
//    public boolean flag = true;
//    String data;
//    private final long TIME_OUT=60000;
    // 相机控制
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    public static CaptureActivity instance;
    private boolean hasSurface;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    // 电量控制
//    private InactivityTimer inactivityTimer;
    // 声音、震动控制
    private BeepManager beepManager;


    private int scanertype;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    /**
     * OnCreate中初始化一些辅助类，如InactivityTimer（休眠）、Beep（声音）以及AmbientLight（闪光灯）
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /**
     * 扫描成功，处理反馈信息
     *
     * @param rawResult
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(final Result rawResult, Bitmap barcode, float scaleFactor) {

    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {

    }

    /**
     * 显示底层错误信息并退出应用
     */
    private void displayFrameworkBugMessageAndExit() {

    }


//    public void moveTaskToBack() {
//        moveTaskToBack(false);
//    }


}
