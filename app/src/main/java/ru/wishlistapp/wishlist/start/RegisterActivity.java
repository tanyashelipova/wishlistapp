package ru.wishlistapp.wishlist.start;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.anotherStuff.User;
import ru.wishlistapp.wishlist.model.UserModel;
import ru.wishlistapp.wishlist.anotherStuff.UserProfile;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.start.StartActivity.db;
import static ru.wishlistapp.wishlist.start.StartActivity.friends;

public class RegisterActivity extends AppCompatActivity {

    static Context context;
    Button mRegisterButton;
    private UserRegisterTask mAuthTask = null;
    static EditText mUsernameView, mPasswordView, mPassword2View, mNameView, mEmailView, mBdayView;
    static Date mBday;
    static String stringBD;
    static String TAG = "RegisterActivity";
    static String imageLink, imageHashDelete;
    static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = getApplicationContext();
        activity = this;
        mUsernameView = (EditText) findViewById(R.id.username);
        mNameView = (EditText) findViewById(R.id.registerName);
        mEmailView = (EditText) findViewById(R.id.registerEmail);
        mBdayView = (EditText) findViewById(R.id.bday);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPassword2View = (EditText) findViewById(R.id.password2);

        mBdayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        mRegisterButton = (Button) findViewById(R.id.buttonRegister);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        imageLink = null;
        imageHashDelete = null;
    }


    // Выбор ДР
    private static void setDateString(int year, int monthOfYear, int dayOfMonth) {

        // Increment monthOfYear for Calendar/Date -> Time Format setting
        monthOfYear++;
        String mon = "" + monthOfYear;
        String day = "" + dayOfMonth;

        if (monthOfYear < 10)
            mon = "0" + monthOfYear;
        if (dayOfMonth < 10)
            day = "0" + dayOfMonth;

        stringBD = day + "/" + mon + "/" + year;
       // mBday = stringToDate(stringBD);
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int year = 1990;
            int month = 1;
            int day = 1;

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
            mBdayView.setText(stringBD);
        }
    }

    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    // Выбор ДР


    private void attemptRegister(){

        String username = mUsernameView.getText().toString();
        String name = mNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String bday = mBdayView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password2 = mPassword2View.getText().toString();

        boolean cancel = false;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (TextUtils.isEmpty(bday)) {
            mBdayView.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (TextUtils.isEmpty(password2)) {
            mPassword2View.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) & !TextUtils.isEmpty(password2)) {
            if (!password.equals(password2)){
                mPassword2View.setError(getString(R.string.error_password_mismatch));
                cancel = true;
            }
        }

        if (!email.contains("@")){
            mEmailView.setError(getString(R.string.error_email_incorrect));
        }

        if (!cancel) {
            mAuthTask = new UserRegisterTask(username, name, email, password, password2);
            mAuthTask.execute((Void) null);
        }
    }


    public static class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mName;
        private final String mEmail;
        private final String mPassword;
        private final String mPassword2;

        UserRegisterTask(String username, String name, String email, String password, String password2) {
            mUsername = username.trim();
            mName = name.trim();
            mEmail = email.trim();
            mPassword = password.trim();
            mPassword2 = password2.trim();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
            ApiClient.getClient().create(WishlistInterface.class);
            try {
                Call<UserModel> call = apiService.register(mName, mUsername, mEmail, stringBD
                        , mPassword, mPassword2, imageLink,imageHashDelete);
                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        if(response.isSuccessful()) {
                            Log.i(TAG, response.body().getMessage());
                            Log.i(TAG, String.valueOf(response.body().getSuccess()));
                            if (response.body().getSuccess()){
                                User user = new User();
                                user.username = mUsername;
                                user.password = mPassword;
                                user.name = mName;
                                user.email = mEmail;
                                user.bday = stringBD;
                               // Toast.makeText(context,response.body().getToken(),Toast.LENGTH_LONG).show();
                                user.id_server = response.body().getUserId();

                                user.imageLink = imageLink;
                                user.imageHashDelete = imageHashDelete;

                                if (friends != null) {
                                    friends.clear();
                                }
                                if (db.getAllUsers().size() > 1){
                                    db.updateUser(user);
                                } else {
                                    db.addUser(user);
                                }
                                openProfile();
                            } else {
                                Toast.makeText(context,response.body().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        Toast.makeText(context,R.string.error_no_connection,Toast.LENGTH_LONG).show();
                        Log.i(TAG,"Fail!");
                    }
                });
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
        }

        @Override
        protected void onCancelled() {
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }


    private static void openProfile(){
        Intent intent = new Intent(context, UserProfile.class);
        intent.putExtra("isNewUser", true);
        intent.putExtra("email", mEmailView.getText().toString().trim());
        intent.putExtra("username", mUsernameView.getText().toString().trim());
        context.startActivity(intent);
        activity.finish();
    }


}