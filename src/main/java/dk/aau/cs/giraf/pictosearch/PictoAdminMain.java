package dk.aau.cs.giraf.pictosearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.Tag;

/**
 * @author SW605f13 Parrot-group
 * The main class in PictoSearch. Contains almost all methods relating to search.
 */
public class PictoAdminMain extends GirafActivity {
    private int guardianInfo_ChildId = -1;

    public ArrayList<Object> checkoutList = new ArrayList<Object>();
    private ArrayList<Pictogram> pictoList = new ArrayList<Pictogram>();
    private ArrayList<Category> catList = new ArrayList<Category>();
    private ArrayList<Object> searchList = new ArrayList<Object>();
    private ArrayList<Object> searchTemp = new ArrayList<Object>();
    private ArrayList<Object> currentViewSearch = new ArrayList<Object>();
    private String gridViewString;

    public GridView checkoutGrid;
    private GridView pictoGrid;

    private String purpose;

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
        setContentView(R.layout.activity_picto_admin_main);
        findViewById(R.id.mainLinearLayout).setBackgroundDrawable(GComponent.GetBackground(GComponent.Background.GRADIENT));

        // Actionbar buttons created
        GirafButton help = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_help));
        GirafButton accept = new GirafButton(this, this.getResources().getDrawable(R.drawable.icon_accept));
        GirafButton categoryTool = new GirafButton(this, this.getResources().getDrawable(R.drawable.collections_view_as_list));
        GirafButton pictoCreatorTool = new GirafButton(this, this.getResources().getDrawable(R.drawable.croc_icon));

        // Example of an onclicklistener
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PictoAdminMain.this,"Hjælp kommer snarest muligt.",Toast.LENGTH_SHORT).show();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendContent(getCurrentFocus());
            }
        });
        categoryTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchCategoryTool(true);
            }
        });
        pictoCreatorTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchPictoCreator(true);
            }
        });

        //Giraf buttons added to actionbar - order is left to right. backButton is always leftmost
        addGirafButtonToActionBar(help, LEFT);
        addGirafButtonToActionBar(accept, LEFT);
        addGirafButtonToActionBar(categoryTool, RIGHT);
        addGirafButtonToActionBar(pictoCreatorTool, RIGHT);

        checkoutList = new ArrayList<Object>();
        pictoList = new ArrayList<Pictogram>();
        catList = new ArrayList<Category>();
        searchList = new ArrayList<Object>();
        searchTemp = new ArrayList<Object>();
        currentViewSearch = new ArrayList<Object>();

        updateGuardianInfo();
        getPurpose();
        onUpdatedCheckoutCount();
        onUpdatedSearchField();
        loadCategoriesIntoCategorySpinner();

        checkoutGrid = (GridView) findViewById(R.id.checkout);
        checkoutGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                checkoutList.remove(position);
                onUpdatedCheckoutCount();
                checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
            }
        });

        pictoGrid = (GridView) findViewById(R.id.pictogram_displayer);
        pictoGrid.setDrawingCacheEnabled(false);
        pictoGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                // if single pictogram requested, only one pictogram is displayed in checkout
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
        });

        Spinner searchSpinner = (Spinner) findViewById(R.id.category_dropdown);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                CategoryController cController = new CategoryController(getApplicationContext());
                List<Category> cTemp = cController.getCategories();

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
                    loadCategoryPictogramIntoGridView(searchTemp);
                } else {
                    loadCategoryPictogramIntoGridView(currentViewSearch);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadPictogramIntoGridView();
            }
        });

        loadPictogramIntoGridView();

        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        searchTerm.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                onUpdatedSearchField();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        ImageButton btnSearch = (ImageButton) findViewById(R.id.search_button);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForPictogram(v);
            }
        });
    }

    /**
     * Get the current child id if information is send by calling application
     * Otherwise the standard value of childId is -1 (invalid)
     */
    private void updateGuardianInfo() {
        guardianInfo_ChildId = -1;
        if (getIntent().hasExtra(getString(R.string.current_child_id)))
            guardianInfo_ChildId = getIntent().getIntExtra(getString(R.string.current_child_id), -1);
    }

    // TODO Insert comment
    public int getChildID() {
        return guardianInfo_ChildId;
    }

    /**
     * Get the purpose from the calling application and displays a message to the user
     * describing what to do in the application and how to finish
     */
	private void getPurpose()
    {
        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);

		if(getIntent().hasExtra(getString(R.string.purpose))) {
			if(getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.single))) {
				isSingle = true;
                purpose = getString(R.string.choose_a_pictogram_press_ok);
			}
			else if(getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.multi))) {
				isSingle = false;
				purpose = getString(R.string.choose_a_pictograms_press_ok);
			}
			else if(getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.CAT))) {
				purpose = getString(R.string.choose_a_pictograms_add_to_category_press_ok);
			}
            searchTerm.setHint(purpose);
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
        loadCategoriesIntoCategorySpinner();
    }

    /**
     * load all pictograms containing words from the searchString and display a limited amount of
     * them.
     */
    private void loadPictogramIntoGridView() {
        pictoGrid.setAdapter(null);
        searchList.clear();
        Search searcher = new Search(getApplicationContext());

        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        String searchString = searchTerm.getText().toString().toLowerCase().trim();
        String[] splitInput = searchString.split("\\s+");

        pictoList = searcher.getAllPictograms(splitInput);
        catList = searcher.getAllCategories(splitInput, getChildID());
        ArrayList<Tag> tagList = searcher.getAllTags(splitInput);

        ArrayList<Pictogram> pictogramsByTags =  searcher.getPictogramByTags(splitInput, tagList);


        ArrayList<Object> allList = new ArrayList<Object>();
        allList.addAll(pictoList);
        allList.addAll(catList);
        allList.addAll(pictogramsByTags);

        allList = searcher.SortPictogramsAndCategories(allList, searchString, splitInput);

        for (Object o : allList) {
            this.searchList.add(o);
            if(this.searchList.size() >= 48) {
                break;
            }
        }

        searchTemp = searchList;


        if(searchList.size() > 0) {
            pictoGrid.setAdapter(new PictoAdapter(searchList, this));
        }
        else {
            //updateErrorMessage(getString(R.string.pictogram_do_not_exist_in_datebase), R.drawable.action_about);
            pictoGrid.setAdapter(new PictoAdapter(searchList, this));
        }
    }

    // TODO Insert comment
    private void loadCategoryPictogramIntoGridView(ArrayList<Object> cpList) {
        pictoGrid.setAdapter(null);
        pictoGrid.setAdapter(new PictoAdapter(cpList, this));

    }

    // TODO Insert comment
    private void loadCategoriesIntoCategorySpinner() {
        //TODO Sort the list of categories in alphabetical order.
        int childID = getChildID();

        CategoryController cController = new CategoryController(getApplicationContext());
        List<Category> catTemp = cController.getCategoriesByProfileId(childID);

        ArrayList<String> catNames = new ArrayList<String>();
        catNames.add(getString(R.string.category_colon));


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
    private int[] getCheckoutPictogramIDsArray() {
        ArrayList<Integer> pictogramIDs = getCheckoutPictogramIDs();
        int[] checkout = new int[pictogramIDs.size()];
        int i = 0;
        for (int p : pictogramIDs) {
            checkout[i] = p;
            i++;
        }
        return checkout;
    }

    /**
     * get the pictogram IDs from all checkout items
     * @return pictogram ID of all pictograms and pictograms in the categories in the checkout list
     */
    private ArrayList<Integer> getCheckoutPictogramIDs() {
        ArrayList<Integer> pictogramCheckoutIDs = new ArrayList<Integer>();
        ArrayList<Object> checkoutObjects = getCheckoutObjects();

        PictogramController pictogramController = new PictogramController(this);

        for(Object o : checkoutObjects) {
            if (o instanceof Pictogram) {
                Pictogram p = (Pictogram)o;
                pictogramCheckoutIDs.add(p.getId());
            }
            else if (o instanceof Category) {
                Category catNew = (Category)o;

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
        int[] output = getCheckoutPictogramIDsArray();
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
        try {
            Intent i = new Intent();
            i.setClassName(getString(R.string.set_class_name_categoryTool), getString(R.string.set_class_name_categoryTool_mainActivity));
            startActivity(i);
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
    public void onUpdatedSearchField() {
        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        Editable s = searchTerm.getText();
    }
}
