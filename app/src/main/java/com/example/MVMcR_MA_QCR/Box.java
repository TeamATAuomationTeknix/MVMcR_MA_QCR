package com.example.MVMcR_MA_QCR;
// hii
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.View;

public class Box extends View {
    private Paint paint = new Paint();
    Box(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) { // Override the onDraw() Method
        super.onDraw(canvas);

        canvas.drawColor(Color.parseColor("#99000000"));

        Paint innerPaint = new Paint();
        innerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Paint outerPaint = new Paint();
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setColor(Color.WHITE);
        outerPaint.setStrokeWidth(7);

/*
        //center
        int x0 = canvas.getWidth()/2;
        int y0 = canvas.getHeight()/2;
        int dx = canvas.getHeight()/3;
        int dy = canvas.getHeight()/3;
        //draw guide box
        //canvas.drawRect(x0-dx, y0-dy, x0+dx, y0+dy, paint);
*/
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int s = w - (w/10);

        int left = (w - s)/2;
        int right = left + s;
        int top = (h - s)/2;
        int bottom = top + s;

        // Toast.makeText(getContext(),"Hello Javatpoint==="+w+"-"+h+" x= "+l+" y= "+t,Toast.LENGTH_LONG).show();

        //draw any shape, here I am drawing Rect shape
        Rect rect=new Rect(left, top, right, bottom);
        canvas.drawRect(rect,innerPaint);

        canvas.drawPath(createCornersPath(left, top, right, bottom, 100), outerPaint);

    }

    private Path createCornersPath(int left, int top, int right, int bottom, int cornerWidth){
        Path path = new Path();

        path.moveTo(left, top + cornerWidth);
        path.lineTo(left, top);
        path.lineTo(left + cornerWidth, top);

        path.moveTo(right - cornerWidth, top);
        path.lineTo(right, top);
        path.lineTo(right , top + cornerWidth);

        path.moveTo(left, bottom - cornerWidth);
        path.lineTo(left, bottom);
        path.lineTo(left + cornerWidth, bottom);

        path.moveTo(right - cornerWidth, bottom);
        path.lineTo(right, bottom);
        path.lineTo(right, bottom - cornerWidth);


        return path;
    }

}


// In Camera Preview
/*
Box box = new Box(this);
setContentView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
*/