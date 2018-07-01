package ru.wishlistapp.wishlist.friendList;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishesModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.start.StartActivity.friends;
import static ru.wishlistapp.wishlist.feed.FeedFragment.refreshFeed;
import static ru.wishlistapp.wishlist.friendList.FriendListFragment.refreshFriendList;

public class FriendProfileActivity extends AppCompatActivity {
    TextView name, bday;
    String userId, username;
    static Context context;
    String TAG = "FriendProfileActivity";
    SwipeRefreshLayout mSwipeRefreshLayout;
    ImageView photoIV;
    private static RecyclerView usersWishesRV;
    public static FriendProfileAdapter adapter;
    List<WishModel> usersWishes;
    boolean flag;
    public static List<WishModel> wishesWanted;
    public static List<WishModel> wishesReceived;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        context = getApplicationContext();

        Intent intent = getIntent();
        name = (TextView) findViewById(R.id.nameTV);
        bday = (TextView) findViewById(R.id.birthdayTV);
        photoIV = (ImageView) findViewById(R.id.profilePhoto);

        userId = intent.getExtras().getString("userId");
        name.setText(intent.getExtras().getString("name"));
        bday.setText(intent.getExtras().getString("bday"));

        if (intent.getExtras().getString("imageLink") != null) {
            Glide
                    .with(context)
                    .load(Uri.parse(intent.getExtras().getString("imageLink")))
                    .centerCrop()
                    .error(R.drawable.profile_pic2)
                    .into(photoIV);
        } else {
            Glide
                    .with(context)
                    .load("")
                    .placeholder(R.drawable.profile_pic2)
                    .centerCrop()
                    .into(photoIV);
        }

        setTitle(intent.getExtras().getString("username"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

       // Toast.makeText(getApplicationContext(),intent.getExtras().getString("userId"),Toast.LENGTH_LONG).show();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutUserProfile);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        usersWishesRV = (RecyclerView) findViewById(R.id.myGiftsRV);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        usersWishesRV.setLayoutManager(llm);

        RecyclerViewHeader header = (RecyclerViewHeader) findViewById(R.id.profileHeader);
        header.attachTo(usersWishesRV);

        refreshList();

        username = intent.getExtras().getString("username").trim();

        flag = false;
        if (friends != null) {
            if (friends.contains(username)) {
                flag = true;
            }
        }

        LinearLayout wantedGift = (LinearLayout) findViewById(R.id.myGiftsWanted);
        wantedGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortWishes();
                wishesWanted.addAll(wishesReceived);
                usersWishes = wishesWanted;
                adapter = new FriendProfileAdapter(usersWishes);
                usersWishesRV.setAdapter(adapter);
            }
        });

        LinearLayout receivedGift = (LinearLayout) findViewById(R.id.myGiftReceivedLL);
        receivedGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortWishes();
                wishesReceived.addAll(wishesWanted);
                usersWishes = wishesReceived;
                adapter = new FriendProfileAdapter(usersWishes);
                usersWishesRV.setAdapter(adapter);
            }
        });
    }

    private void sortWishes() {
        wishesWanted = new ArrayList<>();
        wishesReceived = new ArrayList<>();
        for (int i = 0; i < usersWishes.size(); i++) {
            WishModel wish = usersWishes.get(i);
            if (!wish.getIsReceived()) wishesWanted.add(wish);
            else wishesReceived.add(wish);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (flag) {
            getMenuInflater().inflate(R.menu.friend_profile_settings_remove, menu);
        } else {
            getMenuInflater().inflate(R.menu.friend_profile_settings_add, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.friend_profile_add) {
            final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
            deleteAlert.setTitle("Добавить пользователя в список друзей?");
            deleteAlert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            deleteAlert.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i(TAG,username);

                    WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
                    ApiClient.getClient().create(WishlistInterface.class);
                    try {
                        Call<SuccessAndMessageModel> call = apiService.changeUsersData("addFriend", username,
                                null, null, null, null,null, null, null, currentUser.id_server);
                        call.enqueue(new Callback<SuccessAndMessageModel>() {
                            @Override
                            public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                                if (response.body().getSuccess()) {
                                    Toast.makeText(getApplicationContext(),response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    friends.add(username);
                                    recreate();
                                    refreshFeed();
                                    refreshFriendList();
                                } else {
                                    Toast.makeText(getApplicationContext(),R.string.add_friend_error,
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                                Toast.makeText(getApplicationContext(),getResources().getText(R.string.error_no_connection), Toast.LENGTH_LONG).show();
                                Log.i(TAG, "Fail!");
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            });
            AlertDialog alert1 = deleteAlert.create();
            alert1.show();
            return true;
        }

        if (id == R.id.friend_profile_remove) {
            final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
            deleteAlert.setTitle("Удалить пользователя из списка друзей?");
            deleteAlert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            deleteAlert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
                    ApiClient.getClient().create(WishlistInterface.class);
                    try {
                        Call<SuccessAndMessageModel> call = apiService.changeUsersData("removeFriend", username,
                                null, null, null, null, null, null, null, currentUser.id_server);
                        call.enqueue(new Callback<SuccessAndMessageModel>() {
                            @Override
                            public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                                if (response.body().getSuccess()) {
                                    Toast.makeText(getApplicationContext(),response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    friends.remove(username);
                                    recreate();
                                    refreshFeed();
                                    refreshFriendList();
                                } else {
                                    Toast.makeText(getApplicationContext(),R.string.remove_friend_error,
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                                Toast.makeText(getApplicationContext(),getResources().getText(R.string.error_no_connection), Toast.LENGTH_LONG).show();
                                Log.i(TAG, "Fail!");
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            });
            AlertDialog alert1 = deleteAlert.create();
            alert1.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshList() {
        mSwipeRefreshLayout.setRefreshing(true);
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<WishesModel> call = apiService.getUsersWishes(userId);
            call.enqueue(new Callback<WishesModel>() {
                @Override
                public void onResponse(Call<WishesModel> call, Response<WishesModel> response) {
                    if (response.isSuccessful()) {
                        usersWishes = response.body().getWishes();
                        // Toast.makeText(getApplicationContext(),String.valueOf(usersWishes.isEmpty()),Toast.LENGTH_LONG).show();
                        adapter = new FriendProfileAdapter(usersWishes);
                        usersWishesRV.setAdapter(adapter);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                }

                @Override
                public void onFailure(Call<WishesModel> call, Throwable t) {
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }


    }


}