package org.tragicdilemma.bgloveletter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIo {
    public static final int NAMECONFIRM = 0, ROOMLIST = 1, JOINFAILED = 2, JOINROOM = 3, LEAVEROOM = 4, MSG = 5, INIT = 6, SETTING = 7, DRAWCARD = 8, PEEK = 9, ELIMINATE = 10, USEDCARD = 11, TOINTRO = 12;

    private String ServerIP = "https://tragic-dilemma-loveletter.herokuapp.com/";
    private Socket io;
    private static Handler introHandler = null, hallHandler = null, gameHandler = null;
    private static SocketIo instance;

    private  Emitter.Listener lToIntro = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = TOINTRO;
            if(hallHandler != null)hallHandler.sendMessage(msg);
            if(gameHandler != null)gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lNameConfirm = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = NAMECONFIRM;
            msg.obj = args[0];
            introHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lRoomlist = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = ROOMLIST;
            msg.obj = args[0];
            hallHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lJoinRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = JOINROOM;
            msg.obj = args[0];
            hallHandler.sendMessage(msg);
            msg = new Message();
            msg.what = JOINROOM;
            msg.obj = args[0];
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lJoinFailed = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = MSG;
            msg.obj = args[0];
            hallHandler.sendMessage(msg);
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lLeaveRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = LEAVEROOM;
            gameHandler.sendMessage(msg);
            if((Boolean) args[0]){
                msg = new Message();
                msg.what = MSG;
                msg.obj = "Room closed";
                hallHandler.sendMessage(msg);
            }
        }
    };

    private Emitter.Listener lMsg = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = MSG;
            msg.obj = args[0];
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lInit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = INIT;
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lSetting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Bundle bundle = new Bundle();
            Message msg = new Message();
            msg.what = SETTING;
            try {
                JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                bundle.putInt("7", jsonObject.getInt("card7"));
                bundle.putInt("8", jsonObject.getInt("card8"));
                if(jsonObject.getBoolean("cardX"))bundle.putInt("X", 1);
                else bundle.putInt("X", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg.obj = bundle;
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lDrawCard = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            String tmp1 = null, tmp2 = null;
            try {
                tmp1 = jsonObject.getString("card");
                tmp2 = jsonObject.getString("alives");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tmp1 = tmp1.substring(1, tmp1.length() - 1);
            tmp2 = tmp2.substring(1, tmp2.length() - 1);
            String[] handcardsRaw = tmp1.split(",");
            ArrayList<String> alives = new ArrayList<>(Arrays.asList(tmp2.split(",")));
            ArrayList<Integer> handcards = new ArrayList<>();
            for(int i = 0; i < handcardsRaw.length; i++)handcards.add(Integer.parseInt(handcardsRaw[i]));
            Message msg = new Message();
            msg.what = DRAWCARD;
            Bundle bundle = new Bundle();
            bundle.putSerializable("handcards", handcards);
            bundle.putSerializable("alives", alives);
            msg.obj = bundle;
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lPeek = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = PEEK;
            msg.obj = args[0];
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lEliminate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = ELIMINATE;
            gameHandler.sendMessage(msg);
        }
    };

    private Emitter.Listener lUsedCard = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = new Message();
            msg.what = USEDCARD;
            JSONObject jsonObject = (JSONObject) args[0];
            Integer tmp = null;
            try {
                tmp = Integer.valueOf(jsonObject.getString("card"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg.obj = tmp;
            gameHandler.sendMessage(msg);
        }
    };

    public SocketIo(){
        try {
            io = IO.socket(ServerIP);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static SocketIo getInstance() {
        if (instance == null) instance = new SocketIo();
        return instance;
    }

    public static void setIntroHandler(Handler handler){
        introHandler = handler;
    }

    public static void setHallHandler(Handler handler){
        hallHandler = handler;
    }

    public static void setGameHandler(Handler handler){
        gameHandler = handler;
    }

    public void connect(String userName){
        io.on("nameConfirmed", lNameConfirm);
        io.on("roomList", lRoomlist);
        io.on("joinFailed", lJoinFailed);
        io.on("joinRoom", lJoinRoom);
        io.on("leaveRoom", lLeaveRoom);
        io.on("msg", lMsg);
        io.on("init", lInit);
        io.on("gameSetting", lSetting);
        io.on("drawCard", lDrawCard);
        io.on("peek", lPeek);
        io.on("eliminated", lEliminate);
        io.on("usedCard", lUsedCard);
        io.on("toIntro", lToIntro);

        io.connect();
        io.emit("newUser", userName);
    }

    public void disconnect(){
        io.disconnect();

        io.off("nameConfirmed", lNameConfirm);
        io.off("roomList", lRoomlist);
        io.off("joinFailed", lJoinFailed);
        io.off("joinRoom", lJoinRoom);
        io.off("leaveRoom", lLeaveRoom);
        io.off("msg", lMsg);
        io.off("init", lInit);
        io.off("gameSetting", lSetting);
        io.off("drawCard", lDrawCard);
        io.off("peek", lPeek);
        io.off("eliminated", lEliminate);
        io.off("usedCard", lUsedCard);
        io.off("toIntro", lToIntro);
    }

    public Boolean isConnected(){
        return io.connected();
    }

    public void getRoomlist(){
        io.emit("roomList");
    }

    public void createRoom(){
        io.emit("createRoom");
    }

    public void joinRoom(Integer roomNumber){
        io.emit("joinRoom", roomNumber);
    }

    public void leaveRoom(){
        io.emit("leaveRoom");
    }

    public void startGame(int optCard7, int optCard8, int optCardX){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("card7", optCard7 + 1);
            jsonObject.put("card8", optCard8 + 1);
            if(optCardX == 1)jsonObject.put("cardX", true);
            else jsonObject.put("cardX", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        io.emit("startGame", jsonObject);
    }

    public void abortGame(){
        io.emit("abortGame");
    }

    public void discard(int card, @Nullable Integer target, @Nullable Integer guessCard){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("card", card);
            jsonObject.put("target", target);
            jsonObject.put("extra", guessCard);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        io.emit("discardCard", jsonObject);
    }
}
