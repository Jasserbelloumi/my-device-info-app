package com.jasser.deviceinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private String lastUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        
        // إعدادات الويب لتمكين الكوكيز والجافاسكربت
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                // منع التكرار لنفس الرابط
                if (!url.equals(lastUrl)) {
                    lastUrl = url;
                    String cookies = CookieManager.getInstance().getCookie(url);
                    if (cookies != null && !cookies.isEmpty()) {
                        showCookieDialog(url, cookies);
                    }
                }
            }
        });

        // الرابط الافتراضي
        webView.loadUrl("https://m.youtube.com");
    }

    private void showCookieDialog(String url, final String cookies) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cookies Detected!");
        builder.setMessage("URL: " + url + "\n\nCookies:\n" + cookies);
        
        builder.setPositiveButton("نسخ الكوكيز", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                copyToClipboard(cookies);
            }
        });

        builder.setNegativeButton("إغلاق", null);
        builder.show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Cookies", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "تم نسخ الكوكيز بنجاح!", Toast.LENGTH_SHORT).show();
    }
    
    // للرجوع للخلف داخل المتصفح بدل الخروج من التطبيق
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
