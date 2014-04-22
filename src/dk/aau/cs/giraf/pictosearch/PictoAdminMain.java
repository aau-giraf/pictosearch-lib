package dk.aau.cs.giraf.pictosearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import dk.aau.cs.giraf.categorylib.CatLibHelper;
import dk.aau.cs.giraf.oasis.lib.controllers.CategoryController;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.oasis.lib.models.Category;

/**
 * @author SW605f13 Parrot-group
 * The main class in PictoSearch. Contains almost all methods relating to search.
 */
public class PictoAdminMain extends Activity {
	private int    childId = 12;

	private ArrayList<Object> checkoutList = new ArrayList<Object>();
	private ArrayList<Pictogram> pictoList    = new ArrayList<Pictogram>();
    private ArrayList<Category> catList    = new ArrayList<Category>();
	private ArrayList<Object> searchlist   = new ArrayList<Object>();
	
	private GridView checkoutGrid;
	private GridView pictoGrid;
	
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
		
		//catHelp = new CategoryHelper(this);
        SearchClassInstance = new SearchClass(this);
		
		getProfile();
		getPurpose();
		getAllPictograms();
        getAllCategories();
        onUpdatedCheckoutCount();
        onUpdatedSearchField();
		
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

        loadPictogramIntoGridView();

        EditText searchterm = (EditText) findViewById(R.id.text_input);
        searchterm.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                onUpdatedSearchField();
            }
            public void  beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void  onTextChanged (CharSequence s, int start, int before,int count) {}
        });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.picto_admin_main, menu);
		return true;
	}
	
	/**
	 * Makes sure, that when the application is suspended it is instead destroyed
	 */
	@Override
	protected void onPause() {
		super.onDestroy();
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
	 * Otherwise the standard value of childId is 12
	 */
	public void getProfile() {
		if(getIntent().hasExtra("currentChildID")){
			childId = getIntent().getIntExtra("currentChildID", -1);
		}
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
	public ArrayList<Pictogram> getAllPictograms() {
		List<Pictogram> pictotemp = PictoFactory.INSTANCE.getAllPictograms(getApplicationContext());

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
        if (childId < 0) return catList; // If no child, return empty

        //cattemp = catHelp.getChildsCategories(childId);
        CategoryController categoryController = new CategoryController(getApplicationContext());
        cattemp = categoryController.getCategoriesByProfileId(childId);

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
		GridView picgrid = (GridView) findViewById(R.id.pictogram_displayer);		
		EditText searchterm = (EditText) findViewById(R.id.text_input);
        String searchstring = searchterm.getText().toString().toLowerCase().replaceAll("\\s", "");
		String[] splitinput = searchstring.split(",");
		
        searchlist.clear();
        if (SearchClassInstance != null)
        {
            for (Object o : SearchClassInstance.DoSearch(tag, splitinput, pictoList))
            {
                if (o instanceof Pictogram)
                    searchlist.add(o);
            }
        }

		if(searchlist.size() > 0){
			picgrid.setAdapter(new PictoAdapter(searchlist, this));
		}
		else{
			updateErrorMessage("Pictogram findes ikke i database", R.drawable.action_about);
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
    		
    		if(p.getTextLabel().toLowerCase().replaceAll("\\s", "").equals(s)){
    			searchvalue = 100;
    		}
    		
    		String temps = s;
    		
    		for(int i = 0; i < s.length(); i++){
    			if(p.getTextLabel().toLowerCase().replaceAll("\\s", "").contains(temps) || temps.contains(p.getTextLabel().toLowerCase().replaceAll("\\s", ""))){
    				searchvalue++;
    				}
    			
    			temps = temps.substring(0, temps.length() - 1);
    		}
    	}
    	
    	return searchvalue;
    }

    private ArrayList<Pictogram> getCheckoutPictograms() {
        ArrayList<Pictogram> r = new ArrayList<Pictogram>();

        for(Object o : checkoutList)
        {
            if (o instanceof Pictogram)
            {
                Pictogram p = (Pictogram)o;
                r.add(p);
            }
            /*else if (o instanceof PARROTCategory)
            {
                PARROTCategory c = (PARROTCategory)o;
                r.addAll(c.getPictograms());
            }*/
        }
        return r;
    }
	
	/**
	 * Assess the checkout gridview and load the pictograms into an ArrayList
	 * @return ArrayList of checkout pictograms
	 */
	private long[] getCheckoutList()
    {
        ArrayList<Pictogram> plist = getCheckoutPictograms();
		long[] checkout = new long[plist.size()];
		int i = 0;
		
		for(Pictogram p : plist)
        {
            checkout[i] = p.getPictogramID();
			i++;
		}
		
		return checkout;
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
		long[] output = getCheckoutList();
		Intent data = this.getIntent();
		data.putExtra("checkoutIds", output);
		
		if(getParent() == null) {
			setResult(Activity.RESULT_OK, data);
		}
		else {
			getParent().setResult(Activity.RESULT_OK, data);
		}
		finish();
	}
	
	public void gotoCroc(View view){
		Intent croc = new Intent();
		croc.setClassName("dk.aau.cs.giraf.pictocreator", "dk.aau.cs.giraf.pictocreator.CrocActivity");
		startActivity(croc);
	}

    public void optionsGoToCroc(MenuItem item) {
        Intent croc = new Intent();
        croc.setClassName("dk.aau.cs.giraf.pictocreator", "dk.aau.cs.giraf.pictocreator.CrocActivity");
        startActivity(croc);
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
}