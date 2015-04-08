package dk.aau.cs.giraf.pictosearch;


import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramTagController;
import dk.aau.cs.giraf.oasis.lib.controllers.TagController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.PictogramTag;
import dk.aau.cs.giraf.oasis.lib.models.Tag;

// TODO insert comment
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

    // TODO insert comment
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

    // TODO insert comment
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

    // TODO insert comment
    public ArrayList<Pictogram> getPictogramByTags(String[] input, ArrayList<Tag> listOfTags) {
    public ArrayList<Pictogram> getPictogramByTags(ArrayList<Tag> listOfTags) {
        ArrayList<Integer> tagIDs = new ArrayList<Integer>();
        ArrayList<Pictogram> result = new ArrayList<Pictogram>();

        for (Tag t : listOfTags){
            tagIDs.add(t.getId());
        }

        if (tagIDs.isEmpty()) {
            return result;
        }

        PictogramTagController pictogramTagController = new PictogramTagController(context);
        PictogramController pictogramController = new PictogramController(context);

        List<PictogramTag> pictogramTagList = pictogramTagController.getListOfPictogramTags();

        for (PictogramTag pt : pictogramTagList) {
            for (int i = 0; i < tagIDs.size(); i++) {
                if (pt.getTagId() == tagIDs.get(i)) {
                    result.add(pictogramController.getPictogramById(pt.getPictogramId()));
                }
            }
        }

        return result;
    }

    /**
     * Method that sorts the list of pictograms and category before sending it to the view
     * @param allList the list that needs to be sorted
     * @param searchString the search string that is used to evaluate the relevance for each
     *                     pictogram or category
     * @return sorted list according to the relevance from the searchString
     */
    public ArrayList<Object> SortPictogramsAndCategories(ArrayList<Object>allList, String searchString, String[] splitInput) {
        ArrayList<Object> result = new ArrayList<Object>();

        // A list of pairs, which contains the pictogram or category and the relevance
        List<Pair<Object, Integer>> pairList = new ArrayList<Pair<Object, Integer>>();

        // Calculate for each pictogram or category, their relevance according to the search string
        for (Object o : allList) {
            if (o instanceof Pictogram) {
                Pictogram p = (Pictogram)o;

                int number = Math.abs(p.getName().compareToIgnoreCase(searchString));

                // Check to see if each string in the split input is more relevant than the whole
                // search string
                for (String s : splitInput) {
                    if (Math.abs(p.getName().compareToIgnoreCase(s)) < number) {
                        number = Math.abs(p.getName().compareToIgnoreCase(s));
                    }
                }

                pairList.add(new Pair<Object, Integer>(p, number));

            }
            else if (o instanceof Category) {
                Category c = (Category)o;

                int number = Math.abs(c.getName().compareToIgnoreCase(searchString));

                // Check to see if each string in the split input is more relevant than the whole
                // search string
                for (String s : splitInput) {
                    if (Math.abs(c.getName().compareToIgnoreCase(s)) < number) {
                        number = Math.abs(c.getName().compareToIgnoreCase(s));
                    }
                }

                pairList.add(new Pair<Object, Integer>(c, number));
            }
        }

        int index = 0;
        int relevance;

        // Find the lowest number (the most relevant) and insert it into the result list
        while (!pairList.isEmpty()) {
            relevance = pairList.get(index).second;

            if (relevance != 0) {
                for (int j = 0; j < pairList.size(); j++) {
                    if (relevance > pairList.get(j).second) {
                        relevance = pairList.get(j).second;
                        index = j;
                    }
                }
            }

            result.add(pairList.get(index).first);
            pairList.remove(index);
            index = 0;
        }

        return result;
    }
}
