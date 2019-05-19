package org.tragicdilemma.bgloveletter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView rvHandcards, rvUsedCard;
    ArrayList<Card> handcards;
    ArrayList<Integer> usedCards;
    TextView tvSysmsg, tvRoomNumber, tvUserName;
    ScrollView svText;
    NavigationView navMenu;
    ArrayList<String> players, alives, targets;
    Integer optCard7 = 0, optCard8 = 0, optCardX = 0;

    ImageButton[] imgSettings = new ImageButton[7];
    HandcardAdapter adapter;
    UsedCardAdapter adapterUsedCard;

    RecyclerView.LayoutManager[] layoutManager = new RecyclerView.LayoutManager[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        SocketIo.setGameHandler(systemBroadcast);

        rvHandcards = findViewById(R.id.rvHandcards);
        rvUsedCard = findViewById(R.id.rvUsedCard);
        tvSysmsg = findViewById(R.id.tvSysmsg);
        svText = findViewById(R.id.svText);
        navMenu = findViewById(R.id.navMenu);

        View headerLayout = navMenu.inflateHeaderView(R.layout.nav_header_game);
        tvRoomNumber = headerLayout.findViewById(R.id.tvRoomNumber);
        tvUserName = headerLayout.findViewById(R.id.tvUserName);

        navMenu.setNavigationItemSelectedListener(this);

        handcards = new ArrayList<>();
        Card t = new Card(0);
        handcards.add(t);

        adapter = new HandcardAdapter(GameActivity.this, handcards, false);
        layoutManager[0] = new GridLayoutManager(GameActivity.this, 1);
        layoutManager[0].setAutoMeasureEnabled(true);
        layoutManager[1] = new GridLayoutManager(GameActivity.this, 2);
        layoutManager[1].setAutoMeasureEnabled(true);
        rvHandcards.setLayoutManager(layoutManager[0]);
        rvHandcards.setAdapter(adapter);

        usedCards = new ArrayList<>();
        usedCards.add(0);
        adapterUsedCard = new UsedCardAdapter(usedCards);
        RecyclerView.LayoutManager layoutManagerUsedCard = new GridLayoutManager(GameActivity.this, 1, GridLayoutManager.HORIZONTAL, false);
        rvUsedCard.setLayoutManager(layoutManagerUsedCard);
        rvUsedCard.setAdapter(adapterUsedCard);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else leavingRoom().show();
    }

    private AlertDialog leavingRoom(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameActivity.this);
        dialogBuilder.setTitle(R.string.warning_leave)
                .setPositiveButton(R.string.option_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.option_no, null);
        return dialogBuilder.create();
    }

    private AlertDialog memberList(Boolean choosible, ArrayList<String> list, @Nullable final Integer card){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameActivity.this);
        View view = LayoutInflater.from(GameActivity.this).inflate(R.layout.dialog_member, null);
        RecyclerView rvMember = view.findViewById(R.id.rvMember);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(GameActivity.this);
        rvMember.setLayoutManager(layoutManager);
        MemberListAdapter adapter = new MemberListAdapter(list);
        rvMember.setAdapter(adapter);
        dialogBuilder.setTitle(R.string.nav_member)
                .setNegativeButton(R.string.option_close, null)
                .setView(view);
        final AlertDialog dialog = dialogBuilder.create();
        if(choosible)adapter.setOnItemClickListener(new MemberListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int tag) {
                dialog.cancel();
                if(card == 1)chooseCard(players.indexOf(targets.get(tag))).show();
                else {
                    SocketIo.getInstance().discard(card, players.indexOf(targets.get(tag)), null);
                    if(handcards.get(0).getValue() == card)handcards.remove(0);
                    else handcards.remove(1);
                    updateHandcards();
                }
            }
        });
        else adapter.setOnItemClickListener(null);

        return dialog;
    }

    private AlertDialog chooseCard(final int target){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameActivity.this);
        View view = LayoutInflater.from(GameActivity.this).inflate(R.layout.dialog_card, null);
        RecyclerView rvCard = view.findViewById(R.id.rvCard);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(GameActivity.this, 2);
        rvCard.setLayoutManager(layoutManager);
        ArrayList<Card> cards = new ArrayList<>();
        for(int i = 2; i <= 8; i++)cards.add(new Card(i));
        HandcardAdapter adapterGuess = new HandcardAdapter(GameActivity.this, cards, true);
        rvCard.setAdapter(adapterGuess);
        dialogBuilder.setTitle(R.string.dialog_guess)
                .setNegativeButton(R.string.option_close, null)
                .setView(view);
        final AlertDialog dialog = dialogBuilder.create();
        adapterGuess.setOnItemClickListener(new HandcardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int tag) {
                SocketIo.getInstance().discard(1, target, tag);
                if(handcards.get(0).getValue() == 1)handcards.remove(0);
                else handcards.remove(1);
                updateHandcards();
                dialog.cancel();
            }
        });

        return dialog;
    }

    private AlertDialog setting(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameActivity.this);
        View view = LayoutInflater.from(GameActivity.this).inflate(R.layout.dialog_setting, null);
        imgSettings[0] = view.findViewById(R.id.imgSeven_1);
        imgSettings[1] = view.findViewById(R.id.imgSeven_2);
        imgSettings[2] = view.findViewById(R.id.imgEight_1);
        imgSettings[3] = view.findViewById(R.id.imgEight_2);
        imgSettings[4] = view.findViewById(R.id.imgEight_3);
        imgSettings[5] = view.findViewById(R.id.imgEight_4);
        imgSettings[6] = view.findViewById(R.id.imgX);
        updateSetting(true, true, true);

        for(int i = 0; i < 7; i++){
            imgSettings[i].setTag(i+1);
            imgSettings[i].setOnClickListener(clrSetting);
        }

        dialogBuilder.setTitle(R.string.nav_option)
                .setNegativeButton(R.string.option_close, null)
                .setView(view);
        return dialogBuilder.create();
    }

    private void updateSetting(Boolean card7, Boolean card8, Boolean cardX){
        if(card7)switch (optCard7){
            case 0:
                imgSettings[0].setImageResource(R.drawable.card_7);
                imgSettings[1].setImageResource(R.drawable.card_7_2dis);
                break;
            case 1:
                imgSettings[0].setImageResource(R.drawable.card_7dis);
                imgSettings[1].setImageResource(R.drawable.card_7_2);
                break;
        }
        if(card8)switch (optCard8){
            case 0:
                imgSettings[2].setImageResource(R.drawable.card_8);
                imgSettings[3].setImageResource(R.drawable.card_8_2dis);
                imgSettings[4].setImageResource(R.drawable.card_8_3dis);
                imgSettings[5].setImageResource(R.drawable.card_8_4dis);
                break;
            case 1:
                imgSettings[2].setImageResource(R.drawable.card_8dis);
                imgSettings[3].setImageResource(R.drawable.card_8_2);
                imgSettings[4].setImageResource(R.drawable.card_8_3dis);
                imgSettings[5].setImageResource(R.drawable.card_8_4dis);
                break;
            case 2:
                imgSettings[2].setImageResource(R.drawable.card_8dis);
                imgSettings[3].setImageResource(R.drawable.card_8_2dis);
                imgSettings[4].setImageResource(R.drawable.card_8_3);
                imgSettings[5].setImageResource(R.drawable.card_8_4dis);
                break;
            case 3:
                imgSettings[2].setImageResource(R.drawable.card_8dis);
                imgSettings[3].setImageResource(R.drawable.card_8_2dis);
                imgSettings[4].setImageResource(R.drawable.card_8_3dis);
                imgSettings[5].setImageResource(R.drawable.card_8_4);
                break;
        }
        if(cardX){
            if(optCardX % 2 == 1)imgSettings[6].setImageResource(R.drawable.card_9);
            else imgSettings[6].setImageResource(R.drawable.card_9dis);
        }
    }

    private ImageButton.OnClickListener clrSetting = new ImageButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch((int)v.getTag()){
                case 1:
                    optCard7 = 0;
                    break;
                case 2:
                    optCard7 = 1;
                    break;
                case 3:
                    optCard8 = 0;
                    break;
                case 4:
                    optCard8 = 1;
                    break;
                case 5:
                    optCard8 = 2;
                    break;
                case 6:
                    optCard8 = 3;
                    break;
                case 7:
                    optCardX = 1 - optCardX;
                    break;
            }
            switch((int)v.getTag()){
                case 1: case 2:
                    updateSetting(true, false, false);
                    break;
                case 3: case 4: case 5: case 6:
                    updateSetting(false, true, false);
                    break;
                case 7:
                    updateSetting(false, false, true);
                    break;
            }
        }
    };

    private void init(Boolean isOver){
        MenuItem menuItem = navMenu.getMenu().findItem(R.id.nav_start);
        if(isOver && menuItem != null){
            menuItem.setTitle(R.string.nav_start);
            menuItem.setIcon(R.drawable.ic_baseline_play_arrow_24px);
        }

        rvHandcards.setLayoutManager(layoutManager[0]);
        handcards.clear();
        Card card = new Card(0);
        handcards.add(card);
        adapter.update(handcards);
    }

    private void updateHandcards(){
        if(handcards.size() == 0){
            handcards.clear();
            handcards.add(new Card(0));
        }
        rvHandcards.setLayoutManager(layoutManager[handcards.size() - 1]);
        adapter.update(handcards);
        if(handcards.size() == 1){
            adapter.setOnItemClickListener(null);
        }else if(handcards.size() == 2){
            adapter.setOnItemClickListener(clrDiscard);
        }
    }

    private HandcardAdapter.OnItemClickListener clrDiscard = new HandcardAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int tag) {
            if(tag == 5 || tag == 6){
                for(int i = 0; i < handcards.size(); i++)if(handcards.get(i).getValue() == 7){
                    customToast(GameActivity.this, getResources().getString(R.string.warning_seven_first), true).show();
                    return;
                }
            }
            if(tag == 8 && optCard8 == 1)customToast(GameActivity.this, getResources().getString(R.string.warning_eight_forbid), true).show();
            else if(tag == 1 || tag == 2 || tag == 3 || tag == 6){
                String userName = getIntent().getStringExtra("userName");
                targets = alives;
                targets.remove(userName);
                memberList(true, targets, tag).show();
            }
            else if(tag == 5){
                targets = alives;
                memberList(true, targets, tag).show();
            }else{
                SocketIo.getInstance().discard(tag, null, null);
                if(handcards.get(0).getValue() == tag)handcards.remove(0);
                else handcards.remove(1);
                updateHandcards();
            }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (id == R.id.nav_start) {
            drawer.closeDrawer(GravityCompat.START);
            MenuItem menuItem = navMenu.getMenu().findItem(R.id.nav_start);
            if(menuItem.getTitle().equals(getResources().getString(R.string.nav_start))){
                if(players.size() > 1){
                    menuItem.setTitle(R.string.nav_abort);
                    menuItem.setIcon(R.drawable.ic_baseline_close_24px);
                }
                SocketIo.getInstance().startGame(optCard7, optCard8, optCardX);
            }else SocketIo.getInstance().abortGame();
        } else if (id == R.id.nav_option) {
            drawer.closeDrawer(GravityCompat.START);
            setting().show();
        } else if (id == R.id.nav_leave) {
            drawer.closeDrawer(GravityCompat.START);
            leavingRoom().show();
        } else if (id == R.id.nav_member) {
            memberList(false, players, null).show();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        SocketIo.getInstance().leaveRoom();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    private Handler handler = new Handler();
    public Handler systemBroadcast = new Handler(){
        public void handleMessage(Message msg){
            Object obj = msg.obj;
            JSONObject jsonObject;
            SpannableStringBuilder text;
            SpannableString tmp1;
            String tmp2;
            Bundle bundle;
            switch (msg.what){
                case SocketIo.MSG:
                    jsonObject = (JSONObject) obj;
                    try {
                        tmp1 = SpannableString.valueOf(tvSysmsg.getText());
                        tmp2 = jsonObject.getString("msg");
                        if(jsonObject.getBoolean("isHL")){
                            text = new SpannableStringBuilder(tmp2);
                            text.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, tmp2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            if(tmp1.length() != 0)text.insert(0, "\n");
                            text.insert(0, tmp1);
                            tvSysmsg.setText(text);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    svText.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }else{
                            text = new SpannableStringBuilder(tmp1);
                            if(tmp1.length() != 0)text.insert(text.length(), "\n");
                            text.insert(text.length(), tmp2);
                            tvSysmsg.setText(text);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    svText.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case SocketIo.JOINFAILED:
                    finish();
                    break;
                case SocketIo.LEAVEROOM:
                    finish();
                    break;
                case SocketIo.JOINROOM:
                    Menu menu = navMenu.getMenu();
                    menu.clear();
                    players = new ArrayList<>();
                    try {
                        jsonObject = new JSONObject(String.valueOf(msg.obj));
                        tmp2 = jsonObject.getString("players");
                        tmp2 = tmp2.substring(1, tmp2.length() - 1);
                        String[] memberListRaw = tmp2.split(",");
                        for(int i = 0; i < memberListRaw.length; i++)if(memberListRaw[i].length() > 1)players.add(memberListRaw[i].substring(1, memberListRaw[i].length() - 1));
                        String userName = getIntent().getStringExtra("userName");
                        String creator = jsonObject.getString("creator");
                        tvUserName.setText(userName);
                        tvRoomNumber.setText(String.valueOf(jsonObject.getInt("roomNumber")));
                        menu.add(getResources().getString(R.string.room_label_creator) + " " + creator);
                        if(userName.equals(creator))navMenu.inflateMenu(R.menu.activity_game_drawer_creator);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    navMenu.inflateMenu(R.menu.activity_game_drawer);
                    break;
                case SocketIo.INIT:
                    init(true);
                    break;
                case SocketIo.SETTING:
                    bundle = (Bundle) msg.obj;
                    optCard7 = bundle.getInt("7") - 1;
                    optCard8 = bundle.getInt("8") - 1;
                    optCardX = bundle.getInt("X");
                    Card.setOpt(optCard7, optCard8);
                    break;
                case SocketIo.DRAWCARD:
                    bundle = (Bundle) msg.obj;
                    ArrayList<Integer> handcardsRaw = bundle.getIntegerArrayList("handcards");
                    alives = bundle.getStringArrayList("alives");
                    for(int i = 0; i < alives.size(); i++){
                        String t = alives.get(i);
                        if(t.length() > 0)alives.set(i, t.substring(1, t.length() - 1));
                    }
                    handcards.clear();
                    if(optCard7 == 1 && handcardsRaw.indexOf(7) != -1 && handcardsRaw.size() == 2){
                        if(handcardsRaw.get(0) + handcardsRaw.get(1) >= 12)SocketIo.getInstance().discard(7, null, null);
                    }else if(handcardsRaw.indexOf(9) != -1)SocketIo.getInstance().discard(9, null, null);
                    else{
                        for(int i = 0; i < handcardsRaw.size(); i++)handcards.add(new Card(handcardsRaw.get(i)));
                        updateHandcards();
                    }
                    break;
                case SocketIo.PEEK:
                    jsonObject = (JSONObject) msg.obj;
                    try {
                        customToast(GameActivity.this, jsonObject.getString("target") + getResources().getString(R.string.warning_has) + new Card(jsonObject.getInt("card")).getDisplayName(), false).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case SocketIo.ELIMINATE:
                    init(false);
                    break;
                case SocketIo.TOINTRO:
                    finish();
                    break;
                case SocketIo.USEDCARD:
                    usedCards.add(0, (Integer) msg.obj);
                    adapterUsedCard.update(usedCards);
            }
        }
    };
}
