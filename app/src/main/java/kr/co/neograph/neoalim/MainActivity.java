package kr.co.neograph.neoalim;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.firebase.iid.FirebaseInstanceId;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {
    private static final String ENTRY_URL = "file:///android_asset/intranet/index.html";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private static final String TYPE_IMAGE = "image/*";
    private int anAssert;
    private BackPressCloseHandler backPressCloseHandler;
    private String mCameraPhotoPath;
    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> mUploadMessage;
    WebView web;

    /* renamed from: kr.co.neograph.neograph.MainActivity$1 */
    class fn_apkdownload_cancle implements OnClickListener {
        fn_apkdownload_cancle() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
        }
    }

    /* renamed from: kr.co.neograph.neograph.MainActivity$2 */
    class fn_apkdownload_ok implements OnClickListener {
        fn_apkdownload_ok() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://localhost/appPakage/intranet.apk")));
        }
    }

    /* renamed from: kr.co.neograph.neograph.MainActivity$3 */
    class fn_filedownload extends WebChromeClient {
        fn_filedownload() {
        }

        public void onCloseWindow(WebView w) {
            super.onCloseWindow(w);
            MainActivity.this.finish();
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
            MainActivity.this.mUploadMessage = uploadMsg;
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType(MainActivity.TYPE_IMAGE);
            MainActivity.this.startActivityForResult(intent, 1);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, acceptType, "");
        }

        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            Log.d(getClass().getName(), "openFileChooser : " + acceptType + "/" + capture);
            MainActivity.this.mUploadMessage = uploadFile;
            imageChooser();
        }

        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            System.out.println("WebViewActivity A>5, OS Version : " + VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
            if (MainActivity.this.mFilePathCallback != null) {
                MainActivity.this.mFilePathCallback.onReceiveValue(null);
            }
            MainActivity.this.mFilePathCallback = filePathCallback;
            imageChooser();
            return true;
        }

        private void imageChooser() {
            Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                File file = null;
                try {
                    file = MainActivity.this.createImageFile();
                    takePictureIntent.putExtra("PhotoPath", MainActivity.this.mCameraPhotoPath);
                } catch (IOException ex) {
                    Log.e(getClass().getName(), "Unable to create Image File", ex);
                }
                if (file != null) {
                    MainActivity.this.mCameraPhotoPath = "file:" + file.getAbsolutePath();
                    takePictureIntent.putExtra("output", Uri.fromFile(file));
                } else {
                    takePictureIntent = null;
                }
            }
            Intent contentSelectionIntent = new Intent("android.intent.action.GET_CONTENT");
            contentSelectionIntent.addCategory("android.intent.category.OPENABLE");
            contentSelectionIntent.setType(MainActivity.TYPE_IMAGE);
            Intent[] intentArray = takePictureIntent != null ? new Intent[]{takePictureIntent} : new Intent[0];
            Intent chooserIntent = new Intent("android.intent.action.CHOOSER");
            chooserIntent.putExtra("android.intent.extra.INTENT", contentSelectionIntent);
            chooserIntent.putExtra("android.intent.extra.TITLE", "Image Chooser");
            chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", intentArray);
            MainActivity.this.startActivityForResult(chooserIntent, 1);
        }
    }

    /* renamed from: kr.co.neograph.neograph.MainActivity$4 */
    class fn_setfirebase_token extends WebViewClient {
        fn_setfirebase_token() {
        }

        public void onPageFinished(WebView view, String url) {
            if (url.equals(MainActivity.ENTRY_URL)) {
                String token = "";
                if (FirebaseInstanceId.getInstance().getToken() == null) {
                    Log.d(MainActivity.TAG, "토큰값이 없어요.");
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
            Log.d(MainActivity.TAG, "URL :: " + url);
            Log.d(MainActivity.TAG, "userAgent :: " + userAgent);
            Log.d(MainActivity.TAG, "contentDisposition :: " + contentDisposition);
            Log.d(MainActivity.TAG, "mimeType :: " + mimeType);
            Log.d(MainActivity.TAG, "contentLength :: " + contentLength);
            try {
                String fileName = URLDecoder.decode(contentDisposition.replace("attachment; filename=", "").replace("\"", ""), "UTF-8");
                Request request = new Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file");
                request.setTitle(fileName);
                Log.d(MainActivity.TAG, "setTitle :: " + fileName);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(1);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                ((DownloadManager) MainActivity.this.getSystemService("download")).enqueue(request);
                Toast.makeText(MainActivity.this.getApplicationContext(), "Downloading File", 1).show();
            } catch (Exception e) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                    return;
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                    Toast.makeText(MainActivity.this.getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", 1).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 110);
                    return;
                }
                Toast.makeText(MainActivity.this.getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", 1).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 110);
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
        } else if (VERSION.SDK_INT >= 21) {
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
            if (VERSION.SDK_INT >= 21) {
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
        this.backPressCloseHandler.onBackPressed();
    }

    /*public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);*/

        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        /*String sourceVersion = "";
        String pakageVersion = "";
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://localhost/appPakage/version.txt").openStream()));
            while (true) {
                String str = reader.readLine();
                if (str == null) {
                    break;
                }
                sb.append(str);
            }
            sourceVersion = sb.toString().substring(8, sb.toString().length());
            reader.close();
            Log.d(TAG, "최신버전 " + sourceVersion);
        } catch (IOException e) {
            Log.d(TAG, "읽기실패");
        }
        try {
            pakageVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Log.d(TAG, "설치버전 " + pakageVersion);
        } catch (NameNotFoundException e2) {
            e2.printStackTrace();
        }
        if (!sourceVersion.equals(pakageVersion)) {
            new AlertDialog.Builder(this).setTitle("최신버전 알림").setMessage("최신버전이 있습니다. 다운로드 흐 설치를 진행하시겠습니까?").setIcon(R.mipmap.ic_launcher).setPositiveButton("확인", new fn_apkdownload_ok()).setNegativeButton("취소", new fn_apkdownload_cancle()).show();
        }*/
        Log.d(TAG, "START");
        Log.d("Refreshed token", "Refreshed token");
        Log.d("FirebaseInstanceId", FirebaseInstanceId.getInstance().getToken() + "");
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
        webSet.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webSet.setSupportMultipleWindows(true);
        webSet.setSaveFormData(false);
        webSet.setSavePassword(false);
        webSet.setUseWideViewPort(true);
        webSet.setDefaultTextEncodingName("UTF-8");
        this.web.setWebChromeClient(new fn_filedownload());
        this.web.setWebViewClient(new WebViewClient());
        this.web.setWebViewClient(new fn_setfirebase_token());
        this.web.addJavascriptInterface(new JavaScriptInterface(this.web), "neodroid");
        this.web.loadUrl(ENTRY_URL);
        layout.addView(this.web);
        setContentView((View) layout);
        this.backPressCloseHandler = new BackPressCloseHandler(this, this.web);
        this.web.setDownloadListener(new fn_webfiledownload());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu2:
                Toast.makeText(this, "메일", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(this, MailActivity.class);
                startActivity(intent2);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
