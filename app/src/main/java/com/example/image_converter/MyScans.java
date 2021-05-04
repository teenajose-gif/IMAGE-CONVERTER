package com.example.image_converter;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyScans extends AppCompatActivity {
    private static final String TAG = "MyScans";
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

            holder.options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupMenu(temp, v);
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
            Button options;
            LinearLayout item;

            public ViewHolder(@NonNull View itemView) {

                super(itemView);
                item = itemView.findViewById(R.id.item);
                pdfName = itemView.findViewById(R.id.pdf_name);
                pdfSize = itemView.findViewById(R.id.pdf_size);
                options = itemView.findViewById(R.id.option);
            }
        }
    }

    private void setupMenu(FileModal temp, View v) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.file_action, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.Protect:
                    protectFile(temp.getFile());
                    return true;
                case R.id.Share:
                    shareFile(temp.getFile());
                    return true;
                case R.id.Delete:
                    deleteFile(temp.getFile());
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void protectFile(final File file) {
        //todo protect
        final String url = "https://api.pdf4me.com/PdfA/Protect";
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        Dialog dialog = new Dialog(MyScans.this);
        dialog.setContentView(R.layout.dialog_password);
        dialog.setCanceledOnTouchOutside(false);
        EditText passwordEt = dialog.findViewById(R.id.password_et);
        Button confirm = dialog.findViewById(R.id.confirm_button);
        ImageView cancel = dialog.findViewById(R.id.cancel);
        dialog.show();
        cancel.setOnClickListener(v -> {
            if (!confirm.getText().toString().contains("..."))
                dialog.dismiss();
        });

        confirm.setOnClickListener(v -> {
            String password = passwordEt.getText().toString().trim();
            if (password.length() == 0) {
                Toast.makeText(MyScans.this, "Empty password", Toast.LENGTH_SHORT).show();
                return;
            }
            confirm.setText("Encrypting...");
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String encodedString = Base64.getEncoder().encodeToString(bytes);

                JSONObject wrapper = new JSONObject();

                JSONObject document = new JSONObject();
                document.put("fileName", "tempFile.pdf");
                document.put("docData", encodedString);

                JSONObject protectAction = new JSONObject();
                protectAction.put("userPassword", password);
                protectAction.put("unlock", false);
                JSONArray permissions = new JSONArray();
                permissions.put("all");
                protectAction.put("permissions", permissions);

                wrapper.put("document", document);
                wrapper.put("protectAction", protectAction);

                RequestBody requestBody = RequestBody.create(grid.JSON, String.valueOf(wrapper));
                Request request = new Request.Builder()
                        .addHeader("Authorization", grid.token)
                        .method("POST", requestBody)
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Log.e(TAG, "onClick: adding password to pdf");

                new Thread(() -> {
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            String docStr = jsonObject.getString("document");
                            JSONObject doc = new JSONObject(docStr);
                            String receivedBase64 = doc.getString("docData");
                            byte[] bytesArr = Base64.getDecoder().decode(receivedBase64);
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(bytesArr);
                            Log.e(TAG, "onClick: pdf is password protected");
                            MyScans.this.runOnUiThread(dialog::dismiss);
                        } else {
                            Log.e(TAG, "onClick: unsuccessful response");
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });

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
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Cannot open selected file", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete())
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