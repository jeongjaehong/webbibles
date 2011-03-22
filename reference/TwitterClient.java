package org.apache.twitter;

import android.app.Activity;
import android.content.Intent;
import android.net.http.RequestQueue;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Initial screen with edit box for tweets and
 * a web view to display the tweets from friends
 */
public class TwitterClient extends Activity {

    static final int GET_LOGIN_INFORMATION = 1;

    WebView webView;
    RequestQueue requestQueue;
    String authInfo;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        // Set the initial text
        webView = (WebView) findViewById(R.id.webView);
        webView.loadData(
                "Please click on setup and enter your twitter credentials",
                "text/html", "utf-8");

        // When they click on the set up button show the login screen
        Button button = (Button) findViewById(R.id.setup);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TwitterClient.this, TwitterLogin.class);
                startSubActivity(intent, GET_LOGIN_INFORMATION);
            }
        });

        // When they click on the Tweet! button, then get the
        // text in the edit box and send it to twitter
        final Activity activity = this;
        Button button2 = (Button) findViewById(R.id.update);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("http", "Update clicked");
                Map headers = new HashMap();
                if (authInfo == null) {
                    return;
                }
                headers.put("Authorization", "Basic " + new String(Base64.encodeBase64(authInfo.getBytes())));
                EditText user = (EditText) findViewById(R.id.updateText);
                String text = null;
                try {
                    text = "status=" + URLEncoder.encode(user.getText().toString(), "UTF-8");
                    Log.i("http", "with " + text);
                } catch (UnsupportedEncodingException e) {
                    Log.e("http", e.getMessage());
                }
                byte[] bytes = text.getBytes();
                ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
                // See Twitter API documentation for more information
                // http://groups.google.com/group/twitter-development-talk/web/api-documentation
                requestQueue.queueRequest(
                        "https://twitter.com/statuses/update.xml",
                        "POST", headers, new MyEventHandler2(activity), baos, bytes.length, false);
            }
        });

        // Start a thread to update the tweets from friends every minute
        requestQueue = new RequestQueue(this);
        Thread t = new Thread(new MyRunnable(this));
        t.start();
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    String data, Bundle extras) {
        if (requestCode == GET_LOGIN_INFORMATION && resultCode == RESULT_OK) {
            // Save the user login information
            authInfo = data;
        }
    }

}
