package com.sunyard.i80demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.TextView;

import com.socsi.smartposapi.serialnumber.SerialNumberOperation;
import com.socsi.smartposapi.serialnumber.TerminalSerialNumberInfo;
import com.sunyard.i80demo.util.HexUtil;
import com.sunyard.i80demo.util.StringUtil;

/**
 * Created by KJ-B012 on 2018/5/18.
 */

public class ShowSNActivity extends Activity {

    private TextView tvShowSn, tvShowEnSn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showsn);
        initView();
        showSnAndEnsn();
    }

    private void initView() {
        tvShowSn = (TextView) findViewById(R.id.show_sn);
        tvShowEnSn = (TextView) findViewById(R.id.show_EnSn);
    }

    private void showSnAndEnsn() {
        String sn = getSN();
        String enSn = getEncryptedSn();
        if(TextUtils.isEmpty(sn)){
            tvShowSn.setText("SN:  ");
        } else {
            tvShowSn.setText("SN:  \n" + sn);
        }
        if(TextUtils.isEmpty(enSn)){
            tvShowEnSn.setText("EncryptedSN:  ");
        } else {
            tvShowEnSn.setText("EncryptedSN:  " + enSn);
        }
    }

    private String getSN() {
        TerminalSerialNumberInfo snInfo;
        try {
            snInfo = SerialNumberOperation.getInstance().queryTerminalSn(SerialNumberOperation.TERMINAL_MODEL_I80, (byte) 6);
            if (snInfo != null) {
                return snInfo.getSN();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getEncryptedSn() {
        byte[] res = null;
        try {
            res = SerialNumberOperation.getInstance().getEncryptedSn(SerialNumberOperation.TERMINAL_MODEL_I80,
                    SerialNumberOperation.ALGORITHM_IDENTIFIES_SM4, HexUtil.hexStringToByte("123456"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res != null) {
            return StringUtil.bytesToHexString(res);
        }
        return "";
    }

}
