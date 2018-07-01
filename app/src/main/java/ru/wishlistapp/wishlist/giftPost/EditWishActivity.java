package ru.wishlistapp.wishlist.giftPost;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.anotherStuff.NotificationHelper;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurDeleteModel;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurUploadModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishAndMessageModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.myProfileAndWishList.UserProfileFragment;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ImgurInterface;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.giftPost.AddWishActivity.CAMERA;
import static ru.wishlistapp.wishlist.giftPost.AddWishActivity.GALLERY;
import static ru.wishlistapp.wishlist.giftPost.WishPostActivity.contextGiftPost;
import static ru.wishlistapp.wishlist.giftPost.WishPostActivity.pic;
import static ru.wishlistapp.wishlist.giftPost.WishPostActivity.swipeRefreshLayout;
import static ru.wishlistapp.wishlist.giftPost.AddWishActivity.MY_LIST_HAS_CHANGED;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

public class EditWishActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    private static final String TAG = "EditGiftPosttActivity";
    EditText giftTitle, giftDescription;
    ImageView giftImage;
    Switch giftStatus;
    Uri imageUri;
    Bitmap bitmap;
    WishModel currentGift;
    Context context;
    EditText giftLink;
    String giftId, imageLink, imageHashDelete, filePath;
    boolean activityCloses;
    File chosenFile;
    AlertDialog.Builder ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MY_LIST_HAS_CHANGED = false;
        setContentView(R.layout.activity_edit_gift);
        context = this;

        final Intent giftDataIntent = getIntent();
        giftId = giftDataIntent.getExtras().getString("id");

        // прячем клавиатуру при запуске
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        giftImage = (ImageView) findViewById(R.id.editGiftImage);
        giftTitle = (EditText) findViewById(R.id.giftTitleEditGift);
        giftDescription = (EditText) findViewById(R.id.giftDescriptionEditGift);
        giftStatus = (Switch) findViewById(R.id.switchEditGistStatus);
        giftLink = (EditText) findViewById(R.id.linkEditGift);

        setCurrentData();

        // выбор картинки
        final Button chooseImageButton = (Button) findViewById(R.id.buttonEditGiftChooseImage);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                            ad = new android.support.v7.app.AlertDialog.Builder(context);
                            String[] pictureDialogItems = {
                                    getResources().getString(R.string.select_photo_from_gallery),
                                    getResources().getString(R.string.select_photo_from_camera),
                                    getResources().getString(R.string.delete) };
                            ad.setItems(pictureDialogItems,
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
                                                case 2:
                                                    removePhoto();
                                                    break;
                                            }
                                        }
                                    });
                            AlertDialog alertDialog = ad.create();
                            alertDialog.show();
                        }
        });

        // отмена
        final Button cancelButton = (Button) findViewById(R.id.buttonEditGiftCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final Button saveButton = (Button) findViewById(R.id.buttonEditGiftSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MY_LIST_HAS_CHANGED = true;
                final String title = getGiftTitle();
                if (title.equals("")) {
                    giftTitle.setError(getString(R.string.error_field_required));
                } else if (!getGiftLink().equals("") && (!(getGiftLink().startsWith("http://")) && !(getGiftLink().startsWith("https://")))) {
                    giftLink.setError(getString(R.string.add_gift_link_error));
                } else {
                    if (imageUri == null) {
                        updateWish();
                        finish();
                    } else {
                        chosenFile = new File(filePath);
                        activityCloses = true;
                        uploadImage();
                        finish();
                    }
                }
            }
        });
    }

    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //File picDir = Environment.getExternalStorageDirectory();
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

    private void removePhoto(){
        if (imageLink != null) {
            AlertDialog.Builder deleteAlert = new android.support.v7.app.AlertDialog.Builder(context);
            deleteAlert.setTitle(R.string.delete_photo_apr);
            deleteAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            deleteAlert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    imageLink = null;
                    imageHashDelete = null;
                    chosenFile = null;
                    imageUri = null;
                    activityCloses = false;
                    deleteImage();
                }
            });
            AlertDialog alert1 = deleteAlert.create();
            alert1.show();
        }
    }
    public void setCurrentData() {
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<WishAndMessageModel> call = apiService.getWishById(giftId);
            call.enqueue(new Callback<WishAndMessageModel>() {
                @Override
                public void onResponse(Call<WishAndMessageModel> call, Response<WishAndMessageModel> response) {
                    Log.i(TAG, "onResponse");
                    if (response.isSuccessful()) {
                        currentGift = response.body().getWish();
                        giftTitle.setText(currentGift.getTitle());
                        giftTitle.setSelection(currentGift.getTitle().length());
                        giftDescription.setText(currentGift.getContent());
                        if (currentGift.getIsReceived()) giftStatus.setChecked(true);
                        else giftStatus.setChecked(false);

                        if (!(currentGift.getLink().equals(""))) {
                            giftLink.setText(currentGift.getLink());
                        }

                        imageLink = currentGift.getImageLink();
                        imageHashDelete = currentGift.getImageHashDelete();
                        if (imageLink != null) {
                            Glide
                                    .with(context)
                                    .load(Uri.parse(imageLink))
                                    .centerCrop()
                                    .error(R.drawable.nowishimage)
                                    .into(giftImage);
                        } else {
                            Glide
                                    .with(context)
                                    .load("")
                                    .placeholder(R.drawable.nowishimage)
                                    .centerCrop()
                                    .into(giftImage);
                        }
                    }
                    else{
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<WishAndMessageModel> call, Throwable t) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY) {
            imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
            }

            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = this.getContentResolver().query(imageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            Log.i(TAG,filePath);
            cursor.close();
            chosenFile = new File(filePath);

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

    private void deleteImage(){
        ImgurInterface imgurService = ImgurInterface.retrofit.create(ImgurInterface.class);
        final Call<ImgurDeleteModel> callDelete =
                imgurService.deleteImage(currentGift.getImageHashDelete());
        callDelete.enqueue(new Callback<ImgurDeleteModel>() {
            @Override
            public void onResponse(Call<ImgurDeleteModel> call, Response<ImgurDeleteModel> response) {
                Log.i(TAG,"Image deleted: " + String.valueOf(response.body().getSuccess()));
                if (!activityCloses) {
                    Glide
                            .with(context)
                            .load("")
                            .placeholder(R.drawable.nowishimage)
                            .centerCrop()
                            .into(giftImage);
                    Toast.makeText(context, R.string.edit_profile_photo_removed, Toast.LENGTH_SHORT).show();
                }
                updateWish();
            }
            @Override
            public void onFailure(Call<ImgurDeleteModel> call, Throwable t) {
                imageLink = currentGift.getImageLink();
                imageHashDelete = currentGift.getImageHashDelete();
                Log.i(TAG,"failed to delete photo");
                Log.i(TAG,t.getMessage());
                Log.i(TAG, imageHashDelete);
                Log.i(TAG, imageLink);
            }
        });
    }

    private void uploadImage(){
        ImgurInterface imgurService = ImgurInterface.retrofit.create(ImgurInterface.class);
        final NotificationHelper notificationHelper = new NotificationHelper(this.getApplicationContext());
        notificationHelper.createUploadingNotification();
        // сначала удалим старое изображение
        deleteImage();
        // загрузим новое
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
                notificationHelper.createUploadedNotification();
                updateWish();
            }

            @Override
            public void onFailure(Call<ImgurUploadModel> call, Throwable t) {
                notificationHelper.createFailedUploadNotification();
            }
        });

    }

    private void updateWish(){
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<WishAndMessageModel> call = apiService.updateWish(giftId, getGiftTitle(), getGiftDescription(),
                    getGiftLink(), getGiftStatus(), imageLink, imageHashDelete);
            call.enqueue(new Callback<WishAndMessageModel>() {
                @Override
                public void onResponse(Call<WishAndMessageModel> call, Response<WishAndMessageModel> response) {
                    Log.i(TAG, "onResponse");
                    if (response.body().getSuccess()) {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                        UserProfileFragment.adapter.notifyDataSetChanged();
                        Intent intent = new Intent(getBaseContext(), WishPostActivity.class);
                        intent.putExtra("id", giftId);
                        intent.putExtra("menu", true);
                        swipeRefreshLayout.setRefreshing(false);
                        Glide
                                .with(contextGiftPost)
                                .load(imageLink)
                                .centerCrop()
                                .into(pic);
                    } else {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<WishAndMessageModel> call, Throwable t) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }
}
