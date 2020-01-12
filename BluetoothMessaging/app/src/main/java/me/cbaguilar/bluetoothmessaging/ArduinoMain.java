package me.cbaguilar.bluetoothmessaging;


import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import me.cbaguilar.bluetoothmessaging.DeviceListActivity;
import me.cbaguilar.bluetoothmessaging.R;

public class ArduinoMain extends Activity {

    boolean btConnected = false;
    //Declare buttons & editText
    Button functionOne, functionTwo, pickTime, connectBlue;

    TextView txtView, connStatus;


    private int mYear, mMonth, mDay, mHour, mMinute;


    //Memeber Fields
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // UUID service - This is the type of Bluetooth device that the BT module is
    // It is very likely yours will be the same, if not google UUID for your manufacturer
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module
    public String newAddress = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino_main);

        //addKeyListener();

        //Initialising buttons in the view
        //mDetect = (Button) findViewById(R.id.mDetect);
        functionOne = (Button) findViewById(R.id.functionOne);
        functionTwo = (Button) findViewById(R.id.functionTwo);

        connectBlue = (Button) findViewById(R.id.connect);

        txtView = (TextView) findViewById(R.id.timeTxt);
        connStatus = (TextView) findViewById(R.id.connText);

        pickTime = (Button) findViewById(R.id.pickTime);

        //getting the bluetooth adapter value and calling checkBTstate function
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        /**************************************************************************************************************************8
         *  Buttons are set up with onclick listeners so when pressed a method is called
         *  In this case send data is called with a value and a toast is made
         *  to give visual feedback of the selection made
         */

        functionOne.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(sendData("0")) {
                    Toast.makeText(getBaseContext(), "Turned off", Toast.LENGTH_SHORT).show();
                }
            }
        });

        functionTwo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (sendData("1")) {
                    Toast.makeText(getBaseContext(), "Turned On", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pickTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btConnected) {
                    final Calendar c = Calendar.getInstance();
                    mHour = c.get(Calendar.HOUR_OF_DAY);
                    mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(ArduinoMain.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {

                                    txtView.setText("Delay set at " + setAlarm(mHour, mMinute, hourOfDay, minute));

                                }
                            }, mHour, mMinute, false);
                    timePickerDialog.show();
                }
                else {
                    Toast.makeText(getBaseContext(), "Not connected!" , Toast.LENGTH_SHORT).show();
                }
            }

        });

        connectBlue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ArduinoMain.this, DeviceListActivity.class);
                startActivity(i);
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        // connection methods are best here in case program goes into the background etc

        //Get MAC address from DeviceListActivity
        Intent intent = getIntent();
        newAddress = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        // Set up a pointer to the remote device using its address.

        if (newAddress != null) {
            btConnected = true;

            connStatus.setText("Connected!");

            BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);

            //Attempt to create a bluetooth socket for comms
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e1) {
                btConnected = false;
                updateStatus(btConnected);
                Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
            }

            // Establish the connection.
            try {
                btSocket.connect();
            } catch (Exception e) {
                try {
                    btSocket.close();        //If IO exception occurs attempt to close socket
                } catch (Exception e2) {
                    btConnected = false;
                    updateStatus(btConnected);
                    Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
                }
            }

            // Create a data stream so we can talk to the device
            try {
                outStream = btSocket.getOutputStream();
            } catch (Exception e) {
                btConnected = false;
                updateStatus(btConnected);
                Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
            }
            //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
            // i.e don't wait for a user to press button to recognise connection failure
            sendData("x");
        }
        else {
            connStatus.setText("Not Connected!!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Pausing can be the end of an app if the device kills it or the user doesn't open it again
        //close all connections so resources are not wasted

        //Close BT socket to device
        try     {
            btSocket.close();
        } catch (Exception e2) {
            btConnected = false;
            updateStatus(btConnected);
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }
    //takes the UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws Exception {

        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    //same as in device list activity
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private Handler mHandler;

    private HandlerThread mHandlerThread;

    public void startHandlerThread(){
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    private int setAlarm(int oldHour, int oldMin, int futHour, int futMin) {

        int curTotMs = (oldHour*60+oldMin)*(60)*1000;
        int futTotMs = (futHour*60+futMin)*(60)*1000;

        int delay = futTotMs - curTotMs;
        //Toast.makeText(getBaseContext(), "Delay is" + delay, Toast.LENGTH_SHORT).show();


        if (delay < 0) {
            delay+=(12*60*60)*1000;
        }

        int delayHours = delay/((60*60)*1000);
       int delayMins = (delay/(60*1000))%60;

        Toast.makeText(getBaseContext(), "Alarm set for " + delayHours+ " hours and "+delayMins + " minutes!", Toast.LENGTH_SHORT).show();

        startHandlerThread();
        mHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                sendData("1");
                                Toast.makeText(getBaseContext(), "Turned On", Toast.LENGTH_SHORT).show();
                                Log.i("tag", "This'll run 300 milliseconds later");
                            }
                        },
                        delay);


        return delay;

    }

    void updateStatus(boolean stat) {
        if (stat) {
            connStatus.setText("Connected!");

        }
        else {
            connStatus.setText("Not Connected!!");

        }

    }


    // Method to send data
    private boolean sendData(String message) {

        if (btConnected) {
            byte[] msgBuffer = message.getBytes();

            try {
                //attempt to place data on the outstream to the BT device
                outStream.write(msgBuffer);
                return true;
            } catch (Exception e) {
                btConnected = false;
                updateStatus(btConnected);
                //if the sending fails this is most likely because device is no longer there
                Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else {
            Toast.makeText(getBaseContext(), "Not connected!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}