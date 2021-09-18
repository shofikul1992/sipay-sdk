package com.sunyard.i80demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.socsi.aidl.pinservice.OperationPinListener;
import com.socsi.smartposapi.card.CardReader;
import com.socsi.smartposapi.card.MagCardSearchCallback;
import com.socsi.smartposapi.emv2.EmvL2;
import com.socsi.smartposapi.gmalgorithm.Dukpt;
import com.socsi.smartposapi.gmalgorithm.PinResult;
import com.socsi.smartposapi.magcard.CardInfo;
import com.socsi.smartposapi.ped.MACResult;
import com.socsi.smartposapi.ped.Ped;
import com.socsi.utils.HexUtil;
import com.socsi.utils.Log;

import static android.content.ContentValues.TAG;
import static com.sunyard.i80demo.util.Util.BytesToString;
import static com.sunyard.i80demo.util.Util.StringToBytes;
import static com.sunyard.i80demo.util.Util.concat;

public class DukptActivity extends Activity {

    String pan = "";
    TextView tvResult;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dukpt);
        android.util.Log.d("yangbo", getClass().getSimpleName());

        int ret = EmvL2.getInstance(this, getPackageName()).init();
        if (ret != 0) {
            Log.e("emv init fail");
        }
        tvResult = (TextView) findViewById(R.id.tv_result);
        init();


    }

    private void init() {


        final String DukptKey = "FE9C1B69FB3FAE71FE9C1B69FB3FAE71";
//        final String DukptKey = "0719B99D050E8F1545E7F28DD6B04A1B";
//        final String DukptKey = "4B2274C4EF21FE7EAC28D40069F444DD";
        final String TransportKey = "10E203B3125B3ADC3670481501A57C0E";
        final String ksn = "FFFF9876543210E00000";
//        final String ksn = "BD00015C220EAB000000";
        final String kcv = "A01EB0F6C2758F3C";  //you can use the last button's function to get the kcv
        //load key
        findViewById(R.id.btn_loadDukpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                try {
//                    Ped.getInstance().formatKeyArea();
//                } catch (PINPADException e) {
//                    e.printStackTrace();
//                } catch (SDKException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    Ped.getInstance().loadPlainKEK((byte)0x2,StringToBytes(TransportKey),null);
//                } catch (SDKException e) {
//                    e.printStackTrace();
//                } catch (PINPADException e) {
//                    e.printStackTrace();
//                }

//                try {
//                    Dukpt.getInstance().loadDukptKey((byte)0x2,(byte)0x02,(byte)0x02,StringToBytes(ksn),StringToBytes(DukptKey),StringToBytes(kcv));
                Dukpt.getInstance().loadDukptKey((byte) 0xff, (byte) 0x01, (byte) 0x01, StringToBytes(ksn), StringToBytes(DukptKey), null);
//
                Toast.makeText(getApplication(), "Load DUKPT key success", Toast.LENGTH_LONG).show();
//                } catch (SDKException e) {
//                    e.printStackTrace();
//                } catch (PINPADException e) {
//                    e.printStackTrace();
//                }
            }
        });

        //swipe card
//        findViewById(R.id.btn_useCard).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog("Please swipe your card");
//                CardReader cardReader = CardReader.getInstance();
//                cardReader.setMagCardSearchCallback(new MagCardSearchCallback() {
//
//                    @Override
//                    public void onSearchResult(int ret) {
//                        stopSearch();
//                        switch (ret) {
//                            case 0:
//                                try {
//                                    CardInfo cardInfo = Magcard.getInstance().readUnEncryptTrack();
//                                    pan = cardInfo.getCardNo();
//                                    updateResult(cardInfo.toString());
//                                } catch (SDKException e) {
//                                    e.printStackTrace();
//                                    updateResult("fail to read unecrypt track");
//                                }
//                                break;
//                            case 1:
//                                updateResult("read timeout");
//                                break;
//                            default:
//                                updateResult("read fail:" + ret);
//                                break;
//                        }
//                        dismissDialog();
//                    }
//                });
//                byte type = 0x00;
//                type = (byte) (type | 0x01);    //support magnetic card
//                cardReader.searchCard(type, 20 * 1000);
//            }
//        });

        //encrypt pin
        findViewById(R.id.btn_Pin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use pinpad
                //PIN KEY  EE198B582C69D798938546749CEC79A7
                final Bundle param = new Bundle();
                param.putBoolean("isOnline", true);
//                param.putString("pan", "6274311520010841");  //you can read a card to get the pan
                param.putString("pan", "5523224040426945");
                param.putString("promptString", "Please input the password");
                param.putIntArray("pinLimit", new int[]{4, 5, 6, 7, 8, 9, 10});
//                param.putInt("pinAlgMode", Ped.PAN_FLAG_PAN);
                param.putInt("pinAlgMode", Ped.ALGORITHMTYPE_CBC_USE_PAN_SUPPLY_F);
                param.putByteArray("random", StringToBytes("AACC9675BBA5EF44"));
                param.putInt("desType", Ped.DES_TYPE_3DES);
                param.putInt("keysType", Ped.KEYS_TYPE_DUKPT);
                param.putInt("timeout", 60);

//                try {
//                com.sunyard.smartposapi.emv2.EmvL2.getInstance(DukptActivity.this, DukptActivity.this.getPackageName()).startPinInput(DukptActivity.this, 0x01, param, new OperationPinListener() {
                Ped.getInstance().startPinInput(DukptActivity.this, 0x01, param, new OperationPinListener() {

                    @Override
                    public void onInput(int len, int key) {
                        Log.e(TAG, "onInput  len:" + len + "  key:" + key);
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e(TAG, "onError   errorCode:" + errorCode);
                    }

                    @Override
                    public void onConfirm(byte[] data, boolean isNonePin) {
//                            try {
                        Log.e(TAG, ""+BytesToString(data));
                        updateResult(BytesToString(concat(data, Dukpt.getInstance().getCurrentukptKsn())));
//                            } catch (SDKException e) {
//                                e.printStackTrace();
//                            } catch (PINPADException e) {
//                                e.printStackTrace();
//                            }
                        Log.e(TAG, "onConfirm   data:" + HexUtil.toString(data) + "  isNonePin:" + isNonePin);
                    }

                    @Override
                    public void onCancel() {
                        Log.e(TAG, "onCancel");
                    }
                });
//                } catch (SDKException e) {
//                    e.printStackTrace();
//                }

            }
        });

        //encrypt MAC
        findViewById(R.id.btn_MAC).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et_data = new EditText(DukptActivity.this);
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
                new AlertDialog.Builder(DukptActivity.this).setView(et_data).setTitle("Please input the MAC").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String MAC = et_data.getText().toString();
//                        try {
                        MACResult macResult = Dukpt.getInstance().calculateMAC((byte) 1, Ped.MAC_CALCULATION_MODE_ECB, MAC, null, Ped.DES_TYPE_DES, Ped.MAC_KEY_TYPE_DES, true);
                        updateResult("MAC encrypted: " + macResult.getMAC() + macResult.getKSN());
//                        } catch (SDKException e) {
//                            e.printStackTrace();
//                        } catch (PINPADException e) {
//                            e.printStackTrace();
//                        }

                    }
                }).setNegativeButton("Cancel", null).show();
            }
        });

        //encrypt track
        findViewById(R.id.btn_Track).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("Please swipe your card");
                CardReader cardReader = CardReader.getInstance();
                cardReader.setMagCardSearchCallback(new MagCardSearchCallback() {

                    @Override
                    public void onSearchResult(int ret) {

                        switch (ret) {
                            case 0:
//                                try {
                                //random cannot be null,8 digit
                                CardInfo cardInfo = Dukpt.getInstance().readTrackDataWithEncrypted((byte) 3, StringToBytes("00000000000000000000"),
                                        (byte) 3, (byte) 1, (byte) 1, StringToBytes("0000000000000000"), true);
//                                    updateResult(cardInfo.toString());
                                //track 2
                                Log.i("cardinfo", cardInfo.getEncryptedTk2());
//                                    updateResult("Track encrypted: "+ cardInfo.getEncryptedTk2() + cardInfo.getDupktKSN());
                                updateResult(cardInfo.toString());
//                                } catch (SDKException e) {
//                                    e.printStackTrace();
//                                    updateResult("fail to read encrypted track");
//                                } catch (PINPADException e) {
//                                    e.printStackTrace();
//                                }
                                stopSearch();
                                break;
                            case 1:
                                updateResult("read timeout");
                                break;
                            default:
                                updateResult("read fail:" + ret);
                                break;
                        }
                        dismissDialog();
                    }
                });
                byte type = 0x00;
                type = (byte) (type | 0x01);    //support magnetic card
                cardReader.searchCard(type, 20 * 1000);
            }
        });


        findViewById(R.id.btn_pinblock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    PinResult pinResult =  Dukpt.getInstance().encryptPinblockEcb(1,"06127747ADFFEF7B",Ped.DES_TYPE_3DES);
                PinResult pinResult = Dukpt.getInstance().encryptPinblockCbc(1, "06127747ADFFEF7B", StringToBytes("1122334455667788"), Ped.DES_TYPE_3DES);
                updateResult(pinResult.toString());

//                } catch (SDKException e) {
//                    e.printStackTrace();
//                } catch (PINPADException e) {
//                    e.printStackTrace();
//                }
            }

        });

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String key = "4B2274C4EF21FE7EAC28D40069F444DD";
//                String key = "73BA7A1A3453997EBCC06A5BBF22C389";
//                Ped.getInstance().loadPlainDesKey((byte) 2, key);
//                byte[] result = Ped.getInstance().DESComputation(Ped.DES_COMPUTATATION_KEY_TYPE_DES, (byte) 2, Ped.DES_TYPE_3DES, (byte) 0, StringToBytes("123412341234"));
//                updateResult(BytesToString(result));
                updateResult(BytesToString(Dukpt.getInstance().getCurrentukptKsn()));
            }
        });

    }

    private void showDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
                dialog = new ProgressDialog(DukptActivity.this);
                dialog.setMessage(message);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();
            }
        });
    }

    private void dismissDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void updateResult(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.setText(s);
            }
        });
    }

    public void stopSearch() {
        CardReader.getInstance().stopSearch(7);
    }


}
