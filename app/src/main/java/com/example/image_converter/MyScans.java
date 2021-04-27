package com.example.image_converter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        File folder = new File(getExternalFilesDir(null), "Pdf");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            listOfFiles = new File[0];
        }
        List<FileModal> list = new ArrayList<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                list.add(new FileModal(file.getAbsolutePath(), file.getName(), getSize(file), ""));
            }
        }
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
            FileModal temp = data.get(position);
            holder.textView.setText(temp.getName());
            //holder.textView.setOnClickListener(view -> Toast.makeText(context, "CLicked on" + temp.getName(), Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {

                super(itemView);
                textView = itemView.findViewById(R.id.fileName);
            }
        }
    }
}