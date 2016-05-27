package com.nomura.cardreader.usb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.magtek.mobile.android.mtlib.MTCardDataState;
import com.magtek.mobile.android.mtlib.MTConnectionState;
import com.magtek.mobile.android.mtlib.MTConnectionType;
import com.magtek.mobile.android.mtlib.MTSCRA;
import com.magtek.mobile.android.mtlib.MTSCRAEvent;


public class MainActivity extends ActionBarActivity {

    public static final String DEVICE_NAME = "USB";
    public static final String DEVICE_ADDRESS = "";
    private MTSCRA m_scra;
    private MTConnectionType m_connectionType;
    private String m_deviceName;
    private String m_deviceAddress;
    private TextView txtTrack2Data;
    private Button clearDataBtn;
    private Button connectUSB;
    private Button disconnectUSB;
    private Handler m_scraHandler = new Handler(new SCRAHandlerCallback());
    public static final String TAG = "cardReaderTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        txtTrack2Data = (TextView) findViewById(R.id.cardData);
        txtTrack2Data.setText("");

        clearDataBtn = (Button) findViewById(R.id.clearDataBtn);
        connectUSB = (Button) findViewById(R.id.connectUSB);
        disconnectUSB = (Button) findViewById(R.id.disconnectUSB);

        m_deviceName = DEVICE_NAME;
        m_deviceAddress = DEVICE_ADDRESS;
        m_connectionType = MTConnectionType.USB;
        m_scra = new MTSCRA(this, m_scraHandler);


        clearDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTrack2Data.setText("");
            }
        });

        connectUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! m_scra.isDeviceConnected()) {
                    if (openDevice() != 0) {
                        Log.d(TAG, "Failed to open USB card reader device");
                    } else {
                        Log.d(TAG, "USB card reader device is now open");
                    }
                }
            }
        });

        disconnectUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_scra != null) {
                    m_scra.closeDevice();
                    Log.d(TAG, "USB card reader device has been closed.");
                }
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

        if (m_scra != null) {
            m_scra.closeDevice();
            Log.d(TAG, "USB card reader device has been closed.");
        }
    }

    private class SCRAHandlerCallback implements Handler.Callback {
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MTSCRAEvent.OnDeviceConnectionStateChanged:
                    onDeviceStateChanged((MTConnectionState) msg.obj);
                    break;
                case MTSCRAEvent.OnCardDataStateChanged:
                    onCardDataStateChanged((MTCardDataState) msg.obj);
                    break;
                case MTSCRAEvent.OnDataReceived:
                    onCardDataReceived();
                    break;
                case MTSCRAEvent.OnDeviceResponse:
                    break;
            }
            return true;
        }
    }

    protected void onDeviceStateChanged(MTConnectionState deviceState) {
        switch (deviceState) {
            case Disconnected:
                Toast.makeText(MainActivity.this, R.string.cr_state_disconnect, Toast.LENGTH_LONG).show();
                Log.d(TAG, "USB card reader device is disconnected");
                break;
            case Connected:
                Toast.makeText(MainActivity.this, R.string.cr_state_connected, Toast.LENGTH_LONG).show();
                Log.d(TAG, "USB card reader device is connected");
                break;
            case Error:
                break;
            case Connecting:
                Log.d(TAG, "USB card reader device is connecting...");
                break;
            case Disconnecting:
                break;
        }
    }

    protected void onCardDataStateChanged(MTCardDataState cardDataState) {
        switch (cardDataState) {
            case DataNotReady:
                break;
            case DataReady:
                break;
            case DataError:
                //Toast.makeText(MainActivity.this, R.string.cr_data_error, Toast.LENGTH_LONG).show();
                Log.d(TAG, "There was was an error reading the card data");
                break;
        }

    }

    public void onCardDataReceived() {
        String cardData = m_scra.getTrack2();
        if (cardData != null && !cardData.isEmpty()) {
            Log.d(TAG, "Track 2 card data is: " + cardData);
            txtTrack2Data.setText(cardData);
        }
    }

    public long openDevice() {
        long result = -1;

        if (m_scra != null)
        {
            m_scra.setConnectionType(m_connectionType);
            m_scra.setAddress(m_deviceAddress);
            m_scra.openDevice();
            result = 0;
        }

        return result;
    }

}

