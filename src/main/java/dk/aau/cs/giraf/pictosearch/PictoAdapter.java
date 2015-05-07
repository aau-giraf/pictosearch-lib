package dk.aau.cs.giraf.pictosearch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;

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
            Pictogram pictogramTemp = (Pictogram) objectList.get(position);
            return pictogramTemp.getId();
        }
        else {
            Category categoryTemp = (Category) objectList.get(position);
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

        final Object object = objectList.get(position);
        Drawable catIndicator = context.getResources().getDrawable( R.drawable.icon_category );

        if (convertView == null) {
            GirafPictogramItemView pictogramItemView;
            if (object instanceof Pictogram) {

                Pictogram pictogramNew = (Pictogram) objectList.get(position);
                pictogramItemView = new GirafPictogramItemView(context, pictogramNew, pictogramNew.getName());
            } else {
                Category categoryNew = (Category) objectList.get(position);
                pictogramItemView = new GirafPictogramItemView(context, categoryNew, categoryNew.getName());
                pictogramItemView.setIndicatorOverlayDrawable(catIndicator);
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
                pictogramItemView.setIndicatorOverlayDrawable(catIndicator);

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


