package com.sunyard.i80demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.socsi.smartposapi.printer.Align;
import com.socsi.smartposapi.printer.FontLattice;
import com.socsi.smartposapi.printer.FontType;
import com.socsi.smartposapi.printer.PrintRespCode;
import com.socsi.smartposapi.printer.Printer2;
import com.socsi.smartposapi.printer.TextEntity;
import com.socsi.utils.HexUtil;
import com.socsi.utils.StringUtil;

/**
 * Created by KJ-B012 on 2017/6/29.
 */

public class PrintActivity extends Activity {
    PrintRespCode check;
    Bitmap LineDivider;
    final Context context = this;
    private FontType mCh = FontType.SIMSUM, mEn = FontType.SIMSUM;
    int i = 0 ;

    Printer2 print = Printer2.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        Log.d("yangbo", getClass().getSimpleName());
        initView();
    }

    private void initView() {
        final TextView tvPrint = (TextView) findViewById(R.id.tv_print);
        Button btnPrint = (Button) findViewById(R.id.btn_print);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                print.appendTextEntity2(new TextEntity(tvPrint.getText().toString(), null, false, null));
//                PrintRespCode printRespCode = print.startPrint();

//                String str = "وجهةالتبرع|المبلغ|عدد|المجموع";
//                print.appendTextEntity2(new TextEntity("Arabic left:", null, false,null));
//                print.appendTextEntity2(new TextEntity("وجهةالتبرع|المبلغ|عدد|المجموع", null, false,null));
//                print.appendTextEntity2(new TextEntity("Arabic center:", null, false, Align.CENTER));
//                print.appendTextEntity2(new TextEntity("وجهةالتبرع|المبلغ|عدد|المجموع", null, false,Align.CENTER));
//                print.appendTextEntity2(new TextEntity("Arabic right:", null, false, Align.RIGHT));
//                print.appendTextEntity2(new TextEntity("وجهةالتبرع|المبلغ|عدد|المجموع", null, false,Align.RIGHT));
//
//                print.appendTextEntity2(new TextEntity("中文左对齐打印测试", null, false,null));
//                print.appendTextEntity2(new TextEntity("中文居中打印测试", null, false,Align.CENTER));
//                print.appendTextEntity2(new TextEntity("中文右对齐打印测试", null, false,Align.RIGHT));
//
//                Printer2.getInstance().appendTextEntity2(new TextEntity("Hello", null, false,Align.LEFT,true));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("world", null, false,Align.CENTER,false));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("!", null, false,Align.RIGHT,false));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("Hello", null, false,Align.LEFT,true));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("world", null, false,Align.CENTER,false));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("!", null, false,Align.RIGHT,false));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("Hello", null, false,Align.LEFT,true));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("world", null, false,Align.CENTER,false));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("!", null, false,Align.RIGHT,false));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("Hello", null, false,Align.LEFT,true));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("world", null, false,Align.CENTER,false));
//                Printer2.getInstance().appendTextEntity2(new TextEntity("!", null, false,Align.RIGHT,false));
//
//                Printer2.getInstance().appendTextEntity2(new TextEntity("Hello world!", null, false,Align.LEFT,true));


//                Printer2 printReciept=Printer2.getInstance();
//                String date = ConvertNumberToArabic("09-01-2018");
//                String time = ConvertNumberToArabic("10:18:34");
//                String dueAmt = ConvertNumberToArabic("10.250");
//                String paidAmt = ConvertNumberToArabic("10.250");
//                String Contact = ConvertNumberToArabic("+96279947773");
//                printReciept.appendTextEntity2(new TextEntity("\nتسديد\n", FontLattice.FORTY_EIGHT, true, Align.CENTER));
//                printReciept.appendTextEntity2(new TextEntity("التاريخ :  "+ date, FontLattice.TWENTY_FOUR, true, Align.RIGHT));
//                printReciept.appendTextEntity2(new TextEntity("الوقت :  "+ time , FontLattice.TWENTY_FOUR, true, Align.RIGHT));
//                printReciept.appendTextEntity2(new TextEntity("رقم المعاملة :  " +"MAKIN31355", FontLattice.TWENTY_FOUR, false, Align.RIGHT));
//                printReciept.appendTextEntity2(new TextEntity(" رقم المرجع  :" +"2018010815386", FontLattice.TWENTY_FOUR, false, Align.RIGHT));
//                printReciept.appendTextEntity2(new TextEntity("" , null, false, null));
//                printReciept.appendTextEntity2(new TextEntity("Abcdefghijklmn", FontLattice.THIRTY_TWO, true, Align.CENTER));
//                printReciept.appendTextEntity2(new TextEntity("\n \n", FontLattice.TWENTY_FOUR, true, null));
//                printReciept.appendTextEntity2(new TextEntity(" إسم   :  " + "عمان ابراهيم", FontLattice.THIRTY_TWO, true, Align.RIGHT));
//                printReciept.appendTextEntity2(new TextEntity("رقم التلفون : "+ Contact, FontLattice.THIRTY_TWO, true, Align.RIGHT));
//                printReciept.appendTextEntity2(new TextEntity("\n", FontLattice.EIGHT, false, null));
//                printReciept.appendTextEntity2(new TextEntity("مبلغ الفاتورة   :    " , null, false, Align.RIGHT,true));
//                //printReciept.appendTextEntity2(new TextEntity(Bill_Rec.get("DueAmt"), FontLattice.TWENTY_FOUR, false, Align.LEFT));
//                printReciept.appendTextEntity2(new TextEntity(dueAmt, FontLattice.THIRTY_TWO, true, Align.LEFT,true));
//                printReciept.appendTextEntity2(new TextEntity(" المبلغ المدفوع :    ", null, false, Align.RIGHT,true));
//                //printReciept.appendTextEntity2(new TextEntity(Bill_Rec.get("PaidAmt") , null, false, Align.LEFT));
//                printReciept.appendTextEntity2(new TextEntity(paidAmt, FontLattice.THIRTY_TWO, true, Align.LEFT,true));
//                printReciept.appendImage(LineDivider, Align.CENTER);
//                printReciept.appendTextEntity2(new TextEntity("\n شكرا لك على استخدام تسديد " , FontLattice.TWENTY_FOUR, true, Align.CENTER));
//                printReciept.appendTextEntity2(new TextEntity("\n \n", null, false, null));
//
//                check = printReciept.startPrint();

                printLine();
                appendTextEntity("商户名(MERCHANT NAME):");
                String mchntname = "测试商户个体工商户"+(i+1);
                print.appendTextEntity2(new TextEntity(mchntname,mCh, mEn, FontLattice.TWENTY_FOUR, false, Align.LEFT,true));
                //42域，商户编号
                appendTextEntity("商户号(MERCHANT NO):");
                print.appendTextEntity2(new TextEntity("941121060510004",mCh, mEn, FontLattice.TWENTY_FOUR, false, Align.LEFT,true));
                //41域，终端号
                appendTextEntity("终端编号(TERMINAL NO):");
                print.appendTextEntity2(new TextEntity("60510041",mCh, mEn, FontLattice.TWENTY_FOUR, false, Align.LEFT,true));
                appendTextEntity("操作员号(OPERATOR NO):"+"01");

                appendTextEntity("卡号(CARD NO):");
                String tempString ="  /N";
                tempString ="  /S";
                print.appendTextEntity2(new TextEntity("6226095911375769"+ tempString,mCh, mEn, FontLattice.THIRTY_TWO, true, Align.LEFT,true));

                String transType = null;
                appendTextEntity("交易类型(TRANS TYPE):");

                print.appendTextEntity2(new TextEntity("消费(SALE)",mCh, mEn, FontLattice.THIRTY_TWO, true, Align.RIGHT,true));
                appendTextEntity("批次号(BATCH NO):"+"000001");
                String worktime = "2012";
                if (!"".equals(worktime) && worktime != null) {
                    if (worktime.length()>=4) {
                        worktime = worktime.substring(0, 2)+"/"+worktime.substring(2, 4);
                    }
                }
                appendTextEntity("有效期(EXP DATE):20"+worktime);
                appendTextEntity("凭证号(VOUCHER NO):"+"109909");
                String idrespcode = "132222";
                appendTextEntity("授权码(AUTH NO):"+ "654321");
                appendTextEntity("检索参考号(REFER NO):");
                appendTextEntity( "1234567890123456");

                String datetime = "07月24日";
                appendTextEntity("日期/时间(DATE/TIME):");
                print.appendTextEntity2(new TextEntity(datetime,mCh, mEn, FontLattice.TWENTY_FOUR, false, Align.LEFT,true));
                appendTextEntity("金额(AMOUNT):");
                print.appendTextEntity2(new TextEntity("RMB "+"1.99 ",mCh, mEn, FontLattice.THIRTY_TWO, true, Align.RIGHT,true));

                appendTextEntity("转入卡:"+"622609090909090912");
                appendTextEntity("转入行号:"+"6226090909090909");
                appendTextEntity("转入行名:"+"工商银行");
                appendTextEntity("转入账户名:"+"张三");
                appendTextEntity("转出账户名:"+"李四");
                printLine();
                appendTextEntity("备注(REFERENCE):");
                appendTextEntityFontSixteen("授权码(AUTH NO):"+"123456");
                appendTextEntityFontSixteen("原凭证号(VOUCHER NO):"+"234561");
                String TC = "1A761F136CA9E88A";
                String AID = "A000000333010101";
                String ATC =  "01B5";
                String TSI = "E800";
                String TVR ="0080047000";
                String appLabel = "50424F43204445424954";
                String appPreName = "50424F43204445424954";
                String IAD = "07020103A0A002010A010000009600E1034C3A";
                if(!StringUtil.isEmpty(TC)){
                    appendTextEntityFontSixteen("ARQC:"+TC);
                }
                if(!StringUtil.isEmpty(TSI)){
                    appendTextEntityFontSixteen("TSI:"+TSI);
                }
                if(!StringUtil.isEmpty(AID)){
                    appendTextEntityFontSixteen("AID:"+AID);
                }
                if(!StringUtil.isEmpty(ATC)){
                    appendTextEntityFontSixteen("ATC:"+ATC);
                }
                if(!StringUtil.isEmpty(TVR)){
                    appendTextEntityFontSixteen("TVR:"+TVR);
                }
                if(!StringUtil.isEmpty(IAD)){
                    appendTextEntityFontSixteen("IAD:"+IAD);
                }
                String appLableString = "";
                if(!StringUtil.isEmpty(appLabel)){
                    appLableString ="Appl Label:"+new String(HexUtil.toBCD(appLabel));
                }
                appendTextEntityFontSixteen(appLableString);
                if(!StringUtil.isEmpty(appPreName)){
                    appendTextEntityFontSixteen("Appl Name:"+new String(HexUtil.toBCD(appLabel)));
                }

                appendTextEntity("持卡人签名: ");

                appendTextEntity(" ");
                printLine();
                appendTextEntityFontSixteen("--本人确认以上交易，同意将其记入本卡账户--");
                appendTextEntity(" ");
                appendTextEntity(" ");
                appendTextEntity(" ");

                PrintRespCode printRespCode = print.startPrint();
//
                if (printRespCode != PrintRespCode.Print_Success) {
                    if (printRespCode == PrintRespCode.Printer_PaperLack || printRespCode == PrintRespCode.print_Unknow) {
                        Toast.makeText(PrintActivity.this, "Printer is out of paper", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(PrintActivity.this, "Print failed", Toast.LENGTH_SHORT).show();
                }
                Log.i("PrintActivity", printRespCode.toString().toString());
            }
        });

        findViewById(R.id.btn_Font_cn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog =  new AlertDialog.Builder(PrintActivity.this).setTitle("Please Choose the Chinese Font").setCancelable(false).setSingleChoiceItems(new String[]{"宋体",
                        "报宋简体","黑体","楷体"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch(which){
                            case 0:
                                mCh = FontType.SIMSUM;
                                break;
                            case 1:
                                mCh = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, "/mnt/sdcard/font/fzbsjt.ttf", null);
                                break;
                            case 2:
                                mCh = FontType.DEFAULT;
                                break;
                            case 3:
                                mCh = FontType.SIMKAI;
                                break;
                            default:break;

                        }
                        dialog.dismiss();
                    }
                }
                ).show();
            }
        });

        findViewById(R.id.btn_Font_en).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(PrintActivity.this).setTitle("Please Choose the English Font").setCancelable(false).setSingleChoiceItems(new String[]{"Arial",
                                "dejavu","dekar","euphermia","eurostile","exo2","funtauna","fzbsjt","klartext","mclaren","play","ziline","bruum"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mEn = FontType.SIMSUM;
                                break;
                            case 1:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/mohave.otf");
                                break;
                            case 2:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/dekar.otf");
                                break;
                            case 3:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/euphemia.ttc");
                                break;
                            case 4:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/eurostile.ttf");
                                break;
                            case 5:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/exo2.ttf");
                                break;
                            case 6:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/funtaunamono.otf");
                                break;
                            case 7:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/fzbsjt.ttf");
                                break;
                            case 8:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/klartext.ttf");
                                break;
                            case 9:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/mclaren.ttf");
                                break;
                            case 10:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/play.ttf");
                                break;
                            case 11:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/ziline.ttf");
                                break;
                            case 12:
                                mEn = FontType.APP_SELF_DEFINE;
                                print.setFontsDefineByApp(null, null, "/mnt/sdcard/font/bruum.ttf");
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }}
                ).show();
            }
        });
    }

    public void printLine(){
        appendTextEntity("-------------------------------");
    }
    private int appendTextEntity(String str) {
        int i = Printer2.getInstance().appendTextEntity2(new TextEntity(str, mCh, mEn, FontLattice.TWENTY_FOUR, false, Align.LEFT,true));
        return i;
    }
    private int appendTextEntityFontSixteen(String str) {
        int i = Printer2.getInstance().appendTextEntity2(new TextEntity(str,mCh, mEn, FontLattice.SIXTEEN, false, Align.LEFT,true));
        return i;
    }


    public String ConvertNumberToArabic(String Numbers){
        String temp = Numbers.replace("0","٠").replace("1","١").replace("2","٢").replace("3","٣").replace("4","٤").replace("5","٥").replace("6","٦").replace("7","٧").replace("8","٨").replace("9","٩");
        return temp;
    }

}
