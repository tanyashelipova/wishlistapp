package ru.wishlistapp.wishlist.feed;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishesModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.start.StartActivity.friends;


public class FeedFragment extends Fragment {
    private static List<WishModel> wishesFeed;
    private static RecyclerView wishesFeedRV;
    private static String friendsReq;
    static String TAG = "FeedFragment";
    static SwipeRefreshLayout swipeRefreshLayout;
    static FeedAdapter adapter;
    static Context context;
    static TextView feedEmptyMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycleview_feed, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayoutFeed);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });
        feedEmptyMessage = (TextView) getView().findViewById(R.id.feedEmptyMessage);
        wishesFeedRV = (RecyclerView) getView().findViewById(R.id.giftsFeedRV);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        wishesFeedRV.setLayoutManager(llm);
        refreshFeed();

       /* initializeData();
        initializeAdapter();*/
    }


    public static void refreshFeed() {
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        swipeRefreshLayout.setRefreshing(true);
        friendsReq = "";
        if (friends != null) {
            for (int i = 0; i < friends.size(); i++) {
                if (i != friends.size() - 1) {
                    friendsReq = friendsReq + friends.get(i) + ",";
                } else {
                    friendsReq = friendsReq + friends.get(i);
                }
            }
        }

        try {
            Call<WishesModel> call = apiService.getFriendsWishes(friendsReq);
            call.enqueue(new Callback<WishesModel>() {
                @Override
                public void onResponse(Call<WishesModel> call, Response<WishesModel> response) {
                    swipeRefreshLayout.setRefreshing(false);
                    if (response.body().getSuccess()) {
                        wishesFeed = response.body().getWishes();
                        // Если получили не пустой список с подарками, то заполняем ленту
                        if (wishesFeed.size() > 0) {
                            adapter = new FeedAdapter(wishesFeed);
                            wishesFeedRV.setAdapter(adapter);
                            wishesFeedRV.setVisibility(View.VISIBLE);
                            feedEmptyMessage.setVisibility(View.GONE);
                        }
                        // Иначе говорим, что у друзей пустые списки
                        else {
                            feedEmptyMessage.setText(R.string.feed_empty_friendswishes);
                            feedEmptyMessage.setVisibility(View.VISIBLE);
                            wishesFeedRV.setVisibility(View.GONE);
                        }
                    } else {
                        // Отправили пустой запрос (список друзей пуст), ничего не получили
                        feedEmptyMessage.setText(R.string.feed_empty);
                        feedEmptyMessage.setVisibility(View.VISIBLE);
                        wishesFeedRV.setVisibility(View.GONE);
                        Log.i(TAG, "Список пуст");
                    }
                }

                // Если нет интернета, т.е. ответа от сервера нет
                @Override
                public void onFailure(Call<WishesModel> call, Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    feedEmptyMessage.setText(R.string.feed_no_connection);
                    feedEmptyMessage.setVisibility(View.VISIBLE);
                    wishesFeedRV.setVisibility(View.GONE);
                    Log.i(TAG, "Проблемы со связью");
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();

                }
            });
        } catch (Exception e) {
        }

    }
}




