package bk.congbui.marutoucompackprint;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

import bk.congbui.marutoucompackprint.BlueTooth.BluetoothUtil;

public class ClipBoardService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("aaa,","create service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("aaa,","start command service");
        if (intent.getAction() != null && intent.getAction().equals("STOP_ACTION")) {
            BluetoothUtil.disconnectBlueTooth();
            MainActivity.tvPrint.setText("Connect");
            MainActivity.tvPrint.setEnabled(true);
          //  stopForeground(true);
            stopSelf();
        }else {
            sendNotification();
            if(BluetoothUtil.isBlueToothPrinter == false){
                try {
                    if (BluetoothUtil.connectBlueTooth(getApplicationContext()) == true) {
                        BluetoothUtil.isBlueToothPrinter = true;
                        MainActivity.tvPrint.setText(BluetoothUtil.NameDevice);
                        Toast.makeText(getApplicationContext(), "connected",Toast.LENGTH_SHORT).show();
                    } else {
                        BluetoothUtil.isBlueToothPrinter = false;
                        Toast.makeText(getApplicationContext(), "can't connect",Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d("aaa,",e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return Service.START_NOT_STICKY;
    }

    private void sendNotification() {
        Intent intent =  new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.foreground_print_lauout);
        remoteViews.setTextViewText(R.id.tvTittle,"Marutou Compack Print");
        remoteViews.setTextViewText(R.id.tvDescription,"This app is running");
        remoteViews.setOnClickPendingIntent(R.id.imgPrint,getPendingIntent());
        Notification notification = new NotificationCompat.Builder(this,BluetoothPrintApplication.CHANNEL_ID)
                .setCustomContentView(remoteViews)
                .setSmallIcon(R.drawable.marutoucompack)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
    }

    private PendingIntent getPendingIntent(){
            Intent intent = new Intent(this,MyReceiver.class);
            return  PendingIntent.getBroadcast(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Toast.makeText(getBaseContext(), "service ibinder", Toast.LENGTH_LONG).show();
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        Log.d("aaa,","destroy service");
    }

}
