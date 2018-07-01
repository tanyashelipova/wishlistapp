package ru.wishlistapp.wishlist.start;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.anotherStuff.DataBase;
import ru.wishlistapp.wishlist.anotherStuff.User;
import ru.wishlistapp.wishlist.anotherStuff.UserProfile;
import ru.wishlistapp.wishlist.model.UserModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

public class StartActivity extends AppCompatActivity {

    TextView login, register;
    public static DataBase db;
    public static List<User> users;
    public static List<WishModel> wishes;
    public static boolean isOffline;
    private UserLoginTask mAuthTask = null;
    private ImageView logo;
    final static String TAG = "StartActivity";
    static Context context;
    public static ArrayList<String> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getSupportActionBar().hide();

        login = (TextView) findViewById(R.id.startLogin);
        register = (TextView) findViewById(R.id.startRegister);
        logo = (ImageView) findViewById(R.id.logoImageView);

        context = getApplicationContext();
        db = new DataBase(context);

        users = db.getAllUsers();
        User user;
        if (users.size() > 0) {
            user = users.get(0);
//            Log.i(TAG, user.username);
            if (user.username != null & user.password != null) {
                mAuthTask = new UserLoginTask(user.username, user.password);
                mAuthTask.execute((Void) null);
            }
        }

        else {
            start();
         }
    }

    public void start(){
        login.setVisibility(View.VISIBLE);
        register.setVisibility(View.VISIBLE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
            ApiClient.getClient().create(WishlistInterface.class);
            try {
                Call<UserModel> call = apiService.login(mUsername, mPassword);
                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        if(response.isSuccessful()) {
//                          Log.i(TAG, response.body().getMessage());
                            Log.i(TAG, String.valueOf(response.body().getSuccess()));
                            if (response.body().getSuccess()){
                                friends = response.body().getFriends();
                                Log.i(TAG, String.valueOf(response.body().getImageLink()));
                                Log.i(TAG, String.valueOf(response.body().getName()));
                                openProfile();
                                finish();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        Toast.makeText(context,R.string.error_no_connection,Toast.LENGTH_LONG).show();
                        wishes = db.getAllWishes();
                        isOffline = true;
                        Intent intent = new Intent(context, UserProfile.class);
                        context.startActivity(intent);
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
        intent.putExtra("isNewUser", false);
        context.startActivity(intent);
        finish();
    }
}
