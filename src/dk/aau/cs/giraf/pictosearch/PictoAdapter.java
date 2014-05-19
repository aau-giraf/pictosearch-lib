package dk.aau.cs.giraf.pictosearch;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

//import dk.aau.cs.giraf.categorylib.PARROTCategory;
//import dk.aau.cs.giraf.pictogram.Pictogram;

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
        View view;
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pictogramview, null);
        }
        else {
            view = convertView;
        }

        Object o = pictograms.get(position);
        String TextLabel = "???";

        Pictogram pctNew = null;
        Category catNew = null;
        if (o instanceof Pictogram)
        {
            pctNew = (Pictogram)pictograms.get(position);
            if (pctNew != null) TextLabel = pctNew.getName();
        }
        else if (o instanceof Category)
        {
            catNew = (Category)pictograms.get(position);
            if (catNew != null) TextLabel = catNew.getName();
        }

        ImageView pictoImage = (ImageView) view.findViewById(R.id.pictogrambitmap);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        pictoImage.setLayoutParams(layoutParams);

        TextView pictoName = (TextView) view.findViewById(R.id.pictogramtext);
        pictoName.setText(TextLabel);

        if (pctNew != null)
        {

            try
            {
                Bitmap b = pctNew.getImage();
                if (b != null)
                {
                    //BitmapWorker worker = new BitmapWorker(imageView);
                    //worker.execute(pctNew);
                    pictoImage.setImageBitmap(b);
                }
            }
            catch (java.lang.NullPointerException e)
            {
                System.out.println("Exception: " + e + ", fix nu!!!!!!");
            }

        }
        else if(catNew != null)
        {

            try
            {
                Bitmap b = catNew.getImage();
                if (b != null)
                {
                    //BitmapWorker worker = new BitmapWorker(imageView);
                    //worker.execute(pctNew);
                    pictoImage.setImageBitmap(b);
                }
            }
            catch (java.lang.NullPointerException e)
            {
                System.out.println("Exception: " + e + ", fix nu!!!!!!");
            }

        }

        view.setPadding(5, 5, 5, 5);


        if(displayText)
        {
            view.setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.GONE);
        }

		return view;
	}
}
