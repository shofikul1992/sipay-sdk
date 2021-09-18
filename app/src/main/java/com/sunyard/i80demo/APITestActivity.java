package com.sunyard.i80demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.socsi.exception.SDKException;
import com.socsi.smartposapi.icc.Icc;

/**
 * Created by wuqin on 2018/3/27.
 */

public class APITestActivity extends Activity {
    TextView txtRes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apitest);
        Log.d("yangbo", getClass().getSimpleName());
        txtRes = (TextView) findViewById(R.id.txtRes);
        findViewById(R.id.btn_start_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
                    Icc icc = Icc.getInstance();
                    byte response[];
                    byte apdu[] = {(byte) 0x80, (byte) 0x60, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//                    response = icc.detection(Icc.INIT_TYEP_PSAM2);
                    response = icc.iccInit(Icc.INIT_TYEP_PSAM2);
                    StringBuilder builder = new StringBuilder();
                    builder.append("IC card:" + response[0] + "\n");
                    builder.append("PSAM1 card:" + response[3] + "\n");
                    builder.append("PSAM2 card:" + response[4] + "\n");
                    txtRes.setText(builder.toString());

//                } catch (SDKException e) {
//                    e.printStackTrace();
//                }

            }
        });
    }
}
