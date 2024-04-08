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
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Floating View Service")
                .setContentText("Running...")
                // Ensure you have a valid icon here
                //.setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(FOREGROUND_SERVICE_ID, notification);
        Hawk.init(this).build();
        //Every time you run this app you will need to remove and add the tile
        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        //here is all the science of params
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


        setScale(mTopView, Hawk.get("scale"+mTopView.getId(), 1.0f));
        setScale(mMidView, Hawk.get("scale"+mMidView.getId(), 1.0f));
        setScale(mBottomView, Hawk.get("scale"+mBottomView.getId(), 1.0f));

        setTranslation(mTopView, Hawk.get("transX"+mTopView.getId(), 0f),Hawk.get("transY"+mTopView.getId(), 0f));
        setTranslation(mMidView, Hawk.get("transX"+mMidView.getId(), 0f),Hawk.get("transY"+mMidView.getId(), 0f));
        setTranslation(mBottomView, Hawk.get("transX"+mBottomView.getId(), 0f),Hawk.get("transY"+mBottomView.getId(), 0f));

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

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN ,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP;        //Initially view will be added to top-left corner

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

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
            CharSequence name = "NJ Bus";// getString(R.string.channel_name);
            String description = "NJ Bus";//getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
