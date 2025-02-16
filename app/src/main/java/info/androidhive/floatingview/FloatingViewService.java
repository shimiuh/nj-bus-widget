package info.androidhive.floatingview;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.RenderNode;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.orhanobut.hawk.Hawk;

public class FloatingViewService extends Service {

    private static final int FOREGROUND_SERVICE_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private WindowManager mWindowManager;
    private View mFloatingView;
    private ScaleGestureDetector mScaleDetector;
    private View mTopView, mMidView, mBottomView;
    View mTouchingView;
    TextView mPassenger;
    int mClickCount;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Initialize Hawk
        Hawk.init(this).build();
        
        // Inflate the floating view
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        
        // Initialize views and setup touch listeners
        initializeViews();
        
        // Setup and add window
        setupWindowParams();
    }

    private void initializeViews() {
        mScaleDetector = new ScaleGestureDetector(mFloatingView.getContext(), new ScaleListener());

        mTopView = mFloatingView.findViewById(R.id.mTopView);
        mMidView = mFloatingView.findViewById(R.id.mMidView);
        mPassenger = mFloatingView.findViewById(R.id.mPassenger);
        mPassenger.setText(getString(R.string.passenger,1));
        mClickCount = 0;
        mBottomView = mFloatingView.findViewById(R.id.mBottomView);

        mTopView.setOnTouchListener(mViewTouchListener);
        mMidView.setOnTouchListener(mViewTouchListener);
        mBottomView.setOnTouchListener(mViewTouchListener);

        // Restore saved scales and positions
        setScale(mTopView, Hawk.get("scale"+mTopView.getId(), 1.0f));
        setScale(mMidView, Hawk.get("scale"+mMidView.getId(), 1.0f));
        setScale(mBottomView, Hawk.get("scale"+mBottomView.getId(), 1.0f));

        setTranslation(mTopView, Hawk.get("transX"+mTopView.getId(), 0f), Hawk.get("transY"+mTopView.getId(), 0f));
        setTranslation(mMidView, Hawk.get("transX"+mMidView.getId(), 0f), Hawk.get("transY"+mMidView.getId(), 0f));
        setTranslation(mBottomView, Hawk.get("transX"+mBottomView.getId(), 0f), Hawk.get("transY"+mBottomView.getId(), 0f));

        mFloatingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickCount++;
                if(mClickCount > 10){
                    mClickCount = 0;
                }
                mPassenger.setText(getString(R.string.passenger,mClickCount));
            }
        });
    }

    public View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        private float initialX;
        private float initialY;
        private float initialTouchX;
        private float initialTouchY;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mScaleDetector.onTouchEvent(event);
            mTouchingView = v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    //remember the initial position.
                    initialX = v.getTranslationX();
                    initialY = v.getTranslationY();

                    //get the touch location
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_UP:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    //Calculate the X and Y coordinates of the view.
                    setTranslation(v,initialX + (int) (event.getRawX() - initialTouchX),
                            initialY + (int) (event.getRawY() - initialTouchY) );


//                        //Update the layout with new X & Y coordinate
//                        mWindowManager.updateViewLayout(mFloatingView, params);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }
    private float mScaleFactor = 1.f;
    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //TODO check if we can base mMaxScale and mMinScale depending on ratio reminder
        float mMaxScale = 5;
        float mMinScale = 0.86f;



        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(mMinScale, Math.min(mScaleFactor, mMaxScale));
//            if(shouldScale()) {
            setScale(mTouchingView,mScaleFactor);
//                mDidScale = true;
//            }
            return super.onScale(detector);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            //mScaleBegin = true;
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
//            if (isInScale()) {
//                cancelSlide();
//            }
//            mScaleBegin = false;

        }

    }


    private void setTranslation(View v, float transX, float transY) {

        v.setTranslationX(transX);
        v.setTranslationY(transY);

        Hawk.put("transX"+v.getId(), transX);
        Hawk.put("transY"+v.getId(), transY);
    }

    private void setScale(View v,float scale) {
        v.setScaleX(scale);
        v.setScaleY(scale);
        Hawk.put("scale"+v.getId(), scale);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Floating View Service",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Floating View Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_tile)
            .setPriority(NotificationCompat.PRIORITY_LOW);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return builder.setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            ).build();
        }
        return builder.build();
    }

    private void setupWindowParams() {
        // Always use TYPE_APPLICATION_OVERLAY for Android 8.0 and above
        int LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);
    }
}
