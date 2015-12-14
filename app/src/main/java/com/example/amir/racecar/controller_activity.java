package com.example.amir.racecar;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class controller_activity extends AppCompatActivity {

    private int gear;
    private int direction;
    private int connecting = 0;
    private int connected = 0;
    private int disconnect = 0;

    private String ip;
    private int port = -1;
    Socket socket;
    SocketCreator s;
    PrintWriter sender;

    class SocketCreator extends AsyncTask<String,String,String> {
        protected String doInBackground(String... params) {
            connecting = 1;
            try {
                socket = new Socket(ip,port);
                connected = 1;
                sender = new PrintWriter(socket.getOutputStream(), true);

                while (disconnect == 0) {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sender.print("" + (direction + 1) + (gear + 1));
                    sender.flush();
                }
                socket.close();
                disconnect = 0;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                socket = null;
                connected = connecting = 0;
            } catch (IOException e) {
                e.printStackTrace();
                socket = null;
                connected = connecting = 0;
            }
            return null;
        }
    }

    private boolean show_status() {
        if (socket == null || !socket.isConnected()) {
            Toast.makeText(getApplicationContext(), "Not Connected Yet!!!", Toast.LENGTH_SHORT).show();
            return false;
        }

        TextView text = (TextView)findViewById(R.id.text);
        if (gear == -1) {
            text.setText("R");
            text.setTextColor(Color.parseColor("#ff0000"));
            text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
        } else if (gear == 0) {
            text.setText("N");
            text.setTextColor(Color.parseColor("#00ff00"));
            text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
        } else {
            text.setText("" + gear);
            text.setTextColor(Color.parseColor("#0000ff"));
            text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40 * gear);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_layout);

        gear = 0;
        direction = 0;

        ((Button)findViewById(R.id.right)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (socket == null || !socket.isConnected()) {
                    Toast.makeText(getApplicationContext(), "Not Connected Yet!!!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        direction = 1;
                        return true;
                    case MotionEvent.ACTION_UP:
                        direction = 0;
                        return true;
                }
                return false;
            }
        });

        ((Button)findViewById(R.id.left)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (socket == null || !socket.isConnected()) {
                    Toast.makeText(getApplicationContext(), "Not Connected Yet!!!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        direction = -1;
                        return true;
                    case MotionEvent.ACTION_UP:
                        direction = 0;
                        return true;
                }
                return false;
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (gear > -1 && show_status())
                gear--;
            show_status();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (gear < 5 && show_status())
                gear++;
            show_status();
        }
        return true;
    }

    public void connect(View view) {
        EditText ip_port = (EditText)findViewById(R.id.ip_port);
        if (connecting == 0 && connected == 0) {
            s = new SocketCreator();
            String ip_port_string = ip_port.getText().toString();
            ip = ip_port_string.substring(0, ip_port_string.indexOf(':'));
            port = Integer.parseInt(ip_port_string.substring(ip_port_string.indexOf(':') + 1, ip_port_string.length()));
            s.execute();
            while (connecting == 0);
            while (connecting == 1 && connected == 0);
        } else if (connecting == 1 && connected == 1) {
            disconnect = 1;
            while (disconnect == 1);
            gear = 0;
            direction = 0;
            TextView text = (TextView)findViewById(R.id.text);
            text.setText("N");
            text.setTextColor(Color.parseColor("#00ff00"));
            text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
            connecting = connected = 0;
        }

        if (connecting == 0 && connected == 0) {
            ((Button)findViewById(R.id.connect)).setText("Connect");
        } else if (connecting == 1 && connected == 1) {
            ((Button)findViewById(R.id.connect)).setText("Disconnect");
        }

        if (socket != null) {
            if (socket.isConnected())
                ip_port.setTextColor(Color.parseColor("#00ff00"));
            else
                ip_port.setTextColor(Color.parseColor("#ff0000"));
        } else
            ip_port.setTextColor(Color.parseColor("#0000ff"));
    }

    public void stop(View view) {
        gear = 0;
        show_status();
    }
}
