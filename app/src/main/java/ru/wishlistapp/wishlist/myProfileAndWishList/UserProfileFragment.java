package ru.wishlistapp.wishlist.myProfileAndWishList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import ru.wishlistapp.wishlist.giftPost.AddWishActivity;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishesModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.users;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.giftPost.AddWishActivity.MY_LIST_HAS_CHANGED;
import static ru.wishlistapp.wishlist.start.StartActivity.db;
import static ru.wishlistapp.wishlist.start.StartActivity.wishes;

public class UserProfileFragment extends Fragment {

    private static RecyclerView myGiftsRV;
    private static final String TAG = "UserProfileFragment";
    private static final int ADD_GIFT_REQUEST = 0;
    public static UserProfileAdapter adapter;
    public static Context context;
    public static String currentData;
    static TextView name, username, email, bday;
    static ImageView photoIV;
    static public List<WishModel> myList;

    public static List<WishModel> wishesWanted;
    public static List<WishModel> wishesReceived;
    static SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();
    }

    public void onResume(){
        super.onResume();
        if (MY_LIST_HAS_CHANGED) {
            refreshMyList();
            MY_LIST_HAS_CHANGED = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_my_profile, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // обновление данных
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayoutUserProfile);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMyList();
            }
        });

        refreshMyList();

        name = (TextView) getView().findViewById(R.id.nameTV);
        bday = (TextView) getView().findViewById(R.id.birthdayTV);
        photoIV = (ImageView) getView().findViewById(R.id.profilePhoto);
        myGiftsRV = (RecyclerView) getView().findViewById(R.id.myGiftsRV);

        setUsersData();

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        myGiftsRV.setLayoutManager(llm);

        RecyclerViewHeader header = (RecyclerViewHeader) getView().findViewById(R.id.profileHeader);
        header.attachTo(myGiftsRV);

        // плавающая кнопка добавления нового подарка
        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentAddToDoItem = new Intent(getContext(), AddWishActivity.class);
                startActivityForResult(intentAddToDoItem, ADD_GIFT_REQUEST);
            }
        });
        fab.setCompatElevation(100);

        //сортировка

        LinearLayout wantedGift = (LinearLayout) getView().findViewById(R.id.myGiftsWanted);
        wantedGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myList != null) {
                    sortWishes();
                    wishesWanted.addAll(wishesReceived);
                    myList = wishesWanted;
                    adapter = new UserProfileAdapter(myList);
                    myGiftsRV.setAdapter(adapter);
                }
            }
        });

        LinearLayout receivedGift = (LinearLayout) getView().findViewById(R.id.myGiftReceivedLL);
        receivedGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myList != null) {
                    sortWishes();
                    wishesReceived.addAll(wishesWanted);
                    myList = wishesReceived;
                    adapter = new UserProfileAdapter(myList);
                    myGiftsRV.setAdapter(adapter);
                }
            }
        });

    }

    private void sortWishes() {
        wishesWanted = new ArrayList<>();
        wishesReceived = new ArrayList<>();
        for (int i = 0; i < myList.size(); i++) {
            WishModel wish = myList.get(i);
            if (!wish.getIsReceived()) wishesWanted.add(wish);
            else wishesReceived.add(wish);
        }
    }


    public static void setUsersData(){
        if (users.size() > 0) {
            name.setText(currentUser.name);
            bday.setText(currentUser.bday);
            if (currentUser.imageLink != null) {
                Glide
                        .with(context)
                        .load(Uri.parse(currentUser.imageLink))
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

        }
    }


    public static void refreshMyList() {
        Log.i(TAG, "зашли в Refresh");
        mSwipeRefreshLayout.setRefreshing(true);
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<WishesModel> call = apiService.getUsersWishes(currentUser.id_server);
            Log.i(TAG,String.valueOf(call.toString()));
            call.enqueue(new Callback<WishesModel>() {
                @Override
                public void onResponse(Call<WishesModel> call, Response<WishesModel> response) {
                    if (response.isSuccessful()) {
                        myList = response.body().getWishes();
                        db.deleteAllWishes();
                        Log.i(TAG, String.valueOf(myList.size()));
                        for (int i = myList.size() - 1; i >= 0; i--) {
                            Log.i(TAG, myList.get(i).getContent());
                            db.addWish(myList.get(i));
                        }

                        adapter = new UserProfileAdapter(myList);
                        myGiftsRV.setAdapter(adapter);

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<WishesModel> call, Throwable t) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                    wishes = db.getAllWishes();
                    if (wishes.size() > 0) {
                        adapter = new UserProfileAdapter(wishes);
                        myGiftsRV.setAdapter(adapter);
                    } else {
                        wishes = new ArrayList<>();
                        adapter = new UserProfileAdapter(wishes);
                        myGiftsRV.setAdapter(adapter);
                    }
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }

    }

}