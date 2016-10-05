import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class Main
{
	private static final String LOGIN_URL = "https://account.myunidays.com/megabus/account/log-in";
	private static final String CODE_URL = "https://access.myunidays.com/megabus/access/megabus/online";
	private static final String fileName = "MegabusPromoCodes.txt";
	private static final int refreshTime = 60*60*1000;//ms
	private static Map<String, String> cookies = new HashMap<>();

	public static void main(String[] args)
	{
		try
		{
			if(!login(args[0], args[1]))
			{
				System.out.println("Login unsuccessful");
				return;
			}
			System.out.println("Login successful");
			Thread.sleep(60*1000);
			while(true)
			{
				String code = fetchCode();
				if(!code.equals(""))
				{
					System.out.println(code);
					saveCodeToFile(code);
				}
				Thread.sleep(refreshTime + new Random().nextInt(60)*1000);
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Two arguments are required: email & password.");
		}
		catch (InterruptedException ee)
		{
			ee.printStackTrace();
		}
	}

	private static boolean login(String email, String password)
	{
		try
		{
			Map<String, String> formData = new HashMap<>();
			formData.put("EmailAddress", email);
			formData.put("Password", password);
			formData.put("Human", "");
			Connection.Response loginResponse = Jsoup.connect(LOGIN_URL)
					.followRedirects(true)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.header("Host", "account.myunidays.com")
					.header("Connection", "keep-alive")
					.header("Accept", "application/json, text/javascript, */*; q=0.01")
					.header("Origin", "https://megabus.myunidays.com")
//				.header("ud-validateonly", "post")
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36")
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
					.referrer("https://megabus.myunidays.com/get-perk")
					.header("Accept-Encoding", "gzip, deflate, br")
					.header("Accept-Language", "en-US,en;q=0.8")
					.cookies(cookies)
					.data(formData)
					.method(Connection.Method.POST)
					.execute();
			cookies.putAll(loginResponse.cookies());
			if(cookies.containsKey("auth"))
			{
				return true;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private static String fetchCode()
	{
		try
		{
			Map<String, String> formData = new HashMap<>();
			formData.put("forceNew", "true");
			Connection.Response fetchResponse = Jsoup.connect(CODE_URL)
					.followRedirects(true)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.header("Host", "access.myunidays.com")
					.header("Connection", "keep-alive")
					.header("Accept", "application/json, text/javascript, */*; q=0.01")
					.header("Origin", "https://megabus.myunidays.com")
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36")
					.referrer("https://megabus.myunidays.com/get-perk")
					.header("Accept-Encoding", "gzip, deflate, br")
					.header("Accept-Language", "en-US,en;q=0.8")
					.cookies(cookies)
					.data(formData)
					.method(Connection.Method.POST)
					.execute();
			cookies.putAll(fetchResponse.cookies());
			JSONObject fetchResponseJSON = new JSONObject(fetchResponse.body());
			return fetchResponseJSON.getString("code");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Couldn't fetch promo code");
		}
		catch (JSONException ee)
		{
			ee.printStackTrace();
			System.out.println("Couldn't parse code from the response JSON");
		}
		return "";
	}

	private static void saveCodeToFile(String code)
	{
		try
		{
			Files.write(Paths.get("./" + fileName), (code + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Couldn't save code to file");
		}
	}
}
