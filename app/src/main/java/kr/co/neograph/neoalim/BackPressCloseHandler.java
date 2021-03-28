package kr.co.neograph.neoalim;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.webkit.WebView;
import android.widget.Toast;

public class BackPressCloseHandler {
    private Activity activity;
    private long backKeyPressedTime = 0;
    private Toast toast;
    WebView web;

    class fn_cancle implements OnClickListener {
        fn_cancle() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
        }
    }

    class fn_ok implements OnClickListener {
        fn_ok() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            BackPressCloseHandler.this.activity.finish();
        }
    }

    public BackPressCloseHandler(Activity context, WebView wv) {
        this.activity = context;
        this.web = wv;
    }

    public void onBackPressed() {
        this.web.goBack();
        if (System.currentTimeMillis() > this.backKeyPressedTime + 2000) {
            this.backKeyPressedTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() <= this.backKeyPressedTime + 2000) {
            new Builder(this.activity).setTitle("종료확인").setMessage("앱을 종료하시겠습니까?").setIcon(R.mipmap.ic_launcher).setPositiveButton("확인", new fn_ok()).setNegativeButton("취소", new fn_cancle()).show();
        }
    }

    public void onBackPressed2() {
        this.web.goBack();
        if (System.currentTimeMillis() > this.backKeyPressedTime + 2000) {
            this.backKeyPressedTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() <= this.backKeyPressedTime + 2000) {
            this.activity.finish();
        }
    }
}