package ru.wishlistapp.wishlist.start;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.start.StartActivity.db;
import static ru.wishlistapp.wishlist.start.StartActivity.friends;

public class LoginActivity extends AppCompatActivity {

    static Context context;
    Button mLoginButton;
    private UserLoginTask mAuthTask = null;
    TextView mUsernameOrEmailView, mPasswordView, mForgotPassword;
    static String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        mUsernameOrEmailView = (EditText) findViewById(R.id.usernameOrEmailLogin);
        mPasswordView = (EditText) findViewById(R.id.passwordLogin);
        mLoginButton = (Button) findViewById(R.id.buttonLogin);
        mForgotPassword = (TextView) findViewById(R.id.forgotPassword);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void attemptLogin(){

        String username = mUsernameOrEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;

        if (TextUtils.isEmpty(username)) {
            mUsernameOrEmailView.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (!cancel) {
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }


    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsernameOrEmail;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsernameOrEmail = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
            ApiClient.getClient().create(WishlistInterface.class);
            try {
                Log.i(TAG,"login");
                Call<UserModel> call = apiService.login(mUsernameOrEmail, mPassword);
                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        if(response.isSuccessful()) {
//                          Log.i(TAG, response.body().getMessage());
                            Log.i(TAG, String.valueOf(response.body().getSuccess()));
                            if (response.body().getSuccess()){
                                User user = new User();
                                user.username = response.body().getUsername();
                                user.password = mPassword;
                                user.id_server = response.body().getUserId();
                                user.name = response.body().getName();
                                user.bday = response.body().getBday();
                                user.email = response.body().getEmail();
                                user.imageLink = response.body().getImageLink();
                                user.imageHashDelete = response.body().getImageHashDelete();
                                friends = response.body().getFriends();
                                if (db.getAllUsers().size() > 1){
                                    db.deleteUsers();
                                    db.addUser(user);
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

    private void openProfile(){
        Intent intent = new Intent(context, UserProfile.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isNewUser", false);
        context.startActivity(intent);
        finish();
    }
}
