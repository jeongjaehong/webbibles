import com.google.android.gdata.client.AndroidXmlParserFactory;
import com.google.android.gdata.client.JakartaGDataClient;
import com.google.wireless.gdata.calendar.client.CalendarClient;
import com.google.wireless.gdata.calendar.data.EventEntry;
import com.google.wireless.gdata.calendar.data.When;
import com.google.wireless.gdata.calendar.parser.xml.XmlCalendarGDataParserFactory;
import com.google.wireless.gdata.client.QueryParams;
import com.google.wireless.gdata.data.Feed;
import com.google.wireless.gdata.parser.GDataParser;
    /**
     * When the user clicks on the lookup button we use the ATOM/GData API to fetch the
     * details of events for a specific date
     * @param view
     */
    public void onClick(View view) {
        XmlCalendarGDataParserFactory factory = new XmlCalendarGDataParserFactory(new AndroidXmlParserFactory());
        JakartaGDataClient dataClient = new JakartaGDataClient();

        // Generate the URL for a private ATOM feed in google calendar
        String url = "http://www.google.com/calendar/feeds/" +
                dataClient.encodeUri(mUserId.getText().toString()) + "/private/full";
        QueryParams params = dataClient.createQueryParams();
        String pad1 = (mMonth + 1) < 10 ? "0" : "";
        String pad2 = mDay < 10 ? "0" : "";
        params.setParamValue("start-min", mYear + "-" + pad1 + (mMonth + 1) + "-" + pad2 + mDay + "T00:00:00");
        params.setParamValue("start-max", mYear + "-" + pad1 + (mMonth + 1) + "-" + pad2 + mDay + "T23:59:59");
        url = params.generateQueryUrl(url);
        Log.i("GoogleContacts", "URL :" + url);

        CalendarClient client = new CalendarClient(dataClient, factory, url);
        try {
            Log.i("GoogleContacts", "BaseFeedUrl:" + client.getBaseFeedUrl());

            String user = mUserId.getText().toString();
            String password = mPassword.getText().toString();
            Log.i("GoogleContacts", "userid:" + user);
            Log.i("GoogleContacts", "password:" + password);

            // Get the google token
            String authToken = client.getAuthToken(user, password);
            Log.i("GoogleContacts", "Token:" + authToken);

            // Use the token and access the actual feed.
            java.io.InputStream is = dataClient.getFeedAsStream(url, authToken);
            GDataParser parser = factory.createParser(is);
            Feed feed = parser.init();

            int totalResults = feed.getTotalResults();
            Log.i("GoogleContacts", "Results:" + totalResults);

            // wade thru the entries and pick interesting information
            EventEntry entry = null;
            String[] items = new String[totalResults];
            for (int i = 0; i < totalResults; i++) {
                entry = (EventEntry) parser.readNextEntry(entry);
                Log.i("GoogleContacts", "Entry ID:" + entry.getId());
                Log.i("GoogleContacts", "Entry Title:" + entry.getTitle());
                Log.i("GoogleContacts", "Update Date:" + entry.getUpdateDate());
                When when = entry.getFirstWhen();
                Date time1 = zulu.parse(when.getStartTime());
                Date time2 = zulu.parse(when.getEndTime());
                Log.i("GoogleContacts", "Date/Time: FROM " + zulu2.format(time1) + " TO " + zulu2.format(time2));
                items[i] = zulu2.format(time1) + " - " + zulu2.format(time2) + " : " + entry.getTitle();
            }

            // Display the information in the list view
            ListView listView = (ListView) findViewById(R.id.data);
            listView.setAdapter(new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1,
                    items));
        } catch (Exception e) {
            Log.e("GoogleContacts", e.toString(), e);
        }
    }
