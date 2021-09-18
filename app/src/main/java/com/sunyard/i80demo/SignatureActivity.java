package com.sunyard.i80demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.socsi.smartposapi.printer.Align;
import com.socsi.smartposapi.printer.FontLattice;
import com.socsi.smartposapi.printer.PrintRespCode;
import com.socsi.smartposapi.printer.Printer2;
import com.socsi.smartposapi.printer.TextEntity;

/**
 * Created by wuqin on 2018/3/22.
 */

public class SignatureActivity extends Activity implements View.OnClickListener{
    ImageView imgSignature;
    Bitmap mBitmap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        Log.d("yangbo", getClass().getSimpleName());

        findViewById(R.id.btnSignature).setOnClickListener(this);
        findViewById(R.id.btnPrint).setOnClickListener(this);
        imgSignature = (ImageView) findViewById(R.id.imgSignature);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPrint:
                if (mBitmap == null){
                    Toast.makeText(SignatureActivity.this, "please signature first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Printer2 print;
                print = Printer2.getInstance();
                print.appendTextEntity2(new TextEntity("signature:\n", FontLattice.TWENTY_FOUR,false,Align.LEFT));
                print.appendImage(mBitmap, Align.CENTER);
                print.appendTextEntity2(new TextEntity("------------------------------------------\n\n\n", FontLattice.TWENTY_FOUR,false,Align.LEFT));

                PrintRespCode printRespCode = print.startPrint();
                if (printRespCode != PrintRespCode.Print_Success) {
                    if (printRespCode == PrintRespCode.Printer_PaperLack || printRespCode == PrintRespCode.print_Unknow) {
                        Toast.makeText(SignatureActivity.this, "Printer is out of paper", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(SignatureActivity.this, "Print failed", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnSignature:
                SignPopupWindow signPopupWindow = new SignPopupWindow(this,imgSignature , "", new SignPopupWindow.OnSignCallback() {
                    @Override
                    public void onSuccess(final Bitmap bitmap, String encode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBitmap = scale(bitmap,2);
                                imgSignature.setImageBitmap(mBitmap);
                            }
                        });
                    }

                    @Override
                    public void onFail(String message) {

                    }
                });
                signPopupWindow.backgroundAlpha(this,0.5f);
                break;
                default:
                    break;
        }
    }
    Bitmap scale(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
}
