package dk.aau.cs.giraf.pictosearch;


import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import android.content.Context;

/**
 * Created by Minh Hieu Nigu on 26-03-14.
 */
public class DeleteClass {

    private PictoAdminMain Outer;

    DeleteClass(PictoAdminMain ou){ Outer = ou;}

    public void PictoDelete(Context context, Pictogram pictogram){
		int pictogramId = pictogram.getId();
		
		// Remove from checkout list
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
		
        PictogramController pictogramController = new PictogramController(context);
        pictogramController.removePictogramById(pictogramId);
    }


    public void CategoryDelete(Context context, Category category){
		int categoryId = category.getId();
		
		// Remove from checkout list
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
		
        CategoryController categoryController = new CategoryController(context);
        categoryController.removeCategory(category);
    }


}