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

/**
 * An Adapter object acts as a bridge between an AdapterView and the underlying data for that view.
 * This class is used to import the pictograms into a gridView
 */
public class PictoAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Object> pictograms;
	private boolean displayText = true;

    /**
     * Assigns pictograms to class instance.
     * @param pictograms ArrayList of pictograms
     * @param context provides access to the databases.
     */
	public PictoAdapter(ArrayList<Object> pictograms, Context context) {
		super();
		this.pictograms = pictograms;
		this.context = context;
	}

    /**
     * Assigns pictograms to class instance.
     * @param pictograms ArrayList of pictograms
     * @param display boolean, set view visibility.
     * @param context provides access to the databases.
     */
	public PictoAdapter(ArrayList<Object> pictograms, boolean display, Context context) {
		super();
		this.pictograms = pictograms;
		this.displayText = display;
		this.context = context;
	}

    /**
     * Return the number of pictograms
     * @return number of pictograms
     */
	@Override
	public int getCount() {
		return pictograms.size();
	}

    /**
     * Overridden method is superclass.
     * Get the data item associated with the specified position in the data set.
     * @param position in the data set
     * @return null
     */
	@Override
	public Object getItem(int position) {
		return null;
	}

    /**
     * Overridden method is superclass.
     * Get the row id associated with the specified position in the list.
     * @param position in the list
     * @return 0
     */
	@Override
	public long getItemId(int position) {
		return 0;
	}
	

    /**
     * Get a View that displays the data at the specified position in the data set.
     * Create an imageView for each pictogram in the list.
     * @param position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible
     * @param parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    // Todo: handle NullPointerException
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

        Object object = pictograms.get(position);
        String TextLabel = context.getString(R.string.pictoCreator);

        Pictogram pictogramNew = null;
        Category categoryNew = null;
        if (object instanceof Pictogram)
        {
            pictogramNew = (Pictogram)pictograms.get(position);
            if (pictogramNew != null) TextLabel = pictogramNew.getName();
        }
        else if (object instanceof Category)
        {
            categoryNew = (Category)pictograms.get(position);
            if (categoryNew != null) TextLabel = categoryNew.getName();
        }

        ImageView pictoImage = (ImageView) view.findViewById(R.id.pictogrambitmap);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        pictoImage.setLayoutParams(layoutParams);

        TextView pictoName = (TextView) view.findViewById(R.id.pictogramtext);
        pictoName.setText(TextLabel);

        if (pictogramNew != null) {
            try
            {
                Bitmap bitmap = pictogramNew.getImage();
                if (bitmap != null)
                {
                    pictoImage.setImageBitmap(bitmap);
                }
            }
            catch (java.lang.NullPointerException e)
            {
                System.out.println(context.getString(R.string.exception) + e);
            }
        }
        else if(categoryNew != null) {
            try
            {
                Bitmap b = categoryNew.getImage();
                if (b != null)
                {
                    pictoImage.setImageBitmap(b);
                }
            }
            catch (java.lang.NullPointerException e)
            {
                System.out.println(context.getString(R.string.exception) + e);
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
