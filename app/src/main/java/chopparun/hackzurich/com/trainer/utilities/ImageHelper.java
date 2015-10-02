package chopparun.hackzurich.com.trainer.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

public class ImageHelper {

    public static void getRoundedCornerBitmap(Bitmap bitmap) {

        BitmapShader shader;

        shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        float radius = Math.min(bitmap.getWidth(),bitmap.getHeight());

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(0, 0, radius, paint);
    }
}