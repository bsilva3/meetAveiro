package pi.ua.meetaveiro.adapters;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import pi.ua.meetaveiro.R;


public class RouteHistoryDetailAdapter extends PagerAdapter{


    private ArrayList<Bitmap> images = new ArrayList<>();
    private Context ctx;
    private LayoutInflater inflater;



    public RouteHistoryDetailAdapter(Context context, ArrayList<Bitmap> images) {
        this.ctx = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view ==(LinearLayout)object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.swipe,container,false);
        ImageView img =(ImageView)v.findViewById(R.id.imageView);
        img.setImageBitmap(images.get(position));
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        container.refreshDrawableState();
    }
}



