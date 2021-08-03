package com.example.MVMcR_MA_QCR;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.hardware.Camera.Parameters;

public class CameraPreview extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera camera = null;
    private SurfaceView cameraSurfaceView = null;
    private SurfaceHolder cameraSurfaceHolder = null;
    private boolean previewing = false;
    RelativeLayout relativeLayout;

    private Button btnCapture = null;

    private String page;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera_preview);

        Box box = new Box(this);
        addContentView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));


        relativeLayout=(RelativeLayout) findViewById(R.id.containerImg);
        relativeLayout.setDrawingCacheEnabled(true);
        cameraSurfaceView = (SurfaceView)
                findViewById(R.id.surfaceView1);
        //  cameraSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(640, 480));
        cameraSurfaceHolder = cameraSurfaceView.getHolder();
        cameraSurfaceHolder.addCallback(this);
        //    cameraSurfaceHolder.setType(SurfaceHolder.
        //                                               SURFACE_TYPE_PUSH_BUFFERS);


        btnCapture = (Button)findViewById(R.id.button1);
        btnCapture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                //camera = Camera.open();
                //set camera to continually auto-focus
                final Parameters params = camera.getParameters();

                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
                params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                params.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
                camera.setParameters(params);
                //Toast.makeText(getApplicationContext(), "flash== "+params, Toast.LENGTH_LONG).show();

                camera.takePicture(cameraShutterCallback, cameraPictureCallbackRaw, cameraPictureCallbackJpeg);
                //camera.takePicture(null, null, cameraPictureCallbackJpeg);
            }
        });




        //Getting data from ScanQR
        page = getIntent().getStringExtra("calling_page");

        // Add Back Arrow to Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handle Back Arrow operation Here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    Camera.ShutterCallback cameraShutterCallback = new Camera.ShutterCallback()
    {
        @Override
        public void onShutter()
        {
            // TODO Auto-generated method stub

        }
    };

    Camera.PictureCallback cameraPictureCallbackRaw = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            // TODO Auto-generated method stub

        }
    };

    Camera.PictureCallback cameraPictureCallbackJpeg = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            // TODO Auto-generated method stub

            //Camera camera1 = Camera.open();


            Camera.Parameters params = camera.getParameters();
            //List<String> flashModes = params.getSupportedFlashModes();

/*
            //It is better to use defined constraints as opposed to String, thanks to AbdelHady
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            //params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            params.setAutoWhiteBalanceLock(Boolean.parseBoolean(Camera.Parameters.WHITE_BALANCE_DAYLIGHT));
            params.setColorEffect(Camera.Parameters.EFFECT_NONE);
            camera.setParameters(params);
            camera.startPreview();
*/


            Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            int  wid = cameraBitmap.getWidth();
            int  hgt = cameraBitmap.getHeight();

            //  Toast.makeText(getApplicationContext(), wid+""+hgt, Toast.LENGTH_SHORT).show();
            Bitmap newImage = Bitmap.createBitmap(wid, hgt, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(newImage);

            canvas.drawBitmap(cameraBitmap, 0f, 0f, null);
/*
            Drawable drawable = getResources().getDrawable
                    (R.drawable.mark3);
            drawable.setBounds(20, 30, drawable.getIntrinsicWidth()+20, drawable.getIntrinsicHeight()+30);
            drawable.draw(canvas);
*/


            File storagePath = new File(Environment.
                    getExternalStorageDirectory() + "/PhotoAR/");
            storagePath.mkdirs();

            File myImage = new File(storagePath,
                    Long.toString(System.currentTimeMillis()) + ".jpg");

            try
            {
                FileOutputStream out = new FileOutputStream(myImage);
                newImage.compress(Bitmap.CompressFormat.JPEG, 80, out);


                out.flush();
                out.close();
            }
            catch(FileNotFoundException e)
            {
                Log.d("In Saving File", e + "");
            }
            catch(IOException e)
            {
                Log.d("In Saving File", e + "");
            }



            int w = canvas.getWidth();
            int h = canvas.getHeight();
            int s = h - (h/10);

            int x1 = (w - s)/2;
            int y1 = (h - s)/2;

            //Toast.makeText(CameraPreview.this,"Hello Javatpoint==="+w+"-"+h+" x= "+x1+" y= "+y1,Toast.LENGTH_LONG).show();

            Bitmap croppedImage = Bitmap.createBitmap(newImage, x1, y1, s, s);

            switch (page){
                case "ImageRegistration":
                    if (((BitmapDrawable)ImageRegistration.imagePreview.getDrawable()).getBitmap() == null){
                        // Handle If ImageView is Blank
                        //Toast.makeText(getApplicationContext(), "Blank", Toast.LENGTH_SHORT).show();
                        ImageRegistration.imagePreview.setVisibility(View.VISIBLE);
                        ImageRegistration.imagePreview.setRotation(90);
                        ImageRegistration.imagePreview.setImageBitmap(croppedImage);
                    }else {
                        // Handle If ImageView is Not Blank
                        //Toast.makeText(getApplicationContext(), "Not Blank", Toast.LENGTH_SHORT).show();
                        ImageRegistration.imagePreview.setVisibility(View.VISIBLE);
                        ImageRegistration.imagePreview.setRotation(90);
                        ImageRegistration.imagePreview.setImageBitmap(croppedImage);
                    }
                    onBackPressed();
                    break;

                default:
                    Toast.makeText(CameraPreview.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    break;
            }

/*
            camera.startPreview();



            newImage.recycle();
            newImage = null;

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.parse("file://" + myImage.getAbsolutePath()), "image/*");
            startActivity(intent);
            */

        }
    };



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // TODO Auto-generated method stub

        if(previewing)
        {
            camera.stopPreview();
            previewing = false;
        }
        try
        {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(640, 480);
            parameters.setPictureSize(640, 480);
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                camera.setDisplayOrientation(90);

            }

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            // parameters.setRotation(90);
            camera.setParameters(parameters);

            camera.setPreviewDisplay(cameraSurfaceHolder);
            camera.startPreview();
            previewing = true;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        try
        {
            camera = Camera.open();
        }
        catch(RuntimeException e)
        {
            Toast.makeText(getApplicationContext(), "Device camera  is not working properly, please try after sometime.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }




}
