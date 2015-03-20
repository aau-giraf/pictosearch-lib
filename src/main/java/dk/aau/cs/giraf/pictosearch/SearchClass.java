package dk.aau.cs.giraf.pictosearch;

//import dk.aau.cs.giraf.categorylib.CategoryHelper;
//import dk.aau.cs.giraf.pictogram.Pictogram;
//import dk.aau.cs.giraf.pictogram.PictoFactory;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.controllers.PictogramTagController;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.models.PictogramTag;
import dk.aau.cs.giraf.oasis.lib.models.Tag;

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

    public ArrayList<Object> DoSearch(String[] input, ArrayList<Object> allList){
        ArrayList<Object> result = new ArrayList<Object>();

        result.addAll(FindPictograms(input, allList));
        result.addAll(FindCategories(input, allList));
        result.addAll(FindTags(input, allList));

        return result;
    }

    private ArrayList<Object> FindPictograms(String[] input, ArrayList<Object> allList){
        ArrayList<Object> pictogramResult = new ArrayList<Object>();

        for (Object o : allList){
            if (o instanceof Pictogram){
                Pictogram p = (Pictogram)o;

                if (p != null && p.getName() != null){
                    for (String s : input){
                        if (p.getName().toLowerCase().contains(s)){
                            pictogramResult.add(p);
                        }
                    }
                }
            }
        }

        return pictogramResult;
    }

    private ArrayList<Object> FindCategories(String[] input, ArrayList<Object> allList){
        ArrayList<Object> categoryResult = new ArrayList<Object>();

        for (Object o : allList){
            if (o instanceof Category){
                Category c = (Category)o;

                if (c != null && c.getName() != null){
                    for (String s : input){
                        if (c.getName().toLowerCase().contains(s)){
                            categoryResult.add(c);
                        }
                    }
                }
            }
        }


        return categoryResult;
    }

    private ArrayList<Object> FindTags(String[] input, ArrayList<Object> allList){
        ArrayList<Object> tagResult = new ArrayList<Object>();
        ArrayList<Integer> tagIDs = new ArrayList<Integer>();

        for (Object o : allList){
            if (o instanceof Tag){
                Tag t = (Tag)o;

                if (t != null && t.getName() != null){
                    for (String s : input){
                        if (t.getName().toLowerCase().contains(s)){
                            tagIDs.add(t.getId());
                        }
                    }
                }
            }
        }

        PictogramTagController pictogramTagController = new PictogramTagController(Outer);
        PictogramController pictogramController = new PictogramController(Outer);

        List<PictogramTag> listOfPictogramTags = pictogramTagController.getListOfPictogramTags();

        for (PictogramTag pt : listOfPictogramTags){
            for (int i = 0; i < tagIDs.size(); i++){
                if (pt.getTagId() == tagIDs.get(i)){
                    tagResult.add(pictogramController.getPictogramById(pt.getPictogramId()));
                }
            }
        }


        return tagResult;
    }

    @Deprecated
    public ArrayList<Object> DoSearch(String tag, String[] input, ArrayList<Object> AllPictograms)
    {
        // Todo: Add Strings to Strings.xml
        // ToDo: DoSearch_Category currently receives an empty array, fill it with cats pls
        if (tag.equals("Tags"))
        {
            return DoSearch_Tags(input, AllPictograms);
        }
        else if (tag.equals("Pictogrammer"))
        {
            return DoSearch_Pictogram(input, AllPictograms);
        }
        else if (tag.equals("Kategorier"))
        {
            return DoSearch_Category(input, AllPictograms);
        }
        else
        {
            return DoSearch_All(input, AllPictograms);
        }
    }

    // PICTOGRAM IMPLEMENTATIONS

    private ArrayList<Object> DoSearch_All(String[] input, ArrayList<Object> AllPictograms)
    {
        // ToDo: DoSearch_Category currently receives an empty array, fill it with cats pls
        ArrayList<Object> Result = new ArrayList<Object>();
        Result.addAll(DoSearch_Pictogram(input, AllPictograms));
        Result.addAll(DoSearch_Category(input, AllPictograms));
        return Result;
    }

    private ArrayList<Object> DoSearch_Pictogram(String[] input, ArrayList<Object> AllPictograms)
    {
        ArrayList<Object> lst = new ArrayList<Object>();
        for (Object o : AllPictograms)
        {
            if (o instanceof Pictogram)
            {
                Pictogram p = (Pictogram)o;
                if (p == null || p.getName() == null) continue;

                for(int i = 0; i < input.length; i++)
                {
                    if (p.getName().toLowerCase().contains(input[i]))
                    {
                        lst.add(p);
                        break;
                    }
                }
            }
        }
        return lst;
    }

    private ArrayList<Object> DoSearch_Tags(String[] input, ArrayList<Object> AllPictograms)
    {
        ArrayList<Object> lst = new ArrayList<Object>();

//        for (Object o : AllPictograms)
//        {
//            if (p == null || p.getName() == null) continue;
//
//            boolean added = false;
//            for(int i = 0; i < input.length; i++)
//            {
//                /*
//                for (String tag : p.getTags())
//                {
//                    if (tag.toLowerCase().contains(input[i]))
//                    {
//                        lst.add(p);
//                        added = true;
//                        break;
//                    }
//                }
//                */
//                if (added) break;
//            }
//        }
        return lst;
    }

    // CATEGORY IMPLEMENTATIONS

    private ArrayList<Object> DoSearch_Category(String[] input, ArrayList<Object> CatSearchList)
    {
        ArrayList<Object> lst = new ArrayList<Object>();

        for (Object o : CatSearchList){
            if(o instanceof Category)
            {
                Category pc = (Category)o;

                if (pc == null || pc.getName() == null) continue;

                boolean added = false;

                for (int i = 0; i < input.length; i++)
                {
                    if (added) break;
                    else if (pc.getName().contains(input[i]))
                    {
                        lst.add(pc);
                        added = true;
                        break;
                    }

                }
            }
        }

        return lst;
    }
}
