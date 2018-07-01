package ru.wishlistapp.wishlist.anotherStuff;

import android.Manifest;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.UserManager;
import android.provider.BaseColumns;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.feed.FeedFragment;
import ru.wishlistapp.wishlist.friendList.FriendListFragment;
import ru.wishlistapp.wishlist.friendList.FriendProfileActivity;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.model.UserModel;
import ru.wishlistapp.wishlist.model.UsersModel;
import ru.wishlistapp.wishlist.myProfileAndWishList.EditProfileActivity;
import ru.wishlistapp.wishlist.giftPost.GiftItem;
import ru.wishlistapp.wishlist.myProfileAndWishList.UserProfileFragment;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;
import ru.wishlistapp.wishlist.start.StartActivity;

import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.start.StartActivity.db;

public class UserProfile extends AppCompatActivity {

    private static final String TAG = "UserProfile";
    private RecyclerView myGiftsRV;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static final int ADD_GIFT_REQUEST = 0;
    public static List<GiftItem> gifts;
    public static List<User> users;
    private List<UserModel> results;
    public static final int MY_PERMISSIONS_REQUEST = 1;
    SearchView searchView;
    private static MenuItem myActionMenuItem;
    private CursorAdapter suggestionAdapter;
    public static User currentUser;
    private static Context context;
    private int waitingTime = 500;
    private CountDownTimer cntr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_tabs);

        context = this;

        Intent intent = getIntent();
        boolean isNewUser = intent.getBooleanExtra("isNewUser", false);

        if (isNewUser) {
            String email = intent.getStringExtra("email");
            String username = intent.getStringExtra("username");
            sendMailToConfirm(username, email);
        }

        // gifts = db.getAllGifts();
        users = db.getAllUsers();
        currentUser = users.get(0);

        //Toast.makeText(this,token,Toast.LENGTH_LONG).show();

        Log.i(TAG, String.valueOf(users.size()));

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // заправшиваем разрешение к хранилищу
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        }

        // разрешение к камере
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new UserProfileFragment(), "Мой профиль");
        adapter.addFragment(new FeedFragment(), "Лента");
        adapter.addFragment(new FriendListFragment(), "Друзья");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        myActionMenuItem = menu.findItem(R.id.searchUser);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setQueryHint(getResources().getText(R.string.menu_user_profile_search_user));
        suggestionAdapter = new SimpleCursorAdapter(
                this,
                R.layout.user_search_result,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1},  // from
                new int[]{R.id.searchUsername}, //to
                0);
        searchView.setSuggestionsAdapter(suggestionAdapter);
        searchView.setOnSuggestionListener(onQuerySuggestion);
        searchView.setOnQueryTextListener(onQueryTextListener);
        return true;
    }

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (!query.equals(currentUser.username)) {
                getUser(query.trim());
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
            }
            else  Toast.makeText(getApplicationContext(), R.string.menu_user_profile_search_user_equals, Toast.LENGTH_LONG).show();
            return false;
        }

        @Override
        public boolean onQueryTextChange(final String newText) {
            // текст изменился
            if(cntr != null){
                cntr.cancel();
            }
            cntr = new CountDownTimer(waitingTime, 500) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    if (!newText.equals("")) {
                        new search(newText).execute((Void) null);
                    }
                }
            };
            cntr.start();
            return false;
        }
    };

    private SearchView.OnSuggestionListener onQuerySuggestion = new SearchView.OnSuggestionListener() {
        @Override
        public boolean onSuggestionSelect(int position) {
            return false;
        }

        @Override
        public boolean onSuggestionClick(int position) {
            // переходим на профиль
            Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
            cursor.moveToPosition(position);
            String suggestion = cursor.getString(1);// 2 is the index of col containing suggestion name.
            searchView.setQuery(suggestion,true);// setting suggestion
            return false;
        }
    };

    private void getUser(final String username) {
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<UserModel> call = apiService.getUserByUsername(username);
            call.enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if (response.isSuccessful()) {
                        UserModel user = response.body();
                        if (user.getName() != null) {
                            Intent intent = new Intent(getApplicationContext(), FriendProfileActivity.class);
                            intent.putExtra("name", user.getName());
                            intent.putExtra("bday", user.getBday());
                            intent.putExtra("username", username);
                            intent.putExtra("userId", user.getUserId());
                            intent.putExtra("imageLink", user.getImageLink());
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.menu_user_profile_search_user_fail, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserModel> call, Throwable t) {
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }

    private class search extends AsyncTask<Void, Void, Boolean> {
        String username;

        public search(String username) {
            this.username = username;
        }

        protected Boolean doInBackground(Void... arg0) {
            WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
            ApiClient.getClient().create(WishlistInterface.class);
            try {
                Call<UsersModel> call = apiService.searchUser(username);
                call.enqueue(new Callback<UsersModel>() {
                    @Override
                    public void onResponse(Call<UsersModel> call, Response<UsersModel> response) {
                        if (response.isSuccessful()) {
                            results = response.body().getUsers();
                            String[] columns = {
                                    BaseColumns._ID,
                                    SearchManager.SUGGEST_COLUMN_TEXT_1
                            };

                            MatrixCursor cursor = new MatrixCursor(columns);

                            for (int i = 0; i < results.size(); i++) {
                                String[] tmp = {Integer.toString(i), results.get(i).getUsername()};
                                cursor.addRow(tmp);
                            }
                            suggestionAdapter.changeCursor(cursor);
                        } else
                            Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<UsersModel> call, Throwable t) {
                        Log.i(TAG, "Fail!");
                    }
                });
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        protected void onPostExecute(List results) {
            //
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings_edit_my_profile) {
            Intent intentAddToDoItem = new Intent(this, EditProfileActivity.class);
            //intentAddToDoItem.putExtra("id", id);
            this.startActivity(intentAddToDoItem);
            return true;
        }

        if (id == R.id.logout) {
            final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
            deleteAlert.setTitle("Выйти?");
            deleteAlert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            deleteAlert.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db.deleteUsers();
                    db.deleteAllWishes();
                    Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                    getApplicationContext().startActivity(intent);
                }
            });

            AlertDialog alert1 = deleteAlert.create();
            alert1.show();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static void sendMailToConfirm(String username, String email) {
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        final String emailF = email;
        try {
            Call<SuccessAndMessageModel> call = apiService.sendMailToConfirm(username, email);
            call.enqueue(new Callback<SuccessAndMessageModel>() {
                @Override
                public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                    Log.i(TAG, response.body().getMessage());
                    AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(context);
                    alert.setTitle(emailF);
                    if (response.body().getSuccess()) {
                        alert.setMessage(response.body().getMessage());
                    } else {
                        alert.setMessage(response.body().getMessage());
                    }
                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alert1 = alert.create();
                    alert1.show();
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
