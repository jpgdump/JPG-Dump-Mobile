package com.jpgdump.mobile.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jpgdump.mobile.interfaces.PostsInterface;
import com.jpgdump.mobile.objects.Post;
import com.jpgdump.mobile.util.ContextLogger;

public class PostManager implements PostsInterface
{
    private final ContextLogger log = ContextLogger.getLogger(this);
    
    @Override
    public ArrayList<Post> retrievePosts(String maxResults, String startIndex,
            String sortBy, String filters)
    {
        String postUrl = "http://jpgdump.com/api/v1/posts?startIndex=" + 
                startIndex + "&maxResults=" + maxResults + "&sort=" + sortBy;
        
        if(!filters.equals(""))
        {
            postUrl +=  "&filters=" + filters;
        }
        
        ArrayList<Post> posts = new ArrayList<Post>();
        URL url = null;
        InputStream inputStream = null;
        try
        {
            //Connect to the given url and open the connection
            url = new URL(postUrl);
            URLConnection conn = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                //If the connection is successful, read in the stream
                inputStream = httpConn.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) 
                {
                    total.append(line);
                }
                
                
                //Parse the JSON and split it into the individual Posts
                JSONObject rawJson = new JSONObject(total.toString());
                JSONArray rawList = rawJson.getJSONArray("items");
                
                JSONObject postSplit;
                for(int i = 0; i < rawList.length(); i++)
                {
                    postSplit = rawList.getJSONObject(i);
                    posts.add(new Post(postSplit.getString("kind"), postSplit.getString("id"),
                            postSplit.getString("url"), postSplit.getString("width"),
                            postSplit.getString("height"), postSplit.getString("created"), 
                            postSplit.getInt("safety"), postSplit.getString("mime"),
                            postSplit.getString("upvotes"), postSplit.getString("downvotes"),
                            postSplit.getString("score"), postSplit.getString("title")));
                }
            }
            else
            {
                throw new MalformedURLException("Teh interwebz is broken");
            }
        }
        catch (MalformedURLException e)
        {
            // Intentionally ignore error.
            log.e("The URL is misformed", e);
        }
        catch (IOException e)
        {
            // Intentionally ignore error.
            log.e("There's a problem with the BufferedReader", e);
        }
        catch (JSONException e)
        {
            // Intentionally ignore error.
            log.e("The JSON has returned something unexpected", e);
        }
        return posts;
    }
}
