package com.example.catchcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;
import android.os.HandlerThread;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.example.catchcontroller.ml.ModelUnquant;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener {
    private static final String TAG = "myApp";
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket btSocket = null;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mBTDevice;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH};
    int imageSize = 224;
    private HandlerThread stream_thread,flash_thread,rssi_thread;
    private Handler stream_handler,flash_handler,rssi_handler;
    private ImageView monitor;
    private final int ID_CONNECT = 200;
    private final int ID_FLASH = 201;
    private final int ID_RSSI = 202;
    private boolean flash_on_off = false;
    private String ip_text = "192.168.0.105";
    public TextView tVobject;
    Button btnScan,btnAnalized, btnAddWaypoint,btnSpeak,btnListen;
    ImageButton btnForward, btnBackward, btnStop, btnLeft, btnRight;

    TextView tvCompassHeading, tvMapLatitude, tvMapLongitude;
    EditText txtTextToSpeech;
    ImageView ivCompassHeading;

    private boolean compassFound = false;
    private Compass compass;
    private float currentAzimuth;
    float bearing = 0;

    private GoogleMap myMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    Marker myMarker;
    String CurrentMode = "Manual";
    LatLng currentLatLng;

    Handler handler = new Handler();
    Runnable runnable;
    int delaySend = 1000;
    float currentHeading;
    boolean isDeviceConnected = false;

    TextToSpeech tts;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkPermission();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        lvNewDevices = (ListView) findViewById(R.id.bluetoothList);
        mBTDevices = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(MainActivity.this);
        monitor = findViewById(R.id.monitor);

        stream_thread = new HandlerThread("http");
        stream_thread.start();
        stream_handler = new HttpHandler(stream_thread.getLooper());

        flash_thread = new HandlerThread("http");
        flash_thread.start();
        flash_handler = new HttpHandler(flash_thread.getLooper());

        rssi_thread = new HandlerThread("http");
        rssi_thread.start();
        rssi_handler = new HttpHandler(rssi_thread.getLooper());

        stream_handler.sendEmptyMessage(ID_CONNECT);
        rssi_handler.sendEmptyMessage(ID_RSSI);

        tVobject = (TextView) findViewById(R.id.tVobject);

        tvCompassHeading = (TextView) findViewById(R.id.tvCompassHeading);
        ivCompassHeading = (ImageView) findViewById(R.id.ivCompassHeading);

        setupCompass();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EnableDisableBT();

        tvMapLatitude = (TextView) findViewById(R.id.tvMapLatitude);
        tvMapLongitude = (TextView) findViewById(R.id.tvMapLongitude);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        RadioGroup modeGroup= findViewById(R.id.ModeGroup);
        modeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                CurrentMode = radioButton.getText().toString();
            }
        });

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(1.0f);
                tts.setPitch(1.0f);
                Set<Voice> voices = tts.getVoices();
                List<Voice> voiceList = new ArrayList<>(voices);
                Voice selectedVoice = voiceList.get(5);
                tts.setVoice(selectedVoice);
            }
        });

        txtTextToSpeech = (EditText) findViewById(R.id.txtTextToSpeech);
        btnSpeak= findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = txtTextToSpeech.getText().toString();
                triggerSpeak(text);
            }
        });

        btnListen= findViewById(R.id.btnListen);
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent
                        = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                }
                catch (Exception e) {
                    Toast
                            .makeText(MainActivity.this, " " + e.getMessage(),
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        btnAddWaypoint= findViewById(R.id.btnAddWaypoint);
        btnAddWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentDialogBox dialogFragment = new FragmentDialogBox();
                Bundle bundle = new Bundle();
                ArrayList<String> waypoints = ((MyApplication) getApplication()).getWaypoints();
                bundle.putStringArrayList("waypoints",waypoints);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(),"FragmentDialogBox");
            }
        });

        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanDevices ();
            }
        });

        btnAnalized = findViewById(R.id.btnAnalized);
        btnAnalized.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) monitor.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                int dimension = Math.min(bitmap.getWidth(),bitmap.getHeight());
                bitmap = ThumbnailUtils.extractThumbnail(bitmap,dimension,dimension);
                monitor.setImageBitmap(bitmap);

                bitmap = Bitmap.createScaledBitmap(bitmap,imageSize,imageSize,false);

                classifyImage(bitmap);
            }
        });

        btnForward = findViewById(R.id.btnForward);
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "W";
                triggerCommand(command);
            }
        });

        btnBackward = findViewById(R.id.btnBackward);
        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "S";
                triggerCommand(command);
            }
        });

        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "X";
                triggerCommand(command);
            }
        });

        btnLeft = findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "A";
                triggerCommand(command);
            }
        });

        btnRight = findViewById(R.id.btnRight);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "D";
                triggerCommand(command);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String dataText =Objects.requireNonNull(result).get(0);
                txtTextToSpeech.setText(dataText);
                if(CurrentMode == "Manual"){
                    if(dataText.contains("turn left")){
                        triggerSpeak("turning left");
                        String command = "A";
                        triggerCommand(command);
                    }else if(dataText.contains("turn right")){
                        triggerSpeak("turning right");
                        String command = "D";
                        triggerCommand(command);
                    }else if(dataText.contains("go forward")){
                        triggerSpeak("going forward");
                        String command = "W";
                        triggerCommand(command);
                    }else if(dataText.contains("go backward")){
                        triggerSpeak("going backward");
                        String command = "S";
                        triggerCommand(command);
                    }else if(dataText.contains("stop")){
                        triggerSpeak("stoping");
                        String command = "X";
                        triggerCommand(command);
                    }
                }
            }
        }
    }

    private void getLastLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    currentLocation = location;

                    tvMapLatitude.setText("Latitude: " + currentLocation.getLatitude());
                    tvMapLongitude.setText("Longitude: " + currentLocation.getLongitude());

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }
        });
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

                tvMapLatitude.setText("Latitude: " + location.getLatitude());
                tvMapLongitude.setText("Longitude: " + location.getLongitude());

                myMarker.setPosition(current);
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 35));
            }
        };
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMapClickListener(this);
        LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        currentLatLng = current;
        myMarker = myMap.addMarker(new MarkerOptions().position(current).title("Current Location Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 35));
    }

    public void triggerSpeak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void triggerCommand(String command) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try
                {
                    if(isDeviceConnected){
                        btSocket.getOutputStream().write(command.getBytes());
                    }
                }
                catch (IOException e)
                {
                    Log.d(TAG, "ERROR SOCCKET");
                }
            }
        };
        thread.start();
    }

    @Override
    public void onMapClick(LatLng point) {
        String latlng = point.latitude + "x" + point.longitude;
        ((MyApplication) getApplication()).addWaypoint(latlng);
        myMap.addMarker(new MarkerOptions().position(point).title("Waypoint"));
    }

    public void classifyImage(Bitmap image) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val & 0xFF)*(1.f/255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Cat", "Person", "Cup", "Laptop"};

            tVobject.setText("Object Detected: "+ classes[maxPos]);

            String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }


            model.close();
        } catch (IOException e) {
            Log.e(TAG,"Error: " + e.getMessage());
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        tvMapLatitude.setText("Latitude: " + location.getLatitude());
        tvMapLongitude.setText("Longitude: " + location.getLongitude());
        //myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 35));
        refreshMap();
    }

    public void refreshMap(){
        myMap.clear();
        myMarker = myMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        for(String loc : ((MyApplication) getApplication()).getWaypoints()) {
            String[] locSeparated = loc.split("x");
            Double lat = Double.parseDouble(locSeparated[0]);
            Double lng = Double.parseDouble(locSeparated[1]);
            LatLng currentLoc = new LatLng(lat, lng);
            myMap.addMarker(new MarkerOptions().position(currentLoc).title("Waypoint").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private class HttpHandler extends Handler
    {
        public HttpHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case ID_CONNECT:
                    VideoStream();
                    break;
                case ID_FLASH:
                    SetFlash();
                    break;
                case ID_RSSI:
                    GetRSSI();
                    break;
                default:
                    break;
            }
        }
    }

    private void SetFlash()
    {
        flash_on_off ^= true;

        String flash_url;
        if(flash_on_off){
            flash_url = "http://" + ip_text + ":80/led?var=flash&val=1";
        }
        else {
            flash_url = "http://" + ip_text + ":80/led?var=flash&val=0";
        }

        try
        {

            URL url = new URL(flash_url);

            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("GET");
            huc.setConnectTimeout(1000 * 5);
            huc.setReadTimeout(1000 * 5);
            huc.setDoInput(true);
            huc.connect();
            if (huc.getResponseCode() == 200)
            {
                InputStream in = huc.getInputStream();

                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void GetRSSI() {
        rssi_handler.sendEmptyMessageDelayed(ID_RSSI,500);

        String rssi_url = "http://" + ip_text + ":80/RSSI";

        try {
            URL url = new URL(rssi_url);

            try {

                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("GET");
                huc.setConnectTimeout(1000 * 5);
                huc.setReadTimeout(1000 * 5);
                huc.setDoInput(true);
                huc.connect();
                if (huc.getResponseCode() == 200) {
                    InputStream in = huc.getInputStream();

                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(isr);
                    final String data = br.readLine();
                    if (!data.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //rssi_text.setText(data);
                            }
                        });
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void VideoStream()
    {
        String stream_url = "http://" + ip_text + ":81/stream";

        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try
        {
            Log.e(TAG,"Video Started");
            URL url = new URL(stream_url);

            try
            {
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("GET");
                huc.setConnectTimeout(1000 * 5);
                huc.setReadTimeout(1000 * 5);
                huc.setDoInput(true);
                huc.connect();

                if (huc.getResponseCode() == 200)
                {
                    InputStream in = huc.getInputStream();

                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(isr);

                    String data;

                    int len;
                    byte[] buffer;

                    while ((data = br.readLine()) != null)
                    {
                        if (data.contains("Content-Type:"))
                        {
                            data = br.readLine();

                            len = Integer.parseInt(data.split(":")[1].trim());

                            bis = new BufferedInputStream(in);
                            buffer = new byte[len];

                            int t = 0;
                            while (t < len)
                            {
                                t += bis.read(buffer, t, len - t);
                            }

                            Bytes2ImageFile(buffer, getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                            final Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    monitor.setImageBitmap(bitmap);
                                }
                            });

                        }


                    }
                }

            } catch (IOException e)
            {
                Log.e(TAG,"Error:" + e.getMessage());
            }
        }
        catch (MalformedURLException e)
        {
            Log.e(TAG,"Error:" + e.getMessage());
        } finally
        {
            try
            {
                if (bis != null)
                {
                    bis.close();
                }
                if (fos != null)
                {
                    fos.close();
                }

                stream_handler.sendEmptyMessageDelayed(ID_CONNECT,3000);
            } catch (IOException e)
            {
                Log.e(TAG,"Error:" + e.getMessage());
            }
        }

    }

    private void Bytes2ImageFile(byte[] bytes, String fileName)
    {
        try
        {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void SendData(String address){
        if (btSocket == null || (!btSocket.isConnected()))
        {
            BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(address);
            try {
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(arduinoUUID);
                bluetoothAdapter.cancelDiscovery();
                btSocket.connect();
                isDeviceConnected = true;
            } catch (IOException e) {
                try {
                    btSocket =(BluetoothSocket) mBTDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mBTDevice,1);
                    btSocket.connect();
                    Log.e(TAG,"Connected");
                    isDeviceConnected = true;
                } catch (NoSuchMethodException | IOException | InvocationTargetException |
                         IllegalAccessException ex) {
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BRScan);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver4);
        compass = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        compass.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delaySend);
                if(isDeviceConnected){
                    if(CurrentMode.equals("Automatic")){
                        ArrayList<String> waypoints = ((MyApplication) getApplication()).getWaypoints();
                        if(waypoints.size() > 0){
                            try {
                                String waypoint = waypoints.get(0);
                                String command = currentHeading + "x"+ currentLatLng.latitude + "x" + currentLatLng.longitude + "x" + waypoint;
                                btSocket.getOutputStream().write(command.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }, delaySend);
        super.onResume();
        compass.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        compass.stop();
        handler.removeCallbacks(runnable);
    }

    private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                adjustArrow(azimuth);
            }

            @Override
            public void onMagField(float strength) {
                MagField(strength);
            }
        };
        compass.setListener(cl);

        if (compass != null) {
            compass.setListener(cl);
            if (compass.getStatus()) {
                compassFound = true;
            }
        }
        if (!compassFound) {
            tvCompassHeading.setText("Sorry, but this device not contain magnetic sensor.");
        }
    }

    private void MagField(float strength) {
        //tvMagStrength.setText(strength + " μT"); //milli Tesla
    }

    private void adjustArrow(float azimuth) {
        if (compass.getSensorData()) {
            float heading = azimuth;
            heading = (bearing - heading) * -1;

            Animation an = new RotateAnimation(-currentAzimuth, -heading,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            currentAzimuth = heading;

            an.setDuration(500);
            an.setRepeatCount(0);
            an.setFillAfter(true);

            ivCompassHeading.startAnimation(an);
            //tvOutput.setText(String.valueOf(azimuth));
            //String.valueOf(heading) + " " +
            currentHeading = heading;
            tvCompassHeading.setText(showDirection(heading));
        } else {
            tvCompassHeading.setText("Sorry, no data from compass sensor.");
        }
    }

    private String showDirection(float degree) {
        String heading = "";
        if (degree >= 338 || degree < 23) {
            //GOING NORTH
            heading = "N";
        } else if (degree >= 23 && degree < 68) {
            //GOING NORTH EAST
            heading = "NE";
        } else if (degree >= 68 && degree < 113) {
            //GOING EAST
            heading = "E";
        } else if (degree >= 113 && degree < 158) {
            //GOING SOUTH EAST
            heading = "SE";
        } else if (degree >= 158 && degree < 203) {
            //GOING SOUTH
            heading = "S";
        } else if (degree >= 203 && degree < 248) {
            //GOING SOUTH WEST
            heading = "SW";
        } else if (degree >= 248 && degree < 293) {
            //GOING WEST
            heading = "W";
        } else if (degree >= 293 && degree < 338) {
            //GOING NORTH WEST
            heading = "NW";
        }

        return Math.round(degree) + "° " + heading;
    }

    @SuppressLint({"MissingPermission", "CheckResult"})
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        bluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();
            mBTDevice = mBTDevices.get(i);
            arduinoBTModule = mBTDevices.get(i);
            Log.d(TAG, "UUIDS " + mBTDevice.getUuids().length);
            arduinoUUID = mBTDevice.getUuids()[0].getUuid();
            if (arduinoBTModule != null) {
                SendData(deviceAddress);
            }
        }
    }

    private BroadcastReceiver BRScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("myApp", "Received");
            final String action= intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    public  void ScanDevices (){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();

            Set <BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device: pairedDevices) {
                    mBTDevices.add(device);
                    mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, mBTDevices);
                    lvNewDevices.setAdapter(mDeviceListAdapter);
                }
            }
        }
        if(!bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.startDiscovery();
        }
    }

    protected void checkPermission() {
        final List<String> missingPermissions = new ArrayList<String>();
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @SuppressLint("MissingPermission")
    public void EnableDisableBT(){
        if(bluetoothAdapter==null){
        }else if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            IntentFilter enableBtIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(BRScan, enableBtIntent);
        }else if(bluetoothAdapter.isEnabled()){
        }
    }
}