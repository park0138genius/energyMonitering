package org.iptime.twens.energemonitering;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;
    private Handler handler = new Handler();

    private static final String MAIN_URL = "http://twens.iptime.org:91/teems/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView = findViewById(R.id.main_webView);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setSaveFormData(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);

        myWebView.getSettings().setTextZoom(100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        if (Build.VERSION.SDK_INT >= 16) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        myWebView.setWebViewClient(new MyWebViewClient());
        myWebView.setWebChromeClient(new MyWebChromeClient());
        myWebView.loadUrl(MAIN_URL);
        myWebView.addJavascriptInterface(new JavascriptInterface(),"callMobile");

//        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                myWebView.evaluateJavascript( "mobileActionLogin('pd201500', '4hu/yBRQy+qdFElQ6ENGq/5SHzbYnsrUvko+bKaMaEE=')", null);
//            }
//        });
    }

    final class JavascriptInterface {

        @android.webkit.JavascriptInterface
        public void autoLogin(final String id, final String pw) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    String oldId = pref.getString("id", null);
                    String oldPw = pref.getString("pw", null);

                    if (null == oldId) {
                        editor.putString("id", null);
                        editor.putString("pw", null);
                        editor.commit();
                    }

                    if ((null != oldId && !id.equals(oldId)) || (null != oldPw && !pw.equals(oldPw))) {
                        editor.putString("id", id );
                        editor.putString("pw", pw);
                        editor.commit();
                    }
                }
            });
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
                if (MAIN_URL.equals(url)) {
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    String id = pref.getString("id", null);
                    String pw = pref.getString("pw", null);

                    if (null != id  && null != pw && id.length() > 0 && pw.length() > 0) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            String function = "mobileActionLogin('"+id+"', '"+pw+"');";
                            myWebView.evaluateJavascript( function, null);
                        } else {
                            String function = "javascript:mobileActionLogin('"+id+"', '"+pw+"');";
                            myWebView.loadUrl(function);
                        }
                }
            }


                super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }
    }
}
