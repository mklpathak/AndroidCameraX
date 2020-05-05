package com.mukul.androidcamerax.camera_view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.mukul.androidcamerax.BuildConfig;
import com.mukul.androidcamerax.CropActivity;
import com.mukul.androidcamerax.R;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class CameraActivity extends AppCompatActivity implements ScaleGestureDetector.OnScaleGestureListener {

    public static String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    public static String PHOTO_EXTENSION= ".jpg";







    @BindView(R.id.zoom)
    SeekBar zoom;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ExecutorService cameraExecutor;


    @BindView(R.id.view_finder)
    PreviewView previewView;
    @BindView(R.id.imgCapture)
    ImageButton imgCapture;
    @BindView(R.id.flash)
    ImageButton flash;
    @BindView(R.id.switchcamera)
    ImageButton switchcamera;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private File file;
    ScaleGestureDetector scaleGestureDetector;

    private Preview preview;
    private boolean torchStatus = false;
    int cameraInUs = CameraSelector.LENS_FACING_BACK;

    CameraInfo cameraInfo;
    CameraControl cameraControl;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private Camera camera;
    private float getMaximumZoom;
    private float getMinimumzzom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_fragment);
        ButterKnife.bind(this);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraExecutor =  Executors.newSingleThreadExecutor();
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }


        scaleGestureDetector = new ScaleGestureDetector(this, this);

        if (allPermissionsGranted()) {

            startCamera();
            //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        previewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scaleGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });


        zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(camera!=null){
                    camera.getCameraControl().setLinearZoom(i / 100);
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



    }


    @SuppressLint("RestrictedApi")
    private void startCamera() {

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

       cameraProvider.unbindAll();


        // Set up the view finder use case to display camera preview
        Preview preview = new Preview.Builder().build();



        // Set up the capture use case to allow users to take photos
        imageCapture = new ImageCapture.Builder()

                .setTargetRotation(previewView.getDisplay().getRotation())

                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Choose the camera by requiring a lens facing
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraInUs)
                .build();

        // Attach use cases to the camera with the same lifecycle owner
        camera = cameraProvider.bindToLifecycle(
                ((LifecycleOwner) this),
                cameraSelector,
                preview,
                imageCapture);






        // Connect the preview use case to the previewView


        preview.setSurfaceProvider(
                previewView.createSurfaceProvider(camera.getCameraInfo()));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

//        al currentZoomRatio: Float = cameraInfo.zoomRatio.value ?: 0F
//        val delta = detector.scaleFactor
//        cameraControl.setZoomRatio(currentZoomRatio * delta)
//        return truev

        Float f = 0f;
        f = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();

        float delta = scaleGestureDetector.getScaleFactor();

        camera.getCameraControl().setZoomRatio(f * delta);


        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    @OnClick({R.id.imgCapture, R.id.flash, R.id.switchcamera})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgCapture:

                file=    new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");




                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Uri savedUri = outputFileResults.getSavedUri()==null?Uri.fromFile(file):outputFileResults.getSavedUri();

                        Timber.d(savedUri.toString());

                        String msg = "Pic captured at " + file.getAbsolutePath();
                       //  Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(CameraActivity.this, CropActivity.class);
                        intent.putExtra("Image",file);
                        startActivity(intent);


                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {

                        Timber.e(exception.getMessage());

                    }
                });

                break;
            case R.id.flash:

                torchStatus = !torchStatus;
                camera.getCameraControl().enableTorch(torchStatus);

                break;
            case R.id.switchcamera:
                cameraInUs = cameraInUs == CameraSelector.LENS_FACING_FRONT
                        ? CameraSelector.LENS_FACING_BACK :
                        CameraSelector.LENS_FACING_FRONT;
                startCamera();
                break;
        }
    }
}
