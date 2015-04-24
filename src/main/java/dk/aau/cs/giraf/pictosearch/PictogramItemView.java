package dk.aau.cs.giraf.pictosearch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.BasicImageModel;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Created on 14/04/2015.
 */
public class PictogramItemView extends LinearLayout implements Checkable {

    // The inflated view (See constructors)
    private View inflatedView;

    private LinearLayout pictogramIconContainer;
    private ImageView iconImageView;
    private TextView titleContainer;

    private AsyncTask<Void, Void, Bitmap> loadPictogramImage;
    private Runnable updateSizeAndSetVisible;

    /**
     * Used to implement edit triangle
     */
    private boolean isEditable = false;
    private Paint editableIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Point point1_draw = new Point();
    private Point point2_draw = new Point();
    private Point point3_draw = new Point();
    private Path path = new Path();

    // For the global top left position of @param view
    final int[] viewLocation = new int[2];

    // For the global top left position of this GirafPictogramItemView
    final int[] thisLocation = new int[2];

    // For the conversion to relative bottom right position
    final int[] bottomRightLocation = new int[2];

    /**
     * Do not use this constructor in code. It should only be used to inflate it from xml!
     */
    public PictogramItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            initialize(null, null, attrs);
            return;
        }

        Pictogram sample = new Pictogram();
        sample.setName("Sample imageModel");
        sample.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_copy));
        initialize(sample, sample.getName(), attrs);
    }

    //<editor-fold desc="constructors">
    public PictogramItemView(final Context context, final BasicImageModel imageModel) {
        super(context);
        initialize(imageModel, null, null);
    }

    public PictogramItemView(final Context context, final BasicImageModel imageModel, final String title) {
        super(context);
        initialize(imageModel, title, null);
    }
    //</editor-fold>

    /**
     * Initialized the different components
     */
    private void initialize(final BasicImageModel imageModel, final String title, final AttributeSet attrs) {

        // Disable layout optimization in order to enable this views onDraw method to be called by its parent
        // NOTICE: This is require to draw the edit-triangle
        setWillNotDraw(false);

        // Find the XML for the imageModel and load it into the view
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflatedView = inflater.inflate(R.layout.pictogram, this);

        pictogramIconContainer = (LinearLayout) inflatedView.findViewById(R.id.pictogram_icon_container_local);
        iconImageView = (ImageView) pictogramIconContainer.findViewById(R.id.pictogram_icon_local);

        // Hide the layout until it is loaded correctly
        inflatedView.setVisibility(INVISIBLE);

        // Runnable that will be used to update the size of the box (width = height)
        updateSizeAndSetVisible = new Runnable() {
            @Override
            public void run() {

                // Generate new layout params
                LinearLayout.LayoutParams newParams = (LinearLayout.LayoutParams) pictogramIconContainer.getLayoutParams();
                newParams.height = pictogramIconContainer.getMeasuredWidth();

                // Update the container with new params
                pictogramIconContainer.setLayoutParams(newParams);
                //container.postInvalidate();

                // Now that the height is correct, update the visibility of the component
                inflatedView.setVisibility(VISIBLE);
            }
        };

        // Set the imageModel (image) for the view (Will be done as an ASyncTask)
        setImageModel(imageModel);

        // Set the name of pictogram
        titleContainer = (TextView) inflatedView.findViewById(R.id.pictogram_title_local);
        setTitle(title);

    }

    /**
     * Reset the view (Checked state and imageModel image)
     */
    public synchronized void resetPictogramView() {

        // Hide the layout until it is loaded correctly
        inflatedView.setVisibility(INVISIBLE);

        iconImageView.setImageBitmap(null);
        setChecked(false);
    }

    /**
     * Will update the view with the provided imageModel
     *
     * @param imageModel the imageModel to update based upon
     */
    public synchronized void setImageModel(final BasicImageModel imageModel) {

        // If provided with null, do not update!
        if (imageModel == null) {
            return;
        }

        // Cancel any currently loading imageModel tasks (This will ensure that we do not try and load two different pictograms)
        if (loadPictogramImage != null) {
            loadPictogramImage.cancel(true);
        }

        // This class will be used to load the imageModel (image) from the database and "insert" it into the view
        loadPictogramImage = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {

                /**
                 * We create a temporary reference to a imageModel because the current model objects saves a permanent reference to
                 * their bitmaps once they are loaded once.
                 * This creates memory overflows because we keep a list of all imageModel objects in memory
                 */
                final BasicImageModel b = (BasicImageModel) clone(imageModel);

                // Check if the temp could not be found (This means that no bitmap could be found)
                if (b == null) {
                    return null;
                }

                // Find the imageModel to show
                // Notice that we create a copy to avoid memory leak (See implementation of getImage on imageModel)
                return b.getImage();
            }

            @Override
            protected void onPostExecute(final Bitmap pictogramImage) {
                iconImageView.setImageBitmap(pictogramImage);

                // Register the runnable and invalidate (so that it will be updated)
                inflatedView.post(updateSizeAndSetVisible);
            }

            // This method will be used to clone the the BasicImageModel to avoid memory leak. See doInBackground
            public Object clone(Object o) {
                Object clone = null;

                try {
                    clone = o.getClass().newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                // Walk up the superclass hierarchy
                for (Class obj = o.getClass();
                     !obj.equals(Object.class);
                     obj = obj.getSuperclass()) {
                    Field[] fields = obj.getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setAccessible(true);
                        try {
                            // for each class/superclass, copy all fields
                            // from this object to the clone
                            fields[i].set(clone, fields[i].get(o));
                        } catch (IllegalArgumentException e) {
                        } catch (IllegalAccessException e) {
                        }
                    }
                }
                return clone;
            }
        };

        // Start loading the image of the imageModel
        loadPictogramImage.execute();
    }

    /**
     * Will update the view with the provided title
     *
     * @param title the title to set
     */
    public synchronized void setTitle(final String title) {
        if (title == null) {
            hideTitle();
        } else {
            showTitle();
        }

        titleContainer.setText(title);
    }

    /**
     * Will hide the title of the imageModel
     */
    public void hideTitle() {
        titleContainer.setVisibility(GONE);
    }

    /**
     * Will show the title of the imageModel
     */
    public void showTitle() {
        titleContainer.setVisibility(VISIBLE);
    }




    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        /*
         * Only draw editable-triangle if the view is set to be editable in either xml
         * using the attribute "editable" or using the method setEditable
         */
        if (isEditable) {

            // Get the relative right and bottom coordinate of iconImageView from this GirafPictogramItemView
            final int[] relativeRightAndBottom = getRelativeRightAndBottom(iconImageView);

            // Use the relativeRightAndBottom as xEnd and yEnd
            final int xEnd = relativeRightAndBottom[0];
            final int yEnd = relativeRightAndBottom[1];

            // Calculate xStart and yStart from end points minus 1/4 of the ImageView width and height
            final int xStart = xEnd - (int) Math.ceil(iconImageView.getMeasuredWidth() / 4.0d);
            final int yStart = yEnd - (int) Math.ceil(iconImageView.getMeasuredHeight() / 4.0d);

            // Set 3 points in a triangle
            point1_draw.set(xEnd, yEnd);
            point2_draw.set(xEnd, yStart);
            point3_draw.set(xStart, yEnd);

            // Configure triangle path
            path.reset();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(point1_draw.x, point1_draw.y);
            path.lineTo(point2_draw.x, point2_draw.y);
            path.lineTo(point3_draw.x, point3_draw.y);
            path.lineTo(point1_draw.x, point1_draw.y);
            path.close();

            // Draw triangle
            canvas.drawPath(path, editableIndicatorPaint);
        }
    }

    /**
     * Gets the relative bottom right position of @param view relative to this GirafPictogramItemView
     *
     * @param view
     * @return the relative bottom right position of @param view
     */
    public int[] getRelativeRightAndBottom(final View view) {

        // Get the global top left position of @param view
        view.getLocationInWindow(viewLocation);

        // Get the global top left position of this GirafPictogramItemView
        this.getLocationInWindow(thisLocation);

        // convert to relative bottom right position and return
        bottomRightLocation[0] = viewLocation[0] - thisLocation[0] + view.getMeasuredWidth();
        bottomRightLocation[1] = viewLocation[1] - thisLocation[1] + view.getMeasuredHeight();
        return bottomRightLocation;
    }

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }
}
