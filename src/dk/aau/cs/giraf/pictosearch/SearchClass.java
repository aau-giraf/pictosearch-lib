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
    public ArrayList<Object> DoSearch(String tag, String[] input, ArrayList<Pictogram> AllPictograms)
    {
        // Vælg tag, resten skal tilføjes på et tidspunkt
        if (tag == "Tag") return DoSearch_Tags(input, AllPictograms);
        else return DoSearch_All(input, AllPictograms);
    }
    public ArrayList<Object> DoCategorySearch(String tag, String[] input, ArrayList<Pictogram> AllPictograms)
    {
        return DoSearch_Category(input, AllPictograms);
    }

    // PICTOGRAM IMPLEMENTATIONS

    private ArrayList<Object> DoSearch_All(String[] input, ArrayList<Pictogram> AllPictograms)
    {
        // Combines name and category, but make sure to avoid duplicates!!
    }

    private ArrayList<Object> DoSearch_Name(String[] input, ArrayList<Pictogram> AllPictograms)
    {
        ArrayList<Object> lst = new ArrayList<Object>();
        for (Pictogram p : AllPictograms)
        {
            for(int i = 0; i < input.length; i++)
            {
                if (p.getTextLabel().toLowerCase().contains(input[i]))
                {
                    lst.add(p);
                    break;
                }
            }
        }
        return lst;
    }

    private ArrayList<Object> DoSearch_Tags(String[] input, ArrayList<Pictogram> AllPictograms)
    {

    }

    // CATEGORY IMPLEMENTATIONS

    private ArrayList<Object> DoSearch_Category(String[] input, ArrayList<Pictogram> AllPictograms)
    {

    }
}
