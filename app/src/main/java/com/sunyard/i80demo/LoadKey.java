package com.sunyard.i80demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.socsi.exception.PINPADException;
import com.socsi.exception.SDKException;
import com.socsi.smartposapi.ped.Ped;

public class LoadKey extends Activity implements View.OnClickListener {

    TextView tvResult;
    Ped ped = Ped.getInstance();
    EditText et_main;
    EditText et_work;
    EditText et_des;
    String strMainKey;
    String strWorkKey;
    String strDesKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadkey);
        Log.d("yangbo", getClass().getSimpleName());
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_default).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_checkKey).setOnClickListener(this);
        ped = Ped.getInstance();

        et_main = (EditText)findViewById(R.id.et_main_key);
        et_work = (EditText)findViewById(R.id.et_work_key);
        et_des = (EditText)findViewById(R.id.et_des_key);

        //Main key
        et_main.setText(Keys.mainKeyCurrent);
        et_main.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','A','B','C','D','E','F'};
            }
            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_TEXT;
            }
        });

        //work key
        et_work.setText(Keys.workKeyCurrent);
        et_work.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','A','B','C','D','E','F'};
            }
            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_TEXT;
            }
        });

        //des key
        et_des.setText(Keys.desKeyCurrent);
        et_des.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','A','B','C','D','E','F'};
            }
            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_TEXT;
            }
        });

        tvResult = (TextView) findViewById(R.id.tv_result);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_default:
                et_main.setText(Keys.mainKeyDefault);
                et_work.setText(Keys.workKeyDefault);
                et_des.setText(Keys.desKeyDefault);

                break;
            case R.id.btn_save:
                strMainKey = et_main.getText().toString();
                strWorkKey = et_work.getText().toString();
                strDesKey = et_des.getText().toString();

                if ( strMainKey.length() != 32)
                {
                    tvResult.setText("The main key length should be 32");
                    break;
                }
                else if (strWorkKey.length() != 32)
                {
                    tvResult.setText("The work key length should be 32");
                    break;
                }
                else if (strDesKey.length() != 32)
                {
                    tvResult.setText("The des key length should be 32");
                    break;
                }
                String retStr = "";

                //load main key
                try {
                    if (loadMainKey(strMainKey))
                    {
                        retStr += "Main key load successed\n";
                        Keys.mainKeyCurrent = strMainKey;
                    }
                    else
                    {
                        retStr += "Main key load failed\n";
                        break;
                    }
                } catch (PINPADException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();    
                }

                //load work key
                try {
                    if (loadWorkKey(strWorkKey))
                    {
                        retStr += "Work key load successed\n";
                        Keys.workKeyCurrent = strWorkKey;
                    }
                    else
                    {
                        retStr += "Work key load failed\n";
                        break;
                    }
                } catch (PINPADException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                }

                //load des key
                try {
                    if (loadDesKey(strWorkKey))
                    {
                        retStr += "Des key load successed\n";
                        Keys.desKeyCurrent = strWorkKey;
                    }
                    else
                    {
                        retStr += "Des key load failed\n";
                        break;
                    }
                } catch (PINPADException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                }
                tvResult.setText(retStr);
                break;

            case R.id.btn_checkKey:
                String retStr2 = "";
                if(ped.isKeyExist(Ped.KEY_TYPE_MASTER_KEY,1))
                    retStr2 += "Main key is loaded\n";
                else
                    retStr2 += "Main key is not loaded\n";

                if(ped.isKeyExist(Ped.WORK_KEY_TYPE_PIN_KEY,2))
                    retStr2 += "Work key is loaded\n";
                else
                    retStr2 += "Work key is not loaded\n";

                if(ped.isKeyExist(Ped.KEY_TYPE_DES_KEY,1))
                    retStr2 += "Des key is loaded\n";
                else
                    retStr2 += "Des key is not loaded\n";
                tvResult.setText(retStr2);

                break;
            default:
                break;
        }
    }

    private boolean loadWorkKey(String str) throws PINPADException, SDKException {
//        boolean b = Ped.getInstance().loadWorkKeyByIdx((byte)1, (byte) 0x02, (byte) 0x02, StringUtil.byte2HexStr(HexStringToByteArray("CE053F0D164701A509DE19297AB69C76")), null);
        boolean b = ped.loadWorkKeyByIdx((byte)1, Ped.WORK_KEY_TYPE_PIN_KEY, (byte) 2, str, null);
        return b;
    }

    private  boolean loadDesKey(String str)throws PINPADException, SDKException{
        boolean b =  ped.loadPlainDesKey((byte)1,str);
        return b;
    }
    private boolean loadMainKey(String str) throws PINPADException, SDKException {
//        boolean b = Ped.getInstance().loadMKey((byte) 0xff, (byte) 1, StringUtil.byte2HexStr(HexStringToByteArray("CE053F0D164701A509DE19297AB69C76")), 1, Ped.KEY_TYPE_UNENCTYPTED_KEY, null, false);
        boolean b = ped.loadMKey((byte) 0xff, (byte) 1, str, 1, Ped.KEY_TYPE_UNENCTYPTED_KEY, null, false);
        return b;
    }

    public static byte[] HexStringToByteArray(String s) {
        if (s == null) {return null;}
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
