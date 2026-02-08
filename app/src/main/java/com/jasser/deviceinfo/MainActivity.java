package com.jasser.deviceinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private String lastCookies = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setupWebView();
        
        // البدء بصفحة تسجيل دخول قوقل ثم يوتيوب
        webView.loadUrl("https://accounts.google.com/ServiceLogin?service=youtube&continue=https://m.youtube.com");
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        
        // خدعة User Agent لتخطي حماية قوقل (نتظاهر بأننا كروم على أندرويد حديث)
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 13; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.5735.196 Mobile Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                syncCookies();
                checkAndGrabCookies(url);
            }
        });
    }

    private void syncCookies() {
        CookieManager.getInstance().flush();
    }

    private void checkAndGrabCookies(String url) {
        String cookies = CookieManager.getInstance().getCookie(url);
        
        // التحقق من وجود كوكيز وعدم تكرار نفس الكوكيز
        if (cookies != null && !cookies.isEmpty() && !cookies.equals(lastCookies)) {
            // نتحقق إذا كانت الكوكيز تحتوي على معلومات تسجيل دخول (مثل SID أو SSID)
            if (url.contains("youtube") || url.contains("google")) {
                lastCookies = cookies;
                showCookieDialog(url, cookies);
            }
        }
    }

    private void showCookieDialog(String url, final String cookies) {
        // إنشاء الديالوج في الـ Thread الرئيسي لتجنب الأخطاء
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Cookies Captured! 🍪")
                    .setMessage("Site: " + url + "\n\nCookies (Tap Copy): " + cookies.substring(0, Math.min(cookies.length(), 100)) + "...")
                    .setCancelable(false)
                    .setPositiveButton("نسخ الكل", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            copyToClipboard(cookies);
                        }
                    })
                    .setNegativeButton("إخفاء", null)
                    .show();
            }
        });
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Grabbbed Cookies", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "تم نسخ الكوكيز للحافظة!", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
