package ws.idroid.contentsharing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Link for receiving content goo.gl/iw8hnG

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String imagePath;
    private EditText etTextToShare;
    private ImageView btn_select;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission
                .WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            //Dialog to ask the user to accept the permission
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        findViews();

    }

    private void findViews() {
        etTextToShare = findViewById(R.id.et_share_text);
        Button btnShareText = findViewById(R.id.btn_share_text);
        btn_select = findViewById(R.id.iv_share_select);
        Button btnSharePicture = findViewById(R.id.btn_share_picture);
        btnShareText.setOnClickListener(view -> shareTextUri());
        btn_select.setOnClickListener(view -> {
            Intent intent = new Intent();
            // Show only images, no videos or anything else
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Picture from ..."),
                    1);

        });
        btnSharePicture.setOnClickListener(view -> shareImage());
    }

    private void shareTextUri() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Ghazza Course Title");
        shareIntent.putExtra(Intent.EXTRA_TEXT, etTextToShare.getText().toString());
        startActivity(Intent.createChooser(shareIntent, "Share text example..."));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = intent.getData();
            imagePath = getPath(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            btn_select.setImageBitmap(bitmap);
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void shareImage() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        File imageToShare = new File(imagePath);
        Uri uri = Uri.fromFile(imageToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Now send an Image...!"));
    }
}