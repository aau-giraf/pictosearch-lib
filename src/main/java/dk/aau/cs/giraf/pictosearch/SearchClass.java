package dk.aau.cs.giraf.pictosearch;


import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.controllers.PictogramTagController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.PictogramTag;
import dk.aau.cs.giraf.oasis.lib.models.Tag;

/**
 * Main search class to search for pictograms, categories, and tags.
 */
public class SearchClass
{
    private PictoAdminMain Outer;

    SearchClass(PictoAdminMain ou)
    {
        Outer = ou;
    }

    public ArrayList<Object> DoSearch(String[] input, ArrayList<Object> allList){
        ArrayList<Object> result = new ArrayList<Object>();

        result.addAll(FindPictograms(input, allList));
        result.addAll(FindCategories(input, allList));
        result.addAll(FindTags(input, allList));

        return result;
    }

    private ArrayList<Object> FindPictograms(String[] input, ArrayList<Object> allList){
        ArrayList<Object> pictogramResult = new ArrayList<Object>();

        for (Object o : allList){
            if (o instanceof Pictogram){
                Pictogram p = (Pictogram)o;

                if (p.getName() != null){
                    for (String s : input){
                        if (p.getName().toLowerCase().contains(s)){
                            pictogramResult.add(p);
                        }
                    }
                }
            }
        }

        return pictogramResult;
    }

    private ArrayList<Object> FindCategories(String[] input, ArrayList<Object> allList){
        ArrayList<Object> categoryResult = new ArrayList<Object>();

        for (Object o : allList){
            if (o instanceof Category){
                Category c = (Category)o;

                if (c.getName() != null){
                    for (String s : input){
                        if (c.getName().toLowerCase().contains(s)){
                            categoryResult.add(c);
                        }
                    }
                }
            }
        }


        return categoryResult;
    }

    private ArrayList<Object> FindTags(String[] input, ArrayList<Object> allList){
        ArrayList<Object> tagResult = new ArrayList<Object>();
        ArrayList<Integer> tagIDs = new ArrayList<Integer>();

        for (Object o : allList){
            if (o instanceof Tag){
                Tag t = (Tag)o;

                if (t.getName() != null){
                    for (String s : input){
                        if (t.getName().toLowerCase().contains(s)){
                            tagIDs.add(t.getId());
                        }
                    }
                }
            }
        }

        PictogramTagController pictogramTagController = new PictogramTagController(Outer);
        PictogramController pictogramController = new PictogramController(Outer);

        List<PictogramTag> listOfPictogramTags = pictogramTagController.getListOfPictogramTags();

        for (PictogramTag pt : listOfPictogramTags) {
            for (int i = 0; i < tagIDs.size(); i++) {
                if (pt.getTagId() == tagIDs.get(i)) {
                    tagResult.add(pictogramController.getPictogramById(pt.getPictogramId()));
                }
            }
        }


        return tagResult;
    }
}
