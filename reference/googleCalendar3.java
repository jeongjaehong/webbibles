private boolean clickSign() {
String username = txtUsername.getText().toString();
String password = txtPassword.getText().toString();

XmlCalendarGDataParserFactory factory = new XmlCalendarGDataParserFactory(new AndroidXmlParserFactory());
AndroidGDataClient dataClient = new AndroidGDataClient(this.getContentResolver());

try {
String googleAccountUrl = ¡°https://www.google.com/accounts/ClientLogin¡±;

HttpClient httpclient = new DefaultHttpClient();
QueryParams params = dataClient.createQueryParams();
params.setParamValue(¡°accountType¡±, ¡°GOOGLE¡±);
params.setParamValue(¡°Email¡±, username);
params.setParamValue(¡°Passwd¡±, password);
params.setParamValue(¡°service¡±, ¡°cl¡±);
params.setParamValue(¡°source¡±, ¡°sebi-fullcontact-1.0¡È);
HttpPost post = new HttpPost(params.generateQueryUrl(googleAccountUrl));
post.setHeader(¡°Content-Type¡±, ¡°application/x-www-form-urlencoded¡±);

HttpResponse res = httpclient.execute(post);
StatusLine status = res.getStatusLine();

sundroid.data.setString(¡°GoogleResponse¡±, ¡°¡± + status.getStatusCode() + ¡± ¡± + status.getReasonPhrase());

return (status.getStatusCode() == HttpStatus.SC_OK);
} catch (Exception e1) {
Log.e(TAG, e1.toString(), e1);
sundroid.data.setString(¡°GoogleResponse¡±, ¡°Exception ? ¡± + e1.toString());
} finally {
dataClient.close();
}
return false;
}
