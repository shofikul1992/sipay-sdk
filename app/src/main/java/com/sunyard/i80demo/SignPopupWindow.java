package com.sunyard.i80demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sunyard.i80demo.util.Base64Util;
import com.sunyard.i80demo.util.BmpUtil;



/**
 * 签名弹出框
 * Created by wangyuan on 16/8/4.
 */
public class SignPopupWindow extends PopupWindow implements View.OnClickListener{
    private static final String TAG = "ElecSignActivityLog";

    private RelativeLayout paintViewLayout;
    private SignPopupWindow.PaintView paintView;
    private Button btnResign;
    private Button btnConfirm;

    private int bmpWidth = 237;
    private int bmpHeight = 79;

    private boolean bInitFlag; // 窗口是否初始化完成标记位

    private byte[] bmpData; // 电子签名数据
    private boolean isSignedFlag; // 是否已经签名标记
    private boolean isElecConfirmed; // 签名是否确认标记

    private String featureCode;
    private Bitmap cachebBitmap;

    private OnSignCallback mCallback;   //签名结果回调
    private View mMenuView;             //PopupWindow视图
    private Context mContext;           //上下文

    public SignPopupWindow(Context context, View view, String featureCode, OnSignCallback callback) {
        this.featureCode = featureCode;
        mCallback = callback;
        mContext = context;

        if (featureCode == null || featureCode.length() == 0) {
            if (mCallback != null) {
                mCallback.onFail("无特征码");
            }
        }
        if (context == null) {
            if (mCallback != null) {
                mCallback.onFail("无上下文");
            }
        }

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_elecsign, null);
        if (mMenuView == null) {
            if (mCallback != null) {
                mCallback.onFail("初始化界面失败");
            }
            return;
        }

        this.setOutsideTouchable(false);
        this.setFocusable(false);

        initView(mMenuView);
        initListener();

        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //设置popup的位置
        this.showAtLocation(view,
                Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则不响应
        try {
            mMenuView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if(mMenuView.findViewById(R.id.sign_view_layout)!=null) {
                        int height = mMenuView.findViewById(R.id.sign_view_layout).getTop();
                        int y = (int) event.getY();
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (y > height) {
                                return true;
//                        dismiss();
                            }
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_confirm:
                if (isSignedFlag) {
                    isElecConfirmed = true;

                    Bitmap bitmap = paintView.getCachebBitmap();
                    bmpData = getMonoData(bitmap, bmpWidth, bmpHeight);
//					Jbig jbig = new Jbig();
//					bmpData = jbig.compress(bmpData, bmpWidth, bmpHeight);
                    String encode = Base64Util.encode(bmpData);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 237, 79, true);
                    if (mCallback != null) {
                        mCallback.onSuccess(bitmap,encode);

                    }
                    destroyView();
                    dismiss();
                } else {
                    Toast.makeText(mContext, "请签名!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_resign:
                isSignedFlag = false;
                paintView.clear();
                paintView.drawFeatureText(featureCode);
                break;
            default:
                break;
        }
    }

    void initView(View view) {
        btnResign = (Button) view.findViewById(R.id.btn_resign);
        btnConfirm = (Button) view.findViewById(R.id.btn_confirm);

        paintViewLayout = (RelativeLayout) view.findViewById(R.id.sign_view_layout);
        int width = paintViewLayout.getWidth();
        int height = paintViewLayout.getHeight();
        Log.i(TAG, "paintViewLayout width = " + width + " height = " + height);

        if (width == 0) {
            width = mContext.getResources().getDisplayMetrics().widthPixels;
        }
        if (height == 0) {
            height = width/2;
        }
        paintView = new PaintView(mContext, width, height);
        paintViewLayout.addView(paintView);

        if(featureCode != null) {
            paintView.drawFeatureText(featureCode);
        }

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha((Activity) mContext, 1f);
            }
        });
    }

    private void initListener() {
        btnResign.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private byte[] getMonoData(Bitmap bitmap, int width, int height) {
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        return BmpUtil.getMonoData(bitmap);
    }

    /**
     * 需在dismiss时调用
     */
    protected void destroyView() {
        cachebBitmap.recycle();
        cachebBitmap = null;
    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     */
    public void backgroundAlpha(Activity context, float bgAlpha)
    {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    /**
     * This view implements the drawing canvas. It handles all of the input
     * events and drawing functions.
     */
    class PaintView extends View {
        private Paint paint;
        public Canvas cacheCanvas;
        private Path path;
        ViewGroup.LayoutParams params;
        private int mWidth;

        public PaintView(Context context, int width, int height) {
            super(context);
//            if (width / 3 >= height) {
                params = new ViewGroup.LayoutParams(width, height);
//            } else {
//                params = new ViewGroup.LayoutParams(width, width / 3);
//            }
            setLayoutParams(params);

            mWidth = width;
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(2*mWidth/237);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            path = new Path();
            System.gc();
            cachebBitmap = Bitmap.createBitmap(params.width, params.height,
                    Bitmap.Config.ARGB_8888);
            cacheCanvas = new Canvas(cachebBitmap);
            cacheCanvas.drawColor(Color.WHITE);
        }

        public Bitmap getCachebBitmap() {
            return cachebBitmap;
        }

        /* 加入特征码 */
        public void drawFeatureText(String str) {
            Paint txtPaint = new Paint();
            txtPaint.setTextSize(14*mWidth/237);
            txtPaint.setAntiAlias(true);
            txtPaint.setStrokeWidth(1*mWidth/237);
            txtPaint.setStyle(Paint.Style.STROKE);
            txtPaint.setColor(Color.BLACK);
            cacheCanvas.drawText(str, (params.width - txtPaint.measureText(str))/2, params.height / 2,
                    txtPaint);
        }

        public void clear() {
            if (cacheCanvas != null) {
                paint.setColor(Color.WHITE);
                cacheCanvas.drawPaint(paint);
                paint.setColor(Color.BLACK);
                cacheCanvas.drawColor(Color.WHITE);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(cachebBitmap, 0, 0, null);
            canvas.drawPath(path, paint); // 如果注释掉则不能实时刷新
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
            int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;
            if (curW >= w && curH >= h) {
                return;
            }

            if (curW < w)
                curW = w;
            if (curH < h)
                curH = h;

            Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
                    Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);
            if (cachebBitmap != null) {
                newCanvas.drawBitmap(cachebBitmap, 0, 0, null);
            }
            cachebBitmap = newBitmap;
            cacheCanvas = newCanvas;
        }

        private float cur_x, cur_y;

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    isSignedFlag = true;
                    cur_x = x;
                    cur_y = y;
                    path.moveTo(cur_x, cur_y);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    path.quadTo(cur_x, cur_y, x, y);
                    cur_x = x;
                    cur_y = y;
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    cacheCanvas.drawPath(path, paint);
                    path.reset();
                    break;
                }
            }
            invalidate();

            return true;
        }
    }

    /**
     * 返回输出的结果
     */
    public interface OnSignCallback {
        void onSuccess(Bitmap bitmap, String encode);

        void onFail(String message);
    }


}
