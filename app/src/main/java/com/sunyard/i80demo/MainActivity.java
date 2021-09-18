package com.sunyard.i80demo;

import static com.sunyard.i80demo.util.Util.BytesToString;
import static com.sunyard.i80demo.util.Util.StringToBytes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.socsi.aidl.pinservice.OperationPinListener;
import com.socsi.smartposapi.device.beeper.Beeper;
import com.socsi.smartposapi.emv2.EmvL2;
import com.socsi.smartposapi.ped.Ped;
import com.socsi.utils.HexUtil;
import com.socsi.utils.Log;
import com.sunyard.i80demo.util.EncryptKey;

public class MainActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnInitEmv).setOnClickListener(this);
        findViewById(R.id.btnEmv).setOnClickListener(this);
        findViewById(R.id.btnPrint).setOnClickListener(this);
        findViewById(R.id.btn_inputPassword).setOnClickListener(this);
        findViewById(R.id.btn_loadKey).setOnClickListener(this);
        findViewById(R.id.btn_dukpt).setOnClickListener(this);
        findViewById(R.id.btnDesCalc).setOnClickListener(this);
        findViewById(R.id.btnDesCalcDecrypt).setOnClickListener(this);
        findViewById(R.id.CodeOperate).setOnClickListener(this);
        findViewById(R.id.btnBeep).setOnClickListener(this);
        findViewById(R.id.btnSignature).setOnClickListener(this);
        findViewById(R.id.btnQRCode).setOnClickListener(this);
        findViewById(R.id.btnAPITest).setOnClickListener(this);
        findViewById(R.id.btnDecryptKey).setOnClickListener(this);
        findViewById(R.id.showSnAndEncryptedSn).setOnClickListener(this);
        findViewById(R.id.startPinPad).setOnClickListener(this);
        findViewById(R.id.btn_silent_install).setOnClickListener(this);
    }

    boolean isSecurity() {
        return Build.DISPLAY.contains("DBQBJ");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnInitEmv:
                uploadAidAndCapk();
                break;
            case R.id.btnEmv:
                Intent intent = new Intent(MainActivity.this, DoEmvActivity.class);
                startActivity(intent);
                break;
            case R.id.btnPrint:
                startActivity(new Intent(MainActivity.this, PrintActivity.class));
                break;
            case R.id.btn_inputPassword:
                Intent i = new Intent(this, KeyBoardActivity.class);
                Bundle pinpadBundle = new Bundle();
                pinpadBundle.putInt(KeyBoardConfig.BUNDLE_KEY_MKEYIDX, 2);//索引
                pinpadBundle.putInt(KeyBoardConfig.BUNDLE_KEY_TIMEOUT, 60);//超时时间
                pinpadBundle.putInt(KeyBoardConfig.BUNDLE_KEY_pinAlgMode, 3);//加密算法类型
                if (true) {
                    pinpadBundle.putBoolean(KeyBoardConfig.BUNDLE_KEY_ENCRYPTED, true);//是否加密
//                    pinpadBundle.putString(KeyBoardConfig.BUNDLE_KEY_PAN, "");//卡号
                } else {
                    pinpadBundle.putBoolean(KeyBoardConfig.BUNDLE_KEY_ENCRYPTED, false);//是否加密
                }
                pinpadBundle.putString(KeyBoardConfig.BUNDLE_KEY_textAlert, "");//提示
                pinpadBundle.putString(KeyBoardConfig.BUNDLE_KEY_amount, "");//提示
                i.putExtra(KeyBoardConfig.BUNDLE_KEY, pinpadBundle);
                startActivityForResult(i, 0);
                break;
            case R.id.btn_loadKey:
                startActivity(new Intent(MainActivity.this, LoadKey.class));
                break;
            case R.id.btn_dukpt:
                startActivity(new Intent(MainActivity.this, DukptActivity.class));
                break;
            case R.id.btnDesCalc:
                final EditText et_data = new EditText(this);
                et_data.setKeyListener(new NumberKeyListener() {
                    @Override
                    protected char[] getAcceptedChars() {
                        return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};
                    }

                    @Override
                    public int getInputType() {
                        return InputType.TYPE_CLASS_TEXT;
                    }
                });
                et_data.setHint("3132333435363738");
                new AlertDialog.Builder(this).setTitle("Please input the hex data:").setView(et_data).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        byte[] reData = null;
                        String str = et_data.getText().toString();
                        if (str.equals("")) {
                            et_data.setText("3132333435363738");
                            str = "3132333435363738";
                        }
                        reData = Ped.getInstance().DESComputation(Ped.DES_COMPUTATATION_KEY_TYPE_DES, (byte) 1, Ped.DES_TYPE_DES, (byte) 0, StringToBytes(str));
                        if (BytesToString(reData) == "" || BytesToString(reData) == null)
                            Toast.makeText(getApplication(), "Please input 8 bytes or check whether des key is loaded", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplication(), BytesToString(reData), Toast.LENGTH_LONG).show();

                    }
                }).setNegativeButton("Cancle", null).show();
                break;
            case R.id.btnDesCalcDecrypt:
                final EditText et_data2 = new EditText(this);
                et_data2.setKeyListener(new NumberKeyListener() {
                    @Override
                    protected char[] getAcceptedChars() {
                        return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};
                    }

                    @Override
                    public int getInputType() {
                        return InputType.TYPE_CLASS_TEXT;
                    }
                });
                et_data2.setHint("3F1A6DC8A8424462");
                new AlertDialog.Builder(this).setTitle("Please input the hex data:").setView(et_data2).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        byte[] reData = null;
                        String str = et_data2.getText().toString();
                        if (str.equals("")) {
                            et_data2.setText("3F1A6DC8A8424462");
                            str = "3F1A6DC8A8424462";
//                            str= "D966FAEBDD6B7D21";        //9ACDCC992F92829F
                        }
                        reData = Ped.getInstance().DESComputation(Ped.DES_COMPUTATATION_KEY_TYPE_DES, (byte) 1, Ped.DES_TYPE_3DES, (byte) 1, StringToBytes(str));
                        if (BytesToString(reData) == "" || BytesToString(reData) == null)
                            Toast.makeText(getApplication(), "Please input 8 bytes or check whether des key is loaded", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplication(), BytesToString(reData), Toast.LENGTH_LONG).show();

                    }
                }).setNegativeButton("Cancle", null).show();
                break;
            case R.id.CodeOperate:
                Intent iCode = new Intent(MainActivity.this, CodeOperate.class);
                startActivity(iCode);
                break;
            case R.id.btnBeep:
                Beeper.getInstance().startBeep(this, 2000, 1, 100, 3);
                break;
            case R.id.btnSignature:
                startActivity(new Intent(this, SignatureActivity.class));
                break;
            case R.id.btnQRCode:
                startActivity(new Intent(this, QRCodeActivity.class));
                break;
            case R.id.btnAPITest:
                startActivity(new Intent(this, APITestActivity.class));
                break;
            case R.id.btnDecryptKey:
                startActivity(new Intent(this, DecKeyActivity.class));
                break;
            case R.id.showSnAndEncryptedSn:
                startActivity(new Intent(this, ShowSNActivity.class));
                break;
            case R.id.startPinPad:
                openPinpadService();
                break;
            case R.id.btn_silent_install:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean isSuccess = ((DemoApplication) getApplication()).getDeviceServiceEngine().installApp("/mnt/sdcard/test.apk");
                            toast("silent install: " + isSuccess);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            toast("silent install RemoteException");
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //调用密码键盘服务 Invoke password keyboard service
    private void openPinpadService() {
        //PIN KEY  EE198B582C69D798938546749CEC79A7
        Bundle param = new Bundle();
        param.putBoolean("isOnline", true);
        param.putString("pan", EncryptKey.pan);
        param.putString("promptString", "请输入银行卡密码");
        param.putIntArray("pinLimit", new int[]{0, 6});
        param.putInt("pinAlgMode", 0);
        param.putInt("keysType", 0);
        param.putInt("timeout", 60);
        Ped.getInstance().startPinInput(MainActivity.this, 0x01, param, new OperationPinListener() {

            @Override
            public void onInput(int len, int key) {
                Log.e("Mainactivity", "onInput  len:" + len + "  key:" + key);
            }

            @Override
            public void onError(int errorCode) {
                Log.e("Mainactivity", "onError   errorCode:" + errorCode);
            }

            @Override
            public void onConfirm(final byte[] data, boolean isNonePin) {
                Log.e("Mainactivity", "onConfirm   data:" + HexUtil.toString(data) + "  isNonePin:" + isNonePin);
                if (!isNonePin) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "密码:" + HexUtil.toString(data), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancel() {
                Log.e("Mainactivity", "onCancel");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Log.i("my", "Mainactivity:requestCode=0");
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Password:" + data.getStringExtra(Constant.BundleKey.bundlekey_pwd), Toast.LENGTH_SHORT).show();
            } else {
                if (data.getStringExtra(Constant.BundleKey.bundlekey_pwd) == null)
                    Toast.makeText(MainActivity.this, "User cancel or maybe you need load the work key first", Toast.LENGTH_SHORT).show();
                Log.i("my", "Mainactivity:resultcode=0");
            }
        }
//        else if(requestCode == 1)
//        {
//            if (data != null)
//            {
//                currencyCode = data.getExtras().getString("cuCode");
//                countryCode = data.getExtras().getString("coCode");
//            }
//        }

    }

    private void uploadAidAndCapk() {
        //init first
        EmvL2.getInstance(this, getPackageName()).init();
        //upload aid
        String[] aids = new String[]{
                "9f0607a0000000033010df0101009f08020140df1105d84000a800df1205d84004f800df130500100000009f1b0400000000df150400000000df160199df170199df14039f3704df1801009f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0608a000000333010103df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0607a0000000651010df0101009f08020200df1105fc6024a800df1205fc60acf800df130500100000009f1b0400000000df150400000000df160199df170199df14039f3704df1801009f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0607a0000000032010df0101009f08020140df1105d84000a800df1205d84004f800df130500100000009f1b0400000000df150400000000df160199df170199df14039f3704df1801009f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0607a0000000031010df0101009f08020140df1105d84000a800df1205d84004f800df130500100000009f1b0400000000df150400000000df160199df170199df14039f3704df1801009f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0608a000000333010106df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0608a000000333010101df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0607a0000000043060df0101009f08020002df1105fc5058a000df1205f85058f800df130504000000009f1b0400000000df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0608a000000333010102df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0607a0000000041010df0101009f08020002df1105fc5080a000df1205f85080f800df130504000000009f1b0400000000df150400000000df160199df170199df14039f3704df1801009f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0607A0000000049999df0101009f08020140df1105d84000a800df1205d84004f800df130500100000009f1b0400000000df150400000000df160199df170199df14039f3704df1801009f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f0607A0000000046000df0101009f08020140df1105d84000a800df1205d84004f800df130500100000009f1b0400000000df150400000000df160199df170199df14039f3704df1801009f7b06000000100000df1906000000100000df2006000000100000df2106000000100000",
                "9f660430008181"
        };
        int ret = EmvL2.getInstance(this, getPackageName()).clearAids();
        if (ret != 0) {
            Log.e("clear aids fail");
        }
        //or you can add aid one by one
        //like EmvL2.getInstance(this, getPackageName()).addAids(new String[]{"..."});
        //or EmvL2.getInstance(this, getPackageName()).addAids(new EmvAid[]{});
        ret = EmvL2.getInstance(this, getPackageName()).addAids(aids);
        if (ret != 0) {
            Log.e("add aids fail");
        }

        //upload capk
        String[] capks = new String[]{
                "9f0605a0000000039f220101df05083230313231323331df060101df070101df028180c696034213d7d8546984579d1d0f0ea519cff8deffc429354cf3a871a6f7183f1228da5c7470c055387100cb935a712c4e2864df5d64ba93fe7e63e71f25b1e5f5298575ebe1c63aa617706917911dc2a75ac28b251c7ef40f2365912490b939bca2124a30a28f54402c34aeca331ab67e1e79b285dd5771b5d9ff79ea630b75df040103df0314d34a6a776011c7e7ce3aec5f03ad2f8cfc5503cc",
                "9f0605a0000000049f220103df05083230313231323331df060101df070101df028180c2490747fe17eb0584c88d47b1602704150adc88c5b998bd59ce043edebf0ffee3093ac7956ad3b6ad4554c6de19a178d6da295be15d5220645e3c8131666fa4be5b84fe131ea44b039307638b9e74a8c42564f892a64df1cb15712b736e3374f1bbb6819371602d8970e97b900793c7c2a89a4a1649a59be680574dd0b60145df040103df03145addf21d09278661141179cbeff272ea384b13bb",
                "9f0605a0000003339f220109df05083230313231323331df060101df070101df0281b0eb374dfc5a96b71d2863875eda2eafb96b1b439d3ece0b1826a2672eeefa7990286776f8bd989a15141a75c384dfc14fef9243aab32707659be9e4797a247c2f0b6d99372f384af62fe23bc54bcdc57a9acd1d5585c303f201ef4e8b806afb809db1a3db1cd112ac884f164a67b99c7d6e5a8a6df1d3cae6d7ed3d5be725b2de4ade23fa679bf4eb15a93d8a6e29c7ffa1a70de2e54f593d908a3bf9ebbd760bbfdc8db8b54497e6c5be0e4a4dac29e5df040103df0314a075306eab0045baf72cdd33b3b678779de1f527",
                "9f0605a0000003339f220104df05083230313431323331df060101df070101df0281f8bc853e6b5365e89e7ee9317c94b02d0abb0dbd91c05a224a2554aa29ed9fcb9d86eb9ccbb322a57811f86188aac7351c72bd9ef196c5a01acef7a4eb0d2ad63d9e6ac2e7836547cb1595c68bcbafd0f6728760f3a7ca7b97301b7e0220184efc4f653008d93ce098c0d93b45201096d1adff4cf1f9fc02af759da27cd6dfd6d789b099f16f378b6100334e63f3d35f3251a5ec78693731f5233519cdb380f5ab8c0f02728e91d469abd0eae0d93b1cc66ce127b29c7d77441a49d09fca5d6d9762fc74c31bb506c8bae3c79ad6c2578775b95956b5370d1d0519e37906b384736233251e8f09ad79dfbe2c6abfadac8e4d8624318c27daf1df040103df0314f527081cf371dd7e1fd4fa414a665036e0f5e6e5",
                "9f0605a0000000659f220109df05083230313431323331df060101df070101df028180b72a8fef5b27f2b550398fdcc256f714bad497ff56094b7408328cb626aa6f0e6a9df8388eb9887bc930170bcc1213e90fc070d52c8dcd0ff9e10fad36801fe93fc998a721705091f18bc7c98241cadc15a2b9da7fb963142c0ab640d5d0135e77ebae95af1b4fefadcf9c012366bdda0455c1564a68810d7127676d493890bddf040103df03144410c6d51c2f83adfd92528fa6e38a32df048d0a",
                "9f0605a0000000659f220110df05083230313231323331df060101df070101df02819099b63464ee0b4957e4fd23bf923d12b61469b8fff8814346b2ed6a780f8988ea9cf0433bc1e655f05efa66d0c98098f25b659d7a25b8478a36e489760d071f54cdf7416948ed733d816349da2aadda227ee45936203cbf628cd033aaba5e5a6e4ae37fbacb4611b4113ed427529c636f6c3304f8abdd6d9ad660516ae87f7f2ddf1d2fa44c164727e56bbc9ba23c0285df040103df0314c75e5210cbe6e8f0594a0f1911b07418cadb5bab",
                "9f0605a0000003339f22010adf05083230313431323331df060101df070101df028180b2ab1b6e9ac55a75adfd5bbc34490e53c4c3381f34e60e7fac21cc2b26dd34462b64a6fae2495ed1dd383b8138bea100ff9b7a111817e7b9869a9742b19e5c9dac56f8b8827f11b05a08eccf9e8d5e85b0f7cfa644eff3e9b796688f38e006deb21e101c01028903a06023ac5aab8635f8e307a53ac742bdce6a283f585f48efdf040103df0314c88be6b2417c4f941c9371ea35a377158767e4e3",
                "9f0605a0000000039f220108df05083230313431323331df060101df070101df0281b0d9fd6ed75d51d0e30664bd157023eaa1ffa871e4da65672b863d255e81e137a51de4f72bcc9e44ace12127f87e263d3af9dd9cf35ca4a7b01e907000ba85d24954c2fca3074825ddd4c0c8f186cb020f683e02f2dead3969133f06f7845166aceb57ca0fc2603445469811d293bfefbafab57631b3dd91e796bf850a25012f1ae38f05aa5c4d6d03b1dc2e568612785938bbc9b3cd3a910c1da55a5a9218ace0f7a21287752682f15832a678d6e1ed0bdf040103df031420d213126955de205adc2fd2822bd22de21cf9a8",
                "9f0605a0000003339f220102df05083230313431323331df060101df070101df028190a3767abd1b6aa69d7f3fbf28c092de9ed1e658ba5f0909af7a1ccd907373b7210fdeb16287ba8e78e1529f443976fd27f991ec67d95e5f4e96b127cab2396a94d6e45cda44ca4c4867570d6b07542f8d4bf9ff97975db9891515e66f525d2b3cbeb6d662bfb6c3f338e93b02142bfc44173a3764c56aadd202075b26dc2f9f7d7ae74bd7d00fd05ee430032663d27a57df040103df031403bb335a8549a03b87ab089d006f60852e4b8060",
                "9f0605a0000000659f220112df05083230313431323331df060101df070101df0281b0adf05cd4c5b490b087c3467b0f3043750438848461288bfefd6198dd576dc3ad7a7cfa07dba128c247a8eab30dc3a30b02fcd7f1c8167965463626feff8ab1aa61a4b9aef09ee12b009842a1aba01adb4a2b170668781ec92b60f605fd12b2b2a6f1fe734be510f60dc5d189e401451b62b4e06851ec20ebff4522aacc2e9cdc89bc5d8cde5d633cfd77220ff6bbd4a9b441473cc3c6fefc8d13e57c3de97e1269fa19f655215b23563ed1d1860d8681df040103df0314874b379b7f607dc1caf87a19e400b6a9e25163e8",
                "9f0605a0000000039f220107df05083230313231323331df060101df070101df028190a89f25a56fa6da258c8ca8b40427d927b4a1eb4d7ea326bbb12f97ded70ae5e4480fc9c5e8a972177110a1cc318d06d2f8f5c4844ac5fa79a4dc470bb11ed635699c17081b90f1b984f12e92c1c529276d8af8ec7f28492097d8cd5becea16fe4088f6cfab4a1b42328a1b996f9278b0b7e3311ca5ef856c2f888474b83612a82e4e00d0cd4069a6783140433d50725fdf040103df0314b4bc56cc4e88324932cbc643d6898f6fe593b172",
                "9f0605a0000000049f220163df05083230313231323331df060101df070101df028190cf71f040528c9af2bf4341c639b7f31be1abff269633542cf22c03ab51570402c9cafc14437ae42f4e7cad00c9811b536dff3792facb86a0c7fae5fa50ae6c42546c534ea3a11fbd2267f1cf9ac68874dc221ecb3f6334f9c0bb832c075c2961ca9bbb683bec2477d12344e1b7d6dbe07b286fcf41a0f7f1f6f248a8c86398b7fa1c115111051dd01df3ed08985705fddf040103df03146e5ff80cd0a1cc2e3249b9c198d43427ce874013",
                "9f0605a0000000049f220104df05083230313231323331df060101df070101df028190a6da428387a502d7ddfb7a74d3f412be762627197b25435b7a81716a700157ddd06f7cc99d6ca28c2470527e2c03616b9c59217357c2674f583b3ba5c7dcf2838692d023e3562420b4615c439ca97c44dc9a249cfce7b3bfb22f68228c3af13329aa4a613cf8dd853502373d62e49ab256d2bc17120e54aedced6d96a4287acc5c04677d4a5a320db8bee2f775e5fec5df040103df0314381a035da58b482ee2af75f4c3f2ca469ba4aa6c",
                "9f0605a0000000039f220109df05083230313631323331df060101df070101df0281f89d912248de0a4e39c1a7dde3f6d2588992c1a4095afbd1824d1ba74847f2bc4926d2efd904b4b54954cd189a54c5d1179654f8f9b0d2ab5f0357eb642feda95d3912c6576945fab897e7062caa44a4aa06b8fe6e3dba18af6ae3738e30429ee9be03427c9d64f695fa8cab4bfe376853ea34ad1d76bfcad15908c077ffe6dc5521ecef5d278a96e26f57359ffaeda19434b937f1ad999dc5c41eb11935b44c18100e857f431a4a5a6bb65114f174c2d7b59fdf237d6bb1dd0916e644d709ded56481477c75d95cdd68254615f7740ec07f330ac5d67bcd75bf23d28a140826c026dbde971a37cd3ef9b8df644ac385010501efc6509d7a41df040103df03141ff80a40173f52d7d27e0f26a146a1c8ccb29046",
                "9f0605a0000000039f220163df05083230313231323331df060101df070101df028190cf71f040528c9af2bf4341c639b7f31be1abff269633542cf22c03ab51570402c9cafc14437ae42f4e7cad00c9811b536dff3792facb86a0c7fae5fa50ae6c42546c534ea3a11fbd2267f1cf9ac68874dc221ecb3f6334f9c0bb832c075c2961ca9bbb683bec2477d12344e1b7d6dbe07b286fcf41a0f7f1f6f248a8c86398b7fa1c115111051dd01df3ed08985705fddf040103df0314b2f6af1ddc393be17525d0ea7bf568bed5b71167",
                "9f0605a0000000049f220106df05083230313631323331df060101df070101df0281f8cb26fc830b43785b2bce37c81ed334622f9622f4c89aae641046b2353433883f307fb7c974162da72f7a4ec75d9d657336865b8d3023d3d645667625c9a07a6b7a137cf0c64198ae38fc238006fb2603f41f4f3bb9da1347270f2f5d8c606e420958c5f7d50a71de30142f70de468889b5e3a08695b938a50fc980393a9cbce44ad2d64f630bb33ad3f5f5fd495d31f37818c1d94071342e07f1bec2194f6035ba5ded3936500eb82dfda6e8afb655b1ef3d0d7ebf86b66dd9f29f6b1d324fe8b26ce38ab2013dd13f611e7a594d675c4432350ea244cc34f3873cba06592987a1d7e852adc22ef5a2ee28132031e48f74037e3b34ab747fdf040103df0314f910a1504d5ffb793d94f3b500765e1abcad72d9",
                "9f0605a0000000049f220105df05083230313431323331df060101df070101df0281b0b8048abc30c90d976336543e3fd7091c8fe4800df820ed55e7e94813ed00555b573feca3d84af6131a651d66cff4284fb13b635edd0ee40176d8bf04b7fd1c7bacf9ac7327dfaa8aa72d10db3b8e70b2ddd811cb4196525ea386acc33c0d9d4575916469c4e4f53e8e1c912cc618cb22dde7c3568e90022e6bba770202e4522a2dd623d180e215bd1d1507fe3dc90ca310d27b3efccd8f83de3052cad1e48938c68d095aac91b5f37e28bb49ec7ed597df040103df0314ebfa0d5d06d8ce702da3eae890701d45e274c845",
                "9f0605a0000003339f220101df05083230313431323331df060101df070101df028180bbe9066d2517511d239c7bfa77884144ae20c7372f515147e8ce6537c54c0a6a4d45f8ca4d290870cda59f1344ef71d17d3f35d92f3f06778d0d511ec2a7dc4ffeadf4fb1253ce37a7b2b5a3741227bef72524da7a2b7b1cb426bee27bc513b0cb11ab99bc1bc61df5ac6cc4d831d0848788cd74f6d543ad37c5a2b4c5d5a93bdf040103df0314e881e390675d44c2dd81234dce29c3f5ab2297a0",
                "9f0605a0000003339f220108df05083230323031323331df060101df070101df028190b61645edfd5498fb246444037a0fa18c0f101ebd8efa54573ce6e6a7fbf63ed21d66340852b0211cf5eef6a1cd989f66af21a8eb19dbd8dbc3706d135363a0d683d046304f5a836bc1bc632821afe7a2f75da3c50ac74c545a754562204137169663cfcc0b06e67e2109eba41bc67ff20cc8ac80d7b6ee1a95465b3b2657533ea56d92d539e5064360ea4850fed2d1bfdf040103df0314ee23b616c95c02652ad18860e48787c079e8e85a",
                "9f0605a0000003339f220103df05083230313431323331df060101df070101df0281b0b0627dee87864f9c18c13b9a1f025448bf13c58380c91f4ceba9f9bcb214ff8414e9b59d6aba10f941c7331768f47b2127907d857fa39aaf8ce02045dd01619d689ee731c551159be7eb2d51a372ff56b556e5cb2fde36e23073a44ca215d6c26ca68847b388e39520e0026e62294b557d6470440ca0aefc9438c923aec9b2098d6d3a1af5e8b1de36f4b53040109d89b77cafaf70c26c601abdf59eec0fdc8a99089140cd2e817e335175b03b7aa33ddf040103df031487f0cd7c0e86f38f89a66f8c47071a8b88586f26",
                "9f0605a0000003339f22010bdf05083230313631323331df060101df070101df0281f8cf9fdf46b356378e9af311b0f981b21a1f22f250fb11f55c958709e3c7241918293483289eae688a094c02c344e2999f315a72841f489e24b1ba0056cfab3b479d0e826452375dcdbb67e97ec2aa66f4601d774feaef775accc621bfeb65fb0053fc5f392aa5e1d4c41a4de9ffdfdf1327c4bb874f1f63a599ee3902fe95e729fd78d4234dc7e6cf1ababaa3f6db29b7f05d1d901d2e76a606a8cbffffecbd918fa2d278bdb43b0434f5d45134be1c2781d157d501ff43e5f1c470967cd57ce53b64d82974c8275937c5d8502a1252a8a5d6088a259b694f98648d9af2cb0efd9d943c69f896d49fa39702162acb5af29b90bade005bc157df040103df0314bd331f9996a490b33c13441066a09ad3feb5f66c",
                "9f0605a0000000659f220114df05083230313631323331df060101df070101df0281f8aeed55b9ee00e1eceb045f61d2da9a66ab637b43fb5cdbdb22a2fbb25be061e937e38244ee5132f530144a3f268907d8fd648863f5a96fed7e42089e93457adc0e1bc89c58a0db72675fbc47fee9ff33c16ade6d341936b06b6a6f5ef6f66a4edd981df75da8399c3053f430eca342437c23af423a211ac9f58eaf09b0f837de9d86c7109db1646561aa5af0289af5514ac64bc2d9d36a179bb8a7971e2bfa03a9e4b847fd3d63524d43a0e8003547b94a8a75e519df3177d0a60bc0b4bab1ea59a2cbb4d2d62354e926e9c7d3be4181e81ba60f8285a896d17da8c3242481b6c405769a39d547c74ed9ff95a70a796046b5eff36682dc29df040103df0314c0d15f6cd957e491db56dcdd1ca87a03ebe06b7b"
        };
        ret = EmvL2.getInstance(this, getPackageName()).clearCapks();
        if (ret != 0) {
            Log.e("clear capks fail");
        }
        //or you can add capk one by one
        //like EmvL2.getInstance(this, getPackageName()).addCapks(new String[]{"..."});
        //or EmvL2.getInstance(this, getPackageName()).addAids(new EmvCapk[]{});
        ret = EmvL2.getInstance(this, getPackageName()).addCapks(capks);
        if (ret != 0) {
            Log.e("add capks fail");
        }
        Toast.makeText(this, "init emv success", Toast.LENGTH_SHORT).show();
    }
}
