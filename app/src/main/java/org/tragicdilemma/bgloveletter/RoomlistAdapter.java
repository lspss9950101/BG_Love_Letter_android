package org.tragicdilemma.bgloveletter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RoomlistAdapter extends RecyclerView.Adapter<RoomlistAdapter.ViewHolder> implements RecyclerView.OnClickListener{

    private ArrayList<Room> roomlist;
    private Context context;

    public RoomlistAdapter(Context context, ArrayList<Room> roomlist){
        this.roomlist = roomlist;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_info, parent, false);
        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(roomlist != null){
            String roomNumber = roomlist.get(position).getRoomNumber();
            holder.tvRoomNumber.setText(roomNumber);
            holder.tvPlayers.setText(roomlist.get(position).getPlayerCount().toString() + "/4");
            holder.tvCreator.setText(roomlist.get(position).getCreator());
            holder.itemView.setTag(Integer.valueOf(roomNumber));
            if(roomlist.get(position).getState()){
                holder.clItem.setAlpha((float) 0.5);
                holder.clItem.setEnabled(false);
            }else{
                holder.clItem.setAlpha(1);
                holder.clItem.setEnabled(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        if(roomlist != null)return roomlist.size();
        else return 0;
    }


    public void update(ArrayList roomlist){
        this.roomlist = roomlist;
        this.notifyDataSetChanged();
    }

    private OnItemClickListener clrItem = null;

    public static interface OnItemClickListener {
        void onItemClick(View view , int tag);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clrItem = listener;
    }

    @Override
    public void onClick(View v) {
        if (clrItem != null)clrItem.onItemClick(v, (Integer) v.getTag());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvCreator, tvRoomNumber, tvPlayers;
        ConstraintLayout clItem;

        public ViewHolder(View itemView) {
            super(itemView);
            clItem = itemView.findViewById(R.id.clItem);
            tvCreator = itemView.findViewById(R.id.tvCreator);
            tvPlayers = itemView.findViewById(R.id.tvPlayers);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
        }
    }
}
