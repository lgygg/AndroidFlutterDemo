package com.lgy.crash;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * @Author: lgy
 * @Date: 2019/12/11 下午5:05
 * @Description: 崩溃日志显示的activity
 */
public class CrashErrorActivity extends Activity {

    private String errorMsg="";
    //显示重启
    private boolean showRestart = false;
    private Button btnRestart;
    private String restartActivityName;

    public static void start(Context context,String errorMsg,boolean showRestart,String restartActivityName){
        Intent intent = new Intent(context,CrashErrorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("errorMsg",errorMsg);
        intent.putExtra("showRestart",showRestart);
        intent.putExtra("restartActivityName",restartActivityName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_error);
        errorMsg = getIntent().getStringExtra("errorMsg");
        restartActivityName = getIntent().getStringExtra("restartActivityName");
        showRestart = getIntent().getBooleanExtra("showRestart",false);
        initView();
    }

    private void initView(){
        btnRestart = findViewById(R.id.btnRestart);
        if(!showRestart){
            btnRestart.setText("关闭程序");
        }
        btnRestart.setOnClickListener(view -> {
            if(showRestart){
                restartApplication();
            }else{
                finish();
                killCurrentProcess();
            }
        });
        findViewById(R.id.btnMoreInfo).setOnClickListener(view -> {
            showErrorMsgDialog();
        });
    }


    private void showErrorMsgDialog(){
        AlertDialog dialog = new AlertDialog.Builder(CrashErrorActivity.this)
                .setTitle("错误详情")
                .setMessage(errorMsg)
                .setPositiveButton("关闭", null)
                .setNeutralButton("复制日志",
                        (dialog1, which) -> {
                            copyErrorToClipboard();
                            Toast.makeText(CrashErrorActivity.this, "复制日志", Toast.LENGTH_SHORT).show();
                        })
                .show();
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.details_text_size));
    }


    private void copyErrorToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("错误信息", errorMsg);
        clipboard.setPrimaryClip(clip);
    }


    private void restartApplication() {
        Intent intent;
        try {
            intent = new Intent(CrashErrorActivity.this, Class.forName(restartActivityName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (intent.getComponent() != null) {
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
            }
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        killCurrentProcess();
    }

    private  void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}

