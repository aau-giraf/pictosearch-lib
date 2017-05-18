package dk.aau.cs.giraf.pictosearch;

import android.content.Context;

import android.os.AsyncTask;
import android.util.Pair;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;


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
public class Search extends AsyncTask<String, Void, ArrayList<Pictogram>> {
    private final User currentUser;
    private AsyncResponse delegate;
    private GirafActivity mainActivity;
    private GirafWaitingDialog waitingDialog;
    private final boolean isSingle;
    private boolean showDialog = false;
    private static final String SEARCHING_FOR_PICTOGRAMS_AND_CATEGORIES = "SEARCHING_FOR_PICTOGRAMS";
    private Context currentContext;

    /**
     * The constructor for Search. You already know what that means.
     *
     * @param mainActivity   used as a reference to the callee
     * @param currentUser    tells if the callee is guardian or citizen
     * @param delegate       where the result should be delivered to.
     * @param isSingle       if true it will only be possible to select one pictogram
     * @param currentContext Context required by the rest library, you can probably get it by getApplicationContext
     */
    public Search(GirafActivity mainActivity, User currentUser, AsyncResponse delegate, boolean isSingle, Context currentContext) {
        this.mainActivity = mainActivity;
        this.currentUser = currentUser;
        this.delegate = delegate;
        this.isSingle = isSingle;
        this.currentContext = currentContext;
    }

    /**
     * Yet another constructor, this time you can specify whether or not you want an obtrusive giraf dialog! HYPE!.
     *
     * @param showDialog indicated if a dialog should be shown when searching
     * @link Search(GirafActivity, long, AsyncResponse, boolean)
     */
    public Search(GirafActivity mainActivity, User currentUser, AsyncResponse delegate, boolean isSingle, boolean showDialog, Context currentContext) {
        this(mainActivity, currentUser, delegate, isSingle, currentContext);
        this.showDialog = showDialog;
    }


    /**
     * Gets the pictograms and feeds them to an AsyncResponse.
     * TODO: find out if passing queue in as a parameter is enough to transfer values, or if another AsyncResponse is needed
     * @param query The pictogram name searched for
     * @param queue The request queue
     * @param response An asynchronous response that holds an ArrayList of pictograms (typecast to object)
     */
    private void getPictogramsAsync(final String query, final RequestQueueHandler queue, final AsyncResponse response) {
        Response.Listener<ArrayList<Pictogram>> listener = new Response.Listener<ArrayList<Pictogram>>() {
            @Override
            public void onResponse(ArrayList<Pictogram> pictograms) {
                // using Object due to AsyncResponse's lack of defined types.
                // IDEA: maybe change AsyncResponse to AsyncResponse<T>?
                ArrayList<Pictogram> pictoList = new ArrayList<>();

                for(Pictogram p : pictograms) {
                    if(!pictoList.contains(p)) {
                        pictoList.add(p);
                    }
                }

                response.processFinish(pictoList);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    queue.login(currentUser, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            queue.getArray(300, Pictogram.class, new Response.Listener<ArrayList<Pictogram>>() {
                                @Override
                                public void onResponse(ArrayList<Pictogram> response) {
                                    // TODO: fix later
                                    throw new java.lang.UnsupportedOperationException();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse.statusCode == 401) {
                                        // TODO: display a message saying it failed to connect, try again later
                                        throw new java.lang.UnsupportedOperationException();
                                    }
                                }
                            });
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //TODO: log it
                            throw new java.lang.UnsupportedOperationException();
                        }
                    });
                } else if (error.networkResponse.statusCode == 404) {
                    // TODO: display a message box saying it does not have access to the server
                    throw new java.lang.UnsupportedOperationException();
                }
            }
        };

        queue.getArray(300, query, Pictogram.class, listener, errorListener);
    }

    /**
     * Gets all pictograms matching one of the input words from the database.
     *
     * @param pictogramNames string array with each search word
     * @return List of all pictogram matching the search names
     */
    private ArrayList<Pictogram> getAllPictograms(String[] pictogramNames, Context searchContext) {
        final ArrayList<Pictogram> pictoList = new ArrayList<>();

        if (pictogramNames.length > 0 && pictogramNames[0].isEmpty()) {
            return pictoList;
        }

        //getApplicationContext skal være inde i en override for at virke.
        final RequestQueueHandler queue = RequestQueueHandler.getInstance(searchContext);

        for (String name : pictogramNames) {
            getPictogramsAsync(name, queue, new AsyncResponse() {
                @Override
                public void processFinish(ArrayList<Pictogram> output) {
                    for (Object o : output) {
                        pictoList.add((Pictogram) o);
                    }
                }
            });
        }

        return pictoList;
    }

    /**
     * Get all tags matching one of the input words from the database.
     *
     * @param tagCaptions String array with each search word
     * @return List of all tags matching the search names
     */
    @Deprecated
    private ArrayList<Pictogram> getPictogramByTags(String[] tagCaptions, Context searchContext) {
        final ArrayList<Pictogram> pictoList = new ArrayList<>();
        final ArrayList<Pictogram> pictoTemp = new ArrayList<>();

        final RequestQueueHandler queue = RequestQueueHandler.getInstance(searchContext);

        Response.Listener<ArrayList<Pictogram>> responseListener = new Response.Listener<ArrayList<Pictogram>>() {
            @Override
            public void onResponse(ArrayList<Pictogram> response) {
                pictoTemp.addAll(response);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){
                    queue.login(currentUser, new Response.Listener<Integer>() {
                        @Override
                        public void onResponse(Integer response) {
                            queue.getArray(300, Pictogram.class, new Response.Listener<ArrayList<Pictogram>>() {
                                @Override
                                public void onResponse(ArrayList<Pictogram> response) {
                                    pictoTemp.addAll(response);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse.statusCode == 401) {
                                        //ToDo display a message saying it failed to connect, try again later
                                        throw new java.lang.UnsupportedOperationException();
                                    }
                                }
                            });

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //ToDo log it
                            throw new java.lang.UnsupportedOperationException();
                        }
                    });                }
                else if (error.networkResponse.statusCode == 404){
                    //ToDo display a message box saying it does not have access to the server
                    throw new java.lang.UnsupportedOperationException();
                }
            }
        };

        queue.getArray(300, Pictogram.class, responseListener, errorListener);

        for (Pictogram p : pictoTemp) {
            for (String s : tagCaptions) {
                if (p.getTitle().equals(s)) {
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
    private ArrayList<Pictogram> sortPictogramsAndCategories(List<Pictogram> allList, String[] splitInput) {
        ArrayList<Pictogram> result = new ArrayList<>();

        // A list of pairs, which contains the pictogram or category and the relevance
        List<Pair<Pictogram, Integer>> pairList = new ArrayList<>();

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

                pairList.add(new Pair<Pictogram, Integer>(pictogram, relevance));
            }
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
    protected ArrayList<Pictogram> doInBackground(String... params) {
        // if doInBackground has no input arguments, return an empty array-list.
        if (params.length <= 0) {
            return new ArrayList<>();
        }

        String searchString = params[0];
        String[] splitInput = searchString.replaceAll("\\s+", " ").split(","); // Java really needs raw strings...

        int length = splitInput.length;

        // NOTE: foreach style loop not used here, because it doesn't allow for in-place overwriting
        for (int i = 0; i < length; i++) {
            splitInput[i] = splitInput[i].trim();
        }

        ArrayList<Pictogram> results = new ArrayList<>();

        ArrayList<Pictogram> pictoTagList = new ArrayList<>();


        // Get all pictograms where the name matches the split input
        results.addAll(getAllPictograms(splitInput, currentContext));

        pictoTagList.addAll(getPictogramByTags(splitInput, currentContext));

        // Insert all pictograms from the tags if they are not in the result list
        for (Pictogram p : pictoTagList) {
            if (!results.contains(p)) {
                results.add(p);
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
        results = sortPictogramsAndCategories(results, splitInput);

        return results;
    }

    @Override
    protected void onPostExecute(ArrayList<Pictogram> result) {
        if (showDialog) {
            waitingDialog.dismiss();
        }

        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}
