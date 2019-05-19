package org.tragicdilemma.bgloveletter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> implements RecyclerView.OnClickListener{

    private ArrayList<String> players;

    public MemberListAdapter(ArrayList<String> players){
        this.players = players;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        MemberListAdapter.ViewHolder vh = new MemberListAdapter.ViewHolder(v);
        v.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvMember.setText("> " + players.get(position));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    private MemberListAdapter.OnItemClickListener clrItem = null;

    public static interface OnItemClickListener {
        void onItemClick(View view , int tag);
    }

    public void setOnItemClickListener(MemberListAdapter.OnItemClickListener listener) {
        this.clrItem = listener;
    }

    @Override
    public void onClick(View v) {
        if (clrItem != null)clrItem.onItemClick(v, (Integer) v.getTag());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvMember;
        public ViewHolder(View itemView) {
            super(itemView);
            tvMember = itemView.findViewById(R.id.tvMember);
        }
    }
}
