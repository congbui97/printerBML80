package bk.congbui.marutoucompackprint;

import static android.os.Build.VERSION.SDK_INT;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.R;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import bk.congbui.marutoucompackprint.BlueTooth.BluetoothUtil;
import bk.congbui.marutoucompackprint.BlueTooth.ESCUtil;
import bk.congbui.marutoucompackprint.DiaLog.DeviceDialog;

public class MainActivity extends AppCompatActivity {
    private String NAME_CLASS = "MainActivity";
    public static TextView tvPrint;
    private  String data;
    private int record = 18;
    public static byte codeParse = 1;
    public static String CODE_LANGUAGE = "SJIS";
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1223;
    public static boolean isRectangle = false;
    public static int isFirstRectangle;
    public static int isWidthRectangle;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
            Log.d("bluetooth_device","onCreate");
            tvPrint = findViewById(R.id.tvPrint);
            tvPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,ClipBoardService.class);
                    intent.putExtra("value",data);
                    startService(intent);
                    finish();
                }
            });
        if (isMyServiceRunning(ClipBoardService.class)){
            Log.d("bluetooth_device,","is running");
            tvPrint.setText(BluetoothUtil.NameDevice);
            tvPrint.setEnabled(false);
        }else {
            Log.d("bluetooth_device,","not  service");
        }
        checkPermission();
    }

    private void requestPermissionManager() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Log.d("device_bluetooth", "Permission read granted");
        }
        if (Environment.isExternalStorageManager() == false) {
            requestPermissionManager();
            // perform action when allow permission success
        } else {
            Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 3);
        } else {
            Log.d("device_bluetooth", "Permission manager storage granted");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            Log.d("device_bluetooth", "Permission write granted");
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("bluetooth_device","onResume");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            Log.d("bluetooth_device","onWindowFocusChanged true ");

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null) {
                CharSequence clip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(MainActivity.this).toString();
                data = (String) clip;

                if (BluetoothUtil.isBlueToothPrinter == true){
                    readData(data);
                    new CountDownTimer(100, 100) {
                        @Override
                        public void onTick(long l) {}
                        @Override
                        public void onFinish() {
//                            Intent intent = new Intent(MainActivity.this,ClipBoardService.class);
//                            startService(intent);
//                            finish();
                        }
                    }.start();
                }
            }
        }else {
            Log.d("bluetooth_device","onWindowFocusChanged false");
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("bluetooth_device","onStop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_setting:
                try {
                    String print_image = "print_image/t/receipt.png";

                    readData(print_image);
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //BluetoothUtil.disconnectBlueTooth(MainActivity.this);
        Log.d("bluetooth_device","onDestroy");
    }

    public static void printByBlueTooth(String content,int size, int align,int font) {
        try {
            switch (size){
                case 0:
                    BluetoothUtil.sendData(ESCUtil.cc);
                    break;
                case 1:
                    BluetoothUtil.sendData(ESCUtil.bb);
                    break;
                case 2:
                    BluetoothUtil.sendData(ESCUtil.bb2);
                    break;
                case 3:
                    BluetoothUtil.sendData(ESCUtil.bb3);
                case 4:
                    BluetoothUtil.sendData(ESCUtil.bb4);
                    break;
            }
            switch (font){
                case 0:
                    BluetoothUtil.sendData(ESCUtil.ESC_NORMAL);
                    break;
                case 1:
                    BluetoothUtil.sendData(ESCUtil.ESC_BOLD);
                    break;
            }

            switch (align){
                case 0:
                    //left align
                    BluetoothUtil.sendData(ESCUtil.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    BluetoothUtil.sendData(ESCUtil.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    BluetoothUtil.sendData(ESCUtil.ESC_ALIGN_RIGHT);
                    break;
            }

            if (content != null){
                BluetoothUtil.sendData(ESCUtil.setCodeSystem(codeParse));
                BluetoothUtil.sendData(content.getBytes(CODE_LANGUAGE));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                try {
                    if (BluetoothUtil.connectBlueTooth(getApplicationContext()) == true) {
                        Toast.makeText(getApplicationContext(), "connect", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "can't connect", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                finish();
            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("device_bluetooth", "permission bluetooth granted");
            } else {
                Log.d("device_bluetooth", "permission bluetooth  denied");
            }
        } else if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("device_bluetooth", "permission read granted");
            } else {
                Log.d("device_bluetooth", "permission read denied");
            }
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("device_bluetooth", "permission write granted");
            } else {
                Log.d("device_bluetooth", "permission write denied");
            }
        } else if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("device_bluetooth", "permission manager storage granted");
            } else {
                Log.d("device_bluetooth", "permission manager storage denied");
            }
        } else  if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager() == true) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static void readData(String data){
        //print tittle
        String[] lines = data.split("/title/");
        if (true){
            try {
                ESCUtil.print_image("");
//                ESCUtil.print_image("lines[0].split("/t/")[1]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (lines.length > 1){
            String tittle = lines[0];
            String[] printtitle = tittle.split("&&");
            String content = printtitle[0];
            int font = Integer.parseInt(printtitle[2]);
            int align = Integer.parseInt(printtitle[1]);
            printByBlueTooth(content,4,font,align);
            BluetoothUtil.sendData(ESCUtil.nextLine(2));
            // line other tittle
            String [] lines1 = lines[1].split("/nl/");

            for (int i = 0; i < lines1.length; i++) {
                if (lines1[i].contains("table/spf/")){
                    BluetoothUtil.sendData(ESCUtil.printTable( lines1[i]));
                }else if (lines1[i].contains("/t/")){
                    String[] inLine = lines1[i].split("/t/");
                    try {
                        printInALine(inLine);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    // BluetoothUtil.sendData(ESCUtil.nextLine(1));
                }else {
                    printByBlueTooth(lines1[i],1,0,0);
                    BluetoothUtil.sendData(ESCUtil.nextLine(1));
                }
            }
            BluetoothUtil.sendData(ESCUtil.nextLine(3));
        } else {}

    }

    public static  void printInALine(String [] line) throws UnsupportedEncodingException {
        if (line[0].contains("rectangle")){
                if (line[0].equals("s-rectangle")){
                    if (isRectangle == false){
                        ESCUtil.isManyLine = 0;
                        isRectangle = true;
                        isFirstRectangle = Integer.parseInt(line[2]) / 573 * 48;
                        int last = Integer.parseInt(line[3]) / 573 * 48;
                        BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_24);
                        isWidthRectangle = last - isFirstRectangle;
                        BluetoothUtil.sendData(ESCUtil.initARectangle(isFirstRectangle,isWidthRectangle,new String[]{"first"},ESCUtil.isManyLine));
                    }
                }else  if (line[0].equals("e-rectangle")){
                    if (line[6].equals("6") || line[6].equals("5") || line[6].equals("20") || line[6].equals("10") || line[5].equals("20")){
                        ESCUtil.isManyLine = 0;
                        isRectangle = false;
                        int first = Integer.parseInt(line[2]) / 573 * 48;
                        int last = Integer.parseInt(line[3]) / 573 * 48;
                         //  BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_24);
                        int width = last - first;
                        BluetoothUtil.sendData(ESCUtil.initARectangle(first,width,new String[]{"last","not space"},ESCUtil.isManyLine));
                    }else {
                        ESCUtil.isManyLine = 0;
                        int first = Integer.parseInt(line[2]) / 573 *48;
                        int last = Integer.parseInt(line[3]) / 573 *48;
                        //   BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_24);
                        int width = last - first;
                        BluetoothUtil.sendData(ESCUtil.initARectangle(first,width,new String[]{"last","space"},ESCUtil.isManyLine));
                    }
                }
        }else if (isRectangle == true){
            ESCUtil.isManyLine++;
            if (line.length <= 4){
                printByBlueTooth(null,Integer.parseInt(line[2]),Integer.parseInt(line[2]),Integer.parseInt(line[2]));
                BluetoothUtil.sendData(ESCUtil.initARectangle(Integer.parseInt(line[1]),isWidthRectangle,new String[]{line[0],"nottable"},ESCUtil.isManyLine));
                //setTingLine(line,isRectangle);
            }else if (line.length <= 8){
                if (line[6].equals("1")){
                    printByBlueTooth(null,3,0,Integer.parseInt(line[6]));
                }else {
                    printByBlueTooth(null,1,0,Integer.parseInt(line[6]));
                }
                BluetoothUtil.sendData(ESCUtil.initARectangle(Integer.parseInt(line[3]),isWidthRectangle,new String[]{line[0],line[1],line[6],"nottable"},ESCUtil.isManyLine));
            }
        } else if (line[0].contains("ryoshu/spf")){
                BluetoothUtil.sendData(ESCUtil.initRyoShu(line));
        } else if (line[0].contains("logo")){

        } else {
            BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_30);
            setTingLine(line,isRectangle);
            BluetoothUtil.sendData(ESCUtil.nextLine(1));
        }
//        BluetoothUtil.sendData(ESCUtil.nextLine(0));
    }

    public static void setTingLine(String[] line, boolean isRectangle){
        if (isRectangle == true){
            String li = "[";

            Log.d("aaa,",li);
            String result = "";
            int lenght;
            if (line.length <= 4 ){
                if (Integer.parseInt(line[1])==0){
                    result = li + " " + line[0];
                     lenght = 48 - result.length() - 1;

                    for (int i = 0; i < lenght; i++) {
                        result = result +" ";
                    }
                    result = result +li;
                }else if (Integer.parseInt(line[1])==1){}
            }else if (line.length <= 8){
                lenght = 48 - line[0].getBytes().length - line[1].getBytes().length - 5;
                String space = "";
                for (int i = 0; i < lenght; i++) {
                    space = space + " ";
                }
                result = li + " " + line[0] + space + line[1] + "  "+ li;
            }

            printByBlueTooth(result,1,0,0);

        }else {
            if (line.length <= 4){
                printByBlueTooth(line[0],1,Integer.parseInt(line[1]),0);
            }else {
               // a line not have rec or table
                String result = "";
            int valueSpace = 46 - getByteofCharactor(line[0]) - getByteofCharactor(line[1]);
            if (line[2].equals("2")){
                line[0] = "  " + line[0];
                for (int i = 0; i < valueSpace; i++) {
                    line[0] = line[0] + " ";
                }
                result = line[0] + line[1];
            }else {
                result = line[0];
            }
            printByBlueTooth(result,1,0,0);
            }
        }
    }

    //check NihonGo charactor
    public static int getByteofCharactor(String text){
        boolean flag = false;
        String[] listChar = text.split("");

        for (int i = 0; i < listChar.length; i++) {
            if (listChar[i].getBytes().length == 3){
                flag = true;
                break;
            }
        }
        if (flag == true){
            return text.getBytes().length - 1;
        }
        else {
            return text.getBytes().length;
        }
    }

}