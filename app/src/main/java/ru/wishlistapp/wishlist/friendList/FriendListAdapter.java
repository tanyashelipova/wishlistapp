package ru.wishlistapp.wishlist.friendList;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.model.UserModel;
import ru.wishlistapp.wishlist.model.friendlistmodel.FriendModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static android.support.v4.content.ContextCompat.startActivity;
import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.friendList.FriendListFragment.context;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.friendListViewHolder>{
    String TAG = "FriendListAdapter";

    public static class friendListViewHolder extends RecyclerView.ViewHolder {
        CardView friendCVlist;
        TextView username, name, bday;
        ImageView friendPic;
        friendListViewHolder(View itemView) {
            super(itemView);
            friendCVlist = (CardView)itemView.findViewById(R.id.friendCVlist);
            username = (TextView)  itemView.findViewById(R.id.friendUsernameList);
            name = (TextView)  itemView.findViewById(R.id.friendNameList);
            bday = (TextView)itemView.findViewById(R.id.friendBdayList);
            friendPic = (ImageView)itemView.findViewById(R.id.friendImageList);
        }
    }

    List<FriendModel> friendlist;

    FriendListAdapter(List<FriendModel> friendlist){
        this.friendlist = friendlist;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public friendListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_item_list, viewGroup, false);
        friendListViewHolder friendlistFVH = new friendListViewHolder(v);
        return friendlistFVH;
    }

    @Override
    public void onBindViewHolder(friendListViewHolder listViewHolder, int i) {
        final String username = friendlist.get(i).getUsername();
        final String name = friendlist.get(i).getName();
        final String bday = friendlist.get(i).getBday();
        final String imageLink = friendlist.get(i).getImageLink();
        listViewHolder.username.setText(username);
        listViewHolder.name.setText(name);
        listViewHolder.bday.setText(bday);

        if (imageLink != null) {
            Glide
                    .with(context)
                    .load(Uri.parse(imageLink))
                    .centerCrop()
                    .error(R.drawable.profile_pic2)
                    .into(listViewHolder.friendPic);
        } else {
            Glide
                    .with(context)
                    .load("")
                    .placeholder(R.drawable.profile_pic2)
                    .centerCrop()
                    .into(listViewHolder.friendPic);
        }

        //listViewHolder.friendPic.setImageResource(friendlist.get(i).photoId);

        listViewHolder.friendCVlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
                ApiClient.getClient().create(WishlistInterface.class);
                try {
                    Call<UserModel> call = apiService.getUserByUsername(username);
                    call.enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                            if (response.isSuccessful()) {
                                UserModel user = response.body();
                                Intent intent = new Intent(view.getContext(),FriendProfileActivity.class);
                                intent.putExtra("name",user.getName());
                                intent.putExtra("bday",user.getBday());
                                intent.putExtra("username", username);
                                intent.putExtra("userId", user.getUserId());
                                intent.putExtra("imageLink", user.getImageLink());
                                //Toast.makeText(getApplicationContext(),user.getUserId(),Toast.LENGTH_LONG).show();
                                startActivity(view.getContext(),intent,null);
                            }

                        }

                        @Override
                        public void onFailure(Call<UserModel> call, Throwable t) {
                            Toast.makeText(view.getContext(), R.string.error, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendlist.size();
    }

}
