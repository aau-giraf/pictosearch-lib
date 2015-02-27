package dk.aau.cs.giraf.pictosearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GButtonSearch;
import dk.aau.cs.giraf.gui.GButtonTrash;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GGridView;
import dk.aau.cs.giraf.gui.GSpinner;
import dk.aau.cs.giraf.gui.GVerifyButton;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * @author SW605f13 Parrot-group
 * The main class in PictoSearch. Contains almost all methods relating to search.
 */
public class PictoAdminMain extends Activity {
	private int    guardianInfo_ChildId = -1;

	public ArrayList<Object> checkoutList = new ArrayList<Object>();
	private ArrayList<Pictogram> pictoList    = new ArrayList<Pictogram>();
    private ArrayList<Category> catList    = new ArrayList<Category>();
	private ArrayList<Object> searchlist   = new ArrayList<Object>();
	
	public GGridView checkoutGrid;
	private GGridView pictoGrid;
    private GSpinner searchspinner;
    private Pictogram pictodelete = new Pictogram();
    private Category catdelete = new Category(); // 
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picto_admin_main);
        findViewById(R.id.mainLinearLayout).setBackgroundDrawable(GComponent.GetBackground(GComponent.Background.GRADIENT));
       /* View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_picto_admin_main, null);

        //Set the background
        v.setBackgroundDrawable(GComponent.GetBackground(GComponent.Background.GRADIENT));

        */

        checkoutList = new ArrayList<Object>();
        pictoList    = new ArrayList<Pictogram>();
        catList    = new ArrayList<Category>();
        searchlist   = new ArrayList<Object>();

		//catHelp = new CategoryHelper(this);
        SearchClassInstance = new SearchClass(this);

        updateGuardianInfo();
		getPurpose();
		getAllPictograms("");
        getAllCategories();
        onUpdatedCheckoutCount();
        onUpdatedSearchField();

		checkoutGrid = (GGridView) findViewById(R.id.checkout);
		checkoutGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
				checkoutList.remove(position);
                onUpdatedCheckoutCount();
				checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
				return true;
			}
		});

		pictoGrid = (GGridView) findViewById(R.id.pictogram_displayer);
        pictoGrid.setDrawingCacheEnabled(false);
		pictoGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				// if single pictogram requested, only one pictogram is displayed in checkout
				if(isSingle){
					checkoutList.clear();
				}
				checkoutList.add(searchlist.get(position));
                onUpdatedCheckoutCount();
				checkoutGrid.setAdapter(new PictoAdapter(checkoutList, getApplicationContext()));
			}
		});
        pictoGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(searchlist.get(position) instanceof Pictogram) {
                    pictodelete = (Pictogram) searchlist.get(position);
                    catdelete = null;
                }
                else if (searchlist.get(position) instanceof Category)
                {
                    catdelete = (Category) searchlist.get(position);
                    pictodelete = null;
                }
                showDelete();
                return true;
            }
        });

        searchspinner = (GSpinner)findViewById(R.id.select_search_field);
        searchspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPictogramIntoGridView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadPictogramIntoGridView();
            }
        });

        loadPictogramIntoGridView();

        EditText searchterm = (EditText) findViewById(R.id.text_input);
        searchterm.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                onUpdatedSearchField();
            }
            public void  beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void  onTextChanged (CharSequence s, int start, int before,int count) {}
        });

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

        GButtonSearch btnSearch = (GButtonSearch) findViewById(R.id.search_button);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForPictogram(v);
            }
        });

        GVerifyButton btnVer = (GVerifyButton) findViewById(R.id.sendContentButton);
        btnVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendContent(v);
            }
        });
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.picto_admin_main, menu);
		return true;
	}

	/**
	 * Override the function of the back button. Does the same as sendContent
	 */
	@Override
	public void onBackPressed() {
		sendContent(getCurrentFocus());
	}
	
	/**
	 * Get the current child id if information is send by calling application
	 * Otherwise the standard value of childId is -1 (invalid)
	 */
    private void updateGuardianInfo()
    {
        guardianInfo_ChildId = -1;
        if(getIntent().hasExtra("currentChildID"))
            guardianInfo_ChildId = getIntent().getIntExtra("currentChildID", -1);
    }

	public int getChildID()
    {
		return guardianInfo_ChildId;
	}
	
	/**
	 * Get the purpose from the calling application and displays a message to the user
	 * describing what to do in the application and how to finish
	 */
	private void getPurpose()
    {
        EditText searchterm = (EditText) findViewById(R.id.text_input);

		if(getIntent().hasExtra("purpose")){
			if(getIntent().getStringExtra("purpose").equals("single")){
				isSingle = true;
                purpose = "Vælg et pictogram og klik OK!";
			}
			else if(getIntent().getStringExtra("purpose").equals("multi")){
				isSingle = false;
				purpose = "Vælg pictogrammer og klik OK!";
			}
			else if(getIntent().getStringExtra("purpose").equals("CAT")){
				purpose = "Vælg pictogrammer, som skal tilføjes til kategori og klik OK!";
			}
            searchterm.setHint(purpose);
			//updateErrorMessage(purpose, 0);
		}
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("static-access")
	public ArrayList<Pictogram> getAllPictograms(String name) {
		//List<Pictogram> pictotemp = PictoFactory.INSTANCE.getAllPictograms(getApplicationContext());
        if(name.isEmpty() || name == null)
        {
            return new ArrayList<Pictogram>();
        }
        PictogramController pictogramController = new PictogramController(this);
        List<Pictogram> pictotemp = pictogramController.getPictogramsByName(name);

        pictoList = new ArrayList<Pictogram>();

		for (Pictogram p : pictotemp) {
			pictoList.add(p);
		}

        return pictoList;
	}

    public  ArrayList<Category> getAllCategories()
    {
        List<Category> cattemp = new ArrayList<Category>();
        catList = new ArrayList<Category>();

        int childId = getChildID();
        if (childId < 0) return catList; // If no child, return empty

        CategoryController categoryController = new CategoryController(getApplicationContext());
        //cattemp = categoryController.getCategoriesByProfileId(childId);

        try
        {
            cattemp = categoryController.getCategoriesByProfileId(childId);
        }
        catch (java.lang.NullPointerException e)
        {
            System.out.println("Exception: " + e + ", fix nu!!!!!!");
        }

        for (Category pc : cattemp) {
            catList.add(pc);
        }
        return catList;
    }
	
	/**
	 * Called when pressing search_button
	 * Depending on search_field, search for pictoList in database
	 * @param view: This must be included for the function to work
	 */
	public void searchForPictogram(View view){
		updateErrorMessage("", 0); // Reset purpose
		loadPictogramIntoGridView();
	}
	
	/**
	 * Updates the errorMessage with appropriate error
	 * @param message: Message to be displayed, null = clear
	 * @param icon: get icon from R.drawable
	 */
	private void updateErrorMessage(String message, int icon)
	{
		TextView  errorMessage = (TextView)  findViewById(R.id.errorMessage);
		ImageView errorIcon    = (ImageView) findViewById(R.id.errorIcon);
		
		errorMessage.setText(message);
		errorIcon.setImageResource(icon);
	}
	
	/**
	 * Loads the pictoList into the gridview depending on the search tag
	 *  String identifying whether the user searches for tags, name,
	 * category, sub-category or color
	 */
    private void loadPictogramIntoGridView()
    {
        Spinner searchField = (Spinner)findViewById(R.id.select_search_field);
        String  selectedTag =  searchField.getSelectedItem().toString();
        loadPictogramIntoGridView(selectedTag);
    }
	private void loadPictogramIntoGridView(String tag)
	{
        pictoGrid.setAdapter(null);
        searchlist.clear();

		EditText searchterm = (EditText) findViewById(R.id.text_input);
        String searchstring = searchterm.getText().toString().toLowerCase().replaceAll("\\s", "");
		String[] splitinput = searchstring.split(",");

        pictoList = getAllPictograms(searchstring);

        if (SearchClassInstance != null)
        {
            ArrayList<Object> allList = new ArrayList<Object>();
            allList.addAll(pictoList);
            allList.addAll(catList);

            ArrayList<Object> slist = SearchClassInstance.DoSearch(tag, splitinput, allList);
            for (Object o : slist)
            {
                searchlist.add(o);
                if(searchlist.size() >= 48)
                {
                    break;
                }
            }
        }

		if(searchlist.size() > 0){
            pictoGrid.setAdapter(new PictoAdapter(searchlist, this));
		}
		else{
			updateErrorMessage("Pictogram findes ikke i database", R.drawable.action_about);
            pictoGrid.setAdapter(new PictoAdapter(searchlist, this));
		}
	}

    private boolean searchMatcher(String pictoname, String searchinput) {
		// Mulighed for at g�re s�gefunktionen endnu mere intelligent
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
	private static int calculateValueOfPictogram(Pictogram p, String[] searchterm) {
    	int searchvalue = 0;
    	
    	for(String s : searchterm){
    		s.toLowerCase().replaceAll("\\s", "");
    		
    		if(p.getName().toLowerCase().replaceAll("\\s", "").equals(s)){
    			searchvalue = 100;
    		}
    		
    		String temps = s;
    		
    		for(int i = 0; i < s.length(); i++){
    			if(p.getName().toLowerCase().replaceAll("\\s", "").contains(temps) || temps.contains(p.getName().toLowerCase().replaceAll("\\s", ""))){
    				searchvalue++;
    				}
    			
    			temps = temps.substring(0, temps.length() - 1);
    		}
    	}
    	
    	return searchvalue;
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
            if (o instanceof Pictogram)
            {
                Pictogram p = (Pictogram)o;
                Result.add(p.getId());
            }
            else if (o instanceof Category)
            {
                // TODO: Open up category and get pictogram ids
                Category catNew = (Category)o;
                for (Pictogram p : pictogramController.getPictogramsByCategory(catNew))
                    Result.add(p.getId());

                for (Category c : categoryController.getSubcategoriesByCategory(catNew))
                {
                    if (c == null) continue;
                    for (Pictogram p : pictogramController.getPictogramsByCategory(c))
                        Result.add(p.getId());
                }
            }

		}
		
		return Result;
	}
	
	public void clearSearchField(View view) {
		EditText searchField = (EditText) findViewById(R.id.text_input);
		searchField.setText(null);
        onUpdatedSearchField();
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

		data.putExtra("checkoutIds", output);
        //Java does not like parsing obejcts
        //data.putExtra("checkoutObjects", output_objects);
		
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
            i.setClassName("dk.aau.cs.giraf.pictocreator", "dk.aau.cs.giraf.pictocreator.MainActivity");
            startActivity(i);
            return true;
        }
        catch (android.content.ActivityNotFoundException e)
        {
            if (allow_error_msg)
            {
                MessageDialogFragment message = new MessageDialogFragment("Unable to launch PictoCreator, make sure its installed and up to date!");
                message.show(getFragmentManager(), "PictoCreator");
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
		MessageDialogFragment message = new MessageDialogFragment("Call: +45 24 26 93 98 for tech support");
		message.show(getFragmentManager(), "callTechSupport");
	}

    public void onUpdatedCheckoutCount()
    {
        TextView  messageBox = (TextView)  findViewById(R.id.textView1);
        messageBox.setText("Valg: " + checkoutList.size());
    }

    public void onUpdatedSearchField()
    {
        EditText searchterm = (EditText) findViewById(R.id.text_input);
        //String searchtext = searchterm.getText().toString();
        Editable s = searchterm.getText();
        View clearButton = findViewById(R.id.clearSearchFieldButton);
        if (s != null && s.length() > 0)
            clearButton.setVisibility(View.VISIBLE);
        else
            clearButton.setVisibility(View.INVISIBLE);
    }

    public void showDelete()
    {
        GDialogMessage deleteDialog = new GDialogMessage(this,
                "Vil du slette det valgte pictogram?",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       if(pictodelete != null) {
                           deleteClass.PictoDelete(view.getContext(), pictodelete);
                           getAllPictograms("");
                           }
                       else
                       {
                           deleteClass.CategoryDelete(view.getContext(), catdelete);
                           getAllCategories();
                       }
                       loadPictogramIntoGridView();
                    }
                }
        );

        deleteDialog.show();
    }
}