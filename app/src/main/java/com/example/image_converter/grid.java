package com.example.image_converter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;
import java.io.File;
import org.apache.commons.io.FileUtils;

import com.pdf4me.client.ConvertClient;
import com.pdf4me.client.Pdf4meClient;
import com.pdf4me.client.MergeClient;

import model.ConvertToPdf;

public class grid extends AppCompatActivity {
    GridView gridView;

    private final String token = "NTBiNDA1YjQtZDUwOS00MTI1LWJhYjMtMGZjMTM2MTcyMWRjOkNscWdTT1dudmRJUGw0eDhaWFlrJnpnUHNEMEVheWNn";
    Pdf4meClient pdf4meClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);


        pdf4meClient = new Pdf4meClient("https://api.pdf4me.com", token);
        gridView = findViewById(R.id.grid_view);
        updateGridView();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "You Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.add_more_btn).setOnClickListener(V -> {
            finish();
        });

        findViewById(R.id.convert_btn).setOnClickListener(view -> {
            createPDF();
        });
    }

    private void createPDF() {
        //todo save images to pdf
        ConvertClient convertClient = new ConvertClient(pdf4meClient);

        ConvertToPdf convertToPdf = new ConvertToPdf();


    }

    public class MainAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private List<Uri> list;

        public MainAdapter(Context c, List<Uri> list) {
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
        public View getView(final int position, View view, ViewGroup viewGroup) {
            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            }
            if (view == null) {
                view = inflater.inflate(R.layout.row_item, null);
            }

            ImageView imageView = view.findViewById(R.id.image_view);
            ImageView deleteBtn = view.findViewById(R.id.delete_btn);
            Uri currUri = list.get(position);
            imageView.setImageURI(currUri);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.clickedImages.remove(position);
                    updateGridView();
                }
            });
            return view;
        }
    }

    private void updateGridView() {
        MainAdapter adapter = new MainAdapter(getApplicationContext(), MainActivity.clickedImages);
        gridView.setAdapter(adapter);
    }
}