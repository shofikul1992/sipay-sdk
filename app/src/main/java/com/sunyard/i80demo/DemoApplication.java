package com.sunyard.i80demo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sunyard.sysfuncservice.DeviceServiceEngine;

/**
 *
 * Created by wy on 2018/5/24.
 */

public class DemoApplication extends Application {

    private static final String TAG = "DemoApplication";

    private static final String RemoteServicePackage = "com.sunyard.sysfuncservice";
    private static final String ACTION = "com.sunyard.sysfuncservice.service";

    private boolean bindSuccess;

    DeviceServiceEngine deviceServiceEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        bindService();
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.setPackage(RemoteServicePackage);
        bindSuccess = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.e("Service", "bindService: " + bindSuccess);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bindSuccess = true;
            deviceServiceEngine = DeviceServiceEngine.Stub.asInterface(iBinder);
//            Toast.makeText(DemoApplication.this, "服务绑定成功", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "服务绑定成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bindSuccess = false;
            deviceServiceEngine = null;
//            Toast.makeText(DemoApplication.this, "服务断开", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "服务断开");
        }
    };

    public DeviceServiceEngine getDeviceServiceEngine() {
        return deviceServiceEngine;
    }
}
