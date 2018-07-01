package ru.wishlistapp.wishlist.giftPost;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.model.wishmodel.CommentModel;
import ru.wishlistapp.wishlist.model.wishmodel.CommentsModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishAndMessageModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

public class CommentsActivity extends AppCompatActivity {
    EditText commentView;
    ImageButton button;
    String wishId, wishOwnerId, text;
    static Context context;
    List<CommentModel> comments;
    RecyclerView commentsRV;
    CommentsAdapter adapter;
    String TAG = "Comments";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_comments);
        commentView = (EditText) findViewById(R.id.textComment);
        button = (ImageButton) findViewById(R.id.btnComment);

        Intent intent = getIntent();
        wishId = intent.getStringExtra("wishId");
        wishOwnerId = intent.getStringExtra("wishOwnerId");
        setTitle(R.string.comments);
        context = this;
        commentsRV = (RecyclerView) findViewById(R.id.commentsRV);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        commentsRV.setLayoutManager(llm);

        getComments();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (commentView.getText() != null) {
                    text = commentView.getText().toString();
                    addComment();
                    commentView.setText("");
                }
            }
        });
    }

    private void getComments(){
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<CommentsModel> call = apiService.getComments(wishId);
            call.enqueue(new Callback<CommentsModel>() {
                @Override
                public void onResponse(Call<CommentsModel> call, Response<CommentsModel> response){
                    comments = response.body().getComments();
                    if (comments != null) {
                        adapter = new CommentsAdapter(comments);
                        commentsRV.setAdapter(adapter);
                    }
                }

                @Override
                public void onFailure(Call<CommentsModel> call, Throwable t) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
        }
    }

    private void addComment(){
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<SuccessAndMessageModel> call = apiService.addComment(wishId, currentUser.id_server, currentUser.username, wishOwnerId, text);
            call.enqueue(new Callback<SuccessAndMessageModel>() {
                @Override
                public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response){
                    if (response.body().getSuccess()) {
                        getComments();
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
}
