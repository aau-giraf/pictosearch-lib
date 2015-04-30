package dk.aau.cs.giraf.pictosearch;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

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
import dk.aau.cs.giraf.gui.GirafInflatableDialog;
import dk.aau.cs.giraf.gui.GirafSpinner;

/**
 *  The main class in PictoSearch. Contains almost all methods relating to search.
 */
public class PictoAdminMain extends GirafActivity implements AsyncResponse{
    private long citizenID;
    private long guardianID;

    public ArrayList<Object> checkoutList = new ArrayList<Object>();
    private ArrayList<Object> searchList = new ArrayList<Object>();
    private ArrayList<Object> emptyList = new ArrayList<Object>();
    private ArrayList<Object> searchTemp = new ArrayList<Object>();
    private ArrayList<Object> currentViewSearch = new ArrayList<Object>();
    private String gridViewString;
    public int progressLoad = 0;

    public GridView checkoutGrid;
    private GridView pictoGrid;
    Animation startingAnimation;
    Animation loadAnimation;

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

        // Example of an onclicklistener

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
                sendContent(getCurrentFocus());
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
        //onUpdatedSearchField();
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

                if (!selectedItem.equals(getString(R.string.category_colon))) {
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
                if (selectedItem.equals(getString(R.string.category_colon))) {
                    if (searchTemp.isEmpty()) {
                        currentViewSearch.clear();
                        loadCategoryPictogramIntoGridView(currentViewSearch);


                    }
                    else {
                        loadCategoryPictogramIntoGridView(searchTemp);
                    }
                    onSearchSummaryCount(searchTemp);
                } else {
                    loadCategoryPictogramIntoGridView(currentViewSearch);
                    onEnterCategoryCount(currentViewSearch);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideKeyboard();
                loadPictogramIntoGridView();
            }
        });

        final EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        /*searchTerm.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                onUpdatedSearchField();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });*/
        GirafButton btnSearch = (GirafButton) findViewById(R.id.search_button);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                pictoGrid.setAdapter(new PictoAdapter(emptyList, getApplicationContext()));

                //boolean showAnimation = true;
                findViewById(R.id.progressLoader).setVisibility(View.VISIBLE);

                startingAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.main_activity_rotatelogo_once);
                loadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.main_activity_rotatelogo_infinite);

                if (progressLoad == 0){
                    startingAnimation.setDuration(2000);
                    findViewById(R.id.giraficon).startAnimation(startingAnimation);
                    progressLoad++;
                }
                else {
                    loadAnimation.setDuration(2000);
                    findViewById(R.id.giraficon).startAnimation(loadAnimation);
                }





                searchForPictogram(v);
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
     * Get the current child id if information is send by calling application
     * Otherwise the standard value of childId is -1 (invalid)
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
                Toast.makeText(getApplicationContext(), "Missing guardian ID, please fix ASAP", Toast.LENGTH_LONG).show();
                //throw new IllegalArgumentException("Need guardian ID")
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

    /**
     * Called when pressing search_button
     * Depending on search_field, search for pictoList in database
     *
     * @param view: This must be included for the function to work
     */
    public void searchForPictogram(View view) {
        //updateErrorMessage("", 0); // Reset purpose
        loadPictogramIntoGridView();
    }

    /**
     * Load all pictograms containing words from the searchString and display them.
     */
    private void loadPictogramIntoGridView() {
        Search searcher;

        if (citizenID != -1) {
            searcher = new Search(getApplicationContext(), citizenID, this);
        }
        else {
            searcher = new Search(getApplicationContext(), guardianID, this);
        }

        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        String searchString = searchTerm.getText().toString().toLowerCase().trim();

        searcher.execute(searchString);
    }

    // TODO Insert comment
    private void loadCategoryPictogramIntoGridView(ArrayList<Object> cpList) {
        hideKeyboard();
        pictoGrid.setAdapter(new PictoAdapter(cpList, getApplicationContext()));

    }

    // TODO Insert comment
    private void loadCategoriesIntoCategorySpinner() {
        CategoryController cController = new CategoryController(getApplicationContext());
        List<Category> catTemp;

        if (citizenID != -1) {
            catTemp = cController.getCategoriesByProfileId(citizenID);
        } else {
            catTemp = cController.getCategoriesByProfileId(guardianID);
        }


        ArrayList<String> catNames = new ArrayList<String>();

        if (searchList.isEmpty()) {
            for (Category c : catTemp) {
                catNames.add(c.getName());
            }
        }
        else {
            for (Object o : searchList) {
                if (o instanceof Category) {
                    catNames.add(((Category) o).getName());
                }
            }
        }
        Collections.sort(catNames, String.CASE_INSENSITIVE_ORDER); //Sorts in alphabetical order.
        catNames.add(0, getString(R.string.category_colon));

        Spinner catSpinner = (Spinner) findViewById(R.id.category_dropdown);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, catNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catSpinner.setAdapter(spinnerArrayAdapter);
    }

    // TODO insert comments
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
        //onUpdatedSearchField();
        loadPictogramIntoGridView();
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

    // TODO: comment this
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

    // TODO: comment this
    private int CountPictograms(ArrayList<Object> pTemp) {
        int count = 0;

        for (Object o : pTemp){
            if (o instanceof Pictogram){
                count++;
            }
        }

        return count;
    }

    // TODO: comment this
    private int CountCategories(ArrayList<Object> cTemp) {
        int count = 0;

        for (Object o : cTemp){
            if (o instanceof Category){
                count++;
            }
        }

        return count;
    }

    // TODO: comment this
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
     * @return return false if unable to open PictoCreator.
     */
    private boolean LaunchPictoCreator(boolean allow_error_msg) {
        try {
            Intent i = new Intent();
            i.setClassName(getString(R.string.set_class_name_pictoCreator), getString(R.string.set_class_name_pictoCreator_mainActivity));
            startActivity(i);
            return true;
        } catch (android.content.ActivityNotFoundException e) {
            if (allow_error_msg) {
                MessageDialogFragment message = new MessageDialogFragment(getString(R.string.unable_to_launch_pictoCreator));
                message.show(getFragmentManager(), getString(R.string.pictoCreator));
            }
            return false;
        }
    }

    /**
     * Open the application CategoryTool if the application is installed.
     * @param allow_error_msg boolean for allowing error messages to be displayed to user
     * @return return false if unable to open CategoryTool.
     */
    private boolean LaunchCategoryTool(boolean allow_error_msg) {
        Intent intent = new Intent();
        try {
            intent.setComponent(new ComponentName(getString(R.string.set_class_name_categoryTool), getString(R.string.set_class_name_categoryTool_mainActivity)));
            intent.putExtra(getString(R.string.current_child_id), citizenID);
            intent.putExtra(getString(R.string.current_guardian_id), guardianID);
            startActivity(intent);

            return true;
        } catch (android.content.ActivityNotFoundException e) {
            if (allow_error_msg) {
                MessageDialogFragment message = new MessageDialogFragment(getString(R.string.unable_to_launch_categoryTool));
                message.show(getFragmentManager(), getString(R.string.categoryTool));
            }
            return false;
        }
    }

    /**
     * Hide clearSearchFieldButton if no text has been entered
     */
    /*
    public void onUpdatedSearchField() {
        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        Editable s = searchTerm.getText();
    }
    */

    // TODO: comment this
    public void positionClicked(int position) {
        // if single pictogram requested, only one pictogram is displayed in checkout

        //Toast.makeText(this,position+"",Toast.LENGTH_SHORT).show();
        if (isSingle) {
            checkoutList.clear();
        }

        if (gridViewString.equals(getString(R.string.category_colon))) {
            checkoutList.add(searchTemp.get(position));
        }
        else {
            checkoutList.add(currentViewSearch.get(position));
        }

        onUpdatedCheckoutCount();
        checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
    }

    // TODO: comment this
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    // TODO: comment this
    @Override
    public void processFinish(ArrayList<Object> output) {
        searchList = output;
        searchTemp = searchList;
        findViewById(R.id.progressLoader).setVisibility(View.INVISIBLE);
        findViewById(R.id.giraficon).clearAnimation();

        pictoGrid.setAdapter(new PictoAdapter(searchList, getApplicationContext()));
        onSearchSummaryCount(searchList);
        loadCategoriesIntoCategorySpinner();
    }
}
