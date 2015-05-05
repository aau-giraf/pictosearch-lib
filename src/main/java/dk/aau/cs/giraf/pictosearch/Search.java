package dk.aau.cs.giraf.pictosearch;

import android.os.AsyncTask;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.controllers.CategoryController;
import dk.aau.cs.giraf.dblib.controllers.PictogramController;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.gui.GirafWaitingDialog;

/**
 * Search class used to search for pictograms and/or categories
 */
public class Search extends AsyncTask<String, Void, ArrayList<Object>> {
    private final long citizenID;
    private AsyncResponse delegate;
    private GirafActivity mainActivity;
    private GirafWaitingDialog waitingDialog;
    private static final String SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES = "SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES";

    public Search(GirafActivity mainActivity, long citizenID, AsyncResponse delegate) {
        this.mainActivity = mainActivity;
        this.citizenID = citizenID;
        this.delegate = delegate;
    }

    /**
     * Gets all pictograms matching one of the input words from the database
     *
     * @param pictogramNames string array with each search word
     * @return List of all pictogram matching the search names
     */
    private ArrayList<Pictogram> GetAllPictograms(String[] pictogramNames) {
        ArrayList<Pictogram> pictoList = new ArrayList<Pictogram>();

        if (pictogramNames[0].isEmpty()) {
            return pictoList;
        }

        PictogramController pictogramController = new PictogramController(mainActivity.getApplicationContext());

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
     * @return List of all categories with a matching name
     */
    private ArrayList<Category> GetAllCategories(final String[] categoryNames) {
        ArrayList<Category> catList = new ArrayList<Category>();

        if (citizenID < 0 || categoryNames[0].isEmpty()) {
            return catList;
        }

        CategoryController categoryController = new CategoryController(mainActivity.getApplicationContext());

        List<Category> catTemp = categoryController.getCategoriesByProfileId(citizenID);

        for (String s : categoryNames) {
            for (Category c : catTemp) {
                if (c.getName().toLowerCase().startsWith(s.toLowerCase()) && !catList.contains(c)) {
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
    private ArrayList<Pictogram> GetPictogramByTags(String[] tagCaptions) {
        ArrayList<Pictogram> pictoList = new ArrayList<Pictogram>();
        ArrayList<Pictogram> pictoTemp = new ArrayList<Pictogram>();

        PictogramController pictogramController = new PictogramController(mainActivity.getApplicationContext());

        for (String s : tagCaptions) {
            pictoTemp.addAll(pictogramController.getPictogramsWithTagName(s));
        }

        for (Pictogram p : pictoTemp) {
            if (!pictoList.contains(p)) {
                pictoList.add(p);
            }
        }

        return pictoList;
    }

    /**
     * Method that sorts the list of pictograms and category before sending it to the view
     *
     * @param allList      the list that needs to be sorted
     * @param searchString the search string that is used to evaluate the relevance for each
     *                     pictogram or category
     * @return sorted list according to the relevance from the searchString
     */
    private ArrayList<Object> SortPictogramsAndCategories(List<Object> allList, String searchString) {
        ArrayList<Object> result = new ArrayList<Object>();

        // A list of pairs, which contains the pictogram or category and the relevance
        List<Pair<Object, Integer>> pairList = new ArrayList<Pair<Object, Integer>>();

        // Calculate for each pictogram or category, their relevance according to the search string
        for (Object o : allList) {
            if (o instanceof Pictogram) {
                Pictogram p = (Pictogram) o;

                int relevance = Math.abs(p.getName().compareToIgnoreCase(searchString));

                pairList.add(new Pair<Object, Integer>(p, relevance));

            } else if (o instanceof Category) {
                Category c = (Category) o;

                int relevance = Math.abs(c.getName().compareToIgnoreCase(searchString));

                pairList.add(new Pair<Object, Integer>(c, relevance));
            }
        }

        // Find the lowest number (the most relevant) and insert it into the result list
        while (!pairList.isEmpty()) {
            int index = 0;
            int relevance = pairList.get(index).second;

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
        }

        return result;
    }

    @Override
    protected void onPreExecute() {
        waitingDialog = GirafWaitingDialog.newInstance(mainActivity.getString(R.string.searching_title), mainActivity.getString(R.string.searching_description));
        waitingDialog.show(mainActivity.getSupportFragmentManager(), SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES);
    }

    @Override
    protected ArrayList<Object> doInBackground(String... params) {
        String searchString = params[0];
        String[] splitInput = searchString.split("\\s+");

        ArrayList<Object> result = new ArrayList<Object>();

        ArrayList<Pictogram> pictoTagList = new ArrayList<Pictogram>();

        // Get all pictograms where the name matches the split input
        result.addAll(GetAllPictograms(splitInput));

        pictoTagList.addAll(GetPictogramByTags(splitInput));

        // Insert all pictograms from the tags if they are not in the result list
        for (Pictogram p : pictoTagList) {
            if (!result.contains(p)) {
                result.add(p);
            }
        }

        // Insert all categories where the name matches the split input
        result.addAll(GetAllCategories(splitInput));

        // Sort the pictograms and categories
        result = SortPictogramsAndCategories(result, searchString);

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Object> result) {
        waitingDialog.dismiss();

        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}
