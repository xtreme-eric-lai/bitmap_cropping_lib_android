package io.pivotal.elai.bitmaptest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
    }

    private void cropImage() {
        ImageView originalImageView = (ImageView) findViewById(R.id.originalImageView);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.random, options); //((BitmapDrawable) originalImageView.getDrawable()).getBitmap();

        EditText rotationField = (EditText) findViewById(R.id.rotation);
        int rotation = Integer.parseInt(rotationField.getText().toString());

        Matrix m = new Matrix();
        m.postRotate(rotation);

        int startX = bitmap.getWidth()/2 - 100;
        int startY = bitmap.getHeight()/2 - 100;
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

        float[] pts = new float[8];

        // Bitmap 4 corners
        pts[0] = 0;
        pts[1] = 0;

        pts[2] = 0;
        pts[3] = bitmap.getHeight();

        pts[4] = bitmap.getWidth();
        pts[5] = 0;

        pts[6] = bitmap.getWidth();
        pts[7] = bitmap.getHeight();

        // Crop area start corner
        int width = 600;
        int height = 400;

        EditText xCoordEditText = (EditText) findViewById(R.id.xCoord);
        EditText yCoordEditText = (EditText) findViewById(R.id.yCoord);

        float initX = Float.parseFloat(xCoordEditText.getText().toString());
        float initY = Float.parseFloat(yCoordEditText.getText().toString());

        float[] cropArea = new float[8];
        cropArea[0] = initX;
        cropArea[1] = initY;

        cropArea[2] = cropArea[0]+ width ;
        cropArea[3] = cropArea[1];

        cropArea[4] = cropArea[0];
        cropArea[5] = cropArea[1] + height;

        cropArea[6] = cropArea[0] + width;
        cropArea[7] = cropArea[1] + height;

        m.mapPoints(pts);


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

        Matrix inv_m = new Matrix();
        m.invert(inv_m);

        inv_m.mapPoints(cropArea);

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


        Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, (int) (initX + xOffset),(int) (initY + yOffset), 600, 400);

        Log.d("MainActivity", "bitmap w: " + bitmap.getWidth() + " h : " + bitmap.getHeight() + "; rotated w: " + rotatedBitmap.getWidth() + " h: " + rotatedBitmap.getHeight());
        for(int i = 0; i < pts.length; i++) {
            Log.d("MainActiity", "mapPoints : " + pts[i]);
        }
        for(int i = 0; i < cropArea.length; i++) {
            Log.d("MainActiity", "cropArea mapPoints : " + cropArea[i]);
        }
        ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);
//
//        Matrix inv_m = new Matrix();
//        m.invert(inv_m);
//
//        Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), inv_m, false) ;
        croppedImageView.setImageBitmap(croppedBitmap);


    }

}
