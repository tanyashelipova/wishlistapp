package ru.wishlistapp.wishlist.friendList;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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

import static ru.wishlistapp.wishlist.friendList.FriendProfileActivity.context;

public class FriendProfileAdapter extends RecyclerView.Adapter<FriendProfileAdapter.usersWishesViewHolder> {

    public static class usersWishesViewHolder extends RecyclerView.ViewHolder {
        CardView myGiftCV;
        TextView giftTitle;
        TextView giftDescription;
        TextView giftDate;
        ImageView giftPic;

        ImageView wanted;
        ImageView recieved;

        usersWishesViewHolder(View itemView) {
            super(itemView);
            myGiftCV = (CardView) itemView.findViewById(R.id.giftCV);
            giftTitle = (TextView) itemView.findViewById(R.id.giftTitle);
            giftDescription = (TextView) itemView.findViewById(R.id.giftDescription);
            giftPic = (ImageView) itemView.findViewById(R.id.giftImage);
            giftDate = (TextView) itemView.findViewById(R.id.giftDate);
            wanted = (ImageView) itemView.findViewById(R.id.giftStatusWanted);
            recieved = (ImageView) itemView.findViewById(R.id.giftStatusRecieved);
        }
    }

    List<WishModel> gifts;

    FriendProfileAdapter(List<WishModel> gifts) {
        this.gifts = gifts;
    }

    @Override
    public usersWishesViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gift_item, viewGroup, false);
        return new usersWishesViewHolder(v);
    }



    @Override
    public void onBindViewHolder(final usersWishesViewHolder myViewHolder, final int i) {
        final int id = i;

        final String title = gifts.get(i).getTitle();
        myViewHolder.giftTitle.setText(title);

        final String date = gifts.get(i).getCreatedDate();
        myViewHolder.giftDate.setText(date);

        final String description = gifts.get(i).getContent();
        myViewHolder.giftDescription.setText(description);

        final boolean status = gifts.get(i).getIsReceived();
        if (!status) {
            myViewHolder.recieved.setVisibility(View.INVISIBLE);
            myViewHolder.wanted.setVisibility(View.VISIBLE);
        } else {
            myViewHolder.recieved.setVisibility(View.VISIBLE);
            myViewHolder.wanted.setVisibility(View.INVISIBLE);
        }
        if (gifts.get(i).getImageLink() != null) {
            Glide
                    .with(context)
                    .load(gifts.get(i).getImageLink())
                    .centerCrop()
                    .into(myViewHolder.giftPic);
        } else {
            Glide
                    .with(context)
                    .load("")
                    .placeholder(R.drawable.nowishimage)
                    .centerCrop()
                    .into(myViewHolder.giftPic);
        }

        // при нажатии берем id подарка в списке и отправляет активити с постом о подарке
        myViewHolder.myGiftCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), WishPostActivity.class);
                intent.putExtra("id", gifts.get(i).getId());
                intent.putExtra("menu",false);
                view.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return gifts.size();
    }

    public String convertDate(String date) {
        String year = date.substring(0,4);
        String month = date.substring(5,7);
        String day = date.substring(8,10);
        String d = day + "/" + month + "/" + year;
        return d;
    }

    /*private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
        ImageView bmImage;
        int id;

        public DownloadImageTask(ImageView bmImage, int id) {
            this.bmImage = bmImage;
            this.id = id;
        }

        protected Bitmap doInBackground(Void... arg0) {
            Bitmap pic = null;
            try {
                pic = gifts.get(id).picSmall;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pic;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }*/

}
