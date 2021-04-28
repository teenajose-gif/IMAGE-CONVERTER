package com.example.image_converter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyScans extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_scans);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        File folder = new File(getExternalFilesDir(null), "Pdf");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            listOfFiles = new File[0];
        }
        List<FileModal> list = new ArrayList<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                list.add(new FileModal(file, file.getAbsolutePath(), file.getName(), getSize(file), ""));
            }
        }
        adapter = new RecyclerAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }

    private String getSize(File file) {
        long bytes = file.length();
        long kb = bytes / 1024;
        if (kb > 1024) {
            double mb = (double) bytes / (1024 * 1024);
            return mb + " MB";
        }
        return kb + " kb";
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        List<FileModal> data;
        Context context;

        public RecyclerAdapter(Context context, List<FileModal> data) {
            this.data = data;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.custom_design, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final FileModal temp = data.get(position);
            holder.pdfName.setText(temp.getName());
            holder.pdfSize.setText(temp.getSize());
            holder.pdfDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteFile(temp.getFile());
                }
            });
            holder.pdfShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareFile(temp.getFile());
                }
            });
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewFile(temp.getFile());
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView pdfName, pdfSize;
            Button pdfShare, pdfDelete;
            LinearLayout item;

            public ViewHolder(@NonNull View itemView) {

                super(itemView);
                item = itemView.findViewById(R.id.item);
                pdfName = itemView.findViewById(R.id.pdf_name);
                pdfSize = itemView.findViewById(R.id.pdf_size);
                pdfShare = itemView.findViewById(R.id.pdf_share);
                pdfDelete = itemView.findViewById(R.id.pdf_delete);
            }
        }
    }

    private void viewFile(File file) {
        Uri path = FileProvider.getUriForFile(getApplicationContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        Intent pdfOpenIntent = new Intent(Intent.ACTION_VIEW);
        pdfOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pdfOpenIntent.setDataAndType(path, "application/pdf");
        try {
            startActivity(pdfOpenIntent);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Cannot open selected file", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteFile(File file) {
        if(file.exists()) {
            if(file.delete())
                updateRecyclerView();
        }
    }

    public void shareFile(File file) {
        Uri path = FileProvider.getUriForFile(getApplicationContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        Intent pdfOpenIntent = new Intent(Intent.ACTION_SEND);
        pdfOpenIntent.putExtra(Intent.EXTRA_STREAM, path);
        pdfOpenIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pdfOpenIntent.setType("application/pdf");
        startActivity(Intent.createChooser(pdfOpenIntent, "Share via.."));
    }
}