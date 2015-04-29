package dk.aau.cs.giraf.pictosearch;


import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import android.content.Context;

/**
 * This class is used for deleting pictograms from the local database while using PictoSearch.
 */
public class DeleteClass {

    private final PictoAdminMain Outer;

    DeleteClass(PictoAdminMain outer){ Outer = outer;}

    /**
     * Delete selected pictograms from the local database.
     * @param context provides access to the databases.
     * @param pictogram is the pictogram that is to be deleted.
     */
    public void PictoDelete(Context context, Pictogram pictogram){
		int pictogramId = pictogram.getId();
		
		// Remove pictogram from checkout list
		for (int i = 0; i < Outer.checkoutList.size(); i++)
		{
			Object checkoutItem = Outer.checkoutList.get(i);
			if (!(checkoutItem instanceof Pictogram)) continue;
			Pictogram p = (Pictogram)checkoutItem;
			if (p.getId() == pictogramId)
			{
				Outer.checkoutList.remove(i);
				Outer.onUpdatedCheckoutCount();
				Outer.checkoutGrid.setAdapter(new PictoAdapter(Outer.checkoutList, Outer.getApplicationContext()));
				i--;
			}
		}

        //Delete pictogram
        PictogramController pictogramController = new PictogramController(context);
        //pictogramController.removePictogramById(pictogramId);
    }

    /**
     * Delete selected categories from the local database.
     * @param context provides access to the databases.
     * @param category is the category that is to be deleted.
     */
    public void CategoryDelete(Context context, Category category){
		int categoryId = category.getId();
		
		// Remove category from checkout list
		for (int i = 0; i < Outer.checkoutList.size(); i++)
		{
			Object checkoutItem = Outer.checkoutList.get(i);
			if (!(checkoutItem instanceof Category)) continue;
			Category c = (Category)checkoutItem;
			if (c.getId() == categoryId)
			{
				Outer.checkoutList.remove(i);
				Outer.onUpdatedCheckoutCount();
				Outer.checkoutGrid.setAdapter(new PictoAdapter(Outer.checkoutList, Outer.getApplicationContext()));
				i--;
			}
		}
        //Delete category
        CategoryController categoryController = new CategoryController(context);
        categoryController.removeCategory(category);
    }
}
