package dk.aau.cs.giraf.pictosearch;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.dblib.controllers.CategoryController;
import dk.aau.cs.giraf.dblib.controllers.PictogramController;
import dk.aau.cs.giraf.dblib.controllers.PictogramTagController;
import dk.aau.cs.giraf.dblib.controllers.TagController;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.dblib.models.PictogramTag;
import dk.aau.cs.giraf.dblib.models.Tag;


/**
 * Search class used to search for pictograms and/or categories
 */
public class Search extends AsyncTask<String, Void, ArrayList<Object>> {
    private Context context;
    private int childID;
    private AsyncResponse delegate;

    public Search(Context context, int childID, AsyncResponse delegate) {
        this.context = context;
        this.childID = childID;
        this.delegate = delegate;
    }

    /**
     * Gets all pictograms matching one of the input words from the database
     *
     * @param pictogramNames string array with each search word
     * @return List of all pictogram matching the search names
     */
    private ArrayList<Pictogram> getAllPictograms(String[] pictogramNames) {
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
            if (!pictoList.contains(p)) {
                pictoList.add(p);
            }
        }

        return pictoList;
    }

    /**
     * Gets all categories matching one of the input words from the database
     *
     * @param categoryNames String array with each search word
     * @param childID       ID of the citizen
     * @return List of all categories matching the search names
     */
    private ArrayList<Category> getAllCategories(String[] categoryNames, int childID) {
        ArrayList<Category> catList = new ArrayList<Category>();

        if (childID < 0 || categoryNames[0].isEmpty()) {
            return catList;
        }

        CategoryController categoryController = new CategoryController(context);

        List<Category> catTemp = categoryController.getCategoriesByProfileId(childID);

        for (String s : categoryNames) {
            for (Category c : catTemp) {
                if (c.getName().toLowerCase().contains(s) && !catList.contains(c)) {
                    catList.add(c);
                }
            }
        }

        return catList;
    }

    /**
     * Get all tags matching one of the input words from the database
     *
     * @param tagCaptions String array with each search word
     * @return List of all tags matching the search names
     */
    private ArrayList<Tag> getAllTags(String[] tagCaptions) {
        ArrayList<Tag> tagList = new ArrayList<Tag>();

        if (tagCaptions[0].isEmpty()) {
            return tagList;
        }

        TagController tagController = new TagController(context);

        for (String s : tagCaptions) {
            tagList.addAll(tagController.getTagsByCaption(s));
        }

        return tagList;
    }

    /**
     * Get pictogram by tags
     *
     * @param listOfTags list of tags that matches the search words
     * @return list of pictogram that has a matching tag.
     */
    private ArrayList<Pictogram> getPictogramByTags(List<Tag> listOfTags) {
        ArrayList<Integer> tagIDs = new ArrayList<Integer>();
        ArrayList<Pictogram> result = new ArrayList<Pictogram>();

        for (Tag t : listOfTags) {
            tagIDs.add(t.getId());
        }

        if (tagIDs.isEmpty()) {
            return result;
        }

        PictogramTagController pictogramTagController = new PictogramTagController(context);
        PictogramController pictogramController = new PictogramController(context);

        List<Pictogram> temp = new ArrayList<Pictogram>();

        List<PictogramTag> pictogramTagList = pictogramTagController.getListOfPictogramTags();

        /*for (PictogramTag pt : pictogramTagList) {
            for (Integer tagID : tagIDs) {
                if (pt.getTagId() == tagID) {
                    temp.add(pictogramController.getPictogramById(pt.getTagId()));
                }
            }
        }*/

        for (PictogramTag pt : pictogramTagList) {
            for (int i = 0; i < tagIDs.size(); i++) {
                if (pt.getTagId() == tagIDs.get(i)) {
                    temp.add(pictogramController.getPictogramById(pt.getPictogramId()));
                }
            }
        }

        for (Pictogram p : temp) {
            if (!result.contains(p)) {
                result.add(p);
            }
        }

        return result;
    }

    /**
     * Method that sorts the list of pictograms and category before sending it to the view
     *
     * @param allList      the list that needs to be sorted
     * @param searchString the search string that is used to evaluate the relevance for each
     *                     pictogram or category
     * @return sorted list according to the relevance from the searchString
     */
    private ArrayList<Object> SortPictogramsAndCategories(List<Object> allList, String searchString, String[] splitInput) {
        ArrayList<Object> result = new ArrayList<Object>();

        // A list of pairs, which contains the pictogram or category and the relevance
        List<Pair<Object, Integer>> pairList = new ArrayList<Pair<Object, Integer>>();

        // Calculate for each pictogram or category, their relevance according to the search string
        for (Object o : allList) {
            if (o instanceof Pictogram) {
                Pictogram p = (Pictogram) o;

                int number = Math.abs(p.getName().compareToIgnoreCase(searchString));

                // Check to see if each string in the split input is more relevant than the whole
                // search string
                for (String s : splitInput) {
                    if (Math.abs(p.getName().compareToIgnoreCase(s)) < number) {
                        number = Math.abs(p.getName().compareToIgnoreCase(s));
                    }
                }

                pairList.add(new Pair<Object, Integer>(p, number));

            } else if (o instanceof Category) {
                Category c = (Category) o;

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

    @Override
    protected void onPreExecute() {
        // TODO: make progress bar instead of a toast.
        Toast.makeText(context, "I am searching now", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected ArrayList<Object> doInBackground(String... params) {
        String searchString = params[0];
        String[] splitInput = searchString.split("\\s+");

        ArrayList<Object> result = new ArrayList<Object>();

        ArrayList<Pictogram> pictoTagList = new ArrayList<Pictogram>();

        // Get all pictograms where the name matches the split input
        result.addAll(getAllPictograms(splitInput));

        // Get all pictograms where the tag matches the split input, in a temporary variable
        List<Tag> tagList = getAllTags(splitInput);
        pictoTagList.addAll(getPictogramByTags(tagList));

        // Insert all pictograms from the tags if they are not in the result list
        for (Pictogram p : pictoTagList) {
            if (!result.contains(p)) {
                result.add(p);
            }
        }

        // Insert all categories where the name matches the split input
        result.addAll(getAllCategories(splitInput, childID));

        // Sort the pictograms and categories
        result = SortPictogramsAndCategories(result, searchString, splitInput);

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Object> result) {
        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}
