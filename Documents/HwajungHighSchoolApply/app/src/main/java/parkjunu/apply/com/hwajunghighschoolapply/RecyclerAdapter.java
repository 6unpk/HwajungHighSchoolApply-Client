package parkjunu.apply.com.hwajunghighschoolapply;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Object> mData;
    private static MyClickListener myClickListener;
    private static final int TYPE_PROFILE = 1;
    private static final int TYPE_NORMAL = 2;
    private static final int TYPE_STRING = 3;
    private int lastPos = -1;
    private Context context;
    public static Button logOut;


    public static class CardViewItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ImageView label;
        TextView sub;
        CardView cardView;
        LinearLayout background;
        public CardViewItemHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            label = (ImageView) itemView.findViewById(R.id.thumbnail);
            sub = (TextView) itemView.findViewById(R.id.sub);
            background =(LinearLayout) itemView.findViewById(R.id.card_bckg);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public static class CardViewStringHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView title;
        TextView sub;
        CardView cardView;
        public CardViewStringHolder(View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            sub = (TextView) itemView.findViewById(R.id.sub);
            cardView = (CardView) itemView.findViewById(R.id.card_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public static class CardViewProfileHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView title;
        ImageView imageView;
        CardView cardView;
        public CardViewProfileHolder(View itemView){
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card_view);
            title = (TextView) itemView.findViewById(R.id.title);
            logOut = (Button) itemView.findViewById(R.id.log_out);
            imageView = (ImageView) itemView.findViewById(R.id.profile);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }


    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public RecyclerAdapter(ArrayList<Object> myDataSet, Context context) {
        this.context = context;
        mData = myDataSet;
    }


    @Override
    public int getItemViewType(int position){
        if(mData.get(position) instanceof CardViewItem)
            return TYPE_NORMAL;
        else if(mData.get(position) instanceof CardStringItem)
            return TYPE_STRING;
        else
            return TYPE_PROFILE;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            ((CardViewItemHolder) holder).label.setImageDrawable(((CardViewItem) mData.get(position)).getGrade());
            ((CardViewItemHolder) holder).sub.setText(((CardViewItem) mData.get(position)).getSub());
            ((CardViewItemHolder) holder).background.setBackgroundColor(((CardViewItem) mData.get(position)).getColor());
            setAnimation(((CardViewItemHolder) holder).cardView, position);
        }
        else if (getItemViewType(position) == TYPE_STRING) {
            ((CardViewStringHolder) holder).title.setText(((CardStringItem) mData.get(position)).getTitle());
            ((CardViewStringHolder) holder).sub.setText(((CardStringItem) mData.get(position)).getSub());
            Log.d("tag",((CardStringItem) mData.get(position)).getSub());

            int count = ((CardStringItem) mData.get(position)).getCount();
            //현재 진행중인 수강 신청의 개수가 1개 이상이면 녹색 아니면 황색
            if (count > 0)
                ((CardViewStringHolder) holder).sub.setTextColor(context.getResources().getColor(R.color.fbutton_color_green_sea));
           else if ( count == 0)
                ((CardViewStringHolder) holder).sub.setTextColor(context.getResources().getColor(R.color.blood_light));
            setAnimation(((CardViewStringHolder) holder).cardView, position);
        }
        else if (getItemViewType(position) == TYPE_PROFILE){
            ((CardViewProfileHolder) holder).title.setText(((CardProfileItem) mData.get(position)).getSub());
            setAnimation(((CardViewProfileHolder) holder).cardView, position);
        }
    }


    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }


    private void setAnimation(View viewToAnimate, int position){

            if(position > lastPos){
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade);
                viewToAnimate.startAnimation(animation);
                lastPos = position;
            }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_layout, parent, false);

            CardViewItemHolder cardviewItemHolder = new CardViewItemHolder(view);
            return cardviewItemHolder;
        } else if (viewType == TYPE_STRING){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_string_layout, parent, false);
            CardViewStringHolder cardStringItem = new CardViewStringHolder(view);
            return cardStringItem;
        } else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_profile_layout, parent, false);
            CardViewProfileHolder profileHolder = new CardViewProfileHolder(view);
            return profileHolder;
        }
    }
}