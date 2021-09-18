package com.sunyard.i80demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.socsi.smartposapi.printer.Align;
import com.socsi.smartposapi.printer.FontLattice;
import com.socsi.smartposapi.printer.GraphicsCodeUtils;
import com.socsi.smartposapi.printer.PrintRespCode;
import com.socsi.smartposapi.printer.Printer2;
import com.socsi.smartposapi.printer.TextEntity;
import com.sunyard.i80demo.zxing.android.BackActivity;
import com.sunyard.i80demo.zxing.android.FontActivity;
import com.sunyard.i80demo.zxing.android.Intents;

/**
 * Created by wuqin on 2018/3/23.
 */

public class QRCodeActivity extends Activity {
    Bitmap mBitmap;
    private static final int INTENT_FONT = 1;
    private static final int INTENT_BACK = 2;
    Button font;
    Button back;
    Button generateQR;
    Button generateBar;
    Button print;
    ImageView imgQRCode;
    EditText edtQRCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        Log.d("yangbo", getClass().getSimpleName());

        font = (Button) findViewById(R.id.font);
        back = (Button) findViewById(R.id.back);
        generateQR = (Button) findViewById(R.id.btn_generate);
        generateBar = (Button) findViewById(R.id.btn_barcode);
        imgQRCode = (ImageView) findViewById(R.id.img_QRCode);
        edtQRCode = (EditText) findViewById(R.id.scan_result);
        print = (Button) findViewById(R.id.btn_print);

        font.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QRCodeActivity.this, FontActivity.class);
                startActivityForResult(intent, INTENT_FONT);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QRCodeActivity.this, BackActivity.class);
                startActivityForResult(intent, INTENT_BACK);
            }
        });

        generateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt;
                if (edtQRCode.length() > 0) {
                    txt = edtQRCode.getText().toString();
                } else {
                    txt = "www.sunyard.com";
                }
                mBitmap = GraphicsCodeUtils.Create2DCode(txt, BarcodeFormat.QR_CODE, 200, 200, 1);
                imgQRCode.setImageBitmap(big(mBitmap, 2));
            }
        });

        generateBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt;
                if (edtQRCode.length() > 0) {
                    txt = edtQRCode.getText().toString();
                } else {
                    txt = "www.sunyard.com";
                }
                mBitmap = GraphicsCodeUtils.CreateOneDCodeByPixel(txt, 500, 200, BarcodeFormat.CODE_128, 1);
                imgQRCode.setImageBitmap(big(mBitmap, 2));
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap == null) {
                    Toast.makeText(QRCodeActivity.this, "please generate QR first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Printer2 print;
                print = Printer2.getInstance();
                print.appendImage(mBitmap, Align.CENTER);
                print.appendTextEntity2(new TextEntity("\n\n", FontLattice.EIGHT, false, Align.CENTER));

                PrintRespCode printRespCode = print.startPrint();
                if (printRespCode != PrintRespCode.Print_Success) {
                    if (printRespCode == PrintRespCode.Printer_PaperLack || printRespCode == PrintRespCode.print_Unknow) {
                        Toast.makeText(QRCodeActivity.this, "Printer is out of paper", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(QRCodeActivity.this, "Print failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_FONT:
            case INTENT_BACK:
                if (resultCode == RESULT_OK) {
                    String str = data.getExtras().getString(Intents.Scan.QR_RESULT);
                    edtQRCode.setText(str);
                }
                break;
            default:
                break;
        }
    }

    Bitmap big(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
}
