package com.example.image_converter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class grid extends AppCompatActivity {
    GridView gridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        gridView = findViewById(R.id.grid_view);
        updateGridView();

        //gridView.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(getApplicationContext(), "You Clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.add_more_btn).setOnClickListener(V -> finish());

        findViewById(R.id.convert_btn).setOnClickListener(v -> createPDF());
    }

    private void createPDF() {
        final File file = new File(grid.this.getExternalFilesDir(null),
                "PDF4ME_Scan_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) +
                        ".pdf");
        final List<Uri> list = new ArrayList<>(MainActivity.clickedImages);
        final ProgressDialog dialog = ProgressDialog.show(grid.this, "", "Generating PDF...");
        dialog.show();
        new Thread(() -> {
            Bitmap bitmap;
            PdfDocument document = new PdfDocument();
            try {
                for (int i = 0; i < list.size(); i++) {
                    bitmap = MediaStore.Images.Media.getBitmap(grid.this.getContentResolver(), list.get(i));
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), (i + 1)).create();
                    PdfDocument.Page page = document.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    Paint paint = new Paint();
                    paint.setColor(Color.BLUE);
                    canvas.drawPaint(paint);
                    canvas.drawBitmap(bitmap, 0f, 0f, null);
                    document.finishPage(page);
                    bitmap.recycle();
                }

                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(file);
                    document.writeTo(fos);
                    document.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(dialog::dismiss);
            } catch (IOException e) {
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