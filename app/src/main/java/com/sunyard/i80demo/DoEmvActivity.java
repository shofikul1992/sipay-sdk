package com.sunyard.i80demo;

import static android.content.ContentValues.TAG;
import static com.sunyard.i80demo.util.Util.BytesToString;
import static com.sunyard.i80demo.util.Util.StringToBytes;
import static com.sunyard.i80demo.util.Util.concat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.socsi.aidl.pinservice.OperationPinListener;
import com.socsi.smartposapi.card.CardReader;
import com.socsi.smartposapi.card.IcCardSearchCallback;
import com.socsi.smartposapi.card.MagCardSearchCallback;
import com.socsi.smartposapi.card.RfSearchCallback;
import com.socsi.smartposapi.emv2.AsyncEmvCallback;
import com.socsi.smartposapi.emv2.EmvL2;
import com.socsi.smartposapi.gmalgorithm.Dukpt;
import com.socsi.smartposapi.icc.Icc;
import com.socsi.smartposapi.magcard.CardInfo;
import com.socsi.smartposapi.magcard.Magcard;
import com.socsi.smartposapi.ped.Ped;
import com.socsi.smartposapi.terminal.TerminalManager;
import com.socsi.utils.DateUtil;
import com.socsi.utils.HexUtil;
import com.socsi.utils.Log;
import com.socsi.utils.StringUtil;
import com.socsi.utils.TlvUtil;
import com.sunyard.i80demo.util.EncryptKey;
import com.sunyard.i80demo.util.Tlv;
import com.sunyard.i80demo.util.TlvUtils;
import com.sunyard.i80demo.util.Util;
import com.sunyard.middleware.emvl2lib.EmvCallbackGetPinResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import socsi.middleware.emvl2lib.EmvAidCandidate;
import socsi.middleware.emvl2lib.EmvApi;
import socsi.middleware.emvl2lib.EmvCallback;
import socsi.middleware.emvl2lib.EmvErrorCode;
import socsi.middleware.emvl2lib.EmvStartProcessParam;
import socsi.middleware.emvl2lib.EmvTermConfig;

public class DoEmvActivity extends Activity {

    TextView tvResult;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_emv);
        android.util.Log.d("yangbo", getClass().getSimpleName());

        int ret = EmvL2.getInstance(this, getPackageName()).init();
        if (ret != 0) {
            Log.e("emv init fail");
        }

        Button btnSearch = (Button) findViewById(R.id.btnSearchCard);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvResult.setText("");
                showDialog("please use your card");
                searchCard();
            }
        });
        tvResult = (TextView) findViewById(R.id.tvResult);
    }

    private void searchCard() {
        try {
            CardReader cardReader = CardReader.getInstance();
            cardReader.setMagCardSearchCallback(new MagCardSearchCallback() {

                @Override
                public void onSearchResult(int ret) {
                    switch (ret) {
                        case 0:
                            //TODO
                            CardInfo cardInfo = Magcard.getInstance().readUnEncryptTrack();
//                            CardInfo cardInfo = Dukpt.getInstance().readTrackDataWithEncrypted((byte) 3, StringToBytes("00000000000000000000"),
//                                    (byte) 3, (byte) 1, (byte) 1, StringToBytes("0000000000000000"), true);
//                            cardInfo.setCardNo(cardInfo.getCardNo().replace("E", "0"));
                            updateResult(cardInfo.toString());
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
            cardReader.setIcCardSearchCallback(new IcCardSearchCallback() {
                @Override
                public void onSearchResult(int ret) {
                    switch (ret) {
                        case 0:
                            startProcess(EmvStartProcessParam.EMV_API_CHANNEL_FROM_ICC);
                            return;
                        case 1:
                            updateResult("read timeout");
                            break;
                        case 2:
                            updateResult("read fail: unknown card type");
                            break;
                        default:
                            updateResult("read fail:" + ret);
                            break;
                    }
                    dismissDialog();
                }
            });
            cardReader.setRfSearchCallback(new RfSearchCallback() {
                @Override
                public void onSearchResult(int ret, int cardType, byte[] SerialNumber, byte[] ATQA) {
                    if (ret == 0) {
//                        doMasterRf();
                        startProcess(EmvStartProcessParam.EMV_API_CHANNEL_FORM_PICC);
                        return;
                    } else if (ret == 1) {
                        updateResult("read timeout");
                    } else {
                        updateResult("read fail:" + ret);
                    }
                    dismissDialog();
                }
            });
            byte type = 0x00;
            type = (byte) (type | 0x01);    //support magnetic card
            type = (byte) (type | 0x02);    //support ic card
            type = (byte) (type | 0x04);    //support non-contact card
            cardReader.searchCard(type, 20 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
            stopSearch();
            updateResult("search card fail");
            dismissDialog();
        }
    }

    public void stopSearch() {
        CardReader.getInstance().stopSearch(7);
    }

    /**
     * start emv process
     *
     * @param channelType card type,
     */
    public void startProcess(int channelType) {
        showDialog("reading card, please wait");
        EmvL2.getInstance(this, getPackageName()).resetProcess();
        EmvStartProcessParam emvStartProcessParam = new EmvStartProcessParam();
        String SN = TerminalManager.getInstance().getSN();
        emvStartProcessParam.mIfdSerialNum = SN.getBytes();
        if (channelType == EmvStartProcessParam.EMV_API_CHANNEL_FORM_PICC) {
            emvStartProcessParam.mProcType = EmvStartProcessParam.EMV_API_PROC_QPBOC;
        } else {
            emvStartProcessParam.mProcType = EmvStartProcessParam.EMV_API_PROC_PBOC_FULL;
        }
//        emvStartProcessParam.mProcType = EmvStartProcessParam.EMV_API_PROC_PBOC_FULL;
        emvStartProcessParam.mTransType = EmvStartProcessParam.EMV_API_TRANS_TYPE_NORMAL;
        //交易计数器
        emvStartProcessParam.mSeqNo = 100;
        //交易金额,单位分
        emvStartProcessParam.mTransAmt = 1000;
        //返现金额
        emvStartProcessParam.mCashbackAmt = 0;
        emvStartProcessParam.mTransDate = StringUtil.hexStr2Bytes("20180101");
        emvStartProcessParam.mTransTime = StringUtil.hexStr2Bytes("123344");
        //交易类型，TAG 9C的值
        emvStartProcessParam.mTag9CValue = 0x00;
        //是否支持电子现金
        emvStartProcessParam.mIsSupportEC = true;
        //通道类型,EMV_API_CHANNEL_FROM_ICC表示接触卡 EMV_API_CHANNEL_FORM_PICC表示非接触卡
        emvStartProcessParam.mChannelType = channelType;
        //非电子现金接口，强制联机
        emvStartProcessParam.mIsQpbocForceOnline = true;
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

//        EmvL2.getInstance(this, getPackageName()).setDukptParam((byte) 1, Ped.DES_TYPE_3DES, (byte)0x01, StringToBytes("1122334455667788"));
        EmvTermConfig emvTermConfig = new EmvTermConfig();
        emvTermConfig.setIsOfflinePinSafe(1);
        EmvL2.getInstance(this, getPackageName()).setTermConfig(emvTermConfig);
        List<byte[]> bt = EmvL2.getInstance(this, getPackageName()).getAids();
        EmvL2.getInstance(this, getPackageName()).startProcess(emvStartProcessParam, new AsyncEmvCallback() {


            @Override
            public void confirmTransType(byte[] aid, int oldTransType, EmvL2.ConfirmTransTypeHandler handler) {
                handler.onConfirmTransType(oldTransType);
            }

            @Override
            public void panCofirm(byte[] pan, EmvL2.PanConfirmHandler panConfirmHandler) {
                Log.d("EMVDevice process startProcess panCofirm");
                //0: success, others: fail
                panConfirmHandler.onPanConfirm(0);
            }

            @Override
            public void getPin(int type, int retryTimes, final EmvL2.GetPinHandler getPinHandler) {
                Log.d("EMVDevice process startProcess getPin");
                boolean isOnlinePin = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle param = new Bundle();
                        param.putBoolean("isOnline", true);
//                param.putString("pan", "6274311520010841");  //you can read a card to get the pan
//                param.putString("pan", "5523224040426945");
                        param.putString("promptString", "Please input the password");
                        param.putIntArray("pinLimit", new int[]{4, 5, 6, 7, 8, 9, 10});
//                param.putInt("pinAlgMode", Ped.PAN_FLAG_PAN);
                        param.putInt("pinAlgMode", Ped.ALGORITHMTYPE_CBC_USE_PAN_SUPPLY_F);
                        param.putByteArray("random", StringToBytes("AACC9675BBA5EF44"));
                        param.putInt("desType", Ped.DES_TYPE_3DES);
                        param.putInt("keysType", Ped.KEYS_TYPE_DUKPT);
                        param.putInt("timeout", 60);

//                try {
                        //For MasterCard contactless transaction
//               com.sunyard.smartposapi.emv2.EmvL2.getInstance(DoEmvActivity.this, DoEmvActivity.this.getPackageName()).startPinInput(DoEmvActivity.this, 0x01, param, new OperationPinListener() {
                        //For other all transaction

                        Ped.getInstance().startPinInput(DoEmvActivity.this, 0x01, param, new OperationPinListener() {

                            @Override
                            public void onInput(int len, int key) {
                                Ped.getInstance().cancelPinRequest();
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
                                getPinHandler.onGetPin(EmvCallbackGetPinResult.CV_PIN_SUCC, data);
                                Log.e(TAG, "onConfirm   data:" + HexUtil.toString(data) + "  isNonePin:" + isNonePin);
                            }

                            @Override
                            public void onCancel() {
                                Log.e(TAG, "onCancel");
                                getPinHandler.onGetPin(EmvCallbackGetPinResult.CV_PIN_CANCLE,new byte[]{0x00, 0x00});
                            }
                        });
//                } catch (SDKException e) {
//                    e.printStackTrace();
//                }
                    }
                }).start();
//                if (type == EmvCallback.EMV_ONLINEPIN || type == EmvCallback.EMV_EC_ONLINEPIN) {
//                    isOnlinePin = true;
//                } else if (type == EmvCallback.EMV_OFFLINEPIN || type == EmvCallback.EMV_OFFLINE_ONLY) {
//                    isOnlinePin = false;
//                } else {
//                    Log.d("EMVDevice SyncEmvCallback getPin type error:" + type);
//                }
//

            }

            @Override
            public int onVerifyOfflinePin(byte b, int i) {
                return EmvCallbackGetPinResult.CV_PIN_SUCC;
            }

            @Override
            public void aidSelect(EmvAidCandidate[] emvAidCandidates, int times, EmvL2.AidSelectHandler aidSelectHandler) {
                Log.d("EMVDevice process startProcess aidSelect");
                aidSelectHandler.onAidSelect(0);
            }

            @Override
            public void termRiskManager(byte[] pan, int panSn, EmvL2.TermRiskManageHandler termRiskManageHandler) {
                Log.d("EMVDevice process startProcess termRiskManager");
                termRiskManageHandler.onTermRiskManager(0, 0);
            }

            @Override
            public void issuerReference(byte[] bytes, EmvL2.IssuerReferenceHandler issuerReferenceHandler) {
                Log.d("EMVDevice process startProcess issuerReference");
                issuerReferenceHandler.onIssuerReference(0);
            }

            @Override
            public void accountTypeSelect(EmvL2.AccountTypeSelectHandler accountTypeSelectHandler) {
                accountTypeSelectHandler.onAccountType(EmvCallback.EMV_ACCOUNT_TYPE_DEFAULT);
            }

            @Override
            public void certConfirm(int type, byte[] certNum, EmvL2.CertConfirmHandler certConfirmHandler) {
                Log.d("EMVDevice process startProcess certConfirm");
                certConfirmHandler.onCertConfirm(1);
            }

            @Override
            public void lcdMsg(byte[] bytes, byte[] bytes1, boolean b, int i, EmvL2.LcdMsgHandler lcdMsgHandler) {
                Log.d("EMVDevice process startProcess lcdMsg");
                lcdMsgHandler.onLcdMsg(1);
            }

            @Override
            public void confirmEC(EmvL2.ConfirmEcHandler confirmEcHandler) {
                Log.d("EMVDevice process startProcess confirmEC");
                confirmEcHandler.onConfirmEc(-1);
            }

            @Override
            public void processResult(int ret) {
                Log.d("EMVDevice process startProcess processResult:" + ret);
                int code = 1;
                String desc = "";
                switch (ret) {
                    case EmvApi.EMV_TRANS_FALLBACK:
                        desc = "EMV_TRANS_FALLBACK";
                        break;
                    case EmvApi.EMV_TRANS_TERMINATE:
                        desc = "EMV_TRANS_TERMINATE";
                        break;
                    case EmvApi.EMV_TRANS_ACCEPT:
                        code = 0;
                        desc = "EMV_TRANS_ACCEPT";
                        break;
                    case EmvApi.EMV_TRANS_DENIAL:
                        desc = "EMV_TRANS_DENIAL";
                        break;
                    case EmvApi.EMV_TRANS_GOONLINE:
//                        code = 0;
//                        desc = "联机";
                        updateICCardData();
                        dismissDialog();
                        return;
                    case EmvApi.EMV_TRANS_QPBOC_ACCEPT:
                        code = 0;
                        desc = "EMV_TRANS_QPBOC_ACCEPT";
                        break;
                    case EmvApi.EMV_TRANS_QPBOC_DENIAL:
                        desc = "EMV_TRANS_QPBOC_DENIAL";
                        break;
                    case EmvApi.EMV_TRANS_QPBOC_GOONLINE:
                        code = 0;
                        desc = "EMV_TRANS_QPBOC_GOONLINE";
                        updateICCardData();
                        dismissDialog();
                        return;
//                        break;
                    case EmvErrorCode.EMV_ERR_BASE:
                        desc = "EMV_ERR_BASE";
                        break;
                    case -1334:
                        Icc.getInstance().close(Icc.INIT_TYEP_RF);
                        doMasterRf();
                        return;
                }
                dismissDialog();
                updateResult("code:" + code + ",desc:" + desc);
            }
        });
    }

    private void doMasterRf() {
        final com.sunyard.smartposapi.emv2.EmvL2 emvL2 = com.sunyard.smartposapi.emv2.EmvL2.getInstance(this.getApplicationContext(), this.getPackageName());

        int channelType = com.sunyard.middleware.emvl2lib.EmvStartProcessParam.EMV_API_CHANNEL_FORM_PICC;
        com.sunyard.middleware.emvl2lib.EmvStartProcessParam param = new com.sunyard.middleware.emvl2lib.EmvStartProcessParam();
        param.mTransAmt = 2;
        param.mTransDate = StringUtil.hexStr2Bytes(DateUtil.getCurDateStr("yyMMdd"));
        Log.i("domasterrf",BytesToString(param.mTransDate));
        param.mTransTime = StringUtil.hexStr2Bytes(DateUtil.getCurDateStr("HHmmss"));
        param.mChannelType = channelType;
        param.mTag9CValue = 0;

        param.mProcType = EmvStartProcessParam.EMV_API_PROC_QPBOC;//PBOC标准借贷记流程

//        emvL2.setDukptParam((byte) 1, Ped.DES_TYPE_3DES, (byte)0x02, null);

        emvL2.startProcess(param, new com.sunyard.smartposapi.emv2.AsyncEmvCallback() {
            @Override
            public void panConfirm(final byte[] pan, final com.sunyard.smartposapi.emv2.EmvL2.PanConfirmHandler handler) {
                updateResult("[panConfirm]pan:" + new String(pan));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(DoEmvActivity.this)
                                .title("Pan Confirm")
                                .content(new String(pan))
                                .positiveText("confirm")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        handler.onPanConfirm(0);
                                    }
                                })
                                .negativeText("cancel")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        handler.onPanConfirm(1);
                                    }
                                })
                                .show();
                    }
                });
            }

            @Override
            public void getPin(final int type, final int retryTimes, final com.sunyard.smartposapi.emv2.EmvL2.GetPinHandler handler) {
                updateResult("[getPin]type:" + type + ", retryTimes:" + retryTimes);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle param = new Bundle();
                        param.putBoolean("isOnline", true);
                        param.putString("pan", EncryptKey.pan);
                        param.putString("promptString", "请输入银行卡密码");
                        param.putIntArray("pinLimit", new int[]{0, 6});
                        param.putInt("pinAlgMode", 0);
                        param.putInt("keysType", 0);
                        param.putInt("timeout", 60);
                        com.sunyard.smartposapi.emv2.EmvL2.getInstance(DoEmvActivity.this, DoEmvActivity.this.getPackageName()).startPinInput(DoEmvActivity.this, 0x01, param, new OperationPinListener() {

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
                                            Toast.makeText(DoEmvActivity.this, "密码:" + HexUtil.toString(data), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                handler.onGetPin(EmvCallbackGetPinResult.CV_PIN_SUCC, data);
                            }

                            @Override
                            public void onCancel() {
                                Log.e("Mainactivity", "onCancel");
                            }
                        });
                    }
                }).start();

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String title = "please input password";
//                        if (type == com.sunyard.middleware.emvl2lib.EmvCallback.EMV_OFFLINEPIN) {
//                            title = String.format(Locale.getDefault(), "please input offline password，remain %d times", retryTimes);
//                        } else if (type == com.sunyard.middleware.emvl2lib.EmvCallback.EMV_OFFLINE_ONLY) {
//                            title = "please input offline password，last times";
//                        } else if (type == com.sunyard.middleware.emvl2lib.EmvCallback.EMV_ONLINEPIN) {
//                            title = "please input online password";
//                        }
//                        //following code is just for test, use Ped.getInstance().startPinInput instead
//                        new MaterialDialog.Builder(DoEmvActivity.this)
//                                .title(title)
//                                .inputRange(0, 12)
//                                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
//                                .input("", "", new MaterialDialog.InputCallback() {
//                                    @Override
//                                    public void onInput(MaterialDialog dialog, CharSequence input) {
//                                        if (TextUtils.isEmpty(input)) {
//                                            handler.onGetPin(EmvCallbackGetPinResult.CV_PIN_BYPASS, new byte[8]);
//                                        } else {
//                                            handler.onGetPin(EmvCallbackGetPinResult.CV_PIN_SUCC, input.toString().getBytes());
//                                        }
//                                    }
//                                })
//                                .negativeText("cancel")
//                                .onNegative(new MaterialDialog.SingleButtonCallback() {
//                                    @Override
//                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                        handler.onGetPin(EmvCallbackGetPinResult.CV_PIN_CANCLE, null);
//                                    }
//                                })
//                                .show();
//                    }
//                });
            }

            @Override
            public void aidSelect(final String[] aidNames, final com.sunyard.smartposapi.emv2.EmvL2.AidSelectHandler handler) {
                String data = "";
                for (int i = 0; i < aidNames.length; i++) {
                    data += i + ":" + aidNames[i] + "\n";
                }
                updateResult("[aidSelect]aidNames:" + data);
                final String title = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(DoEmvActivity.this)
                                .title("please select follow aids\n" + title)
                                .inputType(InputType.TYPE_CLASS_NUMBER)
                                .input("", "", new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        handler.onAidSelect(Integer.parseInt(input.toString()));
                                    }
                                })
                                .negativeText("cancel")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        handler.onAidSelect(-1);
                                    }
                                })
                                .show();
                    }
                });
            }

            @Override
            public void termRiskManager(byte[] pan, int panSn, com.sunyard.smartposapi.emv2.EmvL2.TermRiskManageHandler handler) {
                updateResult("[termRiskManager]pan:" + new String(pan) + ", panSn:" + panSn);
                handler.onTermRiskManager(0, 0);
            }

            @Override
            public void issuerReference(byte[] pan, com.sunyard.smartposapi.emv2.EmvL2.IssuerReferenceHandler handler) {
                updateResult("[issuerReference]pan:" + new String(pan));
                handler.onIssuerReference(0);
            }

            @Override
            public void accountTypeSelect(com.sunyard.smartposapi.emv2.EmvL2.AccountTypeSelectHandler handler) {
                updateResult("[accountTypeSelect]");
                handler.onAccountType(1);
            }

            @Override
            public void certConfirm(byte[] type, byte[] certNum, com.sunyard.smartposapi.emv2.EmvL2.CertConfirmHandler handler) {
                updateResult("[certConfirm]type:" + new String(type) + ", certNum:" + new String(certNum));
                handler.onCertConfirm(1);
            }

            @Override
            public void lcdMsg(byte[] title, byte[] msg, boolean isYesNo, int timeout, com.sunyard.smartposapi.emv2.EmvL2.LcdMsgHandler handler) {
                updateResult("[lcdMsg]title:" + new String(title) + ", msg:" + new String(msg) + ", isYesNo:" + isYesNo + ", timeout:" + timeout);
                handler.onLcdMsg(1);
            }

            @Override
            public void confirmEC(com.sunyard.smartposapi.emv2.EmvL2.ConfirmEcHandler handler) {
                updateResult("[accountTypeSelect]");
                handler.onConfirmEc(0);
            }

            @Override
            public void processResult(int ret) {
                appendResult("[processResult]ret:" + ret);
                dismissDialog();
                byte[] tag = StringUtil.hexStr2Bytes("579F269F279F109F379F36959A9F219C9F025F2A829F1A9F039F339F349F359F1E9F06849F099F419F535F345A575F249F079B9F119F125F20995F255F30509F4000");
                if (ret == 0 || ret == 1 || ret == 2) {
                    byte[] tlvData = emvL2.getTLVData(tag);
                    if (tlvData != null) {
                        updateResult("[getTLVData]" + StringUtil.byte2HexStr(tlvData));
                        Log.d("WY", "[getTLVData]" + StringUtil.byte2HexStr(tlvData));
                    } else {
                        updateResult("[getTLVData]fail");
                    }
                } else if (ret == 3) {
                    tag = StringUtil.hexStr2Bytes("509F12849F119F6D9F24569F6B9F5DDF812ADF812BDF81159F6E");
                    byte[] tlvData = emvL2.getTLVData(tag);
                    if (tlvData != null) {
                        updateResult("[getTLVData]" + StringUtil.byte2HexStr(tlvData));
                    } else {
                        updateResult("[getTLVData]fail");
                    }
                }
                if (ret == 1) {
                    String success = "00";
                    String onlineData = "720F860D84240000087B2C5A82C7FCAE5D71709F180441424344860D841E00000853A2C503898A7E9B860D841E000008386176C2CDDE4597860D841E000008225457FD8A602EE0860D841E00000860C17F2A97BD65E7860D841E0000085CE4991432364517860D841E000008A7676474474DD431860D841E0000082203E10993A13927";
                    com.sunyard.smartposapi.emv2.TwiceAuthorResult twiceAuthorResult = null;
//                    try {
//                        twiceAuthorResult = emvL2.twiceAuthorization(success, onlineData);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        updateResult("twiceAuthorization Exception");
//                        return;
//                    }
//                    updateResult("[TwiceAuthorResult]" + twiceAuthorResult.getStatus());
                    if ("00".equals(twiceAuthorResult.getStatus())) {
                        updateResult("[TwiceAuthorResult]success");
                        byte[] tlvData = emvL2.getTLVData( tag);
                        if (tlvData != null) {
                            updateResult("[getTLVData]" + StringUtil.byte2HexStr(tlvData));
                        } else {
                            updateResult("[getTLVData]fail");
                        }
                    }
                }
            }
        });
    }

    private void showPinpad() {

    }

    private void updateICCardData() {
        byte[] cardData = getTlvData(this, getPackageName(), "5A575F345F20");
        Log.i("cardData", Util.BytesToString(cardData));
        if (cardData == null) {
            updateResult("get ic data fail");
            return;
        }
        Map<String, String> cardDataMap = TlvUtil.tlvToMap(cardData);
        String pan = cardDataMap.get("5A");
        if (!StringUtil.isEmpty(pan)) {
            pan = pan.replace("F", "");//card no
        }
        String cardHolderName = cardDataMap.get("5F20");
        String track2data = cardDataMap.get("57");

//        byte[] sendF55Data = getTlvData(this, getPackageName(), "9F269F279F109F379F36959A9C9F025F2A829F1A9F039F339F349F359F1E849F099F41");
        byte[] sendF55Data = getTlvData(this, getPackageName(), "9F269F279F109F379F36959A9C9F025F2A829F1A9F039F339F349F359F1E849F099F419F63" +
                "917172DF32DF33DF34"); //without: 5F24 + 9F12 + 9F53
        String result = "card no: " + pan + "\ncardHolderName:" + cardHolderName
                + "\ntrack 2:" + track2data + "\ntlv data: ";
        List<Tlv> tlvList = TlvUtils.builderTlvList(Util.BytesToString(sendF55Data));
        if (tlvList != null) {
            for (Tlv tlv : tlvList) {
                result += "\ntag:" + tlv.getTag();
                result += " value:" + tlv.getValue();
            }
        }
        updateResult(result);

//        doMasterRf();
//        final String DukptKey ="FE9C1B69FB3FAE71FE9C1B69FB3FAE71";
//        final String ksn = "FFFF9876543210E00000";
//        try {
//            Dukpt.getInstance().loadDukptKey((byte)0xff,(byte)0x01,(byte)0x01,Util.StringToBytes(ksn),Util.StringToBytes(DukptKey),null);
//        } catch (SDKException e) {
//            e.printStackTrace();
//        } catch (PINPADException e) {
//            e.printStackTrace();
//        }
//
////        byte [] data = genEncryptData(sendF55Data);
////        System.out.println(Util.BytesToString(sendF55Data));
//        Dukpt.EncryptDataResult encryptDataResult = null;
//        try {
//            encryptDataResult = Dukpt.getInstance().encryptData((byte)1,Ped.DES_TYPE_3DES,(byte)1,
//                    Util.BytesToString(sendF55Data),Util.StringToBytes("1122334455667788"));
//        } catch (SDKException e) {
//            e.printStackTrace();
//        } catch (PINPADException e) {
//            e.printStackTrace();
//        }
////        System.out.println(encryptDataResult.getCiphertext());
//        updateResult(encryptDataResult.toString());
//        updateResult(sendF55Data.length+"\n"+encryptDataResult.toString()+ "\n"+encryptDataResult.getCiphertext().length());
//        updateResult(result);
//        try {
//            Thread.sleep(10*1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        TwiceAuthorResult twiceAuthorResult = null;
//       try {
//            twiceAuthorResult =  EmvL2.getInstance(getApplicationContext(),getPackageName()).twiceAuthorization("00", "8A023030");
//        } catch (Exception e) {
//          e.printStackTrace();
//        }
//        updateResult(twiceAuthorResult.getStatus());
    }

    private byte[] genEncryptData(byte[] F55Data) {
        int len = F55Data.length;
        byte[] F55len = new byte[2];

        F55len[0] = (byte) (len / 256);
        F55len[1] = (byte) (len % 256);
        byte[] data = new byte[((len + 2) / 8 + 1) * 8];
        System.arraycopy(F55len, 0, data, 0, 2);
        System.arraycopy(F55Data, 0, data, 2, len);
        Arrays.fill(data, len + 2, data.length, (byte) 0xff);
        return data;
    }

    private byte[] getTlvData(Context context, String packageName, String tlvString) {
        try {
            Map<String, String> mMap = new HashMap<String, String>();
            mMap.put("DF35", tlvString);
            return EmvL2.getInstance(context, packageName).getTLVData(TlvUtil.mapToTlv(mMap));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
                dialog = new ProgressDialog(DoEmvActivity.this);
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

    private void updateResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.setText(result);
            }
        });
    }

    private void appendResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.append("\n" + result);
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopSearch();
        super.onDestroy();
    }
}
