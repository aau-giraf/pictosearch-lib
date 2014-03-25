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
    public ArrayList<Pictogram> DoSearch(String tag, String[] input)
    {
        // Vælg tag, resten skal tilføjes på et tidspunkt
        if (tag == "Tag") return DoSearch_Tags(input);
        else return DoSearch_All(input);
    }
    public ArrayList<PARROTCategory> DoCategorySearch(String tag, String[] input)
    {
        return DoSearch_Category(input);
    }

    private ArrayList<Pictogram> DoSearch_All(String[] input)
    {

    }

    private ArrayList<Pictogram> DoSearch_Tags(String[] input)
    {

    }

    private ArrayList<PARROTCategory> DoSearch_Category(String[] input)
    {

    }
}
