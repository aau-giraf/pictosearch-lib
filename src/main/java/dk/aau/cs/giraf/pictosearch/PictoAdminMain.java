package dk.aau.cs.giraf.pictosearch;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.CategoryController;
import dk.aau.cs.giraf.dblib.controllers.PictogramController;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafConfirmDialog;
import dk.aau.cs.giraf.gui.GirafSpinner;
import dk.aau.cs.giraf.pictosearch.showcase.ShowcaseManager;
import dk.aau.cs.giraf.showcaseview.ShowcaseView;
import dk.aau.cs.giraf.showcaseview.targets.ViewTarget;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The main class in PictoSearch. Contains almost all methods relating to search.
 */
public class PictoAdminMain extends GirafActivity implements AsyncResponse, GirafConfirmDialog.Confirmation {
    // Different method id's for notification dialogs
    private static final int ACCEPT_NO_PICTOGRAMS = 101;
    private static final int ACCEPT_WITH_CATEGORIES = 102;
    private static final int ACCEPT_MANY_RETURNS = 103;
    private static final int EXIT_WITH_CHECKOUTS = 104;

    // Global integer used in a notification dialog
    private static final int MAX_NUMBER_OF_RETURNS = 100;

    private long citizenId;
    private long guardianId;

    private Search searcher;

    private ArrayList<Object> checkoutList = new ArrayList<Object>();
    private ArrayList<Object> searchList = new ArrayList<Object>();
    private ArrayList<Object> emptyList = new ArrayList<Object>();
    private ArrayList<Object> searchTemp = new ArrayList<Object>();
    private ArrayList<Object> currentViewSearch = new ArrayList<Object>();
    private String gridViewString;

    // Grid views for both the checkout list and the search result list
    private GridView checkoutGrid;
    private GridView pictoGrid;
    private TextView emptySearchTextView;
    private EditText searchTerm;
    private ImageButton clearButton;

    private ShowcaseManager showcaseManager;
    private boolean isFirstRun;

    /*
     *  Request from another group. It should be possible to only send one pictogram,
     *  and therefore only display one pictogram in the checkout list. isSingle is used
     *  to store information. Default = false, so multiple pictoList are possible.
     *  If the intent that started the search contain the extra "single", isSingle is set
     *  to true
     */
    private boolean isSingle = false;

    /**
     * Method called when initialising PictoSearch activity.
     *
     * @param savedInstanceState saves information about the state of the activity's view hierarchy
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Update the guardian and child ID
        updateGuardianInfo();

        // Find out whether to only return one or multiple pictograms
        getPurpose();

        setContentView(R.layout.activity_picto_admin_main);
        findViewById(R.id.mainLinearLayout).setBackgroundDrawable(
            GComponent.GetBackground(GComponent.Background.GRADIENT));

        // Actionbar buttons created
        final GirafButton help = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_help));
        help.setId(R.id.help_button);
        GirafButton accept = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_accept));
        accept.setId(R.id.accept_button);
        GirafButton categoryTool = new GirafButton(this,
            this.getResources().getDrawable(R.drawable.giraf_app_icon_category_tool));
        categoryTool.setId(R.id.category_manager_button);
        GirafButton pictoCreatorTool = new GirafButton(this,
            this.getResources().getDrawable(R.drawable.giraf_app_icon_picto_creator));
        pictoCreatorTool.setId(R.id.pictocreator_button);
        emptySearchTextView = (TextView) findViewById(R.id.empty_search_result);
        searchTerm = (EditText) findViewById(R.id.text_search_input);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                toggleShowcase();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                // Check the number of pictograms in the checkout list, and prompt the user if it is above the limit
                else if (checkCheckoutListForCount()) {
                    GirafConfirmDialog acceptManyReturns = GirafConfirmDialog.newInstance(
                        getString(R.string.accept_many_returns_title),
                        getString(R.string.accept_many_returns_context),
                        ACCEPT_MANY_RETURNS);
                    acceptManyReturns.show(getSupportFragmentManager(), "" + ACCEPT_MANY_RETURNS);
                }
                // If none of the checks results in false, send the content to the calling application
                else {
                    sendContent();
                }
            }
        });
        categoryTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                launchCategoryTool(true);
            }
        });
        pictoCreatorTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                launchPictoCreator(true);
            }
        });

        //Giraf buttons added to actionbar - order is left to right. backButton is always leftmost
        addGirafButtonToActionBar(help, RIGHT);
        addGirafButtonToActionBar(categoryTool, LEFT);
        addGirafButtonToActionBar(pictoCreatorTool, LEFT);
        addGirafButtonToActionBar(accept, RIGHT);

        // Resets different list
        checkoutList = new ArrayList<Object>();
        searchList = new ArrayList<Object>();
        searchTemp = new ArrayList<Object>();
        currentViewSearch = new ArrayList<Object>();
        emptyList = new ArrayList<Object>();

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        onUpdatedCheckoutCount();
        loadCategoriesIntoCategorySpinner();

        // Grid view for the checkout list and on click listener
        checkoutGrid = (GridView) findViewById(R.id.checkout);
        checkoutGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                hideKeyboard();
                checkoutList.remove(position);
                onUpdatedCheckoutCount();
                checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
            }
        });

        // Grid view for the search result list and on click listener
        pictoGrid = (GridView) findViewById(R.id.pictogram_displayer);
        pictoGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
                positionClicked(position);
            }
        });


        GirafSpinner searchSpinner = (GirafSpinner) findViewById(R.id.category_dropdown);

        // OnItemSelectedListener, is used to check which item it selected at any time.
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Sets the itemSelected to local variable.
                String selectedItem = parent.getItemAtPosition(position).toString();

                CategoryController categoryController = new CategoryController(getApplicationContext());

                List<Category> categoryTempList;

                // Decides which categories to use, based on citizenId or guardianId.
                if (citizenId != -1) {
                    categoryTempList = categoryController.getCategoriesByProfileId(citizenId);
                } else {
                    categoryTempList = categoryController.getCategoriesByProfileId(guardianId);
                }

                Category cat = new Category();

                // If item is not equal to default, then check which category it is equal to.
                if (!selectedItem.equals(getString(R.string.choose_category_colon))) {
                    for (Category c : categoryTempList) {
                        if (selectedItem.equals(c.getName())) {
                            cat = c;
                        }
                    }
                }

                PictogramController pictogramController = new PictogramController(getApplicationContext());
                List<Pictogram> pictogramTempList = pictogramController.getPictogramsByCategory(cat);

                ArrayList<Object> allList = new ArrayList<Object>();
                allList.addAll(pictogramTempList);

                // Sets some global variables, used other places.
                currentViewSearch = allList;
                gridViewString = selectedItem;

                // Checks whether it is equal to default or not.
                if (selectedItem.equals(getString(R.string.choose_category_colon))) {
                    // Clears the view and load the empty view, if not search have been done.
                    if (searchTemp.isEmpty()) {
                        currentViewSearch.clear();
                        loadCategoryPictogramIntoGridView(currentViewSearch);
                    }
                    // Loads the previous search results.
                    else {
                        loadCategoryPictogramIntoGridView(searchTemp);
                    }
                    onSearchSummaryCount(searchTemp);
                }
                // Loads the pictograms inside a category into the grid.
                else {
                    findViewById(R.id.empty_search_result).setVisibility(View.INVISIBLE);
                    loadCategoryPictogramIntoGridView(currentViewSearch);
                    onEnterCategoryCount(currentViewSearch);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                searchForPictogram();
            }
        });

        final EditText searchTerm = (EditText) findViewById(R.id.text_search_input);

        searchTerm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence chars, int start, int count, int after) {

            }

            /**
             * Called when text is inputted in the search field searchTerm.
             */
            @Override
            public void onTextChanged(CharSequence chars, int start, int before, int count) {
                searchOnType();
                if (chars.length() == 0) {
                    clearButton.setVisibility(View.INVISIBLE);
                    emptySearchTextView.setText(getString(R.string.search_to_find));
                } else {
                    clearButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editableChars) {

            }
        });

        searchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchOnKeyBoard();
                }
                return false;
            }

        });
        clearButton = (ImageButton) findViewById(R.id.clear_search_field);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTerm.setText(null);
            }
        });
        clearButton.setVisibility(View.INVISIBLE);

        GirafButton btnSearch = (GirafButton) findViewById(R.id.search_button);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedSearch();
            }

        });

        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                hideKeyboard();
                return true;
            }
        });

        TextView tw = (TextView) findViewById(R.id.is_single_or_not);

        if (isSingle) {
            tw.setText(R.string.is_single_true);
        } else {
            tw.setText(R.string.is_single_false);
        }

        showKeyboard();
    }

    /**
     * Check if the checkout list is empty when the user press back (either the door-button or physical back-press).
     */
    @Override
    public void onBackPressed() {
        if (!checkoutList.isEmpty()) {
            GirafConfirmDialog exitWithCheckouts = GirafConfirmDialog.newInstance("", "", -1);

            if (checkoutList.size() > 1) {
                int catCount = 0;
                int picCount = 0;

                for (Object o : checkoutList) {
                    if (o instanceof Pictogram) {
                        picCount++;
                    } else if (o instanceof Category) {
                        catCount++;
                    }

                    if (picCount > 0 && catCount > 0) {
                        break;
                    }
                }

                if (catCount > 0 && picCount > 0) {
                    exitWithCheckouts = GirafConfirmDialog.newInstance(
                        getString(R.string.exit_with_checkouts_title),
                        getString(R.string.exit_with_checkouts_context_cat_and_pic),
                        EXIT_WITH_CHECKOUTS);
                } else if (picCount > 0) {
                    exitWithCheckouts = GirafConfirmDialog.newInstance(
                        getString(R.string.exit_with_checkouts_title),
                        getString(R.string.exit_with_checkouts_context_pic),
                        EXIT_WITH_CHECKOUTS);
                } else if (catCount > 0) {
                    exitWithCheckouts = GirafConfirmDialog.newInstance(
                        getString(R.string.exit_with_checkouts_title),
                        getString(R.string.exit_with_checkouts_context_cat),
                        EXIT_WITH_CHECKOUTS);
                }
            } else {
                if (checkoutList.get(0) instanceof Pictogram) {
                    exitWithCheckouts = GirafConfirmDialog.newInstance(
                        getString(R.string.exit_with_checkouts_title),
                        getString(R.string.exit_with_checkouts_context_single_pictogram),
                        EXIT_WITH_CHECKOUTS);
                } else if (checkoutList.get(0) instanceof Category) {
                    exitWithCheckouts = GirafConfirmDialog.newInstance(
                        getString(R.string.exit_with_checkouts_title),
                        getString(R.string.exit_with_checkouts_context_single_category),
                        EXIT_WITH_CHECKOUTS);
                }
            }

            exitWithCheckouts.show(getSupportFragmentManager(), "" + EXIT_WITH_CHECKOUTS);
        } else {
            finish();
        }
    }

    /**
     * Updates different id's, throws exception if guardian ID is the default value,
     * because guardian ID is needed for category manager.
     */
    private void updateGuardianInfo() {
        //If user is a monkey, set the childId to the first child in the list
        if (ActivityManager.isUserAMonkey()) {
            citizenId = new Helper(this).profilesHelper.getChildren().get(0).getId();
        } else {
            guardianId = getIntent().getLongExtra(getString(R.string.current_guardian_id), -1);
            citizenId = getIntent().getLongExtra(getString(R.string.current_child_id), -1);

            if (guardianId == -1) {
                throw new IllegalArgumentException("Missing guardian ID");
            }
        }
    }

    /**
     * Get the purpose from the calling application to know if they need a single pictogram.
     */
    private void getPurpose() {
        if (getIntent().hasExtra(getString(R.string.purpose))) {
            if (getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.single))) {
                isSingle = true;
            } else if (getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.multi))) {
                isSingle = false;
            }
        }
    }

    /**
     * Searches for pictograms when the search button is clicked.
     */
    public void clickedSearch() {
        hideKeyboard();
        searchForPictogram(true);
    }

    public void searchOnKeyBoard() {
        hideKeyboard();
    }

    public void searchOnType() {
        searchForPictogram();
    }

    /**
     * Search for pictograms and categories, defaults to not showing dialog.
     */
    public void searchForPictogram() {
        searchForPictogram(false);
    }

    /**
     * Search for pictograms and categories, with or without dialog.
     *
     * @param showDialog boolean that indicated whether or not an obtrusive dialog should be shown
     *                   when the search is being performed
     */
    public void searchForPictogram(boolean showDialog) {
        //Cancel any ongoing search
        if (searcher != null) {
            searcher.cancel(true);
        }

        // Use the citizen id if its send with the intent, else it uses the guardian id
        if (citizenId != -1) {
            searcher = new Search(this, citizenId, this, isSingle, showDialog);
        } else {
            searcher = new Search(this, guardianId, this, isSingle, showDialog);
        }

        //Give feedback about searching, that isn't an obtrusive dialog box
        String searchString = searchTerm.getText().toString().toLowerCase().trim();
        if (!searchString.isEmpty()) {
            showSearchFeedback(searchString, true);
        }

        //Start the actual searching
        searcher.execute(searchString);
    }

    /**
     * Shows text feedback relevant to the current ongoing search
     *
     * @param query       What is being searched for
     * @param showBigText whether or not to show the feedback in big text in the middle
     *                    of the pictogrid.
     */
    private void showSearchFeedback(String query, boolean showBigText) {
        String searchingFor = getString(R.string.searching_for) + " \"" + query + "\" ...";
        TextView searchSummaryText = (TextView) findViewById(R.id.search_summary_count);
        searchSummaryText.setText(searchingFor);
        if (showBigText) {
            emptySearchTextView.setText(searchingFor);
            emptySearchTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Load all pictograms within a the selected category into the gridView.
     *
     * @param cpList List of pictograms and categories from opening and closing a category.
     */
    private void loadCategoryPictogramIntoGridView(ArrayList<Object> cpList) {
        pictoGrid.setAdapter(new PictoAdapter(cpList, getApplicationContext()));
    }

    /**
     * Load all the categories into the category spinner.
     */
    private void loadCategoriesIntoCategorySpinner() {
        CategoryController categoryController = new CategoryController(getApplicationContext());
        List<Category> categoryTempList;

        if (citizenId != -1) {
            categoryTempList = categoryController.getCategoriesByProfileId(citizenId);
        } else {
            categoryTempList = categoryController.getCategoriesByProfileId(guardianId);
        }

        ArrayList<String> catNames = new ArrayList<String>();


        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        String searchString = searchTerm.getText().toString().toLowerCase().trim();

        if (!searchString.isEmpty()) {
            String[] splitInput = searchString.split("\\s+");

            for (Category c : categoryTempList) {
                for (String s : splitInput) {
                    if (c.getName().toLowerCase().startsWith(s)) {
                        catNames.add(c.getName());
                    }
                }
            }
        } else {
            for (Category c : categoryTempList) {
                catNames.add(c.getName());
            }
        }

        //Sorts in alphabetical order.
        Collections.sort(catNames, String.CASE_INSENSITIVE_ORDER);
        catNames.add(0, getString(R.string.choose_category_colon));

        Spinner catSpinner = (Spinner) findViewById(R.id.category_dropdown);

        ArrayAdapter<String> spinnerArrayAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, catNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catSpinner.setAdapter(spinnerArrayAdapter);
    }

    /**
     * Gets the pictograms and categories from the checkout list.
     *
     * @return ArrayList object checkout list
     */
    private ArrayList<Object> getCheckoutObjects() {
        ArrayList<Object> checkout = new ArrayList<Object>();

        for (Object o : checkoutList) {
            if (o instanceof Pictogram) {
                Pictogram pictogram = (Pictogram) o;
                checkout.add(pictogram);
            } else if (o instanceof Category) {
                Category category = (Category) o;
                checkout.add(category);
            }
        }
        return checkout;
    }

    /**
     * Assess the checkout gridView and load the pictograms into an ArrayList.
     *
     * @return Array of checkout pictograms
     */
    private long[] getCheckoutPictogramIDsArray() {
        ArrayList<Long> pictogramIDs = getCheckoutPictogramIDs();
        return ArrayUtils.toPrimitive(pictogramIDs.toArray(new Long[pictogramIDs.size()]));
    }

    /**
     * get the pictogram IDs from all checkout items.
     *
     * @return pictogram ID of all pictograms and pictograms in the categories in the checkout list
     */
    private ArrayList<Long> getCheckoutPictogramIDs() {
        ArrayList<Long> pictogramCheckoutIDs = new ArrayList<Long>();
        ArrayList<Object> checkoutObjects = getCheckoutObjects();

        PictogramController pictogramController = new PictogramController(this);

        for (Object o : checkoutObjects) {
            if (o instanceof Pictogram) {
                Pictogram pictogram = (Pictogram) o;
                pictogramCheckoutIDs.add(pictogram.getId());
            } else if (o instanceof Category) {
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
     * Sends pictogram ids from checkoutList to appropriate calling application.
     */
    public void sendContent() {
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
     * Update the number of checkout items.
     */
    public void onUpdatedCheckoutCount() {
        TextView categoryBox = (TextView) findViewById(R.id.categorySum);
        categoryBox.setText(countCategories(checkoutList) + " x " + getString(R.string.categories_single_capitalized));
        TextView pictogramBox = (TextView) findViewById(R.id.pictogramSum);
        pictogramBox.setText(countPictograms(checkoutList) + " x " + getString(R.string.pictograms_single_capitalized));
    }

    /**
     * Build search summary list.
     *
     * @param summaryTempList temporary object list with pictograms and categories.
     */
    public void onSearchSummaryCount(ArrayList<Object> summaryTempList) {
        int countCatTemp = countCategories(summaryTempList);
        int countPicTemp = countPictograms(summaryTempList);

        TextView searchSummaryText = (TextView) findViewById(R.id.search_summary_count);
        if (summaryTempList.isEmpty()) {
            searchSummaryText.setText("");
            if (searchTerm.getText().length() > 0) {
                emptySearchTextView.setText(getString(R.string.Search_gave_no_results));
            }
            emptySearchTextView.setVisibility(View.VISIBLE);
        } else {
            emptySearchTextView.setVisibility(View.INVISIBLE);
            StringBuilder summaryText = new StringBuilder(100);
            summaryText
                .append(getString(R.string.search_result))
                .append(" \"")
                .append(searchTerm.getText().toString())
                .append("\" ")
                .append(getString(R.string.search_result_end))
                .append(" ");

            if (countPicTemp > 0) summaryText.append(countPicTemp).append(" ");

            if (countPicTemp == 1) {
                summaryText.append(getString(R.string.pictograms_single_lowercase)).append(" ");
            } else if (countPicTemp > 1) {
                summaryText.append(getString(R.string.pictograms_multi_lowercase)).append(" ");
            }

            if (countCatTemp > 0) summaryText.append(countCatTemp).append(" ");

            if (countCatTemp == 1) {
                summaryText.append(getString(R.string.categories_single_lowercase)).append(" ");
            } else if (countCatTemp > 1) {
                summaryText.append(getString(R.string.categories_multi_lowercase)).append(" ");
            }

            searchSummaryText.setText(summaryText);
        }
    }

    /**
     * Counts the amount of pictograms in an object list.
     *
     * @param pictogramTempList object list
     * @return returns the count
     */
    private int countPictograms(ArrayList<Object> pictogramTempList) {
        int count = 0;

        for (Object o : pictogramTempList) {
            if (o instanceof Pictogram) {
                count++;
            }
        }

        return count;
    }

    /**
     * Counts the amount of categories in an object list.
     *
     * @param categoryTempList object list
     * @return return the counts
     */
    private int countCategories(ArrayList<Object> categoryTempList) {
        int count = 0;

        for (Object o : categoryTempList) {
            if (o instanceof Category) {
                count++;
            }
        }

        return count;
    }

    /**
     * counts the amount of pictograms when entering a category.
     *
     * @param pictogramTempList object list
     */
    public void onEnterCategoryCount(ArrayList<Object> pictogramTempList) {
        TextView searchSummaryText = (TextView) findViewById(R.id.search_summary_count);
        if (pictogramTempList.size() == 1) {
            searchSummaryText.setText(getString(R.string.category_contains) + " " +
                pictogramTempList.size() + " " + getString(R.string.pictograms_single_lowercase));
        } else {
            searchSummaryText.setText(getString(R.string.category_contains) + " " +
                pictogramTempList.size() + " " + getString(R.string.pictograms_multi_lowercase));
        }
    }

    /**
     * Open the application PictoCreator if the application is installed.
     *
     * @param allow_error_msg boolean for allowing error messages to be displayed to user
     */
    private void launchPictoCreator(boolean allow_error_msg) {
        try {
            Intent intent = new Intent();
            intent.setClassName(getString(R.string.set_class_name_pictoCreator),
                getString(R.string.set_class_name_pictoCreator_mainActivity));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            if (allow_error_msg) {
                MessageDialogFragment message = new MessageDialogFragment(
                    getString(R.string.unable_to_launch_pictoCreator));
                message.show(getFragmentManager(), getString(R.string.pictoCreator));
            }
        }
    }

    /**
     * Open the application CategoryTool if the application is installed.
     *
     * @param allow_error_msg boolean for allowing error messages to be displayed to user
     */
    private void launchCategoryTool(boolean allow_error_msg) {
        Intent intent = new Intent();
        try {
            intent.setComponent(new ComponentName(getString(R.string.set_class_name_categoryTool),
                getString(R.string.set_class_name_categoryTool_mainActivity)));
            intent.putExtra(getString(R.string.current_child_id), citizenId);
            intent.putExtra(getString(R.string.current_guardian_id), guardianId);
            startActivity(intent);

        } catch (android.content.ActivityNotFoundException e) {
            if (allow_error_msg) {
                MessageDialogFragment message = new MessageDialogFragment(
                    getString(R.string.unable_to_launch_categoryTool));
                message.show(getFragmentManager(), getString(R.string.categoryTool));
            }
        }
    }

    /**
     * add pictograms and categories to checkout list.
     *
     * @param position position clicked on screen
     */
    public void positionClicked(int position) {
        // if single pictogram requested, only one pictogram is displayed in checkout
        if (isSingle) {
            checkoutList.clear();
        }

        if (gridViewString.equals(getString(R.string.choose_category_colon))) {
            if (!checkoutList.contains(searchTemp.get(position))) {
                checkoutList.add(searchTemp.get(position));
            } else {
                if (searchTemp.get(position) instanceof Pictogram) {
                    Toast.makeText(getApplicationContext(),
                        getString(R.string.already_chosen_pictogram), Toast.LENGTH_SHORT).show();
                } else if (searchTemp.get(position) instanceof Category) {
                    Toast.makeText(getApplicationContext(),
                        getString(R.string.already_chosen_category), Toast.LENGTH_SHORT).show();
                }

                return;
            }
        } else {
            if (!checkoutList.contains(currentViewSearch.get(position))) {
                checkoutList.add(currentViewSearch.get(position));
            } else {
                if (currentViewSearch.get(position) instanceof Pictogram) {
                    Toast.makeText(getApplicationContext(),
                        getString(R.string.already_chosen_pictogram), Toast.LENGTH_SHORT).show();
                } else if (currentViewSearch.get(position) instanceof Category) {
                    Toast.makeText(getApplicationContext(),
                        getString(R.string.already_chosen_category), Toast.LENGTH_SHORT).show();
                }

                return;
            }
        }

        onUpdatedCheckoutCount();
        checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
    }

    /**
     * Hide the keyboard when pressing area outside keyboard.
     */
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        searchTerm.clearFocus();
    }

    /**
     * Show the keyboard and move focus to search field.
     */
    private void showKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
        searchTerm.requestFocus();
    }

    /**
     * Check if the checkout list contains a category.
     *
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
     * Check if the size of the checkout list exceeds a maximum.
     *
     * @return if the size exceeds a maximum
     */
    private boolean checkCheckoutListForCount() {
        return checkoutList.size() > MAX_NUMBER_OF_RETURNS;
    }

    /**
     * Process called after searching.
     *
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
        } else {
            findViewById(R.id.empty_search_result).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the action needed when accepting in the confirm dialogs.
     *
     * @param methodId ID's for the different possible methods using the confirm dialog
     */
    @Override
    public void confirmDialog(final int methodId) {
        if (methodId == ACCEPT_NO_PICTOGRAMS) {
            finish();
        } else if (methodId == ACCEPT_WITH_CATEGORIES || methodId == ACCEPT_MANY_RETURNS) {
            sendContent();
        } else if (methodId == EXIT_WITH_CHECKOUTS) {
            finish();
        }
    }

    /**
    * Shows a quick walkthrough of the functionality.
    */
    public synchronized void showShowcase() {

        //Makes sure the clear button is visible for the walkthrough.
        clearButton.setVisibility(View.VISIBLE);

        // Targets for the Showcase
        final ViewTarget chooseSummaryCheckout = new ViewTarget(R.id.checkoutSum, this, 1.5f);
        final ViewTarget chooseCategorySpinner = new ViewTarget(R.id.category_dropdown, this, 1.5f);
        final ViewTarget chooseCheckout = new ViewTarget(R.id.checkout, this, 1.5f);
        final ViewTarget chooseSearchBox = new ViewTarget(R.id.search_box, this, 1.5f);
        final ViewTarget chooseClearSearch = new ViewTarget(R.id.clear_search_field, this, 1.5f);
        final ViewTarget chooseSearchButton = new ViewTarget(R.id.search_button, this, 1.5f);
        final ViewTarget helpButtonTarget = new ViewTarget(this.getActionBar()
            .getCustomView().findViewById(R.id.help_button), 1.5f);
        final ViewTarget acceptButtonTarget = new ViewTarget(this.getActionBar()
            .getCustomView().findViewById(R.id.accept_button), 1.5f);
        final ViewTarget pictocreatorButtonTarget = new ViewTarget(this.getActionBar()
            .getCustomView().findViewById(R.id.pictocreator_button), 1.5f);
        final ViewTarget categoryManagerButtonTarget = new ViewTarget(this.getActionBar()
            .getCustomView().findViewById(R.id.category_manager_button), 1.5f);


        // Create a relative location for the next button
        final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        final int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        // Calculate position for the help text
        final int textX = this.findViewById(R.id.pictogram_displayer).getLayoutParams().width + margin * 2;
        final int textY = getResources().getDisplayMetrics().heightPixels / 2 + margin;
        showcaseManager = new ShowcaseManager();

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(chooseSearchBox, true);
                showcaseView.setContentTitle(getString(R.string.search_box_title));
                showcaseView.setContentText(getString(R.string.search_box_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(chooseClearSearch, true);
                showcaseView.setContentTitle(getString(R.string.clear_search_field_title));
                showcaseView.setContentText(getString(R.string.clear_search_field_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(chooseSearchButton, true);
                showcaseView.setContentTitle(getString(R.string.search_button_title));
                showcaseView.setContentText(getString(R.string.search_button_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {
                showcaseView.setShowcase(chooseCategorySpinner, true);
                showcaseView.setContentTitle(getString(R.string.category_spinner_title));
                showcaseView.setContentText(getString(R.string.category_spinner_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {
                showcaseView.setShowcase(chooseCheckout, true);
                showcaseView.setContentTitle(getString(R.string.checkout_title));
                showcaseView.setContentText(getString(R.string.checkout_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(chooseCheckout.getPoint().x + 500, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(chooseSummaryCheckout, true);
                showcaseView.setContentTitle(getString(R.string.summary_checkout_title));
                showcaseView.setContentText(getString(R.string.summary_checkout_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(categoryManagerButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.category_manager_title));
                showcaseView.setContentText(getString(R.string.category_manager_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(pictocreatorButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.pictocreator_title));
                showcaseView.setContentText(getString(R.string.pictocreator_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(acceptButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.accept_title));
                showcaseView.setContentText(getString(R.string.accept_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        showcaseManager.addShowCase(new ShowcaseManager.Showcase() {
            @Override
            public void configShowCaseView(final ShowcaseView showcaseView) {

                showcaseView.setShowcase(helpButtonTarget, true);
                showcaseView.setContentTitle(getString(R.string.help_title));
                showcaseView.setContentText(getString(R.string.help_context));
                showcaseView.setStyle(R.style.GirafCustomShowcaseTheme);
                showcaseView.setButtonPosition(lps);
                showcaseView.setTextPostion(textX, textY);
            }
        });

        ShowcaseManager.OnDoneListener onDoneCallback = new ShowcaseManager.OnDoneListener() {
            @Override
            public void onDone(ShowcaseView showcaseView) {
                if (searchTerm.getText().toString().isEmpty()) {
                    clearButton.setVisibility(View.INVISIBLE);
                }
                showcaseManager = null;
                isFirstRun = false;
            }
        };
        showcaseManager.setOnDoneListener(onDoneCallback);

        showcaseManager.start(this);
    }

    //ToDo Write JavaDoc
    public synchronized void hideShowcase() {
        if (showcaseManager != null) {
            showcaseManager.stop();
            showcaseManager = null;
        }
    }

    //ToDo Write JavaDoc
    public synchronized void toggleShowcase() {

        if (showcaseManager != null) {
            hideShowcase();
        } else {
            showShowcase();
        }
    }

}
