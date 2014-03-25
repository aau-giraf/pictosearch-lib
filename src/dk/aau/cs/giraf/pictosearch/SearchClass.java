package dk.aau.cs.giraf.pictosearch;

import dk.aau.cs.giraf.pictogram.Pictogram;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Tobias on 25-03-14.
 */
public class SearchClass
{
    public ArrayList<Pictogram> DoSearch(String tag, String input)
    {
        // Vælg tag, resten skal tilføjes på et tidspunkt
        if (tag == "All")DoSearch_All(input);
        else if (tag == "Pictogram") return DoSearch_Pictogram(input);
        else if (tag == "Category") return DoSearch_Category(input);
        else if (tag == "Subcategory") return DoSearch_SubCategory(input);
    }
    private ArrayList<Pictogram> DoSearch_All(String input)
    {

    }

    private ArrayList<Pictogram> DoSearch_Pictogram(String input)
    {

    }

    private ArrayList<Pictogram> DoSearch_Category(String input)
    {

    }

    private ArrayList<Pictogram> DoSearch_SubCategory(String input)
    {

    }
}
