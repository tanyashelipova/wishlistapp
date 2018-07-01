package ru.wishlistapp.wishlist.giftPost;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurDeleteModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishAndMessageModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.myProfileAndWishList.UserProfileFragment;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ImgurInterface;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.anotherStuff.UserProfile.currentUser;
import static ru.wishlistapp.wishlist.giftPost.AddWishActivity.MY_LIST_HAS_CHANGED;
import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

/**
 * Created by Таня on 05.11.2017.
 */

public class WishPostActivity extends AppCompatActivity{
    private static final String TAG = "GiftPost";
    TextView title, description, date, status, link, comments, reserve, reserveWhat;
    static ImageView pic;
    Uri imageUri;
    static String giftId, imageLink;
    ScrollView scrollView;
    static Context contextGiftPost;
    static WishModel currentGift;
    boolean isMine;
    static SwipeRefreshLayout swipeRefreshLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_post);

        contextGiftPost = getApplicationContext();
        scrollView = (ScrollView) findViewById(R.id.scrillViewGiftPost);
        title = (TextView) findViewById(R.id.giftTitleGiftPost);
        description = (TextView) findViewById(R.id.giftDescriptionGiftPost);
        date = (TextView) findViewById(R.id.giftDateGiftPost);
        pic = (ImageView) findViewById(R.id.giftImageGiftPost);
        status = (TextView) findViewById(R.id.giftStatusGiftPost);
        link = (TextView) findViewById(R.id.linkGiftPost);
        comments = (TextView) findViewById(R.id.commentsGiftPost);
        reserve = (TextView) findViewById(R.id.reservedTV);
        reserveWhat = (TextView) findViewById(R.id.reservedTVwhat);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutGiftPost);

        giftId = getIntent().getExtras().getString("id");
        isMine = true;
        isMine = getIntent().getExtras().getBoolean("menu");

        currentGift = new WishModel();
        getWishById();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getWishById();
            }
        });

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextGiftPost,CommentsActivity.class);
                intent.putExtra("wishId", currentGift.getId());
                intent.putExtra("wishOwnerId", currentGift.getUserId());
                startActivity(intent);
            }
        });
    }

    public void onResume(){
        super.onResume();
        getWishById();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG,String.valueOf(isMine));
        if (isMine) {
            getMenuInflater().inflate(R.menu.gift_post_settings, menu);
        }

        else {
            getMenuInflater().inflate(R.menu.wish_post_to_reserve, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // удаление подарка
        if (id == R.id.gift_post_settings_delete) {
            final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
            deleteAlert.setTitle("Удалить подарок из вашего списка?");
            deleteAlert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            deleteAlert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteImage();
                    deleteWish();
                    MY_LIST_HAS_CHANGED = true;
                    UserProfileFragment.adapter.notifyDataSetChanged();
                    finish();
                }
            });
            AlertDialog alert1 = deleteAlert.create();
            alert1.show();
            return true;
        }

        if (id == R.id.gift_post_settings_edit) {
            Intent intent = new Intent(this,EditWishActivity.class);
            intent.putExtra("id",giftId);
            startActivity(intent);
            return true;
        }

        // резервирование желания
        if (id == R.id.reserve_wish) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            if (currentGift.getReserved().equals("false")) {
                alert.setTitle(R.string.reserve_wish_title);
                alert.setMessage(R.string.to_reserve_wish);
                alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reserveWish(currentUser.username);
                    }
                });
                AlertDialog alert1 = alert.create();
                alert1.show();
                return true;
            }

            if (!currentGift.getReserved().equals(currentUser.username)) {
                // говорим, что другой пользователь заразервировал
                alert.setTitle(R.string.reserve_wish_title);
                alert.setMessage(R.string.already_reserved);
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog alert1 = alert.create();
                alert1.show();
                return true;
            }

            if (currentGift.getReserved().equals(currentUser.username)) {
                // отмена резерва
                alert.setTitle(R.string.cancel_reserved);
                alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reserveWish("false");
                    }
                });
                AlertDialog alert1 = alert.create();
                alert1.show();
                return true;
            }
        }



        return super.onOptionsItemSelected(item);
    }

    private void reserveWish(String username){
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Log.i(TAG,String.valueOf(giftId));
            Call<SuccessAndMessageModel> call = apiService.reserveWish(giftId, username);
            call.enqueue(new Callback<SuccessAndMessageModel>() {
                @Override
                public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                    if (response.body().getSuccess()) {
                        Toast.makeText(contextGiftPost, "Успешно", Toast.LENGTH_LONG).show();
                        getWishById();
                    }
                }

                @Override
                public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                    Toast.makeText(contextGiftPost, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }

    public void getWishById() {
        swipeRefreshLayout.setRefreshing(true);
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Log.i(TAG,String.valueOf(giftId));
            Call<WishAndMessageModel> call = apiService.getWishById(giftId);
            call.enqueue(new Callback<WishAndMessageModel>() {
                @Override
                public void onResponse(Call<WishAndMessageModel> call, Response<WishAndMessageModel> response) {
                    if (response.body().getSuccess()) {
                        currentGift = response.body().getWish();
                        imageLink = currentGift.getImageLink();
                        if (imageLink != null) {
                            Glide
                                    .with(contextGiftPost)
                                    .load(Uri.parse(imageLink))
                                    .centerCrop()
                                    .error(R.drawable.nowishimage)
                                    .into(pic);
                        } else {
                            Glide
                                    .with(contextGiftPost)
                                    .load("")
                                    .placeholder(R.drawable.nowishimage)
                                    .centerCrop()
                                    .into(pic);
                        }

                        title.setText(currentGift.getTitle());
                        if (!(currentGift.getContent().equals(""))) {
                            description.setText(currentGift.getContent());
                        } else {
                            description.setVisibility(View.GONE);
                        }
                        setTitle("Желание пользователя " + currentGift.getUsername());
                        date.setText(currentGift.getCreatedDate());
                        if (currentGift.getIsReceived()){
                            status.setText("Этот подарок уже получен!");
                        } else {
                            status.setText("Этот подарок еще не получен");
                        }

                        if (!(currentGift.getLink().equals(""))) {
                            link.setText(currentGift.getLink());
                        } else {
                            link.setVisibility(View.GONE);
                        }

                        if (!isMine) {
                            reserve.setVisibility(View.VISIBLE);
                            reserveWhat.setVisibility(View.VISIBLE);
                            reserveWhat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // алерт с объяснением
                                    final AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                                    alert.setTitle(R.string.reserve_wish_title);
                                    alert.setMessage(R.string.reserve_wish_message);
                                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    AlertDialog alert1 = alert.create();
                                    alert1.show();
                                }
                            });

                            if (currentGift.getReserved().equals("false")) {
                                reserve.setText(R.string.wish_not_reserved);
                            } else {
                                if (currentGift.getReserved().equals(currentUser.username)) {
                                    String message = getResources().getString(R.string.wish_reserved) + " вами";
                                    reserve.setText(message);
                                } else {
                                    String message = getResources().getString(R.string.wish_reserved) + " пользователем " + currentUser.username;
                                    reserve.setText(message);
                                }
                            }
                        } else {
                            reserve.setVisibility(View.GONE);
                            reserveWhat.setVisibility(View.GONE);
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<WishAndMessageModel> call, Throwable t) {
                    Toast.makeText(contextGiftPost, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    title.setText("");
                    description.setText("");
                    date.setText("");
                    setTitle(R.string.error_no_connection);
                    status.setText("");
                    Glide
                            .with(contextGiftPost)
                            .load("")
                            .placeholder(R.drawable.nowishimage)
                            .centerCrop()
                            .into(pic);
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }


    public void deleteWish() {
        WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
        ApiClient.getClient().create(WishlistInterface.class);
        try {
            Call<WishAndMessageModel> call = apiService.deleteWish(giftId);
            call.enqueue(new Callback<WishAndMessageModel>() {
                @Override
                public void onResponse(Call<WishAndMessageModel> call, Response<WishAndMessageModel> response) {
                        Toast.makeText(contextGiftPost, response.body().getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Call<WishAndMessageModel> call, Throwable t) {
                    Toast.makeText(contextGiftPost, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Fail!");
                }
            });
        } catch (Exception e) {
        }
    }

    private void deleteImage(){
        ImgurInterface imgurService = ImgurInterface.retrofit.create(ImgurInterface.class);
        // сначала удалим старое изображение
        final Call<ImgurDeleteModel> callDelete =
                imgurService.deleteImage(currentGift.getImageHashDelete());
        callDelete.enqueue(new Callback<ImgurDeleteModel>() {
            @Override
            public void onResponse(Call<ImgurDeleteModel> call, Response<ImgurDeleteModel> response) {
//                Log.i(TAG,"Image deleted: " + String.valueOf(response.body().getSuccess()));
            }
            @Override
            public void onFailure(Call<ImgurDeleteModel> call, Throwable t) {
            }
        });
    }

}
