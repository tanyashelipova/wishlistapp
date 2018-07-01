package ru.wishlistapp.wishlist.giftPost;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.friendList.FriendProfileActivity;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.model.UserModel;
import ru.wishlistapp.wishlist.model.wishmodel.CommentModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static android.support.v4.content.ContextCompat.startActivity;
import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.giftPost.CommentsActivity.context;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.commentsViewHolder> {

    AlertDialog.Builder ad;
    String TAG = "CommentsAdapter";
    public static class commentsViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView usernameView, textView, dateView;

        commentsViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.commentCV);
            usernameView = (TextView) itemView.findViewById(R.id.usernameComment);
            textView = (TextView) itemView.findViewById(R.id.textComment);
            dateView = (TextView) itemView.findViewById(R.id.dateComment);
        }
    }

    List<CommentModel> comments;

    CommentsAdapter(List<CommentModel> comments) {
        this.comments = comments;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public commentsViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_item, viewGroup, false);
        return new commentsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final commentsViewHolder myViewHolder, final int i) {
        final String username = comments.get(i).getUsername();
        myViewHolder.usernameView.setText(username);

        final String text = comments.get(i).getText();
        myViewHolder.textView.setText(text);

        final String date = comments.get(i).getDate();
        myViewHolder.dateView.setText(date);

        // Удаление комментария
        myViewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ad = new android.support.v7.app.AlertDialog.Builder(context);
                ad.setTitle(R.string.gift_post_delete_comment);
                ad.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        deleteComment(comments.get(i).getId(), i);
                    }
                });
                ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                    }
                });
                AlertDialog alert1 = ad.create();
                alert1.show();
                return true;
            }
        });

        // Переход на страницу комментатора
        myViewHolder.usernameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!username.equals(currentUser.username)) {
                    Log.i(TAG,username);
                    Log.i(TAG,currentUser.username);
                    WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
                    ApiClient.getClient().create(WishlistInterface.class);
                    try {
                        Call<UserModel> call = apiService.getUserByUsername(username);
                        call.enqueue(new Callback<UserModel>() {
                            @Override
                            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                                if (response.isSuccessful()) {
                                    UserModel user = response.body();
                                    Intent intent = new Intent(context, FriendProfileActivity.class);
                                    intent.putExtra("name", user.getName());
                                    intent.putExtra("bday", user.getBday());
                                    intent.putExtra("username", username);
                                    intent.putExtra("userId", user.getUserId());
                                    intent.putExtra("imageLink", user.getImageLink());
                                    startActivity(context, intent, null);
                                }

                            }

                            @Override
                            public void onFailure(Call<UserModel> call, Throwable t) {
                                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    private void deleteComment(String commentId, final int i) {
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<SuccessAndMessageModel> call = apiService.deleteComment(commentId, currentUser.id_server);
            call.enqueue(new Callback<SuccessAndMessageModel>() {
                @Override
                public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                    if (response.body().getSuccess()) {
                        comments.remove(i);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

}
