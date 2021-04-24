package com.example.image_converter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.apache.commons.io.FileUtils;

import com.pdf4me.client.ConvertClient;
import com.pdf4me.client.Pdf4meClient;
import com.pdf4me.client.MergeClient;

import model.ConvertToPdf;

public class grid extends AppCompatActivity {

    private static final String TAG = "grid";
    GridView gridView;

    private final String token = "NWM3OTAxNDEtODE2OC00OGM0LWIyMjMtNTQ2OWM5YTlkYjliOlFLZkQxOTQmSzExJW9uZFB3PVZ6WGpTRSZZdHZSVzI3";
    Pdf4meClient pdf4meClient;
    ConvertClient convertClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);


        pdf4meClient = new Pdf4meClient("https://api.pdf4me.com", token);
        convertClient = new ConvertClient(pdf4meClient);
        gridView = findViewById(R.id.grid_view);
        updateGridView();

        gridView.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(getApplicationContext(), "You Clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.add_more_btn).setOnClickListener(V -> finish());

        findViewById(R.id.convert_btn).setOnClickListener(view -> createPDF());
    }

    private void createPDF() {
        List<Uri> list = new ArrayList<>(MainActivity.clickedImages);
        List<File> files = new ArrayList<>();
        List<File> pdfs = new ArrayList<>();
        for (Uri uri : list) {
            File temp = new File(uri.getPath());
            files.add(temp);
        }

        File mergedPdf = new File(getExternalFilesDir(null),"mergedPdf_" + System.currentTimeMillis() + ".pdf");
        for(int i = 0; i < files.size(); i++) {
            // conversion and writing the generated PDF to disk
            File file = files.get(i);
            byte[] generatedPdf = convertClient.convertFileToPdf("file" + i, file);
            try {
                if(files.size() == 1)
                    FileUtils.writeByteArrayToFile(mergedPdf, generatedPdf);
                else {
                    File converted = new File(getExternalFilesDir(null),"output/generatedPdf"+ i + ".pdf");
                    FileUtils.writeByteArrayToFile(converted, generatedPdf);
                    pdfs.add(converted);
                    Log.e(TAG, "createPDF: Single file converted");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(pdfs.size() == files.size() && pdfs.size() > 1) {
            try {
                MergeClient mergeClient = new MergeClient(pdf4meClient);
                byte[] mergedByteArray;
                for(int i = 0; i < pdfs.size(); i++) {
                    if(i == 0) {
                        //for first and second file
                        mergedByteArray = mergeClient.merge2Pdfs(pdfs.get(i), pdfs.get(i + 1));
                        FileUtils.writeByteArrayToFile(mergedPdf, mergedByteArray);
                        pdfs.get(i).delete();
                        pdfs.get(i + 1).delete();
                        i++;
                    } else {
                        //for third onwards
                        mergedByteArray = mergeClient.merge2Pdfs(mergedPdf, pdfs.get(i));
                        FileUtils.writeByteArrayToFile(mergedPdf, mergedByteArray);
                        pdfs.get(i).delete();
                    }
                }
                Log.e(TAG, "createPDF: all files merged");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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