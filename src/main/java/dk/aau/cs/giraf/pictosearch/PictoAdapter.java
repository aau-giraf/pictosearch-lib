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
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * An Adapter object acts as a bridge between an AdapterView and the underlying data for that view.
 * This class is used to import the pictograms into a gridView
 */
public class PictoAdapter extends BaseAdapter {
    private final Context context;
    private final List<Object> pictograms;
    private boolean displayText = true;
    private ImageView categoryIndicator;

    /**
     * Assigns pictograms to class instance.
     *
     * @param pictograms ArrayList of pictograms
     * @param context    provides access to the databases.
     */
    public PictoAdapter(final List<Object> pictograms, final Context context) {
        super();
        this.pictograms = pictograms;
        this.context = context;
    }

    /**
     * Assigns pictograms to class instance.
     *
     * @param pictograms ArrayList of pictograms
     * @param display    boolean, set view visibility.
     * @param context    provides access to the databases.
     */
    public PictoAdapter(final List<Object> pictograms, final boolean display, final Context context) {
        super();
        this.pictograms = pictograms;
        this.displayText = display;
        this.context = context;
    }

    /**
     * Return the number of pictograms
     *
     * @return number of pictograms
     */
    @Override
    public int getCount() {
        return pictograms.size();
    }

    /**
     * Overridden method is superclass.
     * Get the data item associated with the specified position in the data set.
     *
     * @param position in the data set
     * @return null
     */
    @Override
    public Object getItem(final int position) {
        return pictograms.get(position);
    }

    /**
     * Overridden method is superclass.
     * Get the row id associated with the specified position in the list.
     *
     * @param position in the list
     * @return 0
     */
    @Override
    public long getItemId(final int position) {
        return 0;
    }


    /**
     * Get a View that displays the data at the specified position in the data set.
     * Create an imageView for each pictogram in the list.
     *
     * @param position    of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible
     * @param parent      that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    // Todo: handle NullPointerException
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pictogram, null);
        } else {
            view = convertView;
        }

        final ImageView pictoImageView = (ImageView) view.findViewById(R.id.pictogram_icon);
        final ImageView catIndiImageView = (ImageView) view.findViewById(R.id.category_indicator);

        final Object object = pictograms.get(position);
        String textLabel = context.getString(R.string.pictoCreator);

        Pictogram pictogramNew = null;
        Category categoryNew = null;
        if (object instanceof Pictogram) {
            pictogramNew = (Pictogram) pictograms.get(position);
            if (pictogramNew != null) textLabel = pictogramNew.getName();
        } else if (object instanceof Category) {
            categoryNew = (Category) pictograms.get(position);
            catIndiImageView.setVisibility(View.VISIBLE);
            if (categoryNew != null) textLabel = categoryNew.getName();
        }


        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        //pictoImageView.setLayoutParams(layoutParams);




        final TextView pictoNameTextView = (TextView) view.findViewById(R.id.pictogram_title);
        pictoNameTextView.setText(textLabel);

        if (pictogramNew != null) {
            Bitmap bitmap = pictogramNew.getImage();
            if (bitmap != null) {
                pictoImageView.setImageBitmap(bitmap);
            }
        } else if (categoryNew != null) {
            Bitmap b = categoryNew.getImage();
            if (b != null) {
                pictoImageView.setImageBitmap(b);
            }
        }

        view.setPadding(5, 5, 5, 5);

        if (displayText) {
            pictoNameTextView.setVisibility(View.VISIBLE);
        } else {
            pictoNameTextView.setVisibility(View.GONE);
        }

        return view;
    }
}
