package com.example.image_converter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageButton btBrowse,btReset;
    ImageView imageView;
    Uri uri;
    public static List<Uri> clickedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btBrowse = findViewById(R.id.bt_browse);
        btReset = findViewById(R.id.bt_my_scans);
        imageView = findViewById(R.id.image_view);

        btBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MainActivity.this);
//                CropImage.startPickImageActivity(MainActivity.this);
            }
        });

        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MyScans.class));
                overridePendingTransition(0,0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
//                && resultCode == Activity.RESULT_OK) {
//            Uri imageuri = CropImage.getPickImageResultUri(this, data);
//            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageuri)) {
//                uri = imageuri;
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
//                        , 0);
//            } else {
//                startCrop(imageuri);
//            }
//        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if (resultCode == RESULT_OK){
                 Uri image = result.getUri();
                 clickedImages.add(image);
                 startActivity(new Intent(getApplicationContext(), grid.class));
                 overridePendingTransition(0,0);
//                imageView.setImageURI(image);
//                 Toast.makeText(this,"Image Update Successfully !!!"
//                 ,Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void startCrop(Uri imageuri){
//        CropImage.activity(imageuri)
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .setMultiTouchEnabled(true)
//                .start(this);
//    }
}