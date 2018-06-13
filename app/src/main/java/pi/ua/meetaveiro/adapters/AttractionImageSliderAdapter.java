package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import pi.ua.meetaveiro.R;

public class AttractionImageSliderAdapter extends PagerAdapter {

    private List<Bitmap> images;
    private LayoutInflater inflater;
    private Context context;

    public AttractionImageSliderAdapter(Context context, List<Bitmap> images) {
        this.context = context;
        this.images=images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.attraction_slider_layout, view, false);
        ImageView myImage = (ImageView) myImageLayout
                .findViewById(R.id.image);
        myImage.setImageBitmap(images.get(position));
        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
