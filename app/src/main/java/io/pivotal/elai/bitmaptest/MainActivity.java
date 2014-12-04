package io.pivotal.elai.bitmaptest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cropButton = (Button) findViewById(R.id.croppedButton);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropImage();
            }
        });

        Button setButton = (Button) findViewById(R.id.setButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCropFrame();
            }
        });
    }

    private void setCropFrame() {
        // Crop area start corner
        int width = 600;
        int height = 400;

        int rotation = getRotation();

        float initX = getOriginX();
        float initY = getOriginY();

        float[] cropArea = new float[8];
        cropArea[0] = initX;
        cropArea[1] = initY;

        cropArea[2] = cropArea[0]+ width ;
        cropArea[3] = cropArea[1];

        cropArea[4] = cropArea[0];
        cropArea[5] = cropArea[1] + height;

        cropArea[6] = cropArea[0] + width;
        cropArea[7] = cropArea[1] + height;

        Matrix m = new Matrix();
        m.postRotate(rotation);
        m.mapPoints(cropArea);

        PointF topLeft = new PointF(cropArea[0], cropArea[1]);
        PointF topRight = new PointF(cropArea[2], cropArea[3]);
        PointF bottomLeft = new PointF(cropArea[4], cropArea[5]);
        PointF bottomRight = new PointF(cropArea[6], cropArea[7]);

        int angle = (int) getAngle(topLeft, topRight);
        setCropFrame(topLeft, topRight, bottomRight, bottomLeft);
    }

    private void setCropFrame(PointF topLeft, PointF topRight, PointF bottomRight, PointF bottomLeft) {
        ImageView originalImageView = (ImageView) findViewById(R.id.originalImageView);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.random, options); //((BitmapDrawable) originalImageView.getDrawable()).getBitmap();

        float[] cropArea = new float[8];
        cropArea[0] = topLeft.x;
        cropArea[1] = topLeft.y;

        cropArea[2] = topRight.x;
        cropArea[3] = topRight.y;

        cropArea[4] = bottomLeft.x;
        cropArea[5] = bottomLeft.y;

        cropArea[6] = bottomRight.x;
        cropArea[7] = bottomRight.y;

        float[] lineVertices = new float[] {
                cropArea[0], cropArea[1], cropArea[2], cropArea[3],
                cropArea[0], cropArea[1], cropArea[4], cropArea[5],
                cropArea[4], cropArea[5], cropArea[6], cropArea[7],
                cropArea[6], cropArea[7], cropArea[2], cropArea[3]
        };

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        canvas.drawLines(lineVertices, paint);
        originalImageView.setImageBitmap(bitmap);
    }

    private void cropImage() {
        float initX = getOriginX();
        float initY = getOriginY();

        int rotation = getRotation();
        int width = 600;
        int height = 400;

        float[] cropArea = new float[8];
        cropArea[0] = initX;
        cropArea[1] = initY;

        cropArea[2] = cropArea[0]+ width ;
        cropArea[3] = cropArea[1];

        cropArea[4] = cropArea[0];
        cropArea[5] = cropArea[1] + height;

        cropArea[6] = cropArea[0] + width;
        cropArea[7] = cropArea[1] + height;

        Matrix m = new Matrix();
        m.postRotate(rotation);
        m.mapPoints(cropArea);

        PointF topLeft = new PointF(cropArea[0], cropArea[1]);
        PointF topRight = new PointF(cropArea[2], cropArea[3]);
        PointF bottomRight = new PointF(cropArea[6], cropArea[7]);
        PointF bottomLeft = new PointF(cropArea[4], cropArea[5]);
        cropImage(topLeft, topRight, bottomRight, bottomLeft);
    }

    private void cropImage(PointF topLeft, PointF topRight, PointF bottomRight, PointF bottomLeft) {
        int rotation = getRotation();

        int angle = (int) getAngle(topLeft, topRight);
        int width = 600;
        int height = 400;
        cropImage(topLeft, angle, width, height);
    }

    private void cropImage(PointF topLeft, int rotation, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.random);

        Matrix m = new Matrix();
        m.postRotate(rotation);

        Matrix inv_m = new Matrix();
        m.invert(inv_m);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), inv_m, true);

        float[] pts = new float[10];

        // Bitmap 4 corners
        pts[0] = 0;
        pts[1] = 0;

        pts[2] = 0;
        pts[3] = bitmap.getHeight();

        pts[4] = bitmap.getWidth();
        pts[5] = 0;

        pts[6] = bitmap.getWidth();
        pts[7] = bitmap.getHeight();

        pts[8] = topLeft.x;
        pts[9] = topLeft.y;
        inv_m.mapPoints(pts);

        float xOffset = 0;
        float yOffset = 0;
        for (int i = 0 ; i <8; i+=2) {
            if (pts[i] < xOffset * -1) {
                xOffset = pts[i] * -1;
            }
            if (pts[i+1] < yOffset * -1) {
                yOffset = pts[i+1] * -1;
            }
        }

        Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, (int) (pts[8] + xOffset),(int) (pts[9] + yOffset), width, height);

        setCropFrame();

        ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);
        croppedImageView.setImageBitmap(croppedBitmap);

    }

    private int getRotation() {
        EditText rotationField = (EditText) findViewById(R.id.rotation);
        return Integer.parseInt(rotationField.getText().toString());
    }

    private double getAngle(PointF axisPoint, PointF screenPoint)
    {
        double dx = screenPoint.x - axisPoint.x;
        // Minus to correct for coord re-mapping
        double dy = -(screenPoint.y - axisPoint.y);

        double inRads = Math.atan2(dy,dx);

        // We need to map to coord system when 0 degree is at 3 O'clock, 270 at 12 O'clock
        if (inRads < 0)
            inRads = Math.abs(inRads);
        else
            inRads = 2*Math.PI - inRads;

        return Math.toDegrees(inRads);
    }

    private float getOriginY() {
        EditText yCoordEditText = (EditText) findViewById(R.id.yCoord);
        return Float.parseFloat(yCoordEditText.getText().toString());
    }

    private float getOriginX() {
        EditText xCoordEditText = (EditText) findViewById(R.id.xCoord);
        return Float.parseFloat(xCoordEditText.getText().toString());
    }
}
