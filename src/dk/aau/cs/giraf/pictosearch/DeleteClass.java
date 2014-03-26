package dk.aau.cs.giraf.pictosearch;

import dk.aau.cs.giraf.categorylib.CategoryHelper;
import dk.aau.cs.giraf.categorylib.PARROTCategory;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.MediaHelper;
import dk.aau.cs.giraf.oasis.lib.models.Media;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.categorylib.CategoryHelper;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Context;

/**
 * Created by Minh Hieu Nguyen on 26-03-14.
 */
public class DeleteClass {

    private static Helper databaseHelper;
    private PictoAdminMain Outer;

    DeleteClass(PictoAdminMain ou){ Outer = ou;}

    public void PictoDbDelete(Context context, ArrayList<Pictogram> AllPictograms, Pictogram pictogram){

        databaseHelper = new Helper(context);
        MediaHelper mediaHelper = databaseHelper.mediaHelper;
        Media media = mediaHelper.getMediaById(pictogram.getPictogramID());
        mediaHelper.removeMedia(media);

        AllPictograms.remove(AllPictograms.indexOf(pictogram));
    }

    public void CategoryDbDelete(){

    }

}
