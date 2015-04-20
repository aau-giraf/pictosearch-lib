package dk.aau.cs.giraf.pictosearch;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter{
    private ArrayList<Object> objectInfoList;
    private int rowSize = 3;
    private int coloumnSize = 8;
    OnPositionClickListener caller = null;

    public interface OnPositionClickListener {
        public void positionClicked(int position);
    }

    public ViewPagerAdapter(ArrayList<Object> objectInfoList) {
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
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int pageIndex) {

        final Context context = container.getContext();

        // Try to cast the activity to one that implements OnPositionClickListener
        try {
            caller = (OnPositionClickListener) container.getContext();
        }catch (ClassCastException e) {
            throw new ClassCastException(caller.toString() + " must implement OnPositionClickListener interface");
        }

        GridView page = (GridView) LayoutInflater.from(context).inflate(R.layout.grid_view, null);

        //page.setBackgroundDrawable(container.getResources().getDrawable(R.drawable.border_round));

        final int from = pageIndex * rowSize * coloumnSize;
        final int to = ((pageIndex + 1) * rowSize * coloumnSize);

        if (to < objectInfoList.size())
        {
            page.setAdapter(new PictoAdapter(new ArrayList<Object>(objectInfoList.subList(from, to)), container.getContext()));
        } else {
            page.setAdapter(new PictoAdapter(new ArrayList<Object>(objectInfoList.subList(from, objectInfoList.size())), context));
        }

        page.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                caller.positionClicked(pageIndex * rowSize * coloumnSize + position );

            }
        });

        container.addView(page, 0);

        return page;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
