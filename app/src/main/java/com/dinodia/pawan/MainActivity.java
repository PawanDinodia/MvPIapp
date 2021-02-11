package com.dinodia.pawan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.internal.view.SupportMenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private WebView wb;
    private long mBackPressed;
    private boolean connected = false;
    private ProgressBar pb;
    private Toolbar tb;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tb=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        wb= (WebView) findViewById(R.id.webview);
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.getProgressDrawable().setColorFilter(SupportMenu.CATEGORY_MASK, PorterDuff.Mode.SRC_IN);
        WebSettings wbs= wb.getSettings();
        wbs.setJavaScriptEnabled(true);
        wbs.setBuiltInZoomControls(true);
        wbs.setDisplayZoomControls(false);
        if(isConnected(getApplicationContext())){
            wb.loadUrl("https://ipc.gov.in/mandates/pvpi/materiovigilance-programme-of-india-mvpi.html");
//            wb.loadUrl("https://ipc.gov.in");
            wb.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            });
            if(isStoragePermissionGranted()){
                wb.setDownloadListener(new DownloadListener() {
                    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                        ((DownloadManager) MainActivity.this.getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
                        Toast.makeText(MainActivity.this.getApplicationContext(), "Downloading file...", Toast.LENGTH_SHORT).show();
                        new Intent("android.intent.action.OPEN_DOCUMENT").setType("*/*");
                    }
                });
            }
            wb.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100) {
                        MainActivity.this.pb.setVisibility(View.VISIBLE);
                    } else {
                        MainActivity.this.pb.setVisibility(View.GONE);
                    }
                    MainActivity.this.pb.setProgress(progress);
                }
            });
            Log.v("wfy","connected");
        }else {
            Log.v("wfy","not-connected");
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

    }
    public void onBackPressed() {
        if (this.mBackPressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        }
        if (wb.canGoBack()) {
            wb.goBack();
        }
        Toast.makeText(getApplicationContext(), "press back to exit", Toast.LENGTH_SHORT).show();
        this.mBackPressed = System.currentTimeMillis();
    }
    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobiledata = cm.getNetworkInfo(0);
            this.connected = wifi.isConnected() || mobiledata.isConnected();
        }
        return this.connected;
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("permission_tag","Permission is granted");
                return true;
            } else {

                Log.v("permission_tag","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.v("permission_tag","Permission is granted");
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v("permission_tag","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh:
                if (isConnected(getApplicationContext())) {
                    this.wb.reload();
                    break;
                }
                break;
            case R.id.back:
                if (this.wb.canGoBack()) {
                    if (isConnected(getApplicationContext())) {
                        this.wb.goBack();
                        break;
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Cannot go back!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.forward:
                if (this.wb.canGoForward()) {
                    if (isConnected(getApplicationContext())) {
                        this.wb.goForward();
                        break;
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Cannot go forward!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.share:
                break;
            default:
                super.onOptionsItemSelected(item);
                break;
        }
        return true;
    }
}
