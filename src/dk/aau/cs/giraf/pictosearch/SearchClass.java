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
    public ArrayList<Pictogram> DoSearch(String tag, string input)
    {
        // Vælg tag, resten skal tilføjes på et tidspunkt
        if (tag == "category") return DoSearch_Category(input);
        else return DoSearch_All(input);
    }
    private ArrayList<Pictogram> DoSearch_All(string input)
    {

    }

    private ArrayList<Pictogram> DoSearch_Pictogram(string input)
    {

    }

    private ArrayList<Pictogram> DoSearch_Category(string input)
    {

    }

    private ArrayList<Pictogram> DoSearch_SubCategory(string input)
    {

    }
}
