package com.sunyard.i80demo;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.socsi.exception.PINPADException;
import com.socsi.smartposapi.ped.PinKeyBoardListener;
import com.socsi.smartposapi.ped.PinKeyboardUtil;
import com.socsi.utils.StringUtil;


public class KeyBoardActivity extends Activity {
    private final String TAG = KeyBoardActivity.class.getSimpleName();
    public static PinKeyboardUtil mDefineKeyboardUtil;
    private StringBuffer textContent;
    private int[] pinLen = new int[] {0,4,5,6,7,8,9,10,11,12};

    private boolean isEncrypt = false;
    private String pan = "6222031202000595693";
    private int mKeyIdx = -1;
    private int pinAlgMode = -1;
    //	private PinKeyBoardListener listener;
    private String keyBoardTpye = null;
    private int timeout = 60;
    private boolean isEndTimeout = false;
    private long startTime = 0;
    private int showTime = timeout;

    private TextView textAlert;
    private TextView textView ;
    private Handler mHandler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);
        Log.d("yangbo", getClass().getSimpleName());
        mHandler = new Handler();
        Bundle bundle = getIntent().getBundleExtra(KeyBoardConfig.BUNDLE_KEY);
        final TextView inputText = (TextView) findViewById(R.id.text_input_password);
        textAlert = (TextView) findViewById(R.id.textAlert);
        textView = (TextView) findViewById(R.id.textAmount);
        if (bundle != null) {
            if (bundle.getIntArray(KeyBoardConfig.BUNDLE_KEY_PIN_LEN) != null)
                pinLen = bundle.getIntArray(KeyBoardConfig.BUNDLE_KEY_PIN_LEN);
            isEncrypt = bundle.getBoolean(KeyBoardConfig.BUNDLE_KEY_ENCRYPTED, false);
//            pan = bundle.getString(KeyBoardConfig.BUNDLE_KEY_PAN, null);
            mKeyIdx = bundle.getInt(KeyBoardConfig.BUNDLE_KEY_MKEYIDX);
            timeout = bundle.getInt(KeyBoardConfig.BUNDLE_KEY_TIMEOUT, 60);
//			keyBoardTpye = bundle.getString(KeyBoardConfig.BUNDLE_KEY_TYPE);
            pinAlgMode = bundle.getInt(KeyBoardConfig.BUNDLE_KEY_pinAlgMode);
            String textAlertString = bundle.getString(KeyBoardConfig.BUNDLE_KEY_textAlert);
            textAlert.setText(textAlertString);
            String mAmount = bundle.getString(KeyBoardConfig.BUNDLE_KEY_amount);
            if(mAmount != null && !("".equals(mAmount))){
                textView.setText("金额:" + mAmount + "元");
            }
        }

        // 开启超时定时器
        startTimeTask();
        // 开启倒计时
        startCountdown(inputText);

        mDefineKeyboardUtil = null;
        textContent = new StringBuffer();
        showPinpad();
//		if (keyBoardTpye.equals(KeyBoardConfig.BUNDLE_KEY_PINPAD)) {
//			PinPadManager.getInstance().setKeyBoardActivity(this);
//		} else if (keyBoardTpye.equals(KeyBoardConfig.BUNDLE_KEY_EMV)) {
//			EmvHandlerManager.getInstance().setKeyBoardActivity(this);
//		}

    }

    private void startCountdown(final TextView inputText) {
        new Thread() {
            public void run() {
                if (timeout != 0) {
                    showTime = timeout;
                    while (!isEndTimeout) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                inputText.setText("Please enter the cardholder password within " + (showTime--) + " seconds");
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
        }.start();
    }

    private void startTimeTask() {
        startTime = System.currentTimeMillis();
        isEndTimeout = false;
        new Thread() {
            public void run() {
                while (!isEndTimeout) {
                    long curTime = System.currentTimeMillis();

                    if ((curTime - startTime) > timeout * 1000) {
                        KeyBoardActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                isEndTimeout = true;
                                Intent intent = new Intent();
                                //todo
//								intent.putExtra(Constant.BundleKey.bundlekey_errmsg, PINPADException.PINPAD_CODE_ERR_TIME_OUT[1]);
                                KeyBoardActivity.this.setResult(RESULT_CANCELED,intent);
                                intent.putExtra(Constant.BundleKey.bundlekey_needdisplay, true);
//								listener.onInputResult(new PINPADException(PINPADException.PINPAD_CODE_ERR_TIME_OUT),
//										null);
                                KeyBoardActivity.this.finish();
                            }
                        });
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }

//	public void setKeyBoardListener(PinKeyBoardListener listener) {
//		this.listener = listener;
//	}


    @Override
    protected void onResume() {
        super.onResume();
//		String amount = PinPadManager.getInstance().getAmount();
//		if (amount !=null) {
//			textView.setText("金额:" + amount + "元");
//			PinPadManager.getInstance().setAmount(null);
//		}

    }
//	/**
//	 * 设置金额
//	 *
//	 * @param amount
//	 */
//	public void setAmount(final String amount) {
//		Log.d(TAG, "setAmount ex");
//
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				textView.setText("金额:" + amount + "元");
//
//			}
//		});
//
//	}

//	/**
//	 * 设置提示信息
//	 *
//	 * @param message
//	 */
//	public void setAlertMessage(final String message) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				textAlert.setText(message);
//			}
//		});
//	}

    private void showPinpad() {
        final TextView textView = (TextView) findViewById(R.id.keyboard_text);
        final int maxLen = pinLen[pinLen.length - 1];
        Log.d(TAG, "maxLen = " + maxLen);
        // 显示的坐标
//		mDefineKeyboardUtil = new PinKeyboardUtil(getApplicationContext(), (KeyboardView) findViewById(R.id.keyboard_view), R.xml.symbols, new PinKeyBoardListener() {
//
//			@Override
//			public void onKeyCodeCallback(int keyCode) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onInputResult(PINPADException PINPADException, byte[] data) {
//				// TODO Auto-generated method stub
//
//			}
//		});
        mDefineKeyboardUtil = new PinKeyboardUtil(getApplicationContext(),
                (KeyboardView) findViewById(R.id.keyboard_view), R.xml.symbols, new PinKeyBoardListener() {

            @Override
            public void onKeyCodeCallback(int keycode) {

                switch (keycode) {
                    case 0x08:// 退格
                        if (textContent.length() > 0) {
                            textContent.deleteCharAt(textContent.length() - 1);
                        }
                        break;
                    case 0xFE:// 清除
                        textContent = new StringBuffer();
                        break;
                    case 0x0D:// 确认
                        int len = textContent.length();
                        boolean isOk = false;
                        for (int i = 0; i < pinLen.length; i++) {
                            if (len == pinLen[i]) {
                                isOk = true;
                            }
                        }
                        if (!isOk) {
                            Toast.makeText(KeyBoardActivity.this, "Insufficient bit of password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        isEndTimeout  = true;
//							PinPadManager.getInstance().setIsInputting(false);
                        KeyBoardActivity.this.finish();

                        break;
                    case 0x18:// 取消
//							if (listener != null) {
//								listener.onInputResult(
//										new PINPADException(PINPADException.PINPAD_CODE_ERR_CANCEL_INPUT), null);
//							}
                        Intent intent = new Intent();
                        intent.putExtra(Constant.BundleKey.bundlekey_errmsg, PINPADException.PINPAD_CODE_ERR_CANCEL_INPUT[1]);
                        intent.putExtra(Constant.BundleKey.bundlekey_needdisplay, false);
                        KeyBoardActivity.this.setResult(RESULT_CANCELED,intent );
//							PinPadManager.getInstance().setIsInputting(false);
                        isEndTimeout = true;
                        KeyBoardActivity.this.finish();
                        break;
                    default:
                        if (textContent.length() < maxLen) {
                            textContent.append(Character.toString((char) keycode));
                        }
                        break;
                }
//						if (textContent.length() < maxLen) {
//							listener.onKeyCodeCallback(keycode);
//						}
                textView.setText(textContent);
            }

            @Override
            public void onInputResult(PINPADException exception, byte[] data) {
//						listener.onInputResult(exception, data);
                Intent intent = new Intent();
                if (exception.getErrCode().equals(PINPADException.PINPAD_CODE_OK[0])) {
                    intent.putExtra(Constant.BundleKey.bundlekey_pwd, data==null?"": StringUtil.byte2HexStr(data));
                    KeyBoardActivity.this.setResult(RESULT_OK,intent);
                }else {
                    intent.putExtra(Constant.BundleKey.bundlekey_errmsg, PINPADException.PINPAD_CODE_ERR_OTHER[1]);
                    intent.putExtra(Constant.BundleKey.bundlekey_needdisplay, true);
                    Log.i("my",PINPADException.PINPAD_CODE_ERR_OTHER[1]+"   "+exception.getErrCode());
                    KeyBoardActivity.this.setResult(RESULT_CANCELED,intent);
                }
            }

        });

        mDefineKeyboardUtil.setInputLen(pinLen);
        if (isEncrypt) {
            if (pan != null) {
                mDefineKeyboardUtil.setPinEncryptParams(pan.getBytes(), mKeyIdx, pinAlgMode);
                Log.i("my","mKeyIdx  "+mKeyIdx+"  pinAlgMode"+pinAlgMode);
            } else {
                mDefineKeyboardUtil.setPinEncryptParams(null, mKeyIdx, pinAlgMode);
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //if (listener != null)
            //listener.onKeyCodeCallback(0x18);
        }
        return true;
    }

}
