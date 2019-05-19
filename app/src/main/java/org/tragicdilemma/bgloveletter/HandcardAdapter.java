package org.tragicdilemma.bgloveletter;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;

public class HandcardAdapter extends RecyclerView.Adapter<HandcardAdapter.ViewHolder> implements RecyclerView.OnClickListener{

    ArrayList<Card> handcards;
    Context context;
    Boolean isDialog;

    public HandcardAdapter(Context context, ArrayList<Card> handCards, Boolean isDialog){
        this.handcards = handCards;
        this.context = context;
        this.isDialog = isDialog;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        HandcardAdapter.ViewHolder vh = new HandcardAdapter.ViewHolder(v);

        ViewGroup.LayoutParams layoutParams = vh.imgCard.getLayoutParams();
        WindowManager windowManager = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
        if(isDialog)layoutParams.width = (int) (windowManager.getDefaultDisplay().getWidth()* 0.8 / 2 - 16);
        else layoutParams.width = windowManager.getDefaultDisplay().getWidth()/2 - 16;
        vh.imgCard.setLayoutParams(layoutParams);

        v.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imgCard.setImageResource(handcards.get(position).getDrawable());
        holder.itemView.setTag(handcards.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return handcards.size();
    }

    public void update(ArrayList<Card> handcards){
        this.handcards = handcards;
        this.notifyDataSetChanged();
    }

    private HandcardAdapter.OnItemClickListener clrItem = null;

    public static interface OnItemClickListener {
        void onItemClick(View view , int tag);
    }

    public void setOnItemClickListener(HandcardAdapter.OnItemClickListener listener) {
        this.clrItem = listener;
    }

    @Override
    public void onClick(View v) {
        if (clrItem != null)clrItem.onItemClick(v, (Integer) v.getTag());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgCard;
        public ViewHolder(View itemView) {
            super(itemView);
            imgCard = itemView.findViewById(R.id.imgCard);
        }
    }
}