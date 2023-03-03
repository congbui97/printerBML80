package bk.congbui.marutoucompackprint;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import bk.congbui.marutoucompackprint.BlueTooth.BluetoothUtil;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);

        context.startService(new Intent(context,ClipBoardService.class).setAction("STOP_ACTION"));


        if (text !=null){
            Log.d("bluetooth_device", text.toString()+" cb");
        }else {
            Log.d("bluetooth_device", "null");
        }
    }
}
