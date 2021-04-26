package com.example.image_converter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class grid extends AppCompatActivity {
    private static final String TAG = "grid";
    GridView gridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        gridView = findViewById(R.id.grid_view);
        updateGridView();

        //gridView.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(getApplicationContext(), "You Clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.add_more_btn).setOnClickListener(V -> finish());

        findViewById(R.id.convert_btn).setOnClickListener(v -> convert(""));
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private void convert(String path) {
        if (path.trim().equals(""))
            return;
        File file = new File(path);
        try {
            //file to byte[]
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            //byte[] to encoded base 64 string
            String encodedString = Base64.getEncoder().encodeToString(fileBytes);

            String url = "https://api-dev.pdf4me.com/Convert/ConvertToPdf";
            String token = "Basic NWM3OTAxNDEtODE2OC00OGM0LWIyMjMtNTQ2OWM5YTlkYjliOlFLZkQxOTQmSzExJW9uZFB3PVZ6WGpTRSZZdHZSVzI3";

            JSONObject wrapper = new JSONObject();

            JSONObject document = new JSONObject();
            document.put("name", file.getName());
            document.put("docData", encodedString);

            JSONObject action = new JSONObject();
            action.put("pdfConformance", "pdfA1");
            action.put("conversionMode", "fast");

            wrapper.put("document", document);
            wrapper.put("convertToPdfAction", action);


            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(wrapper));
            Request request = new Request.Builder()
                    .addHeader("Authorization", token)
                    .method("POST", requestBody)
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException(response.toString());
                    }
                    String body = response.body().string();
//                    Log.e(TAG, "onResponse: " + response.body().string());
                    grid.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Response.setText(body);
                            try {
                                JSONObject jsonObject = new JSONObject(body);
                                String docStr = jsonObject.getString("document");
                                JSONObject document = new JSONObject(docStr);
                                String encodedString = document.getString("docData");
                                //encoded string to byte[]
                                byte[] decodedBytes = Base64.getDecoder().decode(encodedString);

                                //creating new file
                                File decodedFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".pdf");

                                //write bytes into file
                                FileOutputStream fos = new FileOutputStream(decodedFile.getAbsolutePath());
                                fos.write(decodedBytes);
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    Log.e(TAG, "onFailure: " + e.getMessage());

                }
            });

//            AndroidNetworking.post(url)
//                    .addHeaders("Authorization", token)
//                    .setContentType("application/json")
//                    .addJSONObjectBody(wrapper)
//                    .build()
//                    .getAsJSONObject(new JSONObjectRequestListener() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.e(TAG, "onResponse: " + response.toString());
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Response.setText(response.toString());
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onError(ANError anError) {
//                            Response.setText(anError.getErrorBody());
//                        }
//                    });


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * Adapter
     */
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