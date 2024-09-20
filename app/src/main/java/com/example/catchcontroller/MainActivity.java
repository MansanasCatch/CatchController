package com.example.catchcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.graphics.Bitmap;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.catchcontroller.ml.ModelUnquant;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.nio.ByteBuffer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.widget.Toast;

import com.example.catchcontroller.Drawing.BorderedText;
import com.example.catchcontroller.Drawing.MultiBoxTracker;
import com.example.catchcontroller.Drawing.OverlayView;
import com.example.catchcontroller.livefeed.CameraConnectionFragment;
import com.example.catchcontroller.livefeed.ImageUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMapClickListener,
        ImageReader.OnImageAvailableListener{
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
            Manifest.permission.BLUETOOTH,
            Manifest.permission.CAMERA};

    int imageSize = 224;

    Button btnScan,btnAddWaypoint,btnSpeak,btnListen;
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

    RecyclerView rvCommands;
    CommandAdapter commandAdapter;

    private Matrix frameToCropTransform;
    private int sensorOrientation;
    private Matrix cropToFrameTransform;
    private static final int TF_OD_API_INPUT_SIZE = 320;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final float TEXT_SIZE_DIP = 10;
    private static final int PERMISSION_CODE = 321;
    OverlayView trackingOverlay;
    private BorderedText borderedText;
    private Detector detector;
    Handler handler2;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        handler2 = new Handler();

        checkPermission();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.CAMERA};
                requestPermissions(permission, PERMISSION_CODE);
            }
            else {
                setFragment();
            }
        }

        //TODO intialize the tracker to draw rectangles
        tracker = new MultiBoxTracker(this);
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            this,
                            "efficientdet_lite0.tflite",
                            "labelmap.txt",
                            TF_OD_API_INPUT_SIZE,
                            true);
            Log.d(TAG,"success");
            Toast.makeText(this, "Model loaded Successfully", Toast.LENGTH_SHORT).show();
        } catch (final IOException e) {
            Log.d(TAG,"error in town"+e.getMessage());
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        lvNewDevices = (ListView) findViewById(R.id.bluetoothList);
        mBTDevices = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(MainActivity.this);

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

        rvCommands = (RecyclerView) findViewById(R.id.rvCommands);
        rvCommands.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<ModelCommand> options =
                new FirebaseRecyclerOptions.Builder<ModelCommand>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("commands"), ModelCommand.class)
                        .build();

        commandAdapter = new CommandAdapter(options);
        rvCommands.setAdapter(commandAdapter);

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

//        btnAnalized = findViewById(R.id.btnAnalized);
//        btnAnalized.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                BitmapDrawable bitmapDrawable = (BitmapDrawable) monitor.getDrawable();
////                Bitmap bitmap = bitmapDrawable.getBitmap();
////
////                int dimension = Math.min(bitmap.getWidth(),bitmap.getHeight());
////                bitmap = ThumbnailUtils.extractThumbnail(bitmap,dimension,dimension);
////                monitor.setImageBitmap(bitmap);
////
////                bitmap = Bitmap.createScaledBitmap(bitmap,imageSize,imageSize,false);
////
////                classifyImage(bitmap);
//            }
//        });

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

    //TODO fragment which show llive footage from camera
    int previewHeight = 0,previewWidth = 0;
    protected void setFragment() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[1];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        Fragment fragment;

        CameraConnectionFragment camera2Fragment =
                CameraConnectionFragment.newInstance(
                        new CameraConnectionFragment.ConnectionCallback() {
                            @Override
                            public void onPreviewSizeChosen(final Size size, final int rotation) {
                                previewHeight = size.getHeight();
                                previewWidth = size.getWidth();

                                final float textSizePx =
                                        TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
                                borderedText = new BorderedText(textSizePx);
                                borderedText.setTypeface(Typeface.MONOSPACE);

                                tracker = new MultiBoxTracker(MainActivity.this);

                                int cropSize = TF_OD_API_INPUT_SIZE;

                                previewWidth = size.getWidth();
                                previewHeight = size.getHeight();

                                sensorOrientation = rotation - getScreenOrientation();

                                rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
                                croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

                                frameToCropTransform =
                                        ImageUtils.getTransformationMatrix(
                                                previewWidth, previewHeight,
                                                cropSize, cropSize,
                                                sensorOrientation, MAINTAIN_ASPECT);

                                cropToFrameTransform = new Matrix();
                                frameToCropTransform.invert(cropToFrameTransform);

                                trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
                                trackingOverlay.addCallback(
                                        new OverlayView.DrawCallback() {
                                            @Override
                                            public void drawCallback(final Canvas canvas) {
                                                tracker.draw(canvas);
                                                Log.d(TAG,"inside draw");
                                            }
                                        });

                                tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
                            }
                        },
                        this,
                        R.layout.camera_fragment,
                        new Size(640, 900));

        camera2Fragment.setCamera(cameraId);
        fragment = camera2Fragment;
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }


    //TODO getting frames of live camera footage and passing them to model
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private Bitmap rgbFrameBitmap;
    Image image;

    @Override
    public void onImageAvailable(ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            Log.d(TAG,"RETURN 1 ");
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                Log.d(TAG,"RETURN 2 ");
                return;
            }
            Log.d(TAG, String.valueOf(isProcessingFrame));

//            if (isProcessingFrame) {
//                image.close();
//                Log.d(TAG,"RETURN 3 ");
//                return;
//            }
            isProcessingFrame = true;
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };

            processImage();

        } catch (final Exception e) {
            Log.d(TAG,e.getMessage()+"abc ");
            return;
        }

    }


    String result = "";
    Bitmap croppedBitmap;
    private MultiBoxTracker tracker;
    public void processImage(){
        Log.d(TAG,"PROCCESS IMAGE 1");
        imageConverter.run();;
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"PROCCESS IMAGE 2");
                //TODO pass image to model and get results
                List<Detector.Recognition> results = detector.recognizeImage(rgbFrameBitmap);
                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;

                final List<Detector.Recognition> mappedRecognitions =
                        new ArrayList<>();

                int maxObjects = 2;
                int currentObject =0;
                for (final Detector.Recognition result : results) {
                    if(currentObject != maxObjects){
                        if (result.getConfidence() >= minimumConfidence) {
                            mappedRecognitions.add(result);
                            currentObject++;
                        }
                    }
                }

                tracker.trackResults(mappedRecognitions, 2);
                trackingOverlay.postInvalidate();
                postInferenceCallback.run();

                image.close();
                isProcessingFrame = false;
                Log.d(TAG,"RETURN CLOSE ");
            }
        });
    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }
    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
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

    public void triggerServerCommand(String commandKey,String commandText) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try
                {
                    if(isDeviceConnected){
                        btSocket.getOutputStream().write(commandText.getBytes());
                        triggerServerDeleteCommand(commandKey);
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

    public void triggerServerDeleteCommand(String commandKey) {
        Log.d(TAG, commandKey);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("commands");

        reference.child(commandKey).removeValue();
    }

    @Override
    public void onMapClick(LatLng point) {
        String latlng = point.latitude + "x" + point.longitude;
        ((MyApplication) getApplication()).addWaypoint(latlng);
        myMap.addMarker(new MarkerOptions().position(point).title("Waypoint"));
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
        detector.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        compass.start();
        commandAdapter.startListening();
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
                    }else if(CurrentMode.equals("Manual")){
                        int serverCommandCount = commandAdapter.getItemCount();
                        rvCommands.scrollToPosition(commandAdapter.getItemCount() - 1);
                        if(serverCommandCount > 0){
                            ModelCommand viewItem = commandAdapter.getItem(commandAdapter.getItemCount()-1);
                            String commandKey = String.valueOf(viewItem.getKey());
                            String commandText = String.valueOf(viewItem.getCommand());
                            triggerServerCommand(commandKey, commandText);
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
        commandAdapter.stopListening();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE && grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            setFragment();
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