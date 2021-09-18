package com.sunyard.i80demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.socsi.exception.SDKException;
import com.socsi.smartposapi.emv2.EmvL2;
import com.socsi.smartposapi.terminal.DeviceInfo;
import com.socsi.smartposapi.terminal.TerminalManager;

import socsi.middleware.emvl2lib.EmvApi;
import socsi.middleware.emvl2lib.EmvTermConfig;

import static com.sunyard.i80demo.util.Util.BytesToString;
import static com.sunyard.i80demo.util.Util.StringToBytes;

public class CodeOperate extends Activity implements View.OnClickListener{

    String currencyCode ;
    String countryCode;
    EmvApi emvApi;
    EmvTermConfig emvconfig;
    TextView tv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_device_data);
        Log.d("yangbo", getClass().getSimpleName());
        findViewById(R.id.btn_Getdeviceno).setOnClickListener(this);
        findViewById(R.id.btn_getCountryCode).setOnClickListener(this);
        findViewById(R.id.btn_getCurrencyCode).setOnClickListener(this);
        findViewById(R.id.btn_setCountryCode).setOnClickListener(this);
        findViewById(R.id.btn_setCurrencyCode).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        tv_result = (TextView)findViewById(R.id.btn_result);

        EmvL2.getInstance(this, getPackageName()).init();
        emvApi = EmvL2.getInstance(this, getPackageName()).getEmvApi();
        emvconfig  = emvApi.getTermConfig();

        currencyCode = BytesToString(emvconfig.getTransCurrCode());
        countryCode =  BytesToString(emvconfig.getCountryCode());

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        dialog.setCancelable(false);
        dialog.setView(et);
        dialog.setNegativeButton("Cancel",null);


        switch (id) {
            case R.id.btn_Getdeviceno:
                String SN = "";
//                try {
                    SN = TerminalManager.getInstance().getSN();
//                } catch (SDKException e) {
//                    e.printStackTrace();
//                }
                tv_result.setText("SN: "+SN);
                break;
            case R.id.btn_getCurrencyCode:
                String curcode = BytesToString(emvconfig.getTransCurrCode());
                tv_result.setText("CurrencyCode: "+curcode);
                break;
            case R.id.btn_getCountryCode:
                String coucode = BytesToString(emvconfig.getCountryCode());
                tv_result.setText("CountryCode: "+coucode);
                break;
            case R.id.btn_setCurrencyCode:
                dialog.setTitle("Please input the currency code");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String string =et.getText().toString();
                        if(!string.equals("") && string.length() >= 4){
                            currencyCode = et.getText().toString();
                            emvconfig.setTransCurrCode(StringToBytes(currencyCode));
                            emvApi.setTermConfig(emvconfig);
                            tv_result.setText("Set currency code "+string+" success");
                        }
                        else
                            tv_result.setText("Set failed,please enter at least 4 numbers");
                    }
                });
                dialog.show();
                break;
            case R.id.btn_setCountryCode:
                dialog.setTitle("Please input the country code");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String string =et.getText().toString();
                        if(!string.equals("") && string.length() >= 4){
                            countryCode = et.getText().toString();
                            emvconfig.setCountryCode(StringToBytes(countryCode));
                            emvApi.setTermConfig(emvconfig);
                            tv_result.setText("Set country code "+string+" success");
                        }
                        else
                            tv_result.setText("Set failed,please enter at least 4 numbers");
                    }
                });
                dialog.show();
                break;
            case R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }
}
