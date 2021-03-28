package kr.co.neograph.neoalim;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.google.firebase.iid.FirebaseInstanceId;

public class JavaScriptInterface {
    private static final String TAG = "JavaScriptInterface";
    WebView web;

    /* renamed from: kr.co.neograph.neograph.JavaScriptInterface$2 */
    class C02812 implements Runnable {
        C02812() {
        }

        public void run() {
            JavaScriptInterface.this.web.loadUrl("javascript:requestToken()");
        }
    }

    JavaScriptInterface(WebView wv) {
        this.web = wv;
    }

    @JavascriptInterface
    public void onPageOpen(final String url) {
        this.web.post(new Runnable() {
            public void run() {
                if (JavaScriptInterface.this.web != null) {
                    JavaScriptInterface.this.web.loadUrl(url);
                }
            }
        });
    }

    @JavascriptInterface
    public void justDoIt(String keyword) {
        Log.d(TAG, "웹에서 호출 : " + keyword);
        if (!keyword.equals("token")) {
            return;
        }
        if (FirebaseInstanceId.getInstance().getToken() == null) {
            Log.d(TAG, "2 토큰값이 없어요.");
            this.web.post(new C02812());
            return;
        }
        final String script = "javascript:setToken('" + FirebaseInstanceId.getInstance().getToken() + "');";
        this.web.post(new Runnable() {
            public void run() {
                JavaScriptInterface.this.web.loadUrl(script);
            }
        });
    }
}