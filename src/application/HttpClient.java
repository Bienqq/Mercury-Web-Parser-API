package application;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class HttpClient implements Runnable
{

    public enum RequestStatus { SUCCESSFUL , TIMEOUT, INCORRECT_INPUT }

    private final String mercuryURL = "https://mercury.postlight.com/parser?url=";
    private final OkHttpClient client = new OkHttpClient();
    private final String [] jsonFields = { "title", "date_published", "lead_image_url",
                                            "dek", "url", "domain", "excerpt", "word_count", "direction",
                                            "total_pages", "rendered_pages", "next_page_url"};
    private String url;
    private String userKey;
    private String lastRequest;
    private String content;
    private String pageInfo;
    private RequestStatus status;

    @Override
    public void run()
    {
        try
        {
            sendGETRequest();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendGETRequest() throws IOException
    {
        String address = mercuryURL + url;

        // building request
        Request request = new Request.Builder()
                .url(address)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", userKey)
                .build();

        Response response;
        try
        {
            response = client.newCall(request).execute();
        }
        catch(UnknownHostException | SocketTimeoutException e)
        {
            status = RequestStatus.TIMEOUT;
            return;
        }

        //getting response and closing connection
        String responseString = response.body().string();
        response.body().close();

        buildLastRequest(address);
        processResponseString(responseString);
    }

    private void buildLastRequest(String address)
    {
        lastRequest = "GET " + address + "\nContent-Type: application/json" + "\nx-api-key: " + userKey;
    }

    private void processResponseString(String responseString)
    {
        String [] formattedResponseString;
        try
        {
            formattedResponseString = formatResponse(responseString);
        }
        catch (JSONException e)
        {
            status = RequestStatus.INCORRECT_INPUT;
            return;
        }
        //formatting content tag
        formatContent(formattedResponseString);
        status = RequestStatus.SUCCESSFUL;
    }

    private String [] formatResponse(String responseString)
    {
        JSONObject obj = new JSONObject(responseString);
        String [] jsonData = new String[jsonFields.length];

        for( int i = 0 ; i < jsonFields.length ; i++)
        {
            String temp = obj.get(jsonFields[i]).toString();
            //if any field is null display <no information> in this field
            if (temp.equals("null"))
            {
                temp = "<no information>";
            }
            jsonData[i] = jsonFields[i] + ": " + temp + "\n";
        }
        content = obj.get("content").toString();
        return jsonData;
    }

    private void formatContent(String [] jsonData)
    {
        //formatting content of website
        Document document = Jsoup.parse(content);
        Elements elements = document.body().children();
        content = elements.toString();
        pageInfo = "";
        for (String s : jsonData)
        {
            pageInfo +=s;
        }
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setUserKey(String userKey)
    {
        this.userKey = userKey;
    }

    public String getURL()
    {
        return url;
    }

    public String getFormattedLastRequest()
    {
        return lastRequest;
    }

    public String getContent()
    {
        return content;
    }

    public String getPageInfo()
    {
        return pageInfo;
    }

    public RequestStatus getStatus()
    {
        return status;
    }
}
