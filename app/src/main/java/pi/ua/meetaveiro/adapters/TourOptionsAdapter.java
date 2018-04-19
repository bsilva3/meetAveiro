package pi.ua.meetaveiro.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import pi.ua.meetaveiro.R;

/**
 * Created by bruno on 4/7/18.
 */

public class TourOptionsAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Integer[] imgid;

    @Override
    public String toString() {
        return "TourOptionsAdapter{" +
                "context=" + context +
                ", itemname=" + Arrays.toString(itemname) +
                ", imgid=" + Arrays.toString(imgid) +
                '}';
    }

    public TourOptionsAdapter(Activity context, String[] itemname, Integer[] imgid) {
            super(context, R.layout.list_view_content_tour_bottom_options, itemname);
            // TODO Auto-generated constructor stub

            this.context=context;
            this.itemname=itemname;
            this.imgid=imgid;
            }

    public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.list_view_content_tour_bottom_options, null,true);

            TextView optionDescription = (TextView) rowView.findViewById(R.id.option_text);
            ImageView optionImage = (ImageView) rowView.findViewById(R.id.option_image);

            optionDescription.setText(itemname[position]);
            optionImage.setImageResource(imgid[position]);
            return rowView;

        };
}
