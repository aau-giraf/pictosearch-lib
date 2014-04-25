package dk.aau.cs.giraf.pictosearch;


import java.io.ObjectOutput;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//import dk.aau.cs.giraf.categorylib.PARROTCategory;
//import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Used to import the pictograms into a gridview.
 * @author SW605f13 Parrot-group
 */
public class PictoAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Object> pictograms;
	private boolean displayText = true;
	
	public PictoAdapter(ArrayList<Object> p, Context c) {
		super();
		this.pictograms = p;
		context = c;
	}
	
	public PictoAdapter(ArrayList<Object> p, boolean display, Context c) {
		super();
		this.pictograms = p;
		this.displayText = display;
		context = c;
	}

	@Override
	public int getCount() {
		return pictograms.size(); // Return the number of pictograms
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	// Create an imageview for each pictogram in the list.
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
        Object o = pictograms.get(position);
        String TextLabel = "???";

        Pictogram pctNew = null;
        if (o instanceof Pictogram)
        {
            pctNew = (Pictogram)pictograms.get(position);
            if (pctNew != null) TextLabel = pctNew.getName();
        }
        /*else if (o instanceof PARROTCategory)
        {
            PARROTCategory catNew = (PARROTCategory)pictograms.get(position);
        }*/

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);

		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = layoutInflater.inflate(R.layout.pictogramview, null);

		ImageView imageView = (ImageView) convertView.findViewById(R.id.pictogrambitmap); 
		imageView.setLayoutParams(layoutParams);
		
		if(displayText)
        {
			TextView textView = (TextView) convertView.findViewById(R.id.pictogramtext);
			textView.setText(TextLabel);
		}

        if (pctNew != null)
        {
            /*
            try
            {
                Bitmap b = pctNew.getImage();
                if (b != null)
                {
		            //BitmapWorker worker = new BitmapWorker(imageView);
		            //worker.execute(pctNew);
                    imageView.setImageBitmap(b);
                }
            }
            catch (java.lang.NullPointerException e)
            {
                System.out.println("Exception: " + e + ", fix nu!!!!!!");
            }
            */
        }


		convertView.setPadding(5, 5, 5, 5);

		return convertView;
	}
}
