package com.example.MVMcR_MA_QCR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

public class CustomCamera extends AppCompatActivity implements SurfaceHolder.Callback{
    private Camera camera = null;
    private SurfaceView cameraSurfaceView = null;
    private SurfaceHolder cameraSurfaceHolder = null;
    //    private boolean previewing = false;
    RelativeLayout relativeLayout;

    private ImageButton btnCapture = null;

    public Bitmap croppedImage;
    public static Bitmap scaledImage;

    public static float xmin, ymin, xmax, ymax;
    public static int clip_id;


    int camera_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        setContentView(R.layout.camera_button);
        CustomCameraBox box = new CustomCameraBox(this);
        addContentView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));


        relativeLayout=(RelativeLayout) findViewById(R.id.containerImg);
        relativeLayout.setDrawingCacheEnabled(true);
        cameraSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        //  cameraSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(640, 480));
        cameraSurfaceHolder = cameraSurfaceView.getHolder();
        cameraSurfaceHolder.addCallback(this);
        //    cameraSurfaceHolder.setType(SurfaceHolder.
        //                                               SURFACE_TYPE_PUSH_BUFFERS);

        btnCapture = (ImageButton)findViewById(R.id.img_btn_capture);
        btnCapture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                btnCapture.setEnabled(false);
                //camera.takePicture(cameraShutterCallback, cameraPictureCallbackRaw, cameraPictureCallbackJpeg);
                camera.takePicture(null, null, cameraPictureCallbackJpeg);
            }
        });

        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Getting data from ScanQR
        String cam_id = getIntent().getStringExtra("calling_page");
        //System.out.println("AAM "+cam_id);
        camera_id = Integer.parseInt(cam_id);

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

    Camera.PictureCallback cameraPictureCallbackRaw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            // TODO Auto-generated method stub

        }
    };

    Camera.PictureCallback cameraPictureCallbackJpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

            Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            int   wid = cameraBitmap.getWidth();
            int  hgt = cameraBitmap.getHeight();

            int left = 90;
            int right = 990;
            int top = 300;
            int bottom = 1200;

/*
            int xmin = (int) ((left/1080.0) * wid);
            int ymin = (int)((top/1920.0) * hgt);

            int xmax = (int)((right/1080.0) * wid);
            int ymax = (int)((bottom/1920.0) * hgt);

            int width = xmax - xmin;
            int height = ymax - ymin;

            System.out.println("## "+xmin+" "+ymin+" "+width+" "+height + " " + wid+ " " + hgt);

            croppedImage = Bitmap.createBitmap(cameraBitmap, xmin, ymin, width, height);
*/

            /*------------------------- When you use ML Model ------------------------------*/
            croppedImage = Bitmap.createBitmap(cameraBitmap, left, top, right-left, bottom-top);

            System.out.println("#Width11 "+croppedImage.getWidth()+" Height "+croppedImage.getHeight());

            /*------------------------------------------------------------------------------*/

            /*------------------ When you use Static Measurement Concept -------------------*/
            staticMeasurement(cameraBitmap);
            /*------------------------------------------------------------------------------*/




        }
    };


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    /*
        try
        {

            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            // parameters.setRotation(90);



        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    */

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub

        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();

        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Camera.Size mSize = null;

        for (Camera.Size size : sizes){
            mSize = size;
            //System.out.println("Size == "+mSize.width+", "+mSize.height);
        }

        mSize = sizes.get(0);

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            parameters.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
            parameters.setRotation(90);

        }else {
            parameters.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
            parameters.setRotation(0);
        }

        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for (int i=0; i<previewSizes.size();i++){
            System.out.println("#5 Preview_Sizes : "+previewSizes.get(i).width+"=="+previewSizes.get(i).height);
        }

        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        for (int i=0; i<pictureSizes.size();i++){
            System.out.println("#5 Picture_Sizes : "+pictureSizes.get(i).width+"=="+pictureSizes.get(i).height);
        }

        // Here we use the 1920x1080 size because it is same Preview_Size and Picture_Size
        //parameters.setPictureSize(mSize.width, mSize.height);
//        parameters.setPictureSize(1920, 1080);
//        parameters.setPreviewSize(1920, 1080);

        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }
        catch(Exception e)
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
    }


    private void staticMeasurement(Bitmap camera_bitmap){
//        int left = 400;
//        int right = 680;
//        int top = 390; //400
//        int bottom = 1070; //1100

        int w = camera_bitmap.getWidth();
        int h = camera_bitmap.getHeight();

        System.out.println("#5 mmm "+w+"----"+h);

        if (w > h){
            camera_bitmap = rotateBitmap(camera_bitmap, 90);
            w = camera_bitmap.getWidth();
            h = camera_bitmap.getHeight();
        }

        int left = 0;
        int right = w;
        int top = (h/3);
        int bottom = (h/2);

        croppedImage = Bitmap.createBitmap(camera_bitmap, left, top, right-left, bottom-top);

        xmin = left;
        ymin = top;
        xmax = right;
        ymax = bottom;

        ImageRegistration.imagePreview.setImageBitmap(croppedImage);
        ImageRegistration.imagePreview.setVisibility(View.VISIBLE);

        onBackPressed();
    }


    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
