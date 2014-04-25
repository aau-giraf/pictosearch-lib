package dk.aau.cs.giraf.pictosearch;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import dk.aau.cs.giraf.pictogram.Pictogram;

/**
 * This class is used to loading the bitmaps into memory and displaying them in the pictogramGrid when 
 * they are to be posted. This is happending off the UI Thread via AsyncTask.
 * @author Anders Vinther, SW605f13 Parrot-group
 */
public class BitmapWorker extends AsyncTask<Object, Void, Bitmap> {
	// En weak reference g�r den "flagged" som "garbage collectable" :)
	// A weak reference flags the imageview as garbage collectable
	private final WeakReference<ImageView> imageview;

	private Pictogram pictogram;
	private Context context;
	
	public BitmapWorker(ImageView img) {
		imageview = new WeakReference<ImageView>(img);
	}

	@Override
	protected Bitmap doInBackground(Object... params) {
		pictogram = (Pictogram) params[0];
		Bitmap bmp = null;

		if(pictogram.getPictogramID() == -1) {
			bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.usynlig);
		}
		else {
			bmp = pictogram.getImageData();
		}
		
		return bmp;
	}
	
	protected void onPostExecute(Bitmap result) {
		if(result != null && imageview != null) {
			final ImageView imgview = imageview.get();
			
			if(imgview != null) {
				imgview.setImageBitmap(result);
			}
		}
	}
}
