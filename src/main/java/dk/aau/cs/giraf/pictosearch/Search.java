package dk.aau.cs.giraf.pictosearch;

import android.os.AsyncTask;
import android.util.Pair;
import android.view.WindowManager;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.controllers.CategoryController;
import dk.aau.cs.giraf.dblib.controllers.PictogramController;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.gui.GirafWaitingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Search class used to search for pictograms and/or categories.
 */
public class Search extends AsyncTask<String, Void, ArrayList<Object>> {
    private final long ID;
    private AsyncResponse delegate;
    private GirafActivity mainActivity;
    private GirafWaitingDialog waitingDialog;
    private final boolean isSingle;
    private boolean showDialog = false;
    private static final String SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES = "SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES";

    /**
     * Constructs an instance of search, just like a constructor is supposed to...
     *
     * @param mainActivity used as a reference to the callee
     * @param ID           tells if the callee is guardian or citizen
     * @param delegate     where the result should be delivered to.
     * @param isSingle     if true it will only be possible to select one pictogram
     */
    public Search(GirafActivity mainActivity, long ID, AsyncResponse delegate, boolean isSingle) {
        this.mainActivity = mainActivity;
        this.ID = ID;
        this.delegate = delegate;
        this.isSingle = isSingle;
    }

    /**
     * Yet another constructor, this time you can specify whether or not you want an obtrusive giraf dialog! HYPE!.
     *
     * @param showDialog indicated if a dialog should be shown when searching
     * @link Search(GirafActivity, long, AsyncResponse, boolean)
     */
    public Search(GirafActivity mainActivity, long ID, AsyncResponse delegate, boolean isSingle, boolean showDialog) {
        this(mainActivity, ID, delegate, isSingle);
        this.showDialog = showDialog;
    }

    /**
     * Gets all pictograms matching one of the input words from the database.
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
     * Gets all categories matching one of the input words from the database.
     *
     * @param categoryNames String array with each search word
     * @return List of all categories with a matching name
     */
    private ArrayList<Category> GetAllCategories(final String[] categoryNames) {
        ArrayList<Category> catList = new ArrayList<Category>();

        if (ID < 0 || categoryNames[0].isEmpty()) {
            return catList;
        }

        CategoryController categoryController = new CategoryController(mainActivity.getApplicationContext());

        List<Category> catTemp = categoryController.getCategoriesByProfileId(ID);

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
     * Get all tags matching one of the input words from the database.
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
     * Method that sorts the list of pictograms and category before sending it to the view.
     *
     * @param allList    the list that needs to be sorted
     * @param splitInput the split input that is used to evaluate the relevance
     * @return sorted list according to the relevance from the searchString
     */
    private ArrayList<Object> SortPictogramsAndCategories(List<Object> allList, String[] splitInput) {
        ArrayList<Object> result = new ArrayList<Object>();

        // A list of pairs, which contains the pictogram or category and the relevance
        List<Pair<Object, Integer>> pairList = new ArrayList<Pair<Object, Integer>>();

        // Calculate for each pictogram or category, their relevance according to the search string
        for (Object o : allList) {
            if (o instanceof Pictogram) {
                Pictogram p = (Pictogram) o;

                int relevance = Integer.MAX_VALUE;

                for (String s : splitInput) {
                    int newRelevance = Math.abs((p.getName().compareToIgnoreCase(s)));

                    if (relevance > newRelevance) {
                        relevance = newRelevance;
                    }
                }

                pairList.add(new Pair<Object, Integer>(p, relevance));
            } else if (o instanceof Category) {
                Category c = (Category) o;

                int relevance = Integer.MAX_VALUE;

                for (String s : splitInput) {
                    int newRelevance = Math.abs((c.getName().compareToIgnoreCase(s)));
                    if (relevance > newRelevance) {
                        relevance = newRelevance;
                    }
                }

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
        if (showDialog) {
            waitingDialog = GirafWaitingDialog.newInstance(mainActivity.getString(R.string.searching_title),
                mainActivity.getString(R.string.searching_description));
            waitingDialog.show(mainActivity.getSupportFragmentManager(), SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES);
        }
    }

    @Override
    protected ArrayList<Object> doInBackground(String... params) {
        String searchString = params[0];
        String[] splitInput = searchString.replaceAll("\\s+", " ").split(",");

        for (int i = 0; i < splitInput.length; i++) {
            splitInput[i] = splitInput[i].trim();
        }

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

        // If isSingle is false, insert all categories where the name matches the split input
        if (!isSingle) {
            result.addAll(GetAllCategories(splitInput));
        }

        // Sort the pictograms and categories
        result = SortPictogramsAndCategories(result, splitInput);

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Object> result) {
        if (showDialog) {
            waitingDialog.dismiss();
        }

        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}
