package com.example.bluetooth.arduinobluetoothcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button connect,on,off;
    TextView text;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice mDevice;
    BluetoothSocket mmSocket;
    InputStream mmInStream;
    OutputStream mmOutStream;
    boolean status = false;
    String deviceHardwareAddress,TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    deviceHardwareAddress = device.getAddress(); // MAC address
                    text.setText(deviceName);
                }

                mDevice = mBluetoothAdapter.getRemoteDevice(deviceHardwareAddress);

                if(status==false) {
                    ConnectThread ct = new ConnectThread(mDevice);      //socket connection two devices
                    ct.start();

                    ConnectedThread cet = new ConnectedThread(mmSocket);  //Stream connection for two devices
                }

                else{
                    Toast.makeText(MainActivity.this,"If you want to connect again close app and agin open it",Toast.LENGTH_SHORT).show();
                }


            }
        });


        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(status==true){
                    on_led(1);
                }

                else{
                    Toast.makeText(MainActivity.this,"first connect to arduino",Toast.LENGTH_SHORT).show();
                }

            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(status==true){
                    off_led(0);
                }

                else{
                    Toast.makeText(MainActivity.this,"first connect to arduino",Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void setUp(){
        connect = (Button)findViewById(R.id.button);
        on = (Button)findViewById(R.id.button2);
        off = (Button)findViewById(R.id.button3);
        text = (TextView)findViewById(R.id.textView2);
    }



    private class ConnectThread extends Thread {

        private final BluetoothDevice mmDevice;
        private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "Socket's create() sucess");
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }



        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                status = true;
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(status==true){
                        Toast.makeText(MainActivity.this,"Sucessfully connected",Toast.LENGTH_SHORT).show();
                    }

                    else{
                        Toast.makeText(MainActivity.this,"connection failed",Toast.LENGTH_SHORT).show();

                    }

                }
            });

        }


    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d(TAG, "Input Output stream create sucessfully");

            } catch (IOException e) {

                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

    }

    public void on_led(int a) {
        try {
            mmOutStream.write(a);
            Log.d(TAG, "on signal send sucessfully");

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

        }
    }

    public void off_led(int b) {
        try {
            mmOutStream.write(b);
            Log.d(TAG, "off signal send sucessfully");

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

        }
    }

}



