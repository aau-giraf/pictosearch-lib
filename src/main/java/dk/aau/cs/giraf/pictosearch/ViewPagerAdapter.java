package dk.aau.cs.giraf.pictosearch;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter{
    private ArrayList<Object> objectInfoList;
    private int rowSize = 4;
    private int coloumnSize = 8;
    LayoutInflater mLayoutInflater;

    ViewPagerAdapter(ArrayList<Object> objectInfoList) {
        this.objectInfoList = objectInfoList;
    }

    @Override
    public int getCount() {
        if (objectInfoList != null) {
            return (int) Math.ceil(((double) objectInfoList.size()) / (rowSize * coloumnSize));
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View objectView = mLayoutInflater.inflate(R.layout.pictogramview, container, false);

        GridView gridView = (GridView) objectView.findViewById(R.id.pictogram_displayer);
        //imageView.setImageResource();

        container.addView(objectView);

        return super.instantiateItem(container, position);

    }


}
