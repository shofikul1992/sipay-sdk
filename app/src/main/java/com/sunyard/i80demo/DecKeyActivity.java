package com.sunyard.i80demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.socsi.exception.PINPADException;
import com.socsi.exception.SDKException;
import com.socsi.smartposapi.ped.MACResult;
import com.socsi.smartposapi.ped.Ped;
import com.socsi.utils.StringUtil;
import com.sunyard.i80demo.util.EncryptKey;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DecKeyActivity extends Activity {

    private static final String TAG = "DecKeyActivity";
    private Ped mPed;
    private TextView tvShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enc_and_dec);
        tvShow = ((TextView) findViewById(R.id.tv_resultShow));
        mPed = Ped.getInstance();

        ExecutorService service = Executors.newCachedThreadPool();
    }

    public void click(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_getPassKey:
                loadPassKey();
                break;
            case R.id.btn_getMainKey:
                loadMainkey();
                break;
            case R.id.btn_getPinKey:
                loadPinKey();
                break;
            case R.id.btn_getMacKey:
                loadMacKey();
                break;
            case R.id.btn_getPinBlock:
                getPinBlock();
                break;
            case R.id.btn_getMac:
                getMac();
                break;
            case R.id.btn_clear:
                tvShow.setText("");
                break;
        }
    }

    //下载明文传输密钥
    private void loadPassKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //第一个参数:表示将传输秘钥存在某一个地方,这个参数表示这个地方的名字,索引
                //明文 不需要传kcv 进行校验
                //注意:需要把返回的明文key通过工具转换成byte[]数组
                boolean flag_PassKey = mPed.loadPlainKEK(EncryptKey.PASS_KEY_INDEX,
                        StringUtil.hexStr2Bytes(EncryptKey.PlAIN_PASS_KEY),
//                    EncryptKey.PlAIN_PASS_KEY.getBytes(),
                        null);

                notifyMsg("loadPassKey: " + flag_PassKey);
                Log.d(TAG, "loadPassKey: " + flag_PassKey);
            }
        }).start();
    }

    //下载主密钥
    private void loadMainkey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag_mainKey = mPed.loadMKey(
                        Ped.KEK_TYPE_TRANS_ENCTYPT_MASTER,          //kekType:传输密钥加密主密钥
                        EncryptKey.MAIN_KEY_INDEX,                  //mKeyIndex:将主密钥存在什么地方
                        EncryptKey.MAIN_KEY,                        //key:主密钥密文
                        EncryptKey.PASS_KEY_INDEX,                  //decMKeyIdx:存放传输秘钥的"地方名字"
                        Ped.KEY_TYPE_ENCTYPTED_KEY,                 //keyType:密钥类型 == DES密文
                        EncryptKey.MAIN_KEY_CHECKVALUE,             //kcv:校验值
                        false                                   //isTmsKey:是否是TMS秘钥
                );

                notifyMsg("loadMainkey: " + flag_mainKey);
                Log.d(TAG, "loadMainkey: " + flag_mainKey);
            }
        }).start();
    }

    private void loadPinKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag_pinKey = mPed.loadWorkKey(EncryptKey.MAIN_KEY_INDEX,
                        Ped.WORK_KEY_TYPE_PIN_KEY,
                        EncryptKey.PIN_KEY,
                        EncryptKey.PIN_KEY_CHECKVALUE);
                notifyMsg("loadPinKey: " + flag_pinKey);
                Log.d(TAG, "loadPinKey: " + flag_pinKey);
            }
        }).start();
    }

    private void loadMacKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag_macKey = mPed.loadWorkKey(EncryptKey.MAIN_KEY_INDEX,
                        Ped.WORK_KEY_TYPE_MAC_KEY,
                        EncryptKey.MAC_KEY,
                        EncryptKey.MAC_KEY_CHECKVALUE);
                notifyMsg("loadMacKey: " + flag_macKey);
                Log.d(TAG, "loadMacKey: " + flag_macKey);
            }
        }).start();
    }

    private void getPinBlock() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //mKeyIdx:The key index, using the group key mode, the work key and the master key share a valid ID value: 1-100
                String pinBlock = mPed.encryptPIN(
                        EncryptKey.MAIN_KEY_INDEX,                              //mKeyIdx:主密钥索引
                        0,                                                  //keysType:default默认
                        Ped.ALGORITHMTYPE_USE_PAN_SUPPLY_F,                    //panFlag:使用主账号加密，密码不足位数补'F'
                        EncryptKey.pan.getBytes(),                             //账号
                        EncryptKey.pin);                                       //密码
                notifyMsg("pinBlock: " + pinBlock);
                Log.d(TAG, " encryptPIN = " + pinBlock);
            }
        }).start();
    }

    private void getMac() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MACResult macResult = mPed.calculateMAC(EncryptKey.MAIN_KEY_INDEX,         //mKeyIndex:主密钥索引
                        Ped.MAC_CALCULATION_MODE_ECB,                                      //type:ECB银联规范ECB算法
                        EncryptKey.MAC_SOURCE,                                             //data:数据源
                        null,                                                       //CBCInitVec:cbc初始值,不填默认0x00
                        Ped.DES_TYPE_DES,                                                 //desType:计算模式 单DES
                        null);                                                    //随机数:可以为空
                Log.d(TAG, "getMac: mac = "+macResult.getMAC());
                notifyMsg("mac: " + StringUtil.hexStr2Str(macResult.getMAC()));
            }
        }).start();
    }

    private void notifyMsg(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvShow.append(s + "\n");
            }
        });
    }
}
