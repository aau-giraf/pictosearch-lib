package dk.aau.cs.giraf.pictosearch;

import dk.aau.cs.giraf.categorylib.CategoryHelper;
import dk.aau.cs.giraf.categorylib.PARROTCategory;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
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

    public void PictoDelete(Context context, ArrayList<Pictogram> AllPictograms, Pictogram pictogram){

        //databaseHelper = new Helper(context);
        //PictogramController pictogramHelper = databaseHelper.pictogramHelper;
        //pictogramHelper.removePictogramById(pictogram.getId());

        PictogramController pictogramController = new PictogramController(context);
        pictogramController.removePictogramById(pictogram.getPictogramID());

        AllPictograms.remove(AllPictograms.indexOf(pictogram));
    }

    public void CategoryDelete(Context context, ArrayList<PARROTCategory> AllCategories, PARROTCategory parrotCategory){

        //databaseHelper = new Helper(context);
        //CategoryController categoryHelper = databaseHelper.categoryHelper;
        //Category category = categoryHelper.getCategoryById(parrotCategory.getId());
        //categoryHelper.removeCategory(category);

        CategoryController categoryController = new CategoryController(context);
        Category category = categoryController.getCategoryById(parrotCategory.getId());
        categoryController.removeCategory(category);

        AllCategories.remove(AllCategories.indexOf(parrotCategory));
    }

}
