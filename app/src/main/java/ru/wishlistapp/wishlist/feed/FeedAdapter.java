package ru.wishlistapp.wishlist.feed;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.giftPost.WishPostActivity;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;

import static ru.wishlistapp.wishlist.feed.FeedFragment.context;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.giftsFeedViewHolder>{
    String TAG = "FeedAdapter";
    public static class giftsFeedViewHolder extends RecyclerView.ViewHolder {
        CardView giftCVfeed;
        TextView userNameFeed;
        TextView actionFeed;
        TextView giftTitleFeed;
        TextView giftDescriptionFeed;
        TextView giftDateFeed;
        ImageView giftPicFeed;
        giftsFeedViewHolder(View itemView) {
            super(itemView);
            giftCVfeed = (CardView)itemView.findViewById(R.id.giftCVfeed);
            userNameFeed = (TextView)  itemView.findViewById(R.id.giftUsernameFeed);
            giftTitleFeed = (TextView)itemView.findViewById(R.id.giftTitleFeed);
            giftDescriptionFeed = (TextView)itemView.findViewById(R.id.giftDescriptionFeed);
            giftDateFeed = (TextView)itemView.findViewById(R.id.giftDateFeed);
            giftPicFeed = (ImageView)itemView.findViewById(R.id.giftImageFeed);
        }
    }

    List<WishModel> giftsFeed;

    FeedAdapter(List<WishModel> giftsFeed){
        this.giftsFeed = giftsFeed;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public giftsFeedViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gift_item_feed, viewGroup, false);
        giftsFeedViewHolder giftsFVH = new giftsFeedViewHolder(v);
        return giftsFVH;
    }

    @Override
    public void onBindViewHolder(giftsFeedViewHolder feedViewHolder, int i) {
        final WishModel wish = giftsFeed.get(i);
        feedViewHolder.userNameFeed.setText(wish.getUsername());
        // feedViewHolder.actionFeed.setText(giftsFeed.get(i).action);
        feedViewHolder.giftTitleFeed.setText(wish.getTitle());
        feedViewHolder.giftDescriptionFeed.setText(wish.getContent());
        feedViewHolder.giftDateFeed.setText(wish.getCreatedDate());
        Log.i(TAG,wish.getCreatedDate());
        if (wish.getImageLink() != null) {
            Glide
                    .with(context)
                    .load(wish.getImageLink())
                    .centerCrop()
                    .into(feedViewHolder.giftPicFeed);
        } else {
            Glide
                    .with(context)
                    .load("")
                    .placeholder(R.drawable.nowishimage)
                    .centerCrop()
                    .into(feedViewHolder.giftPicFeed);
        }


        feedViewHolder.giftCVfeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), WishPostActivity.class);
                intent.putExtra("id", wish.getId());
                intent.putExtra("menu", false);
                view.getContext().startActivity(intent);
            }
        });
}

    @Override
    public int getItemCount() {
        return giftsFeed.size();
    }
}
