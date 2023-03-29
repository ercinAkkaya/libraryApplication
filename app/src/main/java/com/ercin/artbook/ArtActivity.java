package com.ercin.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.ercin.artbook.databinding.ActivityArtBinding;
import com.ercin.artbook.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");


        if (info.equals("new")){
            System.out.println("new");
        }else{
            int artId = intent.getIntExtra("artId", 0);
            System.out.println("id : " + artId);
            binding.btnSave.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[] {String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int artistNameIx = cursor.getColumnIndex("artistname");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("year");

                while (cursor.moveToNext()){
                    binding.nameTxt.setText(cursor.getString(artNameIx));
                    binding.artistTxt.setText(cursor.getString(artistNameIx));
                    binding.yearTxt.setText(cursor.getString(yearIx));

                    System.out.println("image ıx : " + imageIx);

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.selectImage.setImageBitmap(bitmap);
                    binding.selectImage.setEnabled(false);
                }

                cursor.close();

            }catch (Exception exception){
                System.out.println(" hata : " + exception);
            }

        }
    }

    public void save(View view){

        String name = binding.nameTxt.getText().toString();
        String artistName = binding.artistTxt.getText().toString();
        String year = binding.yearTxt.getText().toString();

        Bitmap smallImage = makeSmallImage(selectedImage, 300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)");
            String sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, name);
            sqLiteStatement.bindString(2, artistName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();

        }catch (Exception exception){
            System.out.println(exception);
        }

        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        // sadece gidilecek aktivite açık kalır diğerleri kapatılır
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public Bitmap makeSmallImage(Bitmap image, int maxSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1){
            //yatay
            width = maxSize;
            height = (int) (width / bitmapRatio);
        }else{
            //dikey
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }


        return image.createScaledBitmap(image , width, height, true);
    }

    public void selectImage(View view){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //android 33+ -> READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view, "permisson needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("give permisson", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //request permisson
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }else{

                    //request permisson
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }

            }else{

                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);

            }



        }else{
            //android 32 -> READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "permisson needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("give permisson", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //request permisson
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }else{

                    //request permisson
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

            }else{

                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);

            }
        }


    }

    private void registerLauncher(){


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null){
                        Uri imageData = intentFromResult.getData();
                        try {
                            if (Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.selectImage.setImageBitmap(selectedImage);
                            }else{
                                selectedImage = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(), imageData);
                                binding.selectImage.setImageBitmap(selectedImage);
                            }

                        }catch (Exception exception){
                            exception.printStackTrace();
                        }
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result){
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    //permission denied
                    Toast.makeText(ArtActivity.this, "permission needed", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

}