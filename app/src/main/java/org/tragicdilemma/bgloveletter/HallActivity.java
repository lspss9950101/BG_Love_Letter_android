package org.tragicdilemma.bgloveletter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HallActivity extends AppCompatActivity {

    private Button btnRefreshRoomlist, btnCreateRoom;
    private RecyclerView rvRoomlist;
    private ProgressBar pbLoading;

    private ArrayList<Room> roomlist;
    private RoomlistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall);

        SocketIo.setHallHandler(systemBroadcast);
        SocketIo.getInstance().getRoomlist();

        btnRefreshRoomlist = findViewById(R.id.btnRefreshRoomlist);
        rvRoomlist = findViewById(R.id.rvRoomlist);
        pbLoading = findViewById(R.id.pbLoading);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HallActivity.this);
        rvRoomlist.setLayoutManager(layoutManager);
        rvRoomlist.setHasFixedSize(true);

        adapter = new RoomlistAdapter(HallActivity.this, roomlist);
        rvRoomlist.setAdapter(adapter);
        adapter.setOnItemClickListener(new RoomlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int tag) {
                joinRoom();
                SocketIo.getInstance().joinRoom(tag);
            }
        });

        btnRefreshRoomlist.setOnClickListener(clrRefresh);
        btnCreateRoom.setOnClickListener(clrCreate);
        pbLoading.setVisibility(View.INVISIBLE);
    }

    private Button.OnClickListener clrRefresh = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            SocketIo.getInstance().getRoomlist();
            pbLoading.setVisibility(View.VISIBLE);
            rvRoomlist.setEnabled(false);
            rvRoomlist.setAlpha((float) 0.5);
            btnRefreshRoomlist.setEnabled(false);
        }
    };

    private Button.OnClickListener clrCreate = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            joinRoom();
            SocketIo.getInstance().createRoom();
        }
    };

    private Toast customToast(Context context, String msg, Boolean isWarning){
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 96);
        View view;
        if(isWarning)view = LayoutInflater.from(context).inflate(R.layout.toast_warning, null);
        else view = LayoutInflater.from(context).inflate(R.layout.toast_messege, null);
        TextView tvMsg = view.findViewById(R.id.tvMsg);
        tvMsg.setText(msg);
        toast.setView(view);
        return toast;
    }

    private void joinRoom(){
        Intent it = new Intent(HallActivity.this, GameActivity.class);
        it.putExtra("userName", getIntent().getStringExtra("userName"));
        startActivity(it);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketIo.getInstance().disconnect();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        if(!SocketIo.getInstance().isConnected())finish();
    }

    public Handler systemBroadcast = new Handler(){
        public void handleMessage(Message msg){
            String obj;
            switch(msg.what){
                case SocketIo.ROOMLIST:
                    obj = String.valueOf(msg.obj);
                    obj = obj.substring(1, obj.length() - 1);
                    roomlist = new ArrayList<>();
                    String[] roomlistRaw = obj.split(",");
                    for(int i = 0; i < roomlistRaw.length / 4; i++){
                        String tmp = roomlistRaw[4 * i] + "," + roomlistRaw[4 * i + 1] + "," + roomlistRaw[4 * i + 2] + "," + roomlistRaw[4 * i + 3];
                        Room tmpRoom = new Room(tmp);
                        roomlist.add(tmpRoom);
                    }

                    adapter.update(roomlist);
                    pbLoading.setVisibility(View.INVISIBLE);
                    btnRefreshRoomlist.setEnabled(true);
                    rvRoomlist.setEnabled(true);
                    rvRoomlist.setAlpha(1);
                    break;
                case SocketIo.JOINROOM:

                    break;
                case SocketIo.MSG:
                    customToast(HallActivity.this, (String) msg.obj, true).show();
                    break;
                case SocketIo.TOINTRO:
                    finish();
                    break;
            }
        }
    };
}
