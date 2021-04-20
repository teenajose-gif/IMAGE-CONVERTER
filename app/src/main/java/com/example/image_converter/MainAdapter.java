package com.example.image_converter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MainAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Uri> list;

    public MainAdapter(Context c, List<Uri> list){
        context = c;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (inflater == null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null){
            view = inflater.inflate(R.layout.row_item,null);
        }

        ImageView imageView = view.findViewById(R.id.image_view);
        Uri currUri = list.get(i);
        imageView.setImageURI(currUri);
        return view;
    }
}
