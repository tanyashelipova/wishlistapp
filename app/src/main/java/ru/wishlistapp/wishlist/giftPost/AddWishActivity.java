package ru.wishlistapp.wishlist.giftPost;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.anotherStuff.NotificationHelper;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurUploadModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishAndMessageModel;
import ru.wishlistapp.wishlist.myProfileAndWishList.UserProfileFragment;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ImgurInterface;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

public class AddWishActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    public static final int GALLERY = 0;
    public static final int CAMERA = 1;
    public static boolean MY_LIST_HAS_CHANGED = false;
    private static final String TAG = "AddWishActivity";
    EditText giftTitle, giftDescription;
    ImageView giftImage;
    Switch giftStatus;
    Uri imageUri;
    Bitmap bitmap, bitmapSmaller;
    // DownloadImageTask dl;
    EditText giftLink;
    Context context;
    File chosenFile;
    String imageLink, imageHashDelete, filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gift);
        Log.i(TAG, "AddWishActivity activity is created");
        // прячем клавиатуру при старте активити
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        context = getApplicationContext();

        giftImage = (ImageView) findViewById(R.id.addGiftImage);
        giftTitle = (EditText) findViewById(R.id.giftTitleAddGift);
        giftDescription = (EditText) findViewById(R.id.giftDescriptionAddGift);
        giftStatus = (Switch) findViewById(R.id.switchAddGiftStatus);
        giftLink = (EditText) findViewById(R.id.linkAddGift);

        Drawable myDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.nowishimage, null);
        bitmap = ((BitmapDrawable) myDrawable).getBitmap();
        bitmapSmaller = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
        giftImage.setImageBitmap(bitmap);

        final Button chooseImageButton = (Button) findViewById(R.id.buttonAddGiftChooseImage);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder pictureDialog = new android.support.v7.app.AlertDialog.Builder(view.getContext());
                String[] pictureDialogItems = {
                        getResources().getString(R.string.select_photo_from_gallery),
                        getResources().getString(R.string.select_photo_from_camera)};
                pictureDialog.setItems(pictureDialogItems,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        choosePhotoFromGallery();
                                        break;
                                    case 1:
                                        takePhotoFromCamera();
                                        break;
                                }
                            }
                        });
                AlertDialog alertDialog = pictureDialog.create();
                alertDialog.show();
            }
        });

        final Button cancelButton = (Button) findViewById(R.id.buttonAddGiftCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final Button addButton = (Button) findViewById(R.id.buttonAddGiftAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MY_LIST_HAS_CHANGED = true;
                Log.i(TAG, "Click add button");
                final String title = getGiftTitle();


                if (title.equals("")) {
                    giftTitle.setError(getString(R.string.error_field_required));
                } else if (!getGiftLink().equals("") && (!(getGiftLink().startsWith("http://")) && !(getGiftLink().startsWith("https://")))) {
                    giftLink.setError(getString(R.string.add_gift_link_error));
                } else {
                    if (imageUri == null) {
                        addWish();
                        finish();
                    } else {
                        chosenFile = new File(filePath);
                        uploadImage();
                        finish();
                    }
                }
            }
        });
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }
    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       // File picDir = Environment.getExternalStorageDirectory();
        File picDir = new File(Environment.getExternalStorageDirectory(),"Wish List");
        picDir.mkdir();
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "WISHLIST_" + timeStamp + ".jpg";
        chosenFile = new File(picDir, imageFileName);
        imageUri = FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".provider",chosenFile);
        Log.i(TAG,String.valueOf(imageUri));
        filePath = chosenFile.getAbsolutePath();
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, CAMERA);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY) {
            imageUri = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = this.getContentResolver().query(imageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            Log.i(TAG,filePath);
            cursor.close();
            Glide
                    .with(context)
                    .load(imageUri)
                    .centerCrop()
                    .into(giftImage);
        }

        if (resultCode == RESULT_OK && requestCode == CAMERA) {
            // сохранить фото, получить его путь, загрузить миниатюру и загрузить на сервер
            try {
                Log.i(TAG,filePath);
                Glide
                        .with(context)
                        .load(imageUri)
                        .centerCrop()
                        .into(giftImage);
            } catch (Exception e) {
                Log.i(TAG,e.getMessage());
                Toast.makeText(context,R.string.camera_save_photo_failed,Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getGiftTitle() {
        return giftTitle.getText().toString().trim();
    }

    private String getGiftDescription() {
        return giftDescription.getText().toString().trim();
    }

    private boolean getGiftStatus() {
        return giftStatus.isChecked();
    }

    private String getGiftLink() {
        return giftLink.getText().toString().trim();
    }

    private void uploadImage(){
        ImgurInterface imgurService = ImgurInterface.retrofit.create(ImgurInterface.class);
        final NotificationHelper notificationHelper = new NotificationHelper(this.getApplicationContext());
        notificationHelper.createUploadingNotification();

        final Call<ImgurUploadModel> call =
                imgurService.postImage(
                        "","", "", "",
                        MultipartBody.Part.createFormData(
                                "image",
                                chosenFile.getName(),
                                RequestBody.create(MediaType.parse("image/*"), chosenFile)
                        ));
        call.enqueue(new Callback<ImgurUploadModel>() {
            @Override
            public void onResponse(Call<ImgurUploadModel> call, Response<ImgurUploadModel> response) {
                Log.i(TAG,response.body().getData().getLink());
                Log.i(TAG, chosenFile.getAbsolutePath());
                Log.i(TAG, chosenFile.getName());
                imageLink = response.body().getData().getLink();
                imageHashDelete = response.body().getData().getDeletehash();
                Log.i(TAG, imageHashDelete);
                notificationHelper.createUploadedNotification();
                addWish();
            }

            @Override
            public void onFailure(Call<ImgurUploadModel> call, Throwable t) {
                notificationHelper.createFailedUploadNotification();
            }
        });

    }

    private void addWish(){
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        Call<WishAndMessageModel> call = apiService.addWish(getGiftTitle(), getGiftDescription(),
                getGiftLink().trim(), getGiftStatus(), imageLink, imageHashDelete, currentUser.username, currentUser.id_server);
        call.enqueue(new Callback<WishAndMessageModel>() {
            @Override
            public void onResponse(Call<WishAndMessageModel> call, Response<WishAndMessageModel> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, response.body().getMessage());
                    if (response.body().getSuccess()) {
                        UserProfileFragment.refreshMyList();
                        Toast.makeText(context, "Желание добавлено!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<WishAndMessageModel> call, Throwable t) {
                Log.i(TAG, "Fail!");
            }
        });
    }


}


