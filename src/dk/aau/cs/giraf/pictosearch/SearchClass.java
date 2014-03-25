package dk.aau.cs.giraf.pictosearch;

import dk.aau.cs.giraf.categorylib.PARROTCategory;
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
    public ArrayList<Object> DoSearch(String tag, String[] input)
    {
        // Vælg tag, resten skal tilføjes på et tidspunkt
        if (tag == "Tag") return DoSearch_Tags(input);
        else return DoSearch_All(input);
    }
    public ArrayList<Object> DoCategorySearch(String tag, String[] input)
    {
        return DoSearch_Category(input);
    }

    private ArrayList<Object> DoSearch_All(String[] input)
    {
        // Combines name and category, but make sure to avoid duplicates!!
    }

    private ArrayList<Object> DoSearch_Name(String[] input)
    {

    }

    private ArrayList<Object> DoSearch_Tags(String[] input)
    {

    }

    private ArrayList<Object> DoSearch_Category(String[] input)
    {

    }
}
