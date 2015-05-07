package dk.aau.cs.giraf.pictosearch;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.CategoryController;
import dk.aau.cs.giraf.dblib.controllers.PictogramController;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.gui.GirafInflatableDialog;
import dk.aau.cs.giraf.gui.GirafSpinner;

/**
 *  The main class in PictoSearch. Contains almost all methods relating to search.
 */
public class PictoAdminMain extends GirafActivity implements AsyncResponse, GirafConfirmDialog.Confirmation {
    // Different method id's for notification dialogs
    private static final int ACCEPT_NO_PICTOGRAMS = 101;
    private static final int ACCEPT_WITH_CATEGORIES = 102;
    private static final int ACCEPT_MANY_RETURNS = 103;

    private static final int MAX_NUMBER_OF_RETURNS = 100;

    private long citizenID;
    private long guardianID;

    public ArrayList<Object> checkoutList = new ArrayList<Object>();
    private ArrayList<Object> searchList = new ArrayList<Object>();
    private ArrayList<Object> emptyList = new ArrayList<Object>();
    private ArrayList<Object> searchTemp = new ArrayList<Object>();
    private ArrayList<Object> currentViewSearch = new ArrayList<Object>();
    private String gridViewString;

    public GridView checkoutGrid;
    private GridView pictoGrid;

    /*
     *  Request from another group. It should be possible to only send one pictogram,
     *  and therefore only display one pictogram in the checkout list. isSingle is used
     *  to store information. Default = false, so multiple pictoList are possible.
     *  If the intent that started the search contain the extra "single", isSingle is set
     *  to true
     */
    private boolean isSingle = false;

    /**
     * Method called when initialising PictoSearch activity
     * @param savedInstanceState saves information about the state of the activity's view hierarchy
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateGuardianInfo();
        getPurpose();

        setContentView(R.layout.activity_picto_admin_main);
        findViewById(R.id.mainLinearLayout).setBackgroundDrawable(GComponent.GetBackground(GComponent.Background.GRADIENT));

        // Actionbar buttons created
        final GirafButton help = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_help));
        GirafButton accept = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_accept));
        GirafButton categoryTool = new GirafButton(this, this.getResources().getDrawable(R.drawable.giraf_app_icon_category_tool));
        GirafButton pictoCreatorTool = new GirafButton(this, this.getResources().getDrawable(R.drawable.giraf_app_icon_picto_creator));

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                GirafInflatableDialog helpDialogBox = GirafInflatableDialog.newInstance(String.format("Hjælp"),String.format("Kort overblik over funktionerne i Pikto Søger."), R.layout.help_grid);
                helpDialogBox.show(getSupportFragmentManager(), "");

            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                // Check if the checkout list is empty, and prompt the user if so
                if (checkoutList.isEmpty()) {
                    GirafConfirmDialog acceptNoResults = GirafConfirmDialog.newInstance(
                            getString(R.string.accept_no_result_title),
                            getString(R.string.accept_no_result_context),
                            ACCEPT_NO_PICTOGRAMS);
                    acceptNoResults.show(getSupportFragmentManager(), "" + ACCEPT_NO_PICTOGRAMS);
                }
                // Check if the checkout list contains categories, and prompt the user if so
                else if (checkCheckoutListForCategories()) {
                    GirafConfirmDialog acceptWithCategories = GirafConfirmDialog.newInstance(
                            getString(R.string.accept_with_categories_title),
                            getString(R.string.accept_with_categories_context),
                            ACCEPT_WITH_CATEGORIES);
                    acceptWithCategories.show(getSupportFragmentManager(), "" + ACCEPT_NO_PICTOGRAMS);
                }
                // Check the number of pictograms in the checkout list, and prompt the user if there is above the limit
                else if (checkCheckoutListForCount()) {
                    GirafConfirmDialog acceptManyReturns = GirafConfirmDialog.newInstance(
                            getString(R.string.accept_many_returns_title),
                            getString(R.string.accept_many_returns_context),
                            ACCEPT_MANY_RETURNS);
                    acceptManyReturns.show(getSupportFragmentManager(), "" + ACCEPT_MANY_RETURNS);
                }
                // If none of the checks results in false, send the content to the calling application
                else {
                    sendContent(getCurrentFocus());
                }
            }
        });
        categoryTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                LaunchCategoryTool(true);
            }
        });
        pictoCreatorTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                LaunchPictoCreator(true);
            }
        });

        //Giraf buttons added to actionbar - order is left to right. backButton is always leftmost
        addGirafButtonToActionBar(help, LEFT);
        addGirafButtonToActionBar(categoryTool, RIGHT);
        addGirafButtonToActionBar(pictoCreatorTool, RIGHT);
        addGirafButtonToActionBar(accept, RIGHT);

        checkoutList = new ArrayList<Object>();
        searchList = new ArrayList<Object>();
        searchTemp = new ArrayList<Object>();
        currentViewSearch = new ArrayList<Object>();
        emptyList = new ArrayList<Object>();

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        onUpdatedCheckoutCount();
        loadCategoriesIntoCategorySpinner();

        checkoutGrid = (GridView) findViewById(R.id.checkout);
        checkoutGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                hideKeyboard();
                checkoutList.remove(position);
                onUpdatedCheckoutCount();
                checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
            }
        });

        pictoGrid = (GridView) findViewById(R.id.pictogram_displayer);

        pictoGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                positionClicked(position);
            }
        });

        // TODO: insert more comments
        GirafSpinner searchSpinner = (GirafSpinner) findViewById(R.id.category_dropdown);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                hideKeyboard();

                CategoryController cController = new CategoryController(getApplicationContext());

                List<Category> cTemp;

                if (citizenID != -1) {
                    cTemp = cController.getCategoriesByProfileId(citizenID);
                } else {
                    cTemp = cController.getCategoriesByProfileId(guardianID);
                }

                Category cat = new Category();

                if (!selectedItem.equals(getString(R.string.choose_category_colon))) {
                    for (Category c : cTemp) {
                        if (selectedItem.equals(c.getName())) {
                            cat = c;
                        }
                    }

                }

                PictogramController pictogramController = new PictogramController(getApplicationContext());
                List<Pictogram> pTemp = pictogramController.getPictogramsByCategory(cat);

                ArrayList<Object> allList = new ArrayList<Object>();
                allList.addAll(pTemp);

                currentViewSearch = allList;
                gridViewString = selectedItem;
                if (selectedItem.equals(getString(R.string.choose_category_colon))) {
                    if (searchTemp.isEmpty()) {
                        currentViewSearch.clear();
                        loadCategoryPictogramIntoGridView(currentViewSearch);
                    }
                    else {
                        loadCategoryPictogramIntoGridView(searchTemp);
                    }
                    onSearchSummaryCount(searchTemp);
                } else {
                    findViewById(R.id.empty_search_result).setVisibility(View.INVISIBLE);
                    loadCategoryPictogramIntoGridView(currentViewSearch);
                    onEnterCategoryCount(currentViewSearch);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideKeyboard();
                searchForPictogram();
            }
        });

        final EditText searchTerm = (EditText) findViewById(R.id.text_search_input);

        searchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    clickedSearch();
                }
                return false;
            }

        });
        ImageButton clearButton = (ImageButton) findViewById(R.id.clear_search_field);
        clearButton.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View v) {
                searchTerm.setText(null);
            }
        });
        GirafButton btnSearch = (GirafButton) findViewById(R.id.search_button);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedSearch();
            }

        });

        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(searchTerm
                        .getWindowToken(), 0);
                return true;
            }
        });

    }

    /**
     * Updates different id's, throws exception if guardian ID is the default value,
     * because guardian ID is needed for category manager
     */
    private void updateGuardianInfo() {
        //If user is a monkey, set the childId to the first child in the list
        if (ActivityManager.isUserAMonkey()) {
            citizenID = new Helper(this).profilesHelper.getChildren().get(0).getId();
        }
        else {
            guardianID = getIntent().getLongExtra(getString(R.string.current_guardian_id), -1);
            citizenID = getIntent().getLongExtra(getString(R.string.current_child_id), -1);

            if (guardianID == -1) {
                throw new IllegalArgumentException("Missing guardian ID");
            }
        }
    }

    /**
     * Get the purpose from the calling application to know if they need a single pictogram
     */
	private void getPurpose()
    {
        if (getIntent().hasExtra(getString(R.string.purpose))) {
            if (getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.single))) {
                isSingle = true;
            } else if (getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.multi))){
                isSingle = false;
            }
        }
    }

    public void clickedSearch() {
        hideKeyboard();

        TextView emptySearchTextView = (TextView) findViewById(R.id.empty_search_result);
        emptySearchTextView.setVisibility(View.INVISIBLE);

        pictoGrid.setAdapter(new PictoAdapter(emptyList, getApplicationContext()));

        searchForPictogram();
    }

    /**
     * Search for pictograms and categories
     */
    public void searchForPictogram() {
        Search searcher;

        if (citizenID != -1) {
            searcher = new Search(this, citizenID, this, isSingle);
        }
        else {
            searcher = new Search(this, guardianID, this, isSingle);
        }

        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        String searchString = searchTerm.getText().toString().toLowerCase().trim();

        searcher.execute(searchString);
    }

    /**
     * Load all pictograms within a the selected category into the gridView.
     * @param cpList List of pictograms and categories from opening and closing a category.
     */
   private void loadCategoryPictogramIntoGridView(ArrayList<Object> cpList) {
        hideKeyboard();
        pictoGrid.setAdapter(new PictoAdapter(cpList, getApplicationContext()));

    }

    /**
     * Load all the categories into the category spinner
     */
   private void loadCategoriesIntoCategorySpinner() {
       CategoryController cController = new CategoryController(getApplicationContext());
       List<Category> catTemp;

       if (citizenID != -1) {
           catTemp = cController.getCategoriesByProfileId(citizenID);
       } else {
           catTemp = cController.getCategoriesByProfileId(guardianID);
       }

       ArrayList<String> catNames = new ArrayList<String>();


       EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
       String searchString = searchTerm.getText().toString().toLowerCase().trim();

       if (!searchString.isEmpty()){
           String[] splitInput = searchString.split("\\s+");

           for (Category c : catTemp) {
               for (String s : splitInput) {
                   if (c.getName().toLowerCase().startsWith(s)) {
                       catNames.add(c.getName());
                   }
               }
           }
       } else {
           for (Category c : catTemp) {
               catNames.add(c.getName());
           }
       }

       //Sorts in alphabetical order.
       Collections.sort(catNames, String.CASE_INSENSITIVE_ORDER);
       catNames.add(0, getString(R.string.choose_category_colon));

       Spinner catSpinner = (Spinner) findViewById(R.id.category_dropdown);

       ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, catNames);
       spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       catSpinner.setAdapter(spinnerArrayAdapter);
   }

    /**
     * Gets the pictograms and categories from the checkout list.
     * @return ArrayList object checkout list
     */
    private ArrayList<Object> getCheckoutObjects() {
        ArrayList<Object> checkout = new ArrayList<Object>();

        for(Object o : checkoutList) {
            if (o instanceof Pictogram) {
                Pictogram p = (Pictogram)o;
                checkout.add(p);
            }
            else if (o instanceof Category) {
                Category c = (Category)o;
                checkout.add(c);
            }
        }
        return checkout;
    }

    /**
     * Assess the checkout gridView and load the pictograms into an ArrayList
     * @return ArrayList of checkout pictograms
     */
    private long[] getCheckoutPictogramIDsArray() {
        ArrayList<Long> pictogramIDs = getCheckoutPictogramIDs();
        long[] checkout = new long[pictogramIDs.size()];
        long i = 0;
        for (long p : pictogramIDs) {
            checkout[((int) i)] = p;
            i++;
        }
        return checkout;
    }

    /**
     * get the pictogram IDs from all checkout items
     * @return pictogram ID of all pictograms and pictograms in the categories in the checkout list
     */
    private ArrayList<Long> getCheckoutPictogramIDs() {
        ArrayList<Long> pictogramCheckoutIDs = new ArrayList<Long>();
        ArrayList<Object> checkoutObjects = getCheckoutObjects();

        PictogramController pictogramController = new PictogramController(this);

        for(Object o : checkoutObjects) {
            if (o instanceof Pictogram) {
                Pictogram p = (Pictogram) o;
                pictogramCheckoutIDs.add(p.getId());
            }
            else if (o instanceof Category) {
                Category catNew = (Category) o;

                List<Pictogram> pictogramsInCategory = pictogramController.getPictogramsByCategory(catNew);

                for (Pictogram p : pictogramsInCategory) {
                    if (p != null) {
                        pictogramCheckoutIDs.add(p.getId());
                    }
                }
            }
        }

        return pictogramCheckoutIDs;
    }

    /**
     * Clears the searchField and found pictograms
     * @param view: This must be included for the function to work
     */
    public void clearSearchField(View view) {
        EditText searchField = (EditText) findViewById(R.id.text_search_input);
        searchField.setText(null);
        searchForPictogram();
    }

    /**
     * Clears the checkoutList
     * @param view: This must be included for the function to work
     */
    public void clearCheckoutList(View view) {
        checkoutList.clear();
        onUpdatedCheckoutCount();
        checkoutGrid.setAdapter(new PictoAdapter(checkoutList, this));
    }

    /**
     * Sends pictogram ids from checkoutList to appropriate calling application
     * @param view: This must be included for the function to work
     */
    public void sendContent(View view) {
        long[] output = getCheckoutPictogramIDsArray();
        Intent data = this.getIntent();

        data.putExtra(getString(R.string.checkout_ids), output);

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, data);
        } else {
            getParent().setResult(Activity.RESULT_OK, data);
        }
        finish();
    }

    /**
     * Update the number of checkout items
     */
    public void onUpdatedCheckoutCount() {
        ArrayList<Object> checkoutCat = new ArrayList<Object>();

        for (Object o : checkoutList) {
                if (o instanceof Category) {
                    checkoutCat.add(o);
                }
        }

        TextView  categoryBox = (TextView)  findViewById(R.id.categorySum);
        categoryBox.setText(getString(R.string.category_colon) + checkoutCat.size());
        TextView pictogramBox = (TextView) findViewById(R.id.pictogramSum);
        pictogramBox.setText(getString(R.string.pictogram_colon) + (checkoutList.size() - checkoutCat.size()));
    }

    /**
     * Build search summary list
     * @param sTemp temporary object list with pictograms and categories.
     */
    public void onSearchSummaryCount(ArrayList<Object> sTemp) {
        int countCatTemp = CountCategories(sTemp);
        int countPicTemp = CountPictograms(sTemp);

        TextView searchSummaryText = (TextView) findViewById(R.id.search_summary_count);
        if (sTemp.isEmpty()) {
            searchSummaryText.setText("");
        }
        else {
            if (countPicTemp == 1 && countCatTemp == 1) {
                searchSummaryText.setText(getString(R.string.search_result) + " " + countPicTemp + " " + getString(R.string.pictograms_single_lowercase) + " " + getString(R.string.and) + " " + countCatTemp + " " + getString(R.string.categories_single_lowercase));
            } else if ((countPicTemp == 0 || countPicTemp > 1) && countCatTemp == 1) {
                searchSummaryText.setText(getString(R.string.search_result) + " " + countPicTemp + " " + getString(R.string.pictograms_multi_lowercase) + " " + getString(R.string.and) + " " + countCatTemp + " " + getString(R.string.categories_single_lowercase));
            } else if (countPicTemp == 1 && (countCatTemp == 0 || countCatTemp > 1)) {
                searchSummaryText.setText(getString(R.string.search_result) + " " + countPicTemp + " " + getString(R.string.pictograms_single_lowercase) + " " + getString(R.string.and) + " " + countCatTemp + " " + getString(R.string.categories_multi_lowercase));
            } else {
                searchSummaryText.setText(getString(R.string.search_result) + " " + countPicTemp + " " + getString(R.string.pictograms_multi_lowercase) + " " + getString(R.string.and) + " " + countCatTemp + " " + getString(R.string.categories_multi_lowercase));
            }
        }
    }

    /**
     * Counts the amount of pictograms in an object list.
     * @param pTemp object list
     * @return returns the count
     */
    private int CountPictograms(ArrayList<Object> pTemp) {
        int count = 0;

        for (Object o : pTemp){
            if (o instanceof Pictogram){
                count++;
            }
        }

        return count;
    }

    /**
     * Counts the amount of categories in an object list.
     * @param cTemp object list
     * @return return the counts
     */
    private int CountCategories(ArrayList<Object> cTemp) {
        int count = 0;

        for (Object o : cTemp){
            if (o instanceof Category){
                count++;
            }
        }

        return count;
    }

    /**
     * counts the amount of pictograms when entering a category
     * @param pTemp object list
     */
    public void onEnterCategoryCount(ArrayList<Object> pTemp) {
        TextView searchSummaryText = (TextView) findViewById(R.id.search_summary_count);
        if (pTemp.size() == 1){
            searchSummaryText.setText(getString(R.string.category_contains) + " " + pTemp.size() + " " + getString(R.string.pictograms_single_lowercase));
        }
        else {
            searchSummaryText.setText(getString(R.string.category_contains) + " " + pTemp.size() + " " + getString(R.string.pictograms_multi_lowercase));
        }
    }

    /**
     * Open the application PictoCreator if the application is installed.
     * @param allow_error_msg boolean for allowing error messages to be displayed to user
     */
    private void LaunchPictoCreator(boolean allow_error_msg) {
        try {
            Intent i = new Intent();
            i.setClassName(getString(R.string.set_class_name_pictoCreator), getString(R.string.set_class_name_pictoCreator_mainActivity));
            startActivity(i);
        } catch (android.content.ActivityNotFoundException e) {
            if (allow_error_msg) {
                MessageDialogFragment message = new MessageDialogFragment(getString(R.string.unable_to_launch_pictoCreator));
                message.show(getFragmentManager(), getString(R.string.pictoCreator));
            }
        }
    }

    /**
     * Open the application CategoryTool if the application is installed.
     * @param allow_error_msg boolean for allowing error messages to be displayed to user
     */
    private void LaunchCategoryTool(boolean allow_error_msg) {
        Intent intent = new Intent();
        try {
            intent.setComponent(new ComponentName(getString(R.string.set_class_name_categoryTool), getString(R.string.set_class_name_categoryTool_mainActivity)));
            intent.putExtra(getString(R.string.current_child_id), citizenID);
            intent.putExtra(getString(R.string.current_guardian_id), guardianID);
            startActivity(intent);

        } catch (android.content.ActivityNotFoundException e) {
            if (allow_error_msg) {
                MessageDialogFragment message = new MessageDialogFragment(getString(R.string.unable_to_launch_categoryTool));
                message.show(getFragmentManager(), getString(R.string.categoryTool));
            }
        }
    }

    /**
     * add pictograms and categories to checkout list
     * @param position position clicked on screen
     */
    public void positionClicked(int position) {
        // if single pictogram requested, only one pictogram is displayed in checkout
        if (isSingle) {
            checkoutList.clear();
        }

        if (gridViewString.equals(getString(R.string.choose_category_colon))) {
            checkoutList.add(searchTemp.get(position));
        }
        else {
            checkoutList.add(currentViewSearch.get(position));
        }

        onUpdatedCheckoutCount();
        checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
    }

    /**
     * Hide the keyboard when pressing area outside keyboard
     */
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Check if the checkout list contains a category
     * @return if the checkout list contains a category
     */
    private boolean checkCheckoutListForCategories() {
        for (Object o : checkoutList) {
            if (o instanceof Category) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the size of the checkout list exceeds a maximum
     * @return if the size exceeds a maximum
     */
    private boolean checkCheckoutListForCount() {
        return checkoutList.size() > MAX_NUMBER_OF_RETURNS;
    }

    /**
     * Process called after searching.
     * @param output object list of pictograms and categories from search
     */
    @Override
    public void processFinish(ArrayList<Object> output) {
        searchList = output;
        searchTemp = searchList;
        onSearchSummaryCount(searchList);
        loadCategoriesIntoCategorySpinner();

        if (!output.isEmpty()) {
            pictoGrid.setAdapter(new PictoAdapter(searchList, getApplicationContext()));
        }
        else {
            findViewById(R.id.empty_search_result).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the action needed when accepting in the confirm dialogs
     * @param methodID ID's for the different possible methods using the confirm dialog
     */
    @Override
    public void confirmDialog(final int methodID) {
        if (methodID == ACCEPT_NO_PICTOGRAMS) {
            finish();
        } else if (methodID == ACCEPT_WITH_CATEGORIES || methodID == ACCEPT_MANY_RETURNS) {
            sendContent(getCurrentFocus());
        }
    }
}
