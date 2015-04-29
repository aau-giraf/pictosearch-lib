package dk.aau.cs.giraf.pictosearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.gui.GirafPictogramItemView;



/**
 * An Adapter object acts as a bridge between an AdapterView and the underlying data for that view.
 * This class is used to import the pictograms into a gridView
 */
public class PictoAdapter extends BaseAdapter {
    private final Context context;
    private List<Object> objectList;
    private boolean displayText = true;

    Pictogram pictogramTemp;
    Category categoryTemp;




    /**
     * Assigns pictograms to class instance.
     *
     * @param objectList ArrayList of pictograms
     * @param context    provides access to the databases.
     */
    public PictoAdapter(List<Object> objectList, final Context context) {
        super();

        if (objectList == null) {
            this.objectList = new ArrayList<Object>();
        }
        else {
            this.objectList = objectList;
        }

        this.context = context;
    }

    /**
     * Assigns pictograms to class instance.
     *
     * @param objectList ArrayList of pictograms
     * @param display    boolean, set view visibility.
     * @param context    provides access to the databases.
     */
    public PictoAdapter(final List<Object> objectList, final boolean display, final Context context) {
        super();

        if (objectList == null) {
            this.objectList = new ArrayList<Object>();
        }
        else {
            this.objectList = objectList;
        }
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
        return objectList.size();
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
        return objectList.get(position);
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

        if (objectList.get(position) instanceof Pictogram){
            pictogramTemp = (Pictogram) objectList.get(position);
            return pictogramTemp.getId();
        }
        else {
            categoryTemp = (Category) objectList.get(position);
            return categoryTemp.getId();
        }

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
        //View view;
        final Object object = objectList.get(position);

        ImageView catIndicatorView = new ImageView(context);
        catIndicatorView.setImageResource(R.drawable.icon_category);
        catIndicatorView.setVisibility(View.INVISIBLE);

        //RelativeLayout r1 = (RelativeLayout) convertView.findViewById(R.id.pictogram_icon_container);
        //r1.addView(catIndicatorView);

        //LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //view = layoutInflater.inflate(R.layout.pictogram, null);
        //ImageView catIndiImageView = (ImageView) view.findViewById(R.id.category_indicator);



        if (convertView == null) {
            GirafPictogramItemView pictogramItemView;
            if (object instanceof Pictogram) {

                Pictogram pictogramNew = (Pictogram) objectList.get(position);
                pictogramItemView = new GirafPictogramItemView(context, pictogramNew, pictogramNew.getName());

            } else {
                Category categoryNew = (Category) objectList.get(position);
                catIndicatorView.setVisibility(View.VISIBLE);
                pictogramItemView = new GirafPictogramItemView(context, categoryNew, categoryNew.getName());
            }

            pictogramItemView.setLayoutParams(new AbsListView.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT));
            return pictogramItemView;

        } else {
            GirafPictogramItemView pictogramItemView = (GirafPictogramItemView) convertView;
            pictogramItemView.resetPictogramView();

            if (object instanceof Pictogram) {
                Pictogram pictogramNew = (Pictogram) objectList.get(position);
                pictogramItemView.setImageModel(pictogramNew);
                pictogramItemView.setTitle(pictogramNew.getName());
            } else {
                Category categoryNew = (Category) objectList.get(position);
                pictogramItemView.setImageModel(categoryNew);
                pictogramItemView.setTitle(categoryNew.getName());
                catIndicatorView.setVisibility(View.VISIBLE);
            }
            return pictogramItemView;

        }
    }


    public void swap(List<Object> objectList) {
        this.objectList = objectList;

        // Flag the current data as invalid. After this the view will be re-rendered
        this.notifyDataSetInvalidated();
    }
}


        /*View view;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pictogram, null);
        } else {
            view = convertView;
        }

        final ImageView pictoImageView = (ImageView) view.findViewById(R.id.pictogram_icon);
        final ImageView catIndiImageView = (ImageView) view.findViewById(R.id.category_indicator);

        final Object object = objectList.get(position);
        String textLabel = context.getString(R.string.pictoCreator);

        Pictogram pictogramNew = null;
        Category categoryNew = null;
        if (object instanceof Pictogram) {
            pictogramNew = (Pictogram) objectList.get(position);
            if (pictogramNew != null) textLabel = pictogramNew.getName();
        } else if (object instanceof Category) {
            categoryNew = (Category) objectList.get(position);
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

        return view;*/


