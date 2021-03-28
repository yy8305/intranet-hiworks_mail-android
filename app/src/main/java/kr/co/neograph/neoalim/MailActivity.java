package kr.co.neograph.neoalim;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MailActivity extends AppCompatActivity {
    private static final String ENTRY_URL = "file:///android_asset/hiworks/index.html";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final String TAG = "MailActivity";
    private static final String TYPE_IMAGE = "image/*";
    private int anAssert;
    private BackPressCloseHandler backPressCloseHandler;
    private String mCameraPhotoPath;
    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> mUploadMessage;
    WebView web;

    /* renamed from: kr.co.neograph.neograph.MainActivity$1 */
    class fn_apkdownload_cancle implements DialogInterface.OnClickListener {
        fn_apkdownload_cancle() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
        }
    }

    /* renamed from: kr.co.neograph.neograph.MainActivity$3 */
    class fn_filedownload extends WebChromeClient {
        fn_filedownload() {
        }

        public void onCloseWindow(WebView w) {
            super.onCloseWindow(w);
            MailActivity.this.finish();
        }

        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
            WebSettings settings = view.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            view.setWebChromeClient(this);
            ((WebView.WebViewTransport)resultMsg.obj).setWebView(view);
            resultMsg.sendToTarget();
            return false;
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            MailActivity.this.mUploadMessage = uploadMsg;
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType(MailActivity.TYPE_IMAGE);
            MailActivity.this.startActivityForResult(intent, 1);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, acceptType, "");
        }

        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            Log.d(getClass().getName(), "openFileChooser : " + acceptType + "/" + capture);
            MailActivity.this.mUploadMessage = uploadFile;
            imageChooser();
        }

        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            System.out.println("WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
            if (MailActivity.this.mFilePathCallback != null) {
                MailActivity.this.mFilePathCallback.onReceiveValue(null);
            }
            MailActivity.this.mFilePathCallback = filePathCallback;
            imageChooser();
            return true;
        }

        private void imageChooser() {
            Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (takePictureIntent.resolveActivity(MailActivity.this.getPackageManager()) != null) {
                File file = null;
                try {
                    file = MailActivity.this.createImageFile();
                    takePictureIntent.putExtra("PhotoPath", MailActivity.this.mCameraPhotoPath);
                } catch (IOException ex) {
                    Log.e(getClass().getName(), "Unable to create Image File", ex);
                }
                if (file != null) {
                    MailActivity.this.mCameraPhotoPath = "file:" + file.getAbsolutePath();
                    takePictureIntent.putExtra("output", Uri.fromFile(file));
                } else {
                    takePictureIntent = null;
                }
            }
            Intent contentSelectionIntent = new Intent("android.intent.action.GET_CONTENT");
            contentSelectionIntent.addCategory("android.intent.category.OPENABLE");
            contentSelectionIntent.setType(MailActivity.TYPE_IMAGE);
            Intent[] intentArray = takePictureIntent != null ? new Intent[]{takePictureIntent} : new Intent[0];
            Intent chooserIntent = new Intent("android.intent.action.CHOOSER");
            chooserIntent.putExtra("android.intent.extra.INTENT", contentSelectionIntent);
            chooserIntent.putExtra("android.intent.extra.TITLE", "Image Chooser");
            chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", intentArray);
            MailActivity.this.startActivityForResult(chooserIntent, 1);
        }
    }

    /* renamed from: kr.co.neograph.neograph.MainActivity$4 */
    class fn_setfirebase_token extends WebViewClient {
        fn_setfirebase_token() {
        }

        public void onPageFinished(WebView view, String url) {
            if (url.equals(MailActivity.ENTRY_URL)) {
                String token = "";
                if (FirebaseInstanceId.getInstance().getToken() == null) {
                    Log.d(MailActivity.TAG, "토큰값이 없어요.");
                    view.loadUrl("javascript:requestToken();");
                    return;
                }
                view.loadUrl("javascript:setToken('" + FirebaseInstanceId.getInstance().getToken() + "');");
            }
        }
    }

    /* renamed from: kr.co.neograph.neograph.MainActivity$5 */
    class fn_webfiledownload implements DownloadListener {
        fn_webfiledownload() {
        }

        @SuppressLint("WrongConstant")
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
            Log.d(MailActivity.TAG, "URL :: " + url);
            Log.d(MailActivity.TAG, "userAgent :: " + userAgent);
            Log.d(MailActivity.TAG, "contentDisposition :: " + contentDisposition);
            Log.d(MailActivity.TAG, "mimeType :: " + mimeType);
            Log.d(MailActivity.TAG, "contentLength :: " + contentLength);
            try {
                String fileName = URLDecoder.decode(contentDisposition.replace("attachment; filename=", "").replace("\"", ""), "UTF-8");
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file");
                request.setTitle(fileName);
                Log.d(MailActivity.TAG, "setTitle :: " + fileName);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(1);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                ((DownloadManager) MailActivity.this.getSystemService("download")).enqueue(request);
                Toast.makeText(MailActivity.this.getApplicationContext(), "Downloading File", 1).show();
            } catch (Exception e) {
                if (ContextCompat.checkSelfPermission(MailActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                    return;
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(MailActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                    Toast.makeText(MailActivity.this.getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", 1).show();
                    ActivityCompat.requestPermissions(MailActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 110);
                    return;
                }
                Toast.makeText(MailActivity.this.getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", 1).show();
                ActivityCompat.requestPermissions(MailActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 110);
            }
        }
    }

    private File createImageFile() throws IOException {
        return File.createTempFile("JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || resultCode != -1) {
            if (this.mFilePathCallback != null) {
                this.mFilePathCallback.onReceiveValue(null);
            }
            if (this.mUploadMessage != null) {
                this.mUploadMessage.onReceiveValue(null);
            }
            this.mFilePathCallback = null;
            this.mUploadMessage = null;
            super.onActivityResult(requestCode, resultCode, data);
        } else if (Build.VERSION.SDK_INT >= 21) {
            if (this.mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            this.mFilePathCallback.onReceiveValue(new Uri[]{getResultUri(data)});
            this.mFilePathCallback = null;
        } else if (this.mUploadMessage == null) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Uri result = getResultUri(data);
            Log.d(getClass().getName(), "openFileChooser : " + result);
            this.mUploadMessage.onReceiveValue(result);
            this.mUploadMessage = null;
        }
    }

    private Uri getResultUri(Intent data) {
        if (data != null && !TextUtils.isEmpty(data.getDataString())) {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= 21) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }
            return Uri.parse(filePath);
        } else if (this.mCameraPhotoPath != null) {
            return Uri.parse(this.mCameraPhotoPath);
        } else {
            return null;
        }
    }

    public void onBackPressed() {
        this.backPressCloseHandler.onBackPressed2();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);*/


        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.rgb(255, 255, 255));
        this.web = new WebView(this);
        WebSettings webSet = this.web.getSettings();
        webSet.setAllowUniversalAccessFromFileURLs(true);
        webSet.setBuiltInZoomControls(false);
        webSet.setJavaScriptEnabled(true);
        webSet.setJavaScriptCanOpenWindowsAutomatically(true);
        webSet.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSet.setSupportMultipleWindows(true);
        webSet.setSaveFormData(false);
        webSet.setSavePassword(false);
        webSet.setUseWideViewPort(true);
        webSet.setDefaultTextEncodingName("UTF-8");
        this.web.setWebChromeClient(new MailActivity.fn_filedownload());
        this.web.setWebViewClient(new WebViewClient());
        this.web.setWebViewClient(new MailActivity.fn_setfirebase_token());
        this.web.addJavascriptInterface(new JavaScriptInterface(this.web), "hiworksdroid");
        this.web.loadUrl(ENTRY_URL);
        layout.addView(this.web);
        setContentView((View) layout);
        this.backPressCloseHandler = new BackPressCloseHandler(this, this.web);
        this.web.setDownloadListener(new MailActivity.fn_webfiledownload());
    }
}
