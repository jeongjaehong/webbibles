Sending SMS Messages

To send an SMS message, you use the SmsManager class. Unlike other classes, you do not directly instantiate this class; instead you will call the getDefault() static method to obtain an SmsManager object.

 The sendTextMessage() method sends the SMS message with a PendingIntent.The PendingIntent object is used to identify a target to invoke at a later time. For example, after sending the message, you can use a PendingIntent object to display another activity. In this case, the PendingIntent object (pi) is simply pointing to the same activity (SMS.java), so when the SMS is sent, nothing will happen.


 

public class SMS extends Activity 
{
    //...
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        //...
    }
    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {        

               PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, SMS.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);        
    }    
} 
PendingIntent��ü�� ���߿� invoke�� target�� �з��ϱ� ���� ���ȴ�. �ѿ���, �޽����� ���� ��, �ٸ� activity�� display�ϱ� ���� PendingIntent��ü�� ����� �� �ִ�. �̷��� ���� ���� activity�� �ܼ��� ����Ű�� ���̱� ������, SMS���� ���� (PendingIntent��) �ƹ��͵� ���� �ʴ´�.

 

����, SMS������ ���� ����status�� ����monitor�ؾ� �Ѵٸ�, two���� BroadcastReceiver  �� �Բ� PendingIntent��ü�� �Ʒ��� ���� �� �� �ִ�. (Receive�� sendSMS�� ����, Send����)


         //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        



F11�� ���� Test Emulator�� �߰��� ������� ��������� port��ȣ�� �Է��Ͽ� ���� SMS�� �ְ���� �� �ִ�.

When SMS messages are received, the onCreate() method will be invoked. The SMS message is contained and attached to the Intent object (intent - the second parameter in the onReceive() method) via a Bundle object. The messages are stored in an Object array in the PDU format. To extract each message, you use the staticcreateFromPdu() method from the SmsMessage class. The SMS message is then displayed using the Toast class:

 

Receiving SMS Messages
Besides programmatically sending SMS messages, you can also intercept incoming SMS messages using a BroadcastReceiver object.

To see how to receive SMS messages from within your Android application, in the AndroidManifest.xml file add the<receiver> element so that incoming SMS messages can be intercepted by the SmsReceiver class:

SMS�޽����� �ޱ� ���ؼ��� AndroidManifest.xml�� <receiver>�� �߰��ؾ� �Ѵ�.

</activity>   <receiver android:name=".SmsReceiver"> <intent-filter> <action android:name= "android.provider.Telephony.SMS_RECEIVED" /> </intent-filter> </receiver>   </application> 

public class SmsReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String str = "";            
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                str += "SMS from " + msgs[i].getOriginatingAddress();                     
                str += " :";
                str += msgs[i].getMessageBody().toString();
                str += "\n";        
            }
            //---display the new SMS message---
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }                         
    }
}