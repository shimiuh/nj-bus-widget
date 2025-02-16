package info.androidhive.floatingview;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.core.app.NotificationCompat;


public class MyTileService extends TileService {
    private static final int FOREGROUND_SERVICE_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    public MyTileService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Floating View Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_tile)  // Make sure you have this icon
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FOREGROUND_SERVICE_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(FOREGROUND_SERVICE_ID, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            // Check if service is running to set initial state
            boolean isServiceRunning = isServiceRunning(FloatingViewService.class);
            tile.setState(isServiceRunning ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    @Override
    public void onStopListening() {
        Log.d("test-f","in onClick onStopListening");
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile != null) {
            boolean isActive = tile.getState() == Tile.STATE_ACTIVE;
            // Toggle tile state
            tile.setState(isActive ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
            tile.updateTile();

            // Toggle floating view
            Intent intent = new Intent(this, FloatingViewService.class);
            if (isActive) {
                stopService(intent);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("test-f","in onBind TileService");
        return super.onBind(intent);
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
