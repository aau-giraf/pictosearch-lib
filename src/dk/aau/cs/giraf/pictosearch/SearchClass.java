package dk.aau.cs.giraf.pictosearch;

//import dk.aau.cs.giraf.categorylib.CategoryHelper;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import java.util.ArrayList;
import dk.aau.cs.giraf.oasis.lib.models.Category;

/**
 * Created by Tobias on 25-03-14.
 */
public class SearchClass
{
    private PictoAdminMain Outer;

    SearchClass(PictoAdminMain ou)
    {
        Outer = ou;
    }

    public ArrayList<Object> DoSearch(String tag, String[] input, ArrayList<Pictogram> AllPictograms)
    {
        // ToDo: DoSearch_Category currently receives an empty array, fill it with cats pls
        if (tag == "Tags") return DoSearch_Tags(input, AllPictograms);
        else if (tag == "Pictogrammer") return DoSearch_Pictogram(input, AllPictograms);
        else if (tag == "Kategorier") return DoSearch_Category(input, Outer.getAllCategories());
        else return DoSearch_All(input, AllPictograms);
    }

    // PICTOGRAM IMPLEMENTATIONS

    private ArrayList<Object> DoSearch_All(String[] input, ArrayList<Pictogram> AllPictograms)
    {
        // ToDo: DoSearch_Category currently receives an empty array, fill it with cats pls
        ArrayList<Object> Result = new ArrayList<Object>();
        Result.addAll(DoSearch_Pictogram(input, AllPictograms));
        Result.addAll(DoSearch_Category(input, Outer.getAllCategories()));
        return Result;
    }

    private ArrayList<Object> DoSearch_Pictogram(String[] input, ArrayList<Pictogram> AllPictograms)
    {
        ArrayList<Object> lst = new ArrayList<Object>();
        for (Pictogram p : AllPictograms)
        {
            if (p == null || p.getTextLabel() == null) continue;

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
            if (p == null || p.getTextLabel() == null) continue;

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

    private ArrayList<Object> DoSearch_Category(String[] input, ArrayList<Category> CatSearchList)
    {
        ArrayList<Object> lst = new ArrayList<Object>();

        for (Category pc : CatSearchList){
            if (pc == null || pc.getName() == null) continue;

            boolean added = false;

            for (int i = 0; i < input.length; i++)
            {
                if (pc.getName().contains(input[i]))
                {
                    lst.add(pc);
                    added = true;
                    break;
                }
                if (added) break;
            }
        }

        return lst;
    }
}
