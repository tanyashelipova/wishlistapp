package ru.wishlistapp.wishlist.friendList;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import ru.wishlistapp.wishlist.model.friendlistmodel.FriendListModel;
import ru.wishlistapp.wishlist.model.friendlistmodel.FriendModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;
import static ru.wishlistapp.wishlist.start.StartActivity.friends;

public class FriendListFragment extends Fragment {
    static SwipeRefreshLayout swipeRefreshLayout;
    static FriendListAdapter adapter;
    static Context context;
    private static List<FriendModel> friendlist;
    private static RecyclerView friendlistRV;
    private static final String TAG = "FriendListFragment";
    SearchView searchView;
    static TextView emptyMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycleview_friendlist, container, false);


    }


    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        context = getContext();

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayoutFriendList);

        friendlistRV = (RecyclerView) getView().findViewById(R.id.friendListRV);
        emptyMessage = (TextView) getView().findViewById(R.id.friendListEmptyMessage);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        friendlistRV.setLayoutManager(llm);

        refreshFriendList();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFriendList();
            }
        });

    }

    public static void refreshFriendList() {
        swipeRefreshLayout.setRefreshing(true);
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);

            try {
                Call<FriendListModel> call = apiService.getFriendList(currentUser.username);
                Log.i(TAG, String.valueOf(call.toString()));
                call.enqueue(new Callback<FriendListModel>() {
                    @Override
                    public void onResponse(Call<FriendListModel> call, Response<FriendListModel> response) {
                        if (response.isSuccessful()) {
                            friendlist = response.body().getFriends();
                            friends = new ArrayList<String>();
                            for (int i = 0; i < friendlist.size(); i++){
                                friends.add(friendlist.get(i).getUsername());
                            }
                            swipeRefreshLayout.setRefreshing(false);
                           if (friendlist.size() > 0) {
                               emptyMessage.setVisibility(View.GONE);
                               friendlistRV.setVisibility(View.VISIBLE);
                               adapter = new FriendListAdapter(friendlist);
                               friendlistRV.setAdapter(adapter);
                           } else {
                               emptyMessage.setText(R.string.list_friend_empty);
                               emptyMessage.setVisibility(View.VISIBLE);
                               friendlistRV.setVisibility(View.GONE);
                           }

                        }
                    }

                    @Override
                    public void onFailure(Call<FriendListModel> call, Throwable t) {
                        Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                        emptyMessage.setText(R.string.friends_no_connection);
                        emptyMessage.setVisibility(View.VISIBLE);
                        friendlistRV.setVisibility(View.GONE);
                        Log.i(TAG, "Fail!");
                    }
                });
            } catch (Exception e) {
            }
        }

}




