package org.tragicdilemma.bgloveletter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class IntroActivity extends AppCompatActivity {

    private Button btnSend;
    private SocketIo socket;
    private EditText etUser;
    private ConstraintLayout clLetter;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        SocketIo.setIntroHandler(systemBroadcast);
        btnSend = findViewById(R.id.btnSend);
        etUser = findViewById(R.id.etUser);
        clLetter = findViewById(R.id.clLetter);
        pbLoading = findViewById(R.id.pbLoading);

        pbLoading.setVisibility(View.INVISIBLE);
        etUser.setEnabled(true);
        clLetter.setAlpha(1);
        btnSend.setEnabled(true);

        btnSend.setOnClickListener(clSend);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pbLoading.setVisibility(View.INVISIBLE);
        etUser.setEnabled(true);
        btnSend.setEnabled(true);
        clLetter.setAlpha(1);
    }

    private Button.OnClickListener clSend = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userName = String.valueOf(etUser.getText());
            if(userName.length() > 20)customToast(IntroActivity.this, getResources().getString(R.string.warning_id_too_long), true).show();
            else if(userName.isEmpty())customToast(IntroActivity.this, getResources().getString(R.string.warning_id_empty), true).show();
            else{
                String invalid = "\\/()[]{}+-*/'\"";
                for(int i = 0; i < invalid.length(); i++)if(userName.indexOf(invalid.toCharArray()[i]) != -1){
                    customToast(IntroActivity.this, getResources().getString(R.string.warning_id_invalid), true).show();
                    return;
                }
                socket = SocketIo.getInstance();
                socket.connect(userName);
                pbLoading.setVisibility(View.VISIBLE);
                btnSend.setEnabled(false);
                etUser.setEnabled(false);
                clLetter.setAlpha((float) 0.5);
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

    public Handler systemBroadcast = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case SocketIo.NAMECONFIRM:
                    if((Boolean)msg.obj){
                        Intent it = new Intent(IntroActivity.this, HallActivity.class);
                        it.putExtra("userName", String.valueOf(etUser.getText()));
                        startActivity(it);
                    }else customToast(IntroActivity.this, getResources().getString(R.string.warning_id_repeat), true).show();
                    break;
            }
        }
    };
}
