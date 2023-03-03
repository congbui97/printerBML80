package bk.congbui.marutoucompackprint.DiaLog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

import java.util.ArrayList;




public class DeviceDialog extends Dialog {
    private ListView lvDevice;
    private Button btnCancel;

    private String[] listDevice =  new String[]{"BLM-80","SM1-21","SM2-41"};

    public static interface GetDevice{
        void getNameDevice(String text);
    }
    private GetDevice getDevice;

    public GetDevice getGetDevice() {
        return getDevice;
    }

    public void setGetDevice(GetDevice getDevice) {
        this.getDevice = getDevice;
    }

    public DeviceDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.device_item_dialog);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,listDevice);
        lvDevice = findViewById(R.id.lvDevice);
        lvDevice.setAdapter(adapter);
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getDevice.getNameDevice(listDevice[i]);
            }
        });


        btnCancel = findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
}
