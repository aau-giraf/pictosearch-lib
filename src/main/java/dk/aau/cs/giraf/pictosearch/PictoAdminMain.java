package dk.aau.cs.giraf.pictosearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.jar.Attributes;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GButtonSearch;
import dk.aau.cs.giraf.gui.GButtonTrash;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GGridView;
import dk.aau.cs.giraf.gui.GSpinner;
import dk.aau.cs.giraf.gui.GVerifyButton;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramCategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.controllers.TagController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.PictogramCategory;
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
    private ArrayList<Tag> tagList = new ArrayList<Tag>();
	private ArrayList<Object> searchList = new ArrayList<Object>();
    private ArrayList<Object> searchTemp = new ArrayList<Object>();

	public GridView checkoutGrid;
	private GridView pictoGrid;
    private Spinner searchSpinner;
    private Pictogram pictoDelete = new Pictogram();
    private Category catDelete = new Category();
    private DeleteClass deleteClass = new DeleteClass(this);

	private String purpose;
    private SearchClass SearchClassInstance;
	
	/*
	 *  Request from another group. It should be possible to only send one pictogram,
	 *  and therefore only display one pictogram in the checkout list. isSingle is used
	 *  to store information. Default = false, so multiple pictoList are possible.
	 *  If the intent that started the search contain the extra "single", isSingle is set
	 *  to true
	 */
	private boolean isSingle = false;

    @Override
    public void addGirafButtonToActionBar(GirafButton girafButton, int side) {
        girafButton.setBackgroundResource(R.drawable.icon_help);
        super.addGirafButtonToActionBar(girafButton, LEFT);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picto_admin_main);
        findViewById(R.id.mainLinearLayout).setBackgroundDrawable(GComponent.GetBackground(GComponent.Background.GRADIENT));

        checkoutList = new ArrayList<Object>();
        pictoList = new ArrayList<Pictogram>();
        catList = new ArrayList<Category>();
        searchList = new ArrayList<Object>();
        searchTemp = new ArrayList<Object>();

        SearchClassInstance = new SearchClass(this);

        updateGuardianInfo();
		//getPurpose();
		getAllPictograms("");
        getAllCategories("");
        getAllTags("");
        onUpdatedCheckoutCount();
        onUpdatedSearchField();
        loadCategoriesIntoCategorySpinner();

		checkoutGrid = (GridView) findViewById(R.id.checkout);
		checkoutGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
				checkoutList.remove(position);
                onUpdatedCheckoutCount();
				checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
				return true;
			}
		});

		pictoGrid = (GridView) findViewById(R.id.pictogram_displayer);
        pictoGrid.setDrawingCacheEnabled(false);
		pictoGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				// if single pictogram requested, only one pictogram is displayed in checkout
				if(isSingle){
					checkoutList.clear();
				}
				checkoutList.add(searchList.get(position));
                onUpdatedCheckoutCount();
				checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
			}
		});
        pictoGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(searchList.get(position) instanceof Pictogram) {
                    pictoDelete = (Pictogram) searchList.get(position);
                    catDelete = null;
                }
                else if (searchList.get(position) instanceof Category)
                {
                    catDelete = (Category) searchList.get(position);
                    pictoDelete = null;
                }
                showDelete();
                return true;
            }
        });

        searchSpinner = (Spinner)findViewById(R.id.category_dropdown);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {



            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //ArrayList catPictoList = new ArrayList();

                String selectedItem = parent.getItemAtPosition(position).toString();

                //PictogramCategoryController pcController = new PictogramCategoryController(getApplicationContext());
                //List<PictogramCategory> pcTemp = pcController.getPictogramCategories();

                CategoryController cController = new CategoryController(getApplicationContext());
                List<Category> cTemp = cController.getCategories();

                Category cat = new Category();


                if (!selectedItem.equals(getString(R.string.category_colon))){
                    for (Category c : cTemp){
                        if (selectedItem.equals(c.getName())){
                            cat = c;
                        }
                    }

                }
                else {
                    //loadPictogramIntoGridView();
                }

                PictogramController pictogramController = new PictogramController(getApplicationContext());
                List<Pictogram> pTemp = pictogramController.getPictogramsByCategory(cat);

                ArrayList<Object> allList = new ArrayList<Object>();
                allList.addAll(pTemp);

                if (selectedItem.equals(getString(R.string.category_colon)))
                {
                    loadCategoryPictogramIntoGridView(searchTemp);
                }
                else {
                    loadCategoryPictogramIntoGridView(allList);
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

        /*
        GButtonTrash btnTrash = (GButtonTrash) findViewById(R.id.deleteCheckoutButton);
        btnTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCheckoutList(v);
            }
        });

        ImageButton clearSearch = (ImageButton) findViewById(R.id.clearSearchFieldButton);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearchField(v);
            }
        });
        */
        ImageButton btnSearch = (ImageButton) findViewById(R.id.search_button);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForPictogram(v);
            }
        });
        /*
        GVerifyButton btnVer = (GVerifyButton) findViewById(R.id.sendContentButton);
        btnVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendContent(v);
            }
        });
        */
	}



	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.picto_admin_main, menu);
		return true;
	}
	*/

	/**
	 * Override the function of the back button. Does the same as sendContent
	 */
    /*
	@Override
	public void onBackPressed() {
		sendContent(getCurrentFocus());
	}
	*/
	
	/**
	 * Get the current child id if information is send by calling application
	 * Otherwise the standard value of childId is -1 (invalid)
	 */
    private void updateGuardianInfo()
    {
        guardianInfo_ChildId = -1;
        if(getIntent().hasExtra(getString(R.string.current_child_id)))
            guardianInfo_ChildId = getIntent().getIntExtra(getString(R.string.current_child_id), -1);
    }

	public int getChildID()
    {
		return guardianInfo_ChildId;
	}
	
	/**
	 * Get the purpose from the calling application and displays a message to the user
	 * describing what to do in the application and how to finish
	 */

    /*
	private void getPurpose()
    {
        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);

		if(getIntent().hasExtra(getString(R.string.purpose))){
			if(getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.single))){
				isSingle = true;
                purpose = getString(R.string.choose_a_pictogram_press_ok);
			}
			else if(getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.multi))){
				isSingle = false;
				purpose = getString(R.string.choose_a_pictograms_press_ok);
			}
			else if(getIntent().getStringExtra(getString(R.string.purpose)).equals(getString(R.string.CAT))){
				purpose = getString(R.string.choose_a_pictograms_add_to_category_press_ok);
			}
            searchTerm.setHint(purpose);
		}
	}
	*/
	
	@SuppressWarnings("static-access")
	public ArrayList<Pictogram> getAllPictograms(String name) {
        if(name.isEmpty())
        {
            return new ArrayList<Pictogram>();
        }

        PictogramController pictogramController = new PictogramController(getApplicationContext());
        List<Pictogram> pictoTemp = pictogramController.getPictogramsByName(name);

        pictoList = new ArrayList<Pictogram>();

		for (Pictogram p : pictoTemp) {
			pictoList.add(p);
		}

        return pictoList;
	}

    public ArrayList<Category> getAllCategories(String name){

        int childID = getChildID();

        if (childID < 0 || name.isEmpty()){
            return new ArrayList<Category>();
        }

        CategoryController categoryController = new CategoryController(getApplicationContext());

        List<Category> catTemp = categoryController.getCategoriesByProfileId(childID);

        catList = new ArrayList<Category>();

        for (Category c : catTemp){
            if (c.getName().toLowerCase().contains(name)){
                catList.add(c);
            }
        }

        return catList;
    }

    private ArrayList<Tag> getAllTags(String tagCaption){
        tagList = new ArrayList<Tag>();

        if (tagCaption == null || tagCaption.isEmpty()){
            return new ArrayList<Tag>();
        }


        TagController tagController = new TagController(getApplicationContext());
        List<Tag> tagTemp = tagController.getTagsByCaption(tagCaption);

        for (Tag t : tagTemp){
            tagList.add(t);
        }

        return tagList;
    }

	/**
	 * Called when pressing search_button
	 * Depending on search_field, search for pictoList in database
	 * @param view: This must be included for the function to work
	 */
	public void searchForPictogram(View view){
		//updateErrorMessage("", 0); // Reset purpose
		loadPictogramIntoGridView();
        loadCategoriesIntoCategorySpinner();
	}
	


    /*
	private void updateErrorMessage(String message, int icon)
	{
		TextView  errorMessage = (TextView)  findViewById(R.id.errorMessage);
		ImageView errorIcon    = (ImageView) findViewById(R.id.errorIcon);
		
		errorMessage.setText(message);
		errorIcon.setImageResource(icon);
	}
	*/
	
	private void loadPictogramIntoGridView()
	{
        pictoGrid.setAdapter(null);
        searchList.clear();


        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        String searchString = searchTerm.getText().toString().toLowerCase();
        String[] splitInput = searchString.split("\\s");

        pictoList = getAllPictograms(searchString);
        catList = getAllCategories(searchString);
        tagList = getAllTags(searchString);

        if (SearchClassInstance != null)
        {
            ArrayList<Object> allList = new ArrayList<Object>();
            allList.addAll(pictoList);
            allList.addAll(catList);
            allList.addAll(tagList);

            ArrayList<Object> searchList = SearchClassInstance.DoSearch(splitInput, allList);
            for (Object o : searchList)
            {
                this.searchList.add(o);
                if(this.searchList.size() >= 48)
                {
                    break;
                }
            }
        }
        searchTemp = searchList;

		if(searchList.size() > 0){
            pictoGrid.setAdapter(new PictoAdapter(searchList, this));
		}
		else{
			//updateErrorMessage(getString(R.string.pictogram_do_not_exist_in_datebase), R.drawable.action_about);
            pictoGrid.setAdapter(new PictoAdapter(searchList, this));
		}
	}


    private void loadCategoryPictogramIntoGridView(ArrayList cpList)
    {
        pictoGrid.setAdapter(null);
        pictoGrid.setAdapter(new PictoAdapter(cpList, this));

    }


    private void loadCategoriesIntoCategorySpinner()
    {

        int childID = getChildID();
        EditText tempText = (EditText) findViewById(R.id.text_search_input);
        String tempString = tempText.getText().toString().toLowerCase();

        CategoryController cController = new CategoryController(getApplicationContext());
        List<Category> catTemp = cController.getCategoriesByProfileId(childID);

        ArrayList<String> catNames = new ArrayList<String>();
        catList = getAllCategories(tempString);
        catNames.add(getString(R.string.category_colon));


        if (catList.isEmpty() && tempString.equals("")){
            for (Category c : catTemp){
                catNames.add(c.getName());
            }
        }
        else if (catList.isEmpty()){}

        else {
            for (Category b : catList) {
                catNames.add(b.getName());
            }
        }

        Spinner catspinner = (Spinner) findViewById(R.id.category_dropdown);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, catNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catspinner.setAdapter(spinnerArrayAdapter);



        PictogramCategoryController pcController = new PictogramCategoryController(getApplicationContext());




    }




    private boolean searchMatcher(String pictoname, String searchinput) {
		// Made so that it is possible to make search function more intelligent
		
		if(pictoname.contains(searchinput)) {
			return true;
		} 
		else {
			return false;
		}
	}
	
	// Used in loadPictogramIntoGridview to
	//TODO: INSERT description Jacob
	private static int calculateValueOfPictogram(Pictogram p, String[] searchTerm) {
    	int searchValue = 0;
    	
    	for(String s : searchTerm){
    		s.toLowerCase().replaceAll("\\s", "");
    		
    		if(p.getName().toLowerCase().replaceAll("\\s", "").equals(s)){
    			searchValue = 100;
    		}
    		
    		String temps = s;
    		
    		for(int i = 0; i < s.length(); i++){
    			if(p.getName().toLowerCase().replaceAll("\\s", "").contains(temps) || temps.contains(p.getName().toLowerCase().replaceAll("\\s", ""))){
    				searchValue++;
    				}
    			
    			temps = temps.substring(0, temps.length() - 1);
    		}
    	}
    	
    	return searchValue;
    }

    private Object[] getCheckoutObjectsArray()
    {
        ArrayList<Object> r = getCheckoutObjects();
        Object[] checkout = new Object[r.size()];
        int i = 0;
        for (Object o: r)
        {
            checkout[i] = o;
            i++;
        }
        return checkout;
    }

    private ArrayList<Object> getCheckoutObjects() {
        ArrayList<Object> r = new ArrayList<Object>();

        for(Object o : checkoutList)
        {
            if (o instanceof Pictogram)
            {
                Pictogram p = (Pictogram)o;
                r.add(p);
            }
            else if (o instanceof Category)
            {
                Category c = (Category)o;
                r.add(c);
            }
        }
        return r;
    }

	/**
	 * Assess the checkout gridview and load the pictograms into an ArrayList
	 * @return ArrayList of checkout pictograms
	 */
    private int[] getCheckoutPictogramIDsArray()
    {
        ArrayList<Integer> plist = getCheckoutPictogramIDs();
        int[] checkout = new int[plist.size()];
        int i = 0;
        for (int j : plist)
        {
            checkout[i] = j;
            i++;
        }
        return checkout;
    }
    private ArrayList<Integer> getCheckoutPictogramIDs()
    {
        ArrayList<Integer> Result = new ArrayList<Integer>();
        ArrayList<Object> plist = getCheckoutObjects();

        PictogramController pictogramController = new PictogramController(this);
        CategoryController categoryController = new CategoryController(this);

		for(Object o : plist)
        {
            if (o instanceof Pictogram){
                Pictogram p = (Pictogram)o;
                Result.add(p.getId());
            }
            else if (o instanceof Category){
                Category catNew = (Category)o;

                List<Pictogram> pictogramsInCategory = pictogramController.getPictogramsByCategory(catNew);

                for (Pictogram p : pictogramsInCategory){
                    if (p != null){
                        Result.add(p.getId());
                    }
                }
            }
		}

		return Result;
	}
	
	public void clearSearchField(View view) {
		EditText searchField = (EditText) findViewById(R.id.text_search_input);
		searchField.setText(null);
        //onUpdatedSearchField();
        loadPictogramIntoGridView();
	}
	
	public void clearCheckoutList(View view) {
		checkoutList.clear();
        onUpdatedCheckoutCount();
		checkoutGrid.setAdapter(new PictoAdapter(checkoutList, this));
	}
	
	/**
	 * MenuItem: Sends pictogram ids from checkoutlist to appropriate calling application 
	 */
	public void sendContent(View view) {
		int[] output = getCheckoutPictogramIDsArray();
		Intent data = this.getIntent();

		data.putExtra(getString(R.string.checkout_ids), output);

		if(getParent() == null) {
			setResult(Activity.RESULT_OK, data);
		}
		else {
			getParent().setResult(Activity.RESULT_OK, data);
		}
		finish();
	}

    private boolean LaunchPictoCreator(boolean allow_error_msg)
    {
        try
        {
            Intent i = new Intent();
            i.setClassName(getString(R.string.set_class_name_1), getString(R.string.set_class_name_2));
            startActivity(i);
            return true;
        }
        catch (android.content.ActivityNotFoundException e)
        {
            if (allow_error_msg)
            {
                MessageDialogFragment message = new MessageDialogFragment(getString(R.string.unable_to_launch));
                message.show(getFragmentManager(), getString(R.string.pictocreator));
            }
            return false;
        }
    }
	
	public void gotoCroc(View view){
        LaunchPictoCreator(true);
	}

    public void optionsGoToCroc(MenuItem item) {
        LaunchPictoCreator(true);
    }

	public void callAndersSupport(MenuItem item) {
		MessageDialogFragment message = new MessageDialogFragment(getString(R.string.support_number));
		message.show(getFragmentManager(), getString(R.string.call_tech_support));
	}

    public void onUpdatedCheckoutCount()
    {
        TextView  categoryBox = (TextView)  findViewById(R.id.categorySum);
        categoryBox.setText(getString(R.string.category_colon) + checkoutList.size());

        TextView  pictogramBox = (TextView)  findViewById(R.id.pictogramSum);
        pictogramBox.setText(getString(R.string.pictogram_colon) + checkoutList.size());
    }


    public void onUpdatedSearchField()
    {
        EditText searchTerm = (EditText) findViewById(R.id.text_search_input);
        Editable s = searchTerm.getText();
    }


    public void showDelete()
    {
        GDialogMessage deleteDialog = new GDialogMessage(this,
                getString(R.string.delete_pictogram),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       if(pictoDelete != null) {
                           deleteClass.PictoDelete(view.getContext(), pictoDelete);
                           getAllPictograms("");
                           }
                       else
                       {
                           deleteClass.CategoryDelete(view.getContext(), catDelete);
                           getAllCategories("");
                       }
                       loadPictogramIntoGridView();
                    }
                }
        );

        deleteDialog.show();
    }


}