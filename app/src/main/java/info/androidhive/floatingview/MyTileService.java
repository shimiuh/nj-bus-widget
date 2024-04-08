package info.androidhive.floatingview;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
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
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Floating View Service")
                .setContentText("Running...")
                // Ensure you have a valid icon here
                //.setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(FOREGROUND_SERVICE_ID, notification);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        getQsTile().setState(Tile.STATE_INACTIVE);
        Log.d("test-f","in onClick onTileAdded");
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
//        Tile tile = getQsTile();
//        tile.setIcon(Icon.createWithResource(this,R.drawable.ic_title_started));
//        tile.setLabel(getString(R.string.tile_label));
//        tile.setContentDescription(getString(R.string.tile_content_description);
//        tile.setState(Tile.STATE_ACTIVE);
//        tile.updateTile();
        Log.d("test-f","in onClick onStartListening");
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        Log.d("test-f","in onClick onStopListening");
        super.onStopListening();
    }

    @Override
    public void onClick() {
        // Called when the user click the tile
        Tile tile = getQsTile();
        Log.d("test-f","in onClick TileService"+tile.getState());

        if(tile.getState() == Tile.STATE_ACTIVE){

            tile.setState(Tile.STATE_INACTIVE);
            stopService(new Intent(this, FloatingViewService.class));

        }else{
            tile.setState(Tile.STATE_ACTIVE);
            Intent serviceIntent = new Intent(this, FloatingViewService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
        tile.updateTile();

        super.onClick();
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
}
