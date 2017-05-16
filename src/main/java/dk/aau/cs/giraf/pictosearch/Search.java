package dk.aau.cs.giraf.pictosearch;

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;


import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GirafWaitingDialog;
import dk.aau.cs.giraf.librest.requests.GetArrayRequest;
import dk.aau.cs.giraf.librest.requests.LoginRequest;
import dk.aau.cs.giraf.librest.requests.RequestQueueHandler;
import dk.aau.cs.giraf.models.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Search class used to search for pictograms and/or categories.
 */
public class Search extends AsyncTask<String, Void, ArrayList<Object>> {
    private final User currentUser;
    private AsyncResponse delegate;
    private GirafActivity mainActivity;
    private GirafWaitingDialog waitingDialog;
    private final boolean isSingle;
    private boolean showDialog = false;
    private static final String SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES = "SEARCHING_FOR_PICTOGRAMS";
    private Context theContext;

    /**
     * Constructs an instance of search, just like a constructor is supposed to...
     *
     * @param mainActivity used as a reference to the callee
     * @param currentUser           tells if the callee is guardian or citizen
     * @param delegate     where the result should be delivered to.
     * @param isSingle     if true it will only be possible to select one pictogram
     * @param theContext   Context required by the rest library, you can probably get it by getApplicationContext
     */
    public Search(GirafActivity mainActivity, User currentUser, AsyncResponse delegate, boolean isSingle, Context theContext) {
        this.mainActivity = mainActivity;
        this.currentUser = currentUser;
        this.delegate = delegate;
        this.isSingle = isSingle;
        this.theContext = theContext;
    }

    /**
     * Yet another constructor, this time you can specify whether or not you want an obtrusive giraf dialog! HYPE!.
     *
     * @param showDialog indicated if a dialog should be shown when searching
     * @link Search(GirafActivity, long, AsyncResponse, boolean)
     */
    public Search(GirafActivity mainActivity, User currentUser, AsyncResponse delegate, boolean isSingle, boolean showDialog, Context theContext) {
        this(mainActivity, currentUser, delegate, isSingle, theContext);
        this.showDialog = showDialog;
    }

    /**
     * Use this for changing context
     * Dunno if it is ever necessary, but i figured it would be good to have just in case
     * @param newContext the new context, usually gotten by getApplicationContext
     */
    public void ChangeContext(Context newContext){
        this.theContext = newContext;
    }

    /**
     * Gets all pictograms matching one of the input words from the database.
     *
     * @param pictogramNames string array with each search word
     * @return List of all pictogram matching the search names
     */
    private ArrayList<Pictogram> getAllPictograms(String[] pictogramNames, Context searchContext) {
        final ArrayList<Pictogram> pictoList = new ArrayList<Pictogram>();

        if (pictogramNames.length != 0 && pictogramNames[0].isEmpty()) {
            return pictoList;
        }
/*
        PictogramController pictogramController = new PictogramController(mainActivity.getApplicationContext());

        for (String s : pictogramNames) {
            List<Pictogram> pictoTemp = pictogramController.getPictogramsByName(s);
            for (Pictogram p : pictoTemp) {
                if(!pictoList.contains(p)) {
                    pictoList.add(p);
                }
            }
        }
*/
        //getApplicationContext skal være inde i en override for at virke.
        final RequestQueue queue = RequestQueueHandler.getInstance(searchContext).getRequestQueue();

        for (String s : pictogramNames) {

            GetArrayRequest<Pictogram> arr = new GetArrayRequest<Pictogram>(s, Pictogram.class, new Response.Listener<ArrayList<Pictogram>>() {
                @Override
                public void onResponse(ArrayList<Pictogram> response) {
                    for(Pictogram p : response){
                        if(!pictoList.contains(p)){
                            pictoList.add(p);
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse.statusCode == 401) {
                        LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                            @Override
                            public void onResponse(Integer response) {
                                GetArrayRequest<Pictogram> arr = new GetArrayRequest<Pictogram>(Pictogram.class, new Response.Listener<ArrayList<Pictogram>>() {
                                    @Override
                                    public void onResponse(ArrayList<Pictogram> response) {

                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error.networkResponse.statusCode == 401) {
                                            //ToDo display a message saying it failed to connect, try again later
                                        }

                                    }
                                });

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //ToDo log it
                            }
                        });
                        queue.add(loginRequest);
                    } else if (error.networkResponse.statusCode == 404) {
                        //ToDo display a message box saying it does not have access to the server
                    }
                }
            });
            queue.add(arr);
        }



        return pictoList;
    }

    /**
     * Gets all categories matching one of the input words from the database.
     *
     * @param categoryNames String array with each search word
     * @return List of all categories with a matching name
     */
/*
    private ArrayList<Category> getAllCategories(final String[] categoryNames) {
        ArrayList<Category> catList = new ArrayList<Category>();

        if (id < 0 || categoryNames[0].isEmpty()) {
            return catList;
        }

        CategoryController categoryController = new CategoryController(mainActivity.getApplicationContext());

        List<Category> catTemp = categoryController.getCategoriesByProfileId(id);

        for (String s : categoryNames) {
            for (Category c : catTemp) {
                if (c.getName().toLowerCase().startsWith(s.toLowerCase()) && !catList.contains(c)) {
                    catList.add(c);
                }
            }
        }

        return catList;
    }
*/
    /**
     * Get all tags matching one of the input words from the database.
     *
     * @param tagCaptions String array with each search word
     * @return List of all tags matching the search names
     */
    private ArrayList<Pictogram> getPictogramByTags(String[] tagCaptions, Context searchContext) {
        final ArrayList<Pictogram> pictoList = new ArrayList<Pictogram>();
        final ArrayList<Pictogram> pictoTemp = new ArrayList<Pictogram>();
        /*

        PictogramController pictogramController = new PictogramController(mainActivity.getApplicationContext());

        for (String s : tagCaptions) {
            List<Pictogram> pictoTemp = pictogramController.getPictogramsWithTagName(s);
            for (Pictogram p : pictoTemp) {
                if (!pictoList.contains(p)) {
                    pictoList.add(p);
                }
            }
        }

*/

        final RequestQueue queue = RequestQueueHandler.getInstance(searchContext).getRequestQueue();


        GetArrayRequest<Pictogram> arr = new GetArrayRequest<Pictogram>(Pictogram.class, new Response.Listener<ArrayList<Pictogram>>() {
            @Override
            public void onResponse(ArrayList<Pictogram> response) {
                pictoTemp.addAll(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){
                    LoginRequest loginRequest = new LoginRequest(currentUser, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            GetArrayRequest<Pictogram> arr = new GetArrayRequest<Pictogram>(Pictogram.class, new Response.Listener<ArrayList<Pictogram>>() {
                                @Override
                                public void onResponse(ArrayList<Pictogram> response) {

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse.statusCode == 401) {
                                        //ToDo display a message saying it failed to connect, try again later
                                    }

                                }
                            });

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //ToDo log it
                        }
                    });
                    queue.add(loginRequest);
                }
                else if(error.networkResponse.statusCode == 404){
                    //ToDo display a message box saying it does not have access to the server
                }
            }
        });
        queue.add(arr);

        for(Pictogram p : pictoTemp){
            for(String s : tagCaptions){
                if(p.getTitle() == s){
                    pictoList.add(p);
                }
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
    private ArrayList<Object> sortPictogramsAndCategories(List<Object> allList, String[] splitInput) {
        ArrayList<Object> result = new ArrayList<Object>();

        // A list of pairs, which contains the pictogram or category and the relevance
        List<Pair<Object, Integer>> pairList = new ArrayList<Pair<Object, Integer>>();

        // Calculate for each pictogram or category, their relevance according to the search string
        for (Object o : allList) {
            if (o instanceof Pictogram) {
                Pictogram pictogram = (Pictogram) o;

                int relevance = Integer.MAX_VALUE;

                for (String s : splitInput) {
                    int newRelevance = Math.abs((pictogram.getOwner().getUsername().compareToIgnoreCase(s)));

                    if (relevance > newRelevance) {
                        relevance = newRelevance;
                    }
                }

                pairList.add(new Pair<Object, Integer>(pictogram, relevance));
            } /* else if (o instanceof Category) {
                Category category = (Category) o;

                int relevance = Integer.MAX_VALUE;

                for (String s : splitInput) {
                    int newRelevance = Math.abs((category.getName().compareToIgnoreCase(s)));
                    if (relevance > newRelevance) {
                        relevance = newRelevance;
                    }
                }

                pairList.add(new Pair<Object, Integer>(category, relevance));
            } */
        }

        // Find the lowest number (the most relevant) and insert it into the result list
        while (!pairList.isEmpty()) {
            int index = 0;
            int relevance = pairList.get(index).second;

            if (relevance != 0) {
                int j;
                int size = pairList.size();
                for (j = 0; j < size; j++) {
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

        int i;
        int size = splitInput.length;
        for (i = 0; i < size; i++) {
            splitInput[i] = splitInput[i].trim();
        }

        ArrayList<Object> result = new ArrayList<Object>();

        ArrayList<Pictogram> pictoTagList = new ArrayList<Pictogram>();


        // Get all pictograms where the name matches the split input
        result.addAll(getAllPictograms(splitInput, theContext));

        pictoTagList.addAll(getPictogramByTags(splitInput, theContext));

        // Insert all pictograms from the tags if they are not in the result list
        for (Pictogram p : pictoTagList) {
            if (!result.contains(p)) {
                result.add(p);
            }
        }

        // If isSingle is false, insert all categories where the name matches the split input
        // I think it already adds all the pictogram up in line 228 so it shouldn't be a problem to comment this out
        /*
        if (!isSingle) {
            result.addAll(getAllCategories(splitInput));
        }
        */
        // Sort the pictograms and categories
        result = sortPictogramsAndCategories(result, splitInput);

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
