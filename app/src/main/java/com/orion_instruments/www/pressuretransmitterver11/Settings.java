package com.orion_instruments.www.pressuretransmitterver11;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Settings extends AppCompatActivity {

    Button buttonUpdate;
    TextView sensor;
    Handler bluetoothIn;
    final int handlerState = 0;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private Settings.ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static String address;
    GridView gridView1;

    String[] gridViewString={
            "Mode","Relay","Transmitter","Data Logger","Calibration","Device Settings","WiFi","About"
    };
    int[] gridViewImageId={
            R.drawable.mode,R.drawable.relay,
            R.drawable.transmitter, R.drawable.datalogger,
            R.drawable.calibration, R.drawable.devicesettings,
            R.drawable.wifi,R.drawable.about

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        CustomGridViewActivity customGridViewActivity=new CustomGridViewActivity(Settings.this,gridViewString,gridViewImageId);




        gridView1=(GridView)findViewById(R.id.gridView);
        gridView1.setAdapter(customGridViewActivity);
        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    Intent intent = new Intent(Settings.this, Mode.class);
                  //  Intent  = new Intent(Settings.this, Mode.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    overridePendingTransition(0, 0);

                }
                if(i==1){
                Intent intent = new Intent(Settings.this, Relay.class);
                    startActivity(intent);

                }
                if(i==2){
                    Intent intent = new Intent(Settings.this, Transmitter.class);
                    startActivity(intent);
                }
                if(i==3){
                    Intent intent = new Intent(Settings.this, Datalogger.class);
                    startActivity(intent);
                }
                if(i==4){
                    Intent intent = new Intent(Settings.this, Calibration.class);
                    startActivity(intent);
                }
                if(i==5){
                    Intent intent = new Intent(Settings.this, DeviceSettings.class);
                    startActivity(intent);
                }
                if(i==6){
                    Intent intent = new Intent(Settings.this, WiFI.class);
                    startActivity(intent);
                }
                if(i==7){
                    Intent intent = new Intent(Settings.this, About.class);
                    startActivity(intent);
                }




                // Toast.makeText(MainActivity.this,"Selected: "+gridViewString[+i], Toast.LENGTH_LONG).show();
            }
        });
        ///////////////////////////////////////////////////////////////////////////////////////////////
     //   buttonUpdate.setOnClickListener(new View.OnClickListener()
     //   {
     //       public void onClick(View v) {
     //           mConnectedThread.write(txtArduino.getText().toString());    // Send text via Bluetooth
     //           Toast.makeText(getBaseContext(),txtArduino.getText().toString() + "Data send to device", Toast.LENGTH_SHORT).show();
     //       }
     //   });

        /////////////////////////////////////code for Rx from arduino//////////////////////////////////////////////////////////

        bluetoothIn = new Handler() {

            public void handleMessage(android.os.Message msg) {
                String dataInPrint;
                if (msg.what == handlerState)
                {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0)
                    {                                           // make sure there data before ~
                        String sensor0 = recDataString.substring(0, endOfLineIndex);    // extract string
                        sensor.setText(sensor0);    //update the textviews with sensor values
                        //int value=Integer.parseInt(sensor.getText().toString());

                        sensor0 = " ";
                        //value = 0;
                    }
                    recDataString.delete(0, recDataString.length());                    //clear all string data
                    // strIncom =" ";
                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    //   Code for Send Receive
//////////////////////////////////////////////////////////////////////////////////////////////////////
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onResume()
    {
        super.onResume();


        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();
        //Get the MAC address from the DeviceListActivty via EXTRA
        //address = intent.getStringExtra(Startup.EXTRA_ADDRESS);
        address = "20:16:01:18:20:20";
        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        //sensor.setText();            /*

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e)
        {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e)
        {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread= new com.orion_instruments.www.pressuretransmitterver11.Settings.ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState()
    {

        if(btAdapter==null)
        {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else
        {
            if (btAdapter.isEnabled())
            {
            } else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //create new class for connect thread
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true)
            {
                try
                {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e)
                {
                    break;
                }
            }
        }
        //write method
        public void write(String input)
        {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try
            {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e)
            {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
            }
        }


    }
}



