package dk.aau.cs.giraf.pictosearch;


import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import java.util.ArrayList;
import android.content.Context;

/**
 * Created by Minh Hieu Nguyen on 26-03-14.
 */
public class DeleteClass {

    private static Helper databaseHelper;
    private PictoAdminMain Outer;

    DeleteClass(PictoAdminMain ou){ Outer = ou;}

    public void PictoDelete(Context context, Pictogram pictogram){

        PictogramController pictogramController = new PictogramController(context);
        pictogramController.removePictogramById(pictogram.getId());

    }


    public void CategoryDelete(Context context, Category category){

        CategoryController categoryController = new CategoryController(context);
        categoryController.removeCategory(category);

    }


}
