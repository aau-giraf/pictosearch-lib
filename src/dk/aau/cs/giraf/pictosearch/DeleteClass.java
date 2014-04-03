package dk.aau.cs.giraf.pictosearch;


import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import java.util.ArrayList;
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

    public void CategoryDelete(Context context, ArrayList<Category> AllCategories, Category category){

        //databaseHelper = new Helper(context);
        //CategoryController categoryHelper = databaseHelper.categoryHelper;
        //categoryHelper.removeCategory(category);

        CategoryController categoryController = new CategoryController(context);
        categoryController.removeCategory(category);

        AllCategories.remove(AllCategories.indexOf(category));
    }

}
