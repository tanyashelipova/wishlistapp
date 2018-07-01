package ru.wishlistapp.wishlist.myProfileAndWishList;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.anotherStuff.NotificationHelper;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurDeleteModel;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurUploadModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ImgurInterface;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;
import ru.wishlistapp.wishlist.start.StartActivity;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.sendMailToConfirm;
import static ru.wishlistapp.wishlist.myProfileAndWishList.UserProfileFragment.myList;
import static ru.wishlistapp.wishlist.myProfileAndWishList.UserProfileFragment.setUsersData;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.start.StartActivity.db;

public class EditProfileActivity extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    private static final String TAG = "EditProfileActivity";
    EditText name, username, email;
    TextView password, newPassword, newPassword2;
    static EditText bday;
    ImageView photo;
    Button choosePhoto, cancel, save;
    Uri imageUri;
    boolean newUser, photoDeleted;
    File chosenFile;
    static Context context;
    static String bdayString, filePath, imageLink, imageHashDelete;
    String nameStr, usernameStr, emailStr, passwordStr;
    AlertDialog.Builder ad;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_profile);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        context = this;
        //setDefaultProfilePhoto();
        photoDeleted = false;

        Intent intent = getIntent();
        newUser = intent.getBooleanExtra("newUser", false);
        Log.i(TAG, String.valueOf(newUser));

        bday = (EditText) findViewById(R.id.bdayEditProfile);
        choosePhoto = (Button) findViewById(R.id.buttonEditProfileChooseImage);
        photo = (ImageView) findViewById(R.id.profileImageProfileEdit);
        cancel = (Button) findViewById(R.id.buttonEditProfileCancel);
        save = (Button) findViewById(R.id.buttonEditProfileSave);
        name = (EditText) findViewById(R.id.nameEditProfile);
        username = (EditText) findViewById(R.id.usernameEditProfile);
        email = (EditText) findViewById(R.id.emailEditProfile);

        if (currentUser != null) {
            name.setText(currentUser.name);
            name.setSelection(currentUser.name.length());
            username.setText(currentUser.username);
            email.setText(currentUser.email);
            bday.setText(currentUser.bday);
            bdayString = currentUser.bday;
            imageLink = currentUser.imageLink;
            imageHashDelete = currentUser.imageHashDelete;
            if (imageLink != null) {
                Glide
                        .with(context)
                        .load(Uri.parse(imageLink))
                        .centerCrop()
                        .error(R.drawable.profile_pic2)
                        .into(photo);
            } else {
                Glide
                        .with(context)
                        .load("")
                        .placeholder(R.drawable.profile_pic2)
                        .centerCrop()
                        .into(photo);
            }
        }

        passwordStr = null;

        // выбор фото
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // алерт для выбора фото
                if (currentUser.imageLink != null) {
                    ad = new android.support.v7.app.AlertDialog.Builder(context);
                    ad.setPositiveButton(R.string.change_photo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            startActivityForResult(choosePhotoIntent, PICK_IMAGE);
                        }
                    });
                    ad.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
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
                                    deleteImage(currentUser.imageHashDelete,true);
                                }
                            });
                            AlertDialog alert1 = deleteAlert.create();
                            alert1.show();
                        }
                    });
                    AlertDialog alert1 = ad.create();
                    alert1.show();
                } else {
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(choosePhotoIntent, PICK_IMAGE);
                }
            }
        });


        // выбор даты рождения
        bday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        // отмена
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // сохранить
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameStr = name.getText().toString();
                usernameStr = username.getText().toString();
                emailStr = email.getText().toString();
                // Если данные были изменены
                if (!TextUtils.equals(currentUser.name,nameStr) || !TextUtils.equals(currentUser.username, usernameStr) ||
                        !TextUtils.equals(currentUser.email, emailStr) || !TextUtils.equals(currentUser.bday, bdayString) ||
                        (imageUri != null || photoDeleted)) {
                    if (nameStr.equals("") || usernameStr.equals("") || emailStr.equals("") || bdayString.equals("")) {
                        Toast.makeText(view.getContext(), "Все поля обязательны к заполнению!", Toast.LENGTH_LONG).show();
                        if (!emailStr.contains("@"))
                            email.setError(getString(R.string.error_email_incorrect));
                    } else {
                        if (imageUri == null) {
                            changeUsersData(false);
                            finish();
                        } else {
                            chosenFile = new File(filePath);
                            uploadImage();
                            finish();
                        }
                    }  // Иначе просто закрываем активити
                } else {
                    finish();
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_profile_menu_change_password) {
            // спрашиваем текущий пароль и новый пароль
            // если все ок, то обновляем данные на сервере и в локальной базе
            ad = new AlertDialog.Builder(context);
            ad.setTitle(R.string.edit_profile_change_password);
            LayoutInflater inflater = this.getLayoutInflater();
            View view = inflater.inflate(R.layout.change_password_dialog, null);
            ad.setView(view);
            password = (EditText) view.findViewById(R.id.change_password);
            newPassword = (EditText) view.findViewById(R.id.change_password_new);
            newPassword2 = (EditText) view.findViewById(R.id.change_password_new2);
            ad.setPositiveButton(R.string.new_password_submit, null);
            ad.setNeutralButton(R.string.cancel, null);
            final AlertDialog alert = ad.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button ok = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (checkPassword()){
                                // Отправляем данные на сервер
                                passwordStr = newPassword.getText().toString();
                                changeUsersData(true);
                                alert.dismiss();
                            } else {
                                passwordStr = null;
                            }
                        }
                    });

                    Button cancel = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();
                        }
                    });

                }
            });
            alert.show();
            return true;
        }
        if (id == R.id.edit_profile_menu_confirm_email) {
            finish();
            sendMailToConfirm(currentUser.username, currentUser.email);
            return true;
        }
        //
        if (id == R.id.edit_profile_menu_delete) {
            ad = new android.support.v7.app.AlertDialog.Builder(context);
            ad.setTitle(R.string.edit_profile_delete);
            ad.setMessage(R.string.delete_profile_confirm);
            // При подтверждении вызываем нужные методы
            ad.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Если список желаний не пуст, то удаляем желания
                    if (myList != null) {
                        // Удаляем изображения, если они есть
                        for (int j = 0; j < myList.size(); j++) {
                            if (myList.get(j).getImageLink() != null & myList.get(j).getImageHashDelete() != null) {
                                Log.i(TAG, myList.get(j).getImageLink());
                                deleteImage(myList.get(j).getImageHashDelete(), false);
                            }
                        }
                        // Удаляем желания
                        deleteUsersWishes();
                    }
                    // Удаляем фото профиля, если оно есть
                    if (currentUser.imageHashDelete != null) {
                        deleteImage(currentUser.imageHashDelete, false);
                    }
                    // Удаляем профиль из базы
                    deleteUser();
                }
            });
            // При нажатии на кпоку "Отмена" закрываем диалоговое окно
            ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // ничего не делаем
                }
            });
            AlertDialog alert = ad.create();
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkPassword(){
        if (TextUtils.isEmpty(password.getText())) {
            password.setError(getText(R.string.error_field_required));
            return false;
        }
        if (!TextUtils.equals(password.getText(), currentUser.password)) {
            password.setError(getText(R.string.incorrect_password));
            return false;
        }
        if (TextUtils.isEmpty(newPassword.getText())) {
            newPassword.setError(getText(R.string.error_field_required));
            return false;
        }
        if (!TextUtils.equals(newPassword.getText(), newPassword2.getText())) {
            newPassword2.setError(getText(R.string.error_password_mismatch));
            return false;
        }
        if (newPassword.getText().toString().length() < 8) {
            newPassword.setError(getText(R.string.error_invalid_password));
            return false;
        }
        return  true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = this.getContentResolver().query(imageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            Log.i(TAG, filePath);
            cursor.close();

            Glide
                    .with(context)
                    .load(imageUri)
                    .centerCrop()
                    .into(photo);
        }
    }


    // выбор дня рождения
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Исполльзуйте текущую дату в качестве значения по умолчанию
            int year = 1990;
            int month = 1;
            int day = 1;

            if (currentUser != null) {
                year = Integer.valueOf(bdayString.substring(6, 10));
                month = Integer.valueOf(bdayString.substring(3, 5)) - 1;
                day = Integer.valueOf(bdayString.substring(0, 2));
            }

            // задаем максимальную дату
            Calendar c = Calendar.getInstance();
            c.set(2010, 0, 1);//Year,Mounth -1,Day
            // Создать новый экземпляр DatePickerDialog и вернуть его
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            return dialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            setDateString(year, monthOfYear, dayOfMonth);
            bday.setText(bdayString);
        }

    }

    private static void setDateString(int year, int monthOfYear, int dayOfMonth) {

        monthOfYear++;
        String mon = "" + monthOfYear;
        String day = "" + dayOfMonth;

        if (monthOfYear < 10)
            mon = "0" + monthOfYear;
        if (dayOfMonth < 10)
            day = "0" + dayOfMonth;

        bdayString = day + "/" + mon + "/" + year;
    }

    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void uploadImage() {
        ImgurInterface imgurService = ImgurInterface.retrofit.create(ImgurInterface.class);
        final NotificationHelper notificationHelper = new NotificationHelper(this.getApplicationContext());
        notificationHelper.createUploadingNotification();

        final Call<ImgurUploadModel> call =
                imgurService.postImage(
                        "", "", "", "",
                        MultipartBody.Part.createFormData(
                                "image",
                                chosenFile.getName(),
                                RequestBody.create(MediaType.parse("image/*"), chosenFile)
                        ));
        call.enqueue(new Callback<ImgurUploadModel>() {
            @Override
            public void onResponse(Call<ImgurUploadModel> call, Response<ImgurUploadModel> response) {
                Log.i(TAG, response.body().getData().getLink());
                Log.i(TAG, chosenFile.getAbsolutePath());
                Log.i(TAG, chosenFile.getName());
                imageLink = response.body().getData().getLink();
                imageHashDelete = response.body().getData().getDeletehash();
                notificationHelper.createUploadedNotification();
                changeUsersData(false);
            }

            @Override
            public void onFailure(Call<ImgurUploadModel> call, Throwable t) {
                notificationHelper.createFailedUploadNotification();
            }
        });

    }

    private void changeUsersData(final boolean isPassword) {
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<SuccessAndMessageModel> call = apiService.changeUsersData("changeData",null,
                    nameStr, usernameStr, emailStr, passwordStr, bdayString, imageLink, imageHashDelete, currentUser.id_server);
            call.enqueue(new Callback<SuccessAndMessageModel>() {
                @Override
                public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                    if (response.body().getSuccess()) {
                        if (isPassword) {
                            if (!currentUser.password.equals(passwordStr)) currentUser.password = passwordStr;
                        } else {
                            if (!currentUser.email.equals(emailStr) & emailStr != null) {
                                sendMailToConfirm(usernameStr, emailStr);
                                currentUser.email = emailStr;
                            }
                            currentUser.name = nameStr;
                            currentUser.username = usernameStr;
                            currentUser.bday = bdayString;
                            currentUser.imageLink = imageLink;
                            currentUser.imageHashDelete = imageHashDelete;
                        }

                        db.deleteUsers();
                        db.addUser(currentUser);
                        setUsersData();

                        if (!photoDeleted) {
                            Toast.makeText(getApplicationContext(), R.string.data_changed, Toast.LENGTH_SHORT).show();
                            photoDeleted = false;
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), response.body().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                    Toast.makeText(context, getResources().getText(R.string.error_no_connection), Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }

    private void deleteImage(String hashDelete, final boolean isUserPic) {
        ImgurInterface imgurService = ImgurInterface.retrofit.create(ImgurInterface.class);
        final Call<ImgurDeleteModel> callDelete =
                imgurService.deleteImage(hashDelete);
        callDelete.enqueue(new Callback<ImgurDeleteModel>() {
            @Override
            public void onResponse(Call<ImgurDeleteModel> call, Response<ImgurDeleteModel> response) {
                if (response.isSuccessful()) {
                    if (isUserPic) {
                        currentUser.imageLink = null;
                        currentUser.imageHashDelete = null;
                        imageLink = null;
                        imageHashDelete = null;
                        imageUri = null;
                        Log.i(TAG, "lalal");
                        photoDeleted = true;
                        changeUsersData(false);
                        Toast.makeText(context, R.string.edit_profile_photo_removed, Toast.LENGTH_LONG).show();
                        Glide
                                .with(context)
                                .load("")
                                .placeholder(R.drawable.profile_pic2)
                                .centerCrop()
                                .into(photo);
                    }
                }
            }

            @Override
            public void onFailure(Call<ImgurDeleteModel> call, Throwable t) {
                Toast.makeText(context, getResources().getText(R.string.error_no_connection), Toast.LENGTH_LONG).show();
                Log.i(TAG, t.getMessage());
            }
        });
    }

    private void deleteUsersWishes(){
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<SuccessAndMessageModel> call = apiService.deleteUsersWishes(currentUser.id_server);
            Log.i(TAG,String.valueOf(call.toString()));
            call.enqueue(new Callback<SuccessAndMessageModel>() {
                @Override
                public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                    if (response.isSuccessful()) {
                        db.deleteAllWishes();
                    }
                }

                @Override
                public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }

    private void deleteUser(){
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<SuccessAndMessageModel> call = apiService.deleteUser(currentUser.id_server, currentUser.email);
            Log.i(TAG,String.valueOf(call.toString()));
            call.enqueue(new Callback<SuccessAndMessageModel>() {
                @Override
                public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                        db.deleteUsers();
                        // закрывает все активити
                        finishAffinity();
                        Intent intent = new Intent(context, StartActivity.class);
                        startActivity(intent);
                        Toast.makeText(context,response.body().getMessage(),Toast.LENGTH_LONG).show();
                }
                @Override
                public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }
}
