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
import android.widget.Button;
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
import org.json.JSONArray;
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
    Button convert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        gridView = findViewById(R.id.grid_view);
        updateGridView();

        //gridView.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(getApplicationContext(), "You Clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.add_more_btn).setOnClickListener(V -> finish());

        convert = findViewById(R.id.convert_btn);
        convert.setOnClickListener(v -> convert());
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private void convert() {
        final List<Uri> list = new ArrayList<>(MainActivity.clickedImages);
        if (list.size() == 1) {
            Toast.makeText(getApplicationContext(), "Select at least 1 more file", Toast.LENGTH_SHORT).show();
            return;
        }
        final List<String> PDFs = new ArrayList<>();
        convert.setText("Converting...");
        final String convertToPdfUrl = "https://api-dev.pdf4me.com/Convert/ConvertToPdf";
        final String mergePdfUrl = "https://api-dev.pdf4me.com/Merge/Merge";
        final String token = "Basic NWM3OTAxNDEtODE2OC00OGM0LWIyMjMtNTQ2OWM5YTlkYjliOlFLZkQxOTQmSzExJW9uZFB3PVZ6WGpTRSZZdHZSVzI3";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        new Thread(() -> {
            try {
                //converting image to PDF
                for (int i = 0; i < list.size(); i++) {
                    File selectedFile = new File(list.get(i).getPath());
                    //file to byte[]
                    byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                    //byte[] to encoded base 64 string
                    String encodedString = Base64.getEncoder().encodeToString(fileBytes);
                    JSONObject wrapper = new JSONObject();
                    JSONObject document = new JSONObject();
                    document.put("name", selectedFile.getName());
                    document.put("docData", "encodedString");

                    JSONObject action = new JSONObject();
                    action.put("pdfConformance", "pdfA1");
                    action.put("conversionMode", "fast");

                    wrapper.put("document", document);
                    wrapper.put("convertToPdfAction", action);


                    Log.e(TAG, "convert: " + wrapper.toString());

                    RequestBody requestBody = RequestBody.create(JSON, String.valueOf(wrapper));
                    Request request = new Request.Builder()
                            .addHeader("Authorization", token)
                            .method("POST", requestBody)
                            .url(convertToPdfUrl)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    Log.e(TAG, "convert: converting " + selectedFile.getName());
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String docStr = jsonObject.getString("document");
                        JSONObject doc = new JSONObject(docStr);
                        String receivedBase64 = doc.getString("docData");
                        PDFs.add(receivedBase64);
                        Log.e(TAG, "convert: file created to single pdf");
                    } else {
                        Log.e(TAG, "convert: converting failed unsuccessful response for " + selectedFile.getName());
                        break;
                    }
                }

                //merging all converted PDFs
                if (PDFs.size() == list.size()) {
                    JSONArray documents = new JSONArray();
                    for (int i = 0; i < PDFs.size(); i++) {
                        JSONObject document = new JSONObject();
                        document.put("name", "file" + i + ".pdf");
                        document.put("docData", PDFs.get(i));
                        documents.put(document);
                    }

                    //request object
                    JSONObject wrapper = new JSONObject();

                    //second request object in JSON
                    JSONObject action = new JSONObject();

                    wrapper.put("documents", documents);
                    wrapper.put("mergeAction", action);

                    RequestBody requestBody = RequestBody.create(JSON, String.valueOf(wrapper));
                    Request request = new Request.Builder()
                            .addHeader("Authorization", token)
                            .method("POST", requestBody)
                            .url(mergePdfUrl)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    Log.e(TAG, "convert: merging all PDFs");
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String docStr = jsonObject.getString("document");
                        JSONObject doc = new JSONObject(docStr);
                        String receivedBase64 = doc.getString("docData");
                        byte[] bytes = Base64.getDecoder().decode(receivedBase64);
                        File mergedPdf = new File(getExternalFilesDir(null), "PDF4ME_" +
                                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) +
                                ".pdf");
                        FileOutputStream fos = new FileOutputStream(mergedPdf);
                        fos.write(bytes);
                        Log.e(TAG, "convert: responseData : \n" + responseData);
                    } else {
                        Log.e(TAG, "convert: response isSuccessful false");
                    }
                    //operation complete
                    grid.this.runOnUiThread(() -> convert.setText("Converted"));
                    MainActivity.clickedImages.clear();
                    updateGridView();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
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