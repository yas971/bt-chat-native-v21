package com.native.chat;
import android.app.Activity;
import android.bluetooth.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private TextView chatLog;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        
        chatLog = new TextView(this);
        chatLog.setText("Master V21 - En attente...");
        layout.addView(chatLog);

        Button btn = new Button(this);
        btn.setText("SCANNER & CONNECTER");
        btn.setOnClickListener(v -> startConnection());
        layout.addView(btn);

        input = new EditText(this);
        layout.addView(input);

        Button send = new Button(this);
        send.setText("ENVOYER");
        send.setOnClickListener(v -> sendMsg());
        layout.addView(send);

        setContentView(layout);
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (Build.VERSION.SDK_INT >= 31) {
            requestPermissions(new String[]{
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT",
                "android.permission.ACCESS_FINE_LOCATION"
            }, 1);
        }
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                BluetoothServerSocket server = adapter.listenUsingInsecureRfcommWithServiceRecord("Chat", BT_UUID);
                socket = server.accept();
                listen();
            } catch (Exception e) {}
        }).start();
    }

    private void startConnection() {
        Set<BluetoothDevice> bonded = adapter.getBondedDevices();
        for (BluetoothDevice d : bonded) {
            new Thread(() -> {
                try {
                    socket = d.createInsecureRfcommSocketToServiceRecord(BT_UUID);
                    socket.connect();
                    listen();
                } catch (Exception e) {}
            }).start();
        }
    }

    private void listen() {
        runOnUiThread(() -> chatLog.append("\nConnecté !"));
        try {
            InputStream is = socket.getInputStream();
            byte[] buf = new byte[1024];
            while (true) {
                int len = is.read(buf);
                String msg = new String(buf, 0, len);
                runOnUiThread(() -> chatLog.append("\nCarole: " + msg));
            }
        } catch (Exception e) {}
    }

    private void sendMsg() {
        try {
            String m = input.getText().toString();
            socket.getOutputStream().write(m.getBytes());
            chatLog.append("\nMoi: " + m);
            input.setText("");
        } catch (Exception e) {}
    }
}