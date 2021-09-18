package com.sunyard.i80demo.zxing;

public interface OnScannedListener{
    void onScanResult(int retCode, byte[] data);
}