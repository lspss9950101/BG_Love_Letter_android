package org.tragicdilemma.bgloveletter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;

public class UsedCardAdapter extends RecyclerView.Adapter<UsedCardAdapter.ViewHolder> {

    private ArrayList<Integer> usedCards;

    public UsedCardAdapter(ArrayList<Integer> usedCards){
        this.usedCards = usedCards;
    }

    public void update(ArrayList<Integer> usedCards){
        this.usedCards = usedCards;
        this.notifyDataSetChanged();
    }

    @Override
    public UsedCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_used, parent, false);
        ViewHolder vh = new ViewHolder(view);
        ViewGroup.LayoutParams layoutParams = vh.imgCard.getLayoutParams();
        WindowManager windowManager = (WindowManager)(parent.getContext().getSystemService(Context.WINDOW_SERVICE));
        layoutParams.width = (windowManager.getDefaultDisplay().getWidth()/6 - 8);
        return vh;
    }

    @Override
    public void onBindViewHolder(UsedCardAdapter.ViewHolder holder, int position) {
        holder.imgCard.setImageResource(new Card(usedCards.get(position)).getDrawable());
    }

    @Override
    public int getItemCount() {
        return usedCards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgCard;
        public ViewHolder(View itemView) {
            super(itemView);
            imgCard = itemView.findViewById(R.id.imgCard);
        }
    }
}
