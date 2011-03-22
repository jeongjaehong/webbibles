package org.nilriri.webbibles.tools;

import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Prefs;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SendMail extends Activity {
    /**  
     * Called with the activity is first created.  
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.sendmail);
        final Button send = (Button) this.findViewById(R.id.send);
        final EditText sendto = (EditText) this.findViewById(R.id.sendto);
        final EditText body = (EditText) this.findViewById(R.id.body);
        final EditText subject = (EditText) this.findViewById(R.id.subject);

        String msgbody = this.getIntent().getStringExtra("msgbody");

        body.setText(msgbody);

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String userName = Prefs.getGMailUserID(SendMail.this);
                String userPassword = Prefs.getGMailPassword(SendMail.this);

                GMailSender sender = new GMailSender(userName, userPassword); // SUBSTITUTE HERE                     
                try {
                    sender.sendMail(subject.getText().toString()//
                            , body.getText().toString()//
                            , userName//
                            , sendto.getText().toString());
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        });
    }
}
