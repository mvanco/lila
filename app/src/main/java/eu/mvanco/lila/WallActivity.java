package eu.mvanco.lila;


import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WallActivity extends Activity {
	WebView browser;
	WebSettings webSettings;
	public static String URL = "url";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wall);
		
		browser = (WebView) findViewById(R.id.web_view);
		browser.setWebViewClient(webClient);

		webSettings = browser.getSettings();
		
		webSettings.setPluginState(PluginState.ON);
		webSettings.setAllowFileAccess(true); 
		
		
		browser.loadUrl(getIntent().getStringExtra(URL));
	}
	

	WebViewClient webClient = new WebViewClient() {
	// Override page so it's load on my view only
		@Override
		public boolean shouldOverrideUrlLoading(WebView  view, String  url)
		{
			webSettings.setBuiltInZoomControls(false);
				   
			if ( url.contains("show_picture") == true ) {
				webSettings.setBuiltInZoomControls(true);
			}
				             
			browser.loadUrl(url);
			return true;
		}
	};
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
            case KeyEvent.KEYCODE_BACK:
                if(browser.canGoBack() == true){
                	browser.goBack();
                }else{
                    finish();
                }
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wall, menu);
		return true;
	}

}
