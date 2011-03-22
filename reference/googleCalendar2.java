import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import com.google.android.gdata.client.JakartaGDataClient;
import com.google.wireless.gdata.client.QueryParams;

public class GoogleAccountConnector {
static final String googleAccountUrl = ¡°https://www.google.com/accounts/ClientLogin¡±;

public static String getAuthToken(String user, String password) {
HttpClient httpclient = new HttpClient();
JakartaGDataClient dataClient = new JakartaGDataClient();
QueryParams params = dataClient.createQueryParams();
params.setParamValue(¡°accountType¡±, ¡°GOOGLE¡±);
params.setParamValue(¡°Email¡±, user);
params.setParamValue(¡°Passwd¡±, password);
params.setParamValue(¡°service¡±, ¡°cl¡±);
params.setParamValue(¡°source¡±, ¡°sebi-fullcontact-1.0¡È);
PostMethod post = new PostMethod(params
.generateQueryUrl(googleAccountUrl));
post.addRequestHeader(¡°Content-Type¡±,
¡°application/x-www-form-urlencoded¡±);
try {
int status = httpclient.executeMethod(post);
} catch (HttpException e1) {
// TODO Auto-generated catch block
e1.printStackTrace();
} catch (IOException e1) {
// TODO Auto-generated catch block
e1.printStackTrace();
}
try {
return extractTokenFromResponse(¡°Auth¡±, post
.getResponseBodyAsString());
} catch (IOException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
return null;

}

private static String extractTokenFromResponse(String token, String response) {
String result = null;
String tokenPrefix = (new StringBuilder()).append(token).append(¡°=¡±)
.toString();
int startIdx = response.indexOf(tokenPrefix);
if (startIdx >= 0) {
int endIdx = response.indexOf(¡®\n¡¯, startIdx);
if (endIdx > 0) {
result = response.substring(startIdx + tokenPrefix.length(),
endIdx);
} else {
result = response.substring(startIdx + tokenPrefix.length());
}
}
return result;
}
}
