package ws.idroid.contentsharing;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.*;

import java.io.File;

import butterknife.*;

public class MainActivity extends AppCompatActivity {

    //Link for receiving content goo.gl/iw8hnG

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String imagePath;

    @BindView(R.id.et_share_text)
    EditText etTextToShare;

    private ImageView btnSelect;

    @OnClick(R.id.btn_share_picture)
    void onImagePressed() {
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
    }

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
        ButterKnife.bind(this);
        verifyStoragePermissions(MainActivity.this);
        ActivityCompat.requestPermissions(
                MainActivity.this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
        findViews();


    }

    private void findViews() {
        Button btnShareText = findViewById(R.id.btn_share_text);
        btnSelect = findViewById(R.id.iv_share_select);
        Button btnSharePicture = findViewById(R.id.btn_share_picture);
        btnShareText.setOnClickListener(view -> shareTextUri());
        btnSelect.setOnClickListener(view -> {
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
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Ghazza Course Title");
        shareIntent.putExtra(Intent.EXTRA_TEXT, etTextToShare.getText().toString());
        startActivity(Intent.createChooser(shareIntent, "Share text example..."));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = intent.getData();
            Log.i("image path Uri", "path = " + selectedImageUri);
            imagePath = getPath(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            btnSelect.setImageBitmap(bitmap);
        }
    }

    public String getPath(Uri selectedImageUri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null,
                null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int columnIndexOrThrow = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String selectedImagePath = cursor.getString(columnIndexOrThrow);
        Log.i("image path", "path = " + selectedImagePath);
        if (selectedImagePath != null) {
            return selectedImagePath;
        } else {
            return getPath2(this, selectedImageUri);
        }
    }

    private String getPath2(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor;
        if (Build.VERSION.SDK_INT > 19) {
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String sel = MediaStore.Images.Media._ID + "=?";
            cursor = context.getContentResolver().query(MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI, projection, sel, new String[]{id}, null);
        } else {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(columnIndex).toString();
            cursor.close();
        } catch (NullPointerException e) {

        }
        return path;
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
