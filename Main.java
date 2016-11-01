import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import static java.nio.file.StandardOpenOption.*;

public class Main {
	private static Map<String, String> cookies = new HashMap<>(), formData = new HashMap<>();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz");

	private static JSONObject POST(String url) throws IOException, InterruptedException {
		Connection.Response response = Jsoup.connect(url)
				.followRedirects(true)
				.ignoreContentType(true)
				.ignoreHttpErrors(true)
				.header("Host","access.myunidays.com")
				.header("Connection","keep-alive")
				.header("Accept","application/json, text/javascript, */*; q=0.01")
				.header("Origin","https://megabus.myunidays.com")
				.header("ud-source", "megabus")
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36")
				.referrer("https://megabus.myunidays.com/get-perk")
				.header("Accept-Encoding","gzip, deflate, br")
				.header("Accept-Language","en-US,en;q=0.8")
				.cookies(cookies)
				.data(formData)
				.method(Connection.Method.POST)
				.execute();
		cookies.putAll(response.cookies());
		return new JSONObject(response.body());
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length != 2){
			System.out.println("Exactly 2 arguments are required: email & password");
			return;
		}
		formData.put("Password", args[1]);
		formData.put("EmailAddress", args[0]);
		formData.put("Human", "");
		POST("https://account.myunidays.com/US/en-US/account/log-in");
		System.out.println(cookies.containsKey("auth") ? "Login successful" : "Login unsuccessful. Wrong credentials?");
		formData.clear();
		formData.put("forceNew", "true");
		for (long refreshTime; cookies.containsKey("auth"); Thread.sleep(900000 + refreshTime)) {
			try {
				JSONObject response = POST("https://access.myunidays.com/megabus/access/megabus/online");
				refreshTime = dateFormat.parse(response.getString("canReissueOn")).getTime() - System.currentTimeMillis();
				System.out.println(response.getString("code"));
				Files.write(Paths.get("./MegabusPromoCodes.txt"), (response.getString("code") + "\n").getBytes(), CREATE, APPEND);
			}
			catch (ParseException | JSONException | IOException e) {
				e.printStackTrace();
				refreshTime = 900000;
			}
		}
	}
}
