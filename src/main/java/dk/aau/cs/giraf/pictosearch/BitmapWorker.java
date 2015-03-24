package dk.aau.cs.giraf.pictosearch;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * BitmapWorker is used to loading the bitmaps into memory and displaying them in the pictogramGrid
 * when they are to be posted. This is happening off the UI Thread via AsyncTask.
 */
public class BitmapWorker extends AsyncTask<Object, Void, Bitmap> {
    /**
     * The WeakReference to the ImageView ensures that the AsyncTask does not prevent the ImageView
     * and anything it references from being garbage collected.
     */
	private final WeakReference<ImageView> imageViewReference;
    private Context context;
    /**
     // Use a WeakReference to ensure the ImageView can be garbage collected
     * @param imageView Displays an arbitrary image, such as a pictogram.
     */
    public BitmapWorker(ImageView imageView) {
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

    /**
     * Decode image in background.
     * @param params Objects
     * @return decoded image bitmap
     */
	@Override
	protected Bitmap doInBackground(Object... params) {
        Pictogram pictogram = (Pictogram) params[0];
		Bitmap bitmap;

		if(pictogram.getId() == -1) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.action_help);
		}
		else {
			bitmap = pictogram.getImageData();
		}
		
		return bitmap;
	}

    /**
     * Once complete, see if ImageView is still around and set bitmap.
     * @param bitmap decoded image bitmap
     */
    protected void onPostExecute(Bitmap bitmap) {
		if(bitmap != null && imageViewReference != null) {
			final ImageView imageView = imageViewReference.get();

			if(imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}
}
