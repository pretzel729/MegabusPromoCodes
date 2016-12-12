package com.pretzel.unidays.megabus;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.APPEND;

public class Main
{
	private static String FILE_NAME = "codes.txt";

	//Run with
	//java -jar ./MegabusPromo.jar 'example@utexas.edu' 'password'
	public static void main(String[] args) throws InterruptedException, IOException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		Map<String, String> cookies = new HashMap<>();
		Map<String, String> formData = new HashMap<>();

		//Login
		if (args.length < 2)
		{
			System.out.println("2 arguments are required: email & password.");
			return;
		}
		formData.put("Password", args[1]);
		formData.put("EmailAddress", args[0]);
		formData.put("Human", "");
		POST("https://account.myunidays.com/US/en-US/account/log-in", cookies, formData);
		if (!cookies.containsKey("auth"))
		{
			System.out.println("Login unsuccessful. Wrong credentials?");
			return;
		}
		System.out.println("Login successful!");

		formData.clear();
		formData.put("forceNew", "true");
		//fetch promo code and sleep until a new one is available. Repeat
		while (true)
		{
			long refreshTime = 54*60*1000;
			try
			{
				JSONObject response = new JSONObject(POST("https://access.myunidays.com/megabus/access/megabus/online", cookies, formData));
				refreshTime = dateFormat.parse(response.getString("canReissueOn")).getTime() - System.currentTimeMillis();
				String data = dateFormat.format(new Date()) + "\t" + response.getString("code");
				Files.write(Paths.get("./" + FILE_NAME), (data + "\n").getBytes(), CREATE, APPEND);
				System.out.println(data);
			}
			catch (ParseException | JSONException | IOException e)
			{
				e.printStackTrace();
			}
			Thread.sleep(refreshTime + 60*1000);
		}
	}

	//Send an HTTP POST request
	private static String POST(String url, Map cookies, Map formData) throws IOException, InterruptedException
	{
		Connection.Response response = Jsoup.connect(url)
											.followRedirects(true)
											.ignoreContentType(true)
											.ignoreHttpErrors(true)
											.header("ud-source", "megabus")
											.cookies(cookies)
											.data(formData)
											.method(Connection.Method.POST)
											.execute();
		cookies.putAll(response.cookies());
		return response.body();
	}
}
