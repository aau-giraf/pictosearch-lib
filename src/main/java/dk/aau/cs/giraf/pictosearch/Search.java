package dk.aau.cs.giraf.pictosearch;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.controllers.TagController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.Tag;

public class Search {
    private Context context;

    public Search(Context context){
        this.context = context;
    }


    /**
     * gets all pictograms with one of the input words from the database
     * @param pictogramNames string array with each search word.
     */
    public ArrayList<Pictogram> getAllPictograms(String[] pictogramNames) {
        ArrayList<Pictogram> pictoList = new ArrayList<Pictogram>();

        if (pictogramNames[0].isEmpty()) {
            return pictoList;
        }

        PictogramController pictogramController = new PictogramController(context);
        List<Pictogram> pictoTemp = new ArrayList<Pictogram>();

        for (String s : pictogramNames) {
            pictoTemp.addAll(pictogramController.getPictogramsByName(s));
        }

        for (Pictogram p : pictoTemp) {
            pictoList.add(p);
        }

        return pictoList;
    }

    public ArrayList<Category> getAllCategories(String[] categoryNames, int childID) {
        ArrayList<Category> catList = new ArrayList<Category>();

        if (childID < 0 || categoryNames[0].isEmpty()) {
            return catList;
        }

        CategoryController categoryController = new CategoryController(context);

        List<Category> catTemp = categoryController.getCategoriesByProfileId(childID);

        for (String s : categoryNames) {
            for (Category c : catTemp) {
                if (c.getName().toLowerCase().contains(s)) {
                    catList.add(c);
                }
            }
        }

        return catList;
    }

    public ArrayList<Tag> getAllTags(String[] tagCaptions) {
        ArrayList<Tag> tagList = new ArrayList<Tag>();

        if (tagCaptions[0].isEmpty()) {
            return tagList;
        }


        TagController tagController = new TagController(context);
        List<Tag> tagTemp = new ArrayList<Tag>();

        for (String s : tagCaptions) {
            tagTemp.addAll(tagController.getTagsByCaption(s));
        }

        for (Tag t : tagTemp) {
            tagList.add(t);
        }

        return tagList;
    }
}
