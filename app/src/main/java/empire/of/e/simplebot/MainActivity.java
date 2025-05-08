package empire.of.e.simplebot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ClientCertRequest;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.view.MotionEvent;

public class MainActivity extends Activity {
		MyWebView browser;
		EditText urlBar;
		TextView js;
		Button run;
		Activity me;
		Handler handler;
		int watchTime = 0;
		int targetTime = 0;
		int targetOffset =0;
		int quickTime = 17000;
		int quickTimeOffset = 4000;
		int longTime = 40000;
		int longTimeOffset = 21000;
		int smartUAtarget = 1;
		int smartUAoffset = 4;
		int elapsed = 0;
		String user;
		CookieManager cm;
		SharedPreferences pref;
		SharedPreferences.Editor editor;
		Bundle state;
		CookieSyncManager csm;
		TextView time;
		ProgressBar pb;
		Intent noti;
		PowerManager.WakeLock wl;
		Switch smartUA;
		Switch watchTimeSwitch;
		Random rand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
				handler = new Handler();
				me = this;
				browser = findViewById(R.id.browser);
				run = findViewById(R.id.runButton);
				urlBar = findViewById(R.id.urlBar);
				time = findViewById(R.id.time);
				js = findViewById(R.id.script);
				pb = findViewById(R.id.loader);




				smartUA = findViewById(R.id.userAgentSmart);
				watchTimeSwitch = findViewById(R.id.watchTimeSwitch);

				pref = getSharedPreferences("custprefs", MODE_PRIVATE);
				editor = pref.edit();
				browser.getSettings().setJavaScriptEnabled(true);
				browser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);


				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				//getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
				// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				setStatusColour(R.color.colorPrimary);

				PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wl = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Beam:WakeLock");
				wl.acquire();

			  createNotificationChannel();
				noti = new Intent(this, Notify.class);

				browser.setWebViewClient(loadManager);
				browser.setWebChromeClient(new WebChromeClient());
				browser.addJavascriptInterface(new WebAppInterface(this), "Reflect");
				browser.setLongClickable(false);
				browser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
				browser.getSettings().setDisplayZoomControls(false);
				browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

				cm = CookieManager.getInstance();
				csm = 	CookieSyncManager.createInstance(this);
				csm.getInstance().startSync();
				rand = new Random();
				if (useQuickLoads()) {
						targetTime = quickTime;
						targetOffset = quickTimeOffset;
						watchTime = targetTime + rand.nextInt(targetOffset);
				}
				else {
						targetTime = longTime;
						targetOffset = longTimeOffset;
						watchTime = targetTime + rand.nextInt(targetOffset);
				}

				user = generateRandomAgent();


				browser.getSettings().setUserAgentString(user);
//				browser.getSettings().setAppCacheEnabled(true);
				browser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
				//browser.getSettings().setBlockNetworkImage(true);
				//browser.getSettings().setSafeBrowsingEnabled(true);
				//browser.getSettings().setPluginState(WebSettings.PluginState.OFF);

				browser.getSettings().setDatabaseEnabled(false);
				browser.getSettings().setLoadWithOverviewMode(true);
				browser.getSettings().setDomStorageEnabled(false);
				browser.getSettings().setLoadsImagesAutomatically(false);
				browser.getSettings().setBlockNetworkImage(true);
				browser.getSettings().setAllowFileAccess(false);
				browser.getSettings().setUseWideViewPort(true);
				browser.getSettings().setGeolocationEnabled(false);
				browser.getSettings().setSupportZoom(false);
				browser.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
				browser.setFocusable(false);
				browser.setClickable(false);
				browser.setActivated(false);
				browser.setEnabled(false);

				browser.setOnTouchListener(new WebView.OnTouchListener(){

								@Override
								public boolean onTouch(View p1, MotionEvent p2) {
										return true;
								}
						});
						browser.setClickable(false);



				browser.saveState(state);

				String enteredURL = pref.getString("oldUrls", urlBar.getText().toString());
				urlBar.setText(enteredURL);

				Intent intent = getIntent();
				String action = intent.getAction();
				String type = intent.getType();
				if ("android.intent.action.SEND".equals(action) && type != null) {
						String urlShared =  intent.getStringExtra("android.intent.extra.TEXT");
						urlBar.setText(urlShared);
				}
		}
		public void setStatusColour(int color) {
				Window window =  getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				window.setStatusBarColor(ContextCompat.getColor(this, color));
		}

		private void createNotificationChannel() {
				// Create the NotificationChannel, but only on API 26+ because
				// the NotificationChannel class is new and not in the support library
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						CharSequence name = "Video Playback";
						String description = "Background Video Playback";
						int importance = NotificationManager.IMPORTANCE_LOW;
						NotificationChannel channel = new NotificationChannel("zoopy", name, importance);
						channel.setDescription(description);
						// Register the channel with the system; you can't change the importance
						// or other notification behaviors after this
						NotificationManager notificationManager = getSystemService(NotificationManager.class);
						notificationManager.createNotificationChannel(channel);
				}
		}
		int load = 0;
		boolean running = false;
		boolean error = false;


		public boolean useSmartUA() {
				return smartUA.isChecked();
		}

		public boolean useQuickLoads() {
				return watchTimeSwitch.isChecked();
		}

		public void go(final View view) {
				if(urlBar.getText() != null && urlBar.getText().length() > 8 && urlBar.getText().toString().contains("youtu") && urlBar.getText().toString().toLowerCase().startsWith("http"))
				{
				if (hasInternet()) {

						if (!running) {
								running = true;
								run.setText("STOP");
								smartUA.setEnabled(false);
								watchTimeSwitch.setEnabled(false);

								load++;
								String enteredURL = "";
								if (urlBar.getText() != null) {
										enteredURL = urlBar.getText().toString();
										browser. restoreState(state);
										String first = enteredURL.split("\n")[0];
										js.setText("Gathering Data,\n " + first);

										editor.putString("oldUrls", enteredURL);
										editor.commit();

										cm.setAcceptCookie(true);
										cm.setAcceptThirdPartyCookies(browser, true);
										cm.setAcceptFileSchemeCookies(true);
										browser. clearFormData();
										browser. clearSslPreferences();
										browser. clearHistory();
										browser. clearCache(true);
										cm.removeSessionCookie();
										cm.removeAllCookie();


										if (useQuickLoads()) {
												targetTime = quickTime;
												targetOffset = quickTimeOffset;
												watchTime = targetTime + rand.nextInt(targetOffset);
										}
										else {
												targetTime = longTime;
												targetOffset = longTimeOffset;
												watchTime = targetTime + rand.nextInt(targetOffset);
										}




										elapsed = watchTime / 1000;
										user = generateRandomAgent();
										browser.getSettings().setUserAgentString(user);
										browser.loadUrl(parseURLToFeature(first));
										doCountdown(watchTime);
										handler.postDelayed(r, watchTime);
								}
								else {
										new AlertDialog.Builder(this, R.style.AppThemeRev)
												.setCancelable(false)
												.setTitle("Link Unsupported")
												.setMessage("Please provide link that you would like to open.")
												.setNegativeButton(" OK ", new DialogInterface.OnClickListener()
												{
														public void onClick(DialogInterface dialog, int which) {
																error = true;
																go(view);
														}
												})
												.setIcon(R.drawable.ic_launcher)
												.show();

								}
						}
						else {
								running = false;
								if (!error) {
										run.setText("STOPPING");
										run.setEnabled(false);
								}
								else {
										load = 0;
										totalLoops = 0;
										nowLoop = 0;
										breaker = 0;
										load = 0;
										error = false;
										run.setText("START");
										run.setEnabled(true);
										smartUA.setEnabled(true);
										watchTimeSwitch.setEnabled(true);

								}
						}
				}
				else {
						new AlertDialog.Builder(this,R.style.AppThemeRev)
								.setCancelable(false)
								.setTitle("Internet Disabled")
								.setMessage("We can not find an active connection, internet is required.")
								.setNegativeButton(" OK ", new DialogInterface.OnClickListener()
								{
										public void onClick(DialogInterface dialog, int which) {

												// DO SOMETHING WITH THANKS BUTTON HERE

										}
								})
								.setIcon(R.drawable.ic_launcher)
								.show();
				}
				}
				else {
						new AlertDialog.Builder(this, R.style.AppThemeRev)
								.setCancelable(false)
								.setTitle("No URL Provided")
								.setMessage("We can not find a link, please provide one.\n\nP.S You can add multiple links, 1 URL per line.")
								.setNegativeButton(" OK ", new DialogInterface.OnClickListener()
								{
										public void onClick(DialogInterface dialog, int which) {

												// DO SOMETHING WITH THANKS BUTTON HERE

										}
								})
								.setIcon(R.drawable.ic_launcher)
								.show();
				}
		}

		private String getYouTubeId(String youTubeUrl) {

        String pattern = "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*";
        Pattern compiledPattern = Pattern.compile(pattern,
																									Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
		}


		public void log(String data) {
				try {
						File myFile = new File(Environment.getExternalStorageDirectory() + "/ChangeBot_Accounts.csv");
						if (!myFile.exists()) {
								myFile.createNewFile();
						}
						FileOutputStream fOut = new FileOutputStream(myFile, true);
						OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
						myOutWriter.append(data + "\n");
						myOutWriter.flush();
						myOutWriter.close();
						fOut.close();
				}
				catch (Exception e) {
						//
				}
		}

		public class WebAppInterface {
				Context mContext;
				/** Instantiate the interface and set the context */
				WebAppInterface(Context c) {
						mContext = c;
				}
				/** Show a toast from the web page */
				@JavascriptInterface
				public void showToast(String toast) {
						Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
				}
		}

		Thread x;
		int count = 0;
		String last;
		WebViewClient loadManager = new WebViewClient()
		{
				@Override
				public void onPageFinished(WebView view, String url) {
				    csm.sync();
						super.onPageFinished(view, url);
				}


				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
						return true;
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
						return true;
				}


				@Override
				public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
						//	super.onReceivedClientCertRequest(view, request);
				}

				@Override
				public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
						// TODO: Implement this method
						//	super.onReceivedError(view, request, error);
				}

				@Override
				public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
						// TODO: Implement this method
						//	super.onReceivedHttpAuthRequest(view, handler, host, realm);
				}

				@Override
				public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
						// TODO: Implement this method
					  //	super.onReceivedSslError(view, handler, error);
				}

		};

		int done = 0;
		public void doCountdown(int timeMs) {
				final int timespan = timeMs / 1000;
				new Thread(new Runnable(){
								@Override
								public void run() {
										while (done < timespan) {
												me.runOnUiThread(new Runnable(){
																@Override
																public void run() {
																		pb.setVisibility(pb.VISIBLE);
																		time.setText((timespan - done) + "");
																}
														});
												SystemClock.sleep(1000);
												done ++;
										}
										done = 0;
										me.runOnUiThread(new Runnable(){
														@Override
														public void run() {
																time.setText("0");
																pb.setVisibility(pb.GONE);
														}
												});
								}
						}).start();
		}

		int thisUrl = 0;
		int maxUrl = 0;

		int totalLoops;
		int nowLoop;

		int breaker = 0;

		boolean needsPause = false;
		int baseWidth = 0;
		int baseHeight = 0;

		final Runnable r = new Runnable() {
				public void run() {
						new Thread(new Runnable(){

										@Override
										public void run() {


												me.runOnUiThread(new Runnable(){

																@Override
																public void run() {
																		if (running) {
																				browser.stopLoading();
																				browser. clearFormData();
																				browser. clearSslPreferences();
																				browser. clearHistory();
																			  browser. clearCache(true); // important
																				browser. clearAnimation();

																				browser. restoreState(state);
																				ViewGroup.LayoutParams web = browser. getLayoutParams();

																				if(baseWidth+baseHeight == 0)
																				{
																						baseWidth = web.width;
																						baseHeight = web.height;
																				}
																				setDimensions(browser,baseWidth-rNo(), baseHeight-rNo());
																				browser. setLayoutParams(web);
																				browser. refreshDrawableState();
																				cm.removeSessionCookie();
																				cm.removeAllCookie();
																				totalLoops++;
																				running = true;
																				run.setText("STOP");
																				load++;
																				String enteredURL = urlBar.getText().toString();
																				Random rand = new Random();
																				if (useQuickLoads()) {
																						targetTime = quickTime;
																						targetOffset = quickTimeOffset;
																						watchTime = targetTime + rand.nextInt(targetOffset);
																				}
																				else {
																						targetTime = longTime;
																						targetOffset = longTimeOffset;
																						watchTime = targetTime + rand.nextInt(targetOffset);
																				}





																				if (useSmartUA()) {
																						if (totalLoops - nowLoop >= smartUAtarget + rand.nextInt(smartUAoffset)) {
																								user = generateRandomAgent();
																								nowLoop = totalLoops;
																						}
																				}
																				else {
																						user = generateRandomAgent();
																				}

																				browser.clearMatches();

																				count ++;
																				String[] urls = enteredURL.split("\n");
																				maxUrl = urls.length - 1;
																				if (thisUrl < maxUrl) {
																						thisUrl++;
																				}
																				else {
																						thisUrl = 0;
																				}
																				if (urls[thisUrl].length() > 2) {
																						String toLoad = urls[thisUrl].trim();
																						browser.getSettings().setUserAgentString(user);

																						int totalSecs = elapsed;
																						int		hours = totalSecs / 3600;
																						int 	minutes = (totalSecs % 3600) / 60;
																						int seconds = totalSecs % 60;

																						String timeString = String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);

																						int percent = (int)((count / 100.0f) *85);

																						if(notifyVisible)
																						{
																								String descr = "Auto viewing in progress, " + count + " views done.";
																								Notify.updateNotification(MainActivity.this, descr);
																						}


																						js.setText("Load Statistics,\n" + count + " total loads\n"+ "" + timeString + " view time" + "\n\nLast Load,\n" + browser.getTitle().replace(" - YouTube", ""));
																					//	stopService(noti);
																						int detectionAt = 45+rand.nextInt(5);
																						if((totalLoops - breaker)>= detectionAt)
																						{
																								int sec = 1000;
																								int minute = 60*sec;
																								int hour = 60*minute;

																								watchTime = (5*minute)+rand.nextInt((20*minute));
																								js.setText("High Traffic Request Detected on Load "+totalLoops+"\n\nBypass Method Used,\nTemporary random wait time, set to "+(watchTime/minute)+" minutes.");
																								toLoad = "about:blank";
																								breaker = totalLoops;
																						}


																						elapsed += watchTime / 1000;



																						browser.loadUrl(parseURLToFeature(toLoad));



																						//browser.scrollTo(60,0);
																						doCountdown(watchTime);

																						handler.postDelayed(r, watchTime);
																				}

																		}
																		else {
																				browser.stopLoading();
																				browser. clearFormData();
																				browser. clearSslPreferences();
																				browser. clearHistory();
																				browser. clearCache(true);
																				browser. clearAnimation();
																				browser. restoreState(state);
																				ViewGroup.LayoutParams web = browser. getLayoutParams();
																				web.height = web.height - rNo();
												  			 				web.width = web.width - rNo();
																				browser. setLayoutParams(web);
																				browser. refreshDrawableState();
																				cm.removeSessionCookie();
																				cm.removeAllCookie();
																				totalLoops = 0;
																				nowLoop = 0;
																				thisUrl = 0;
																				maxUrl = 0;
																				breaker = 0;
																				count = 0;

																				load = 0;
				 																run.setText("START");
																				run.setEnabled(true);
																				smartUA.setEnabled(true);
																				watchTimeSwitch.setEnabled(true);
																		}
																}
														});

										}
								}).start();

				}
		};
		private void setDimensions(View view, int width, int height){
				android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
				params.width = width;
				params.height = height;
				view.setLayoutParams(params);
		}
		public boolean hasInternet() {
				boolean isWifiConnected = false;
				boolean isMobileConnected = false;
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (networkInfo != null) isWifiConnected = networkInfo.isConnected();
				networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (networkInfo != null) isMobileConnected = networkInfo.isConnected();
				if (isWifiConnected == false && isMobileConnected == false) {
						return false;
				}
				else {
						return true;
				}
		}

		public String parseURLToFeature(String url) {
				String id = getYouTubeId(url.trim());
				String followRules = "https://youtu.be/" + id;
				String proper = "https://m.youtube.com/watch?v=" + id + "&feature=youtu.be";
				return proper;
		}

		public String generateRandomAgent() {
				Random rand = new Random();
				int rNo = rand.nextInt(99999);
				int[] x = new int[]{7,8,9,10,11};
				int sel = rand.nextInt(x.length - 1);
				String[] styles = new String[]{"SM","N","M","MX","K","S","D"};
				String randomStyle = styles[rand.nextInt(styles.length - 1)];
				return "Mozilla/5.0 (Linux; Android " + x[sel] + ".0; Build/" + randomStyle + rNo + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36";
		}

		public int rNo() {
				return new Random().nextInt(9);
		}
		public String randomAgentFromList() {
				String[] agent = getResources().getStringArray(R.array.agents);
				String random;
				random = agent[new Random().nextInt(agent.length)];
				return random;
		}

		public String[] randomUser() {
				String[] firsts = getResources().getStringArray(R.array.firstNames);
				String randomFirst;
				randomFirst = (firsts[new Random().nextInt(firsts.length)]);
				String[] lasts = getResources().getStringArray(R.array.lastNames);
				String randomLast;
				randomLast = (lasts[new Random().nextInt(lasts.length)]);
				if (randomLast.contains("/")) {
						randomLast = randomLast.replace("/", "-");
				}
				if (randomFirst.contains(" ")) {
						randomFirst = randomFirst.replaceAll(" ", "");
				}
				String randomUser = randomFirst + randomLast  + rNo() + rNo() + rNo();
				String randomPass = randomFirst + randomLast + "Pass" + rNo() + rNo() + rNo();
				return new String[]{upperFirst(randomFirst.trim()),upperFirst(randomLast.trim()),upperFirst(randomUser.trim()),upperFirst(randomPass.trim())};
		}

		public String upperFirst(String val) {
				String first = val.substring(0, 1).toUpperCase();
				String rest = val.substring(1, val.length());
				return first + rest;
		}

		boolean notifyVisible = false;

		@Override
		protected void onPause() {
				wl.release();
				if (running) {
					Notify.updateNotification(this, "Auto viewing in background");
					notifyVisible = true;
				}
				super.onPause();
		}

		@Override
		protected void onResume() {
				wl.acquire();
				if (running) {
						stopService(noti);
						notifyVisible = false;
				}
				super.onResume();
		}




}




class MyWebView extends WebView {
		public MyWebView(Context context) {
				super(context);
		}
		public MyWebView(Context context, AttributeSet attrs) {
				super(context, attrs);
		}
		public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
				super(context, attrs, defStyleAttr);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
				// TODO: Implement this method
				return false;
		}

		@Override
		protected void onWindowVisibilityChanged(int visibility) {
				boolean useBackground = true;
				if (useBackground) {
						if (visibility != View.GONE && visibility != View.INVISIBLE && visibility != View.SCREEN_STATE_ON)
								super.onWindowVisibilityChanged(visibility);
				}
				else
						super.onWindowVisibilityChanged(visibility);
		}

}
