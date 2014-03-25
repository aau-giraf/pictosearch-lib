package dk.aau.cs.giraf.pictosearch;

import dk.aau.cs.giraf.categorylib.CategoryHelper;
import dk.aau.cs.giraf.categorylib.PARROTCategory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.categorylib.CategoryHelper;
import dk.aau.cs.giraf.pictogram.PictoFactory;
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
    public ArrayList<Object> DoCategorySearch(String[] input, ArrayList<PARROTCategory> AllCategories)
    {
        return DoSearch_Category(input, AllCategories);
    }

    // PICTOGRAM IMPLEMENTATIONS

    private ArrayList<Object> DoSearch_All(String[] input, ArrayList<Pictogram> AllPictograms)
    {
        // TO DO: NOT COMPLETE
        // Combines name and category, but make sure to avoid duplicates!!
        return DoSearch_Name(input, AllPictograms);
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
        ArrayList<Object> lst = new ArrayList<Object>();
        for (Pictogram p : AllPictograms)
        {
            boolean added = false;
            for(int i = 0; i < input.length; i++)
            {
                for (String tag : p.getTags())
                {
                    if (tag.toLowerCase().contains(input[i]))
                    {
                        lst.add(p);
                        added = true;
                        break;
                    }
                }
                if (added) break;
            }
        }
        return lst;
    }

    // CATEGORY IMPLEMENTATIONS

    private ArrayList<Object> DoSearch_Category(String[] input, ArrayList<PARROTCategory> CatSearchList)
    {
        ArrayList<Object> lst = new ArrayList<Object>();


        for (PARROTCategory pc : CatSearchList){
            boolean added = false;
            for (int i = 0; i < input.length; i++){
                if (pc.getCategoryName().contains(input[i])){
                    lst.add(pc);
                    added = true;
                    break;
                }
                else if (pc.getSubCategories() != null){
                    for (PARROTCategory spc : pc.getSubCategories()){
                        if (spc.getCategoryName().contains(input[i])){
                            lst.add(pc);
                            added = true;
                            break;
                        }
                    }
                }
                else if (pc.getPictograms() != null){
                    for (Pictogram p : pc.getPictograms()){
                        if (p.getName().contains(input[i])){
                            lst.add(pc);
                            added = true;
                            break;
                        }
                    }
                }
                if (added) break;
            }
        }

        return lst;
    }
}
