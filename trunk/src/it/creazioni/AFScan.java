package it.creazioni;

import it.creazioni.VulnWifi.recive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;


public class AFScan extends ListActivity implements OnItemClickListener {
    WifiManager wifi;
	TextView tv;
	//LinearLayout head;
	WifiReceiver receiverWifi; //boradcast receiver cambio stato wifimanager
	reciveWifiEna reciveWifiEna; //broadcast receiver (abilitazione wifi)
	ProgressBar waitbar;
	Button butScan;
	ProgressDialog waitScanResult;
	ProgressDialog waitWifiEna;
	ListAdapter adapter;
	List<ScanResult> wifiList;
	VulnWifi a;
	ListView lv;
	Button butON;
	Button butOFF;
    Button submit;
    ListView mainListview;
    ImageView iget;
    recive wifiReciver;
	
	/** Called when the activity is first created. */

    @Override
     
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListviewContent.clear();
        butON= (Button) findViewById(R.id.ButtonON);
        final Button butOFF= (Button) findViewById(R.id.ButtonOFF);
        //me stesso
        lv = getListView();
        //add listener 
        lv.setOnItemClickListener(this);
        registerForContextMenu(lv);
        setListAdapter(new ListViewAdapter(this));
        butScan= (Button) findViewById(R.id.ButtonScan);
		butScan.setClickable(false);
		butScan.setEnabled(false);
		butOFF.setClickable(false);
		butOFF.setEnabled(false);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        reciveWifiEna= new reciveWifiEna();
        //registerReceiver(reciveWifiEna, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        
    	if(wifi.isWifiEnabled()){ 
    		Toast.makeText(this, "Wifi Arealy Enabled",1).show();
    		butON.setClickable(false);
    		butON.setEnabled(false);
    		butOFF.setClickable(true);
    		butOFF.setEnabled(true);
    		butScan.setClickable(true);
    		butScan.setEnabled(true);
    		
    	 }
        butON.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) { 
            		wifi.setWifiEnabled(true);
             		butON.setClickable(false);
            		butON.setEnabled(false);
            		butOFF.setClickable(true);
            		butOFF.setEnabled(true);
            		butScan.setClickable(true);
            		butScan.setEnabled(true);
            		registerReceiver(reciveWifiEna, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
                 	waitWifiEna = ProgressDialog.show(v.getContext(), "Wait", "Enabling WiFi...", true);
            	 	
            }
        });

        butScan.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) { 
            	receiverWifi = new WifiReceiver();
            	registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            	wifi.startScan();
            	butScan.setEnabled(false);
            	butScan.setClickable(false);
            	waitScanResult = ProgressDialog.show(v.getContext(), "Wait", "Scan in progress...", true);
            }
        });
        
        butOFF.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) { 
            	wifi.setWifiEnabled(false);
            	if(wifi.isWifiEnabled()){ 
            		 Toast.makeText(v.getContext(), "Wifi still on",1).show();
            	 }else{
            		butON.setClickable(true);
         			butON.setEnabled(true);
         			butOFF.setClickable(false);
         			butOFF.setEnabled(false);
         			butScan.setClickable(false);
         			butScan.setEnabled(false);
         			//registerReceiver(reciveWifiEna, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            		waitWifiEna = ProgressDialog.show(v.getContext(), "Wait", "Disabling WiFi...", true);
            	 }
            }
        });
    }
    //end of onCreate
 
    //creating adapter
    private static class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
     
        public ListViewAdapter(Context context) {
        	mInflater = LayoutInflater.from(context);   
        }
        public int getCount() {
        	return ListviewContent.size();
        }
        public Object getItem(int position) {
        	return position;
        }
        public long getItemId(int position) {
        	return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
        	ListContent holder;
        	Drawable iset,iset1;
        	if (convertView == null) {
        		convertView = mInflater.inflate(R.layout.listviewinflate, null);

        	holder  = new ListContent();

        	holder.essid = (TextView) convertView.findViewById(R.id.Essid);
        	holder.bssid = (TextView) convertView.findViewById(R.id.Bssid);
        	holder.crypt = (TextView) convertView.findViewById(R.id.Crypt);
        	holder.level = (TextView) convertView.findViewById(R.id.Level);
        	holder.iget = (ImageView) convertView.findViewById(R.id.SignalIcon);
        	holder.iget1 = (ImageView) convertView.findViewById(R.id.CryptIcon);
            convertView.setTag(holder);
        	} else {
        		holder = (ListContent) convertView.getTag();

        	}
        	holder.essid.setText(ListviewContent.get(position).get("SSID"));
        	holder.bssid.setText(ListviewContent.get(position).get("BSSID"));
        	holder.level.setText(ListviewContent.get(position).get("level"));
        	//showing bars for signal strength
        	int livello=WifiManager.calculateSignalLevel (Integer.parseInt(ListviewContent.get(position).get("level")), 5);
        	switch (livello){
        	case 4:	iset=convertView.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_4);break;
        	case 3:	iset=convertView.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_3);break;
        	case 2:	iset=convertView.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_2);break;
        	case 1:	iset=convertView.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_1);break;
        	case 0:	iset=convertView.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_0);break;
        	default: iset=convertView.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_0);break;
        		}
        	//showing lock for wep/wpa and unlock for open network
        	String cifra= ListviewContent.get(position).get("crypt");
        	if(cifra.indexOf("WPA")>0){
        		iset1=convertView.getResources().getDrawable(R.drawable.lock_closed);
        		holder.crypt.setText("WPA");
        	}
        	else if(cifra.indexOf("WEP")>0){
        		iset1=convertView.getResources().getDrawable(R.drawable.lock_closed);
        		holder.crypt.setText("WEP");
        	}
        	else{
        		iset1=convertView.getResources().getDrawable(R.drawable.lock_open);
        		holder.crypt.setText("No Enc");
        	}
        	holder.iget.setImageDrawable(iset);
        	holder.iget1.setImageDrawable(iset1);
            return convertView;
        }

        static class ListContent {
        	TextView essid;
        	TextView bssid;
        	TextView crypt;
        	TextView level;
        	ImageView iget;
        	ImageView iget1;
        }
    }//end adapter creation
    
    class WifiReceiver extends BroadcastReceiver {
    	public void onReceive(Context c, Intent intent) {
    		ListviewContent.clear();
    		wifiList = wifi.getScanResults();
    		for(int i = 0; i < wifiList.size(); i++){
    			WFNet=new HashMap<String, String>();
        		setListAdapter(new ListViewAdapter(c));
        		WFNet.put("BSSID", wifiList.get(i).BSSID);
        		WFNet.put("SSID", wifiList.get(i).SSID);
        		WFNet.put("level", (Integer.toString(wifiList.get(i).level)));
        		WFNet.put("crypt", wifiList.get(i).capabilities);
        		ListviewContent.add(WFNet);
        		}
    		butScan.setEnabled(true);
    		butScan.setClickable(true);
    		butScan.setText("Refresh");
    		waitScanResult.dismiss();
    		unregisterReceiver(this);
    	}
    }
    class reciveWifiEna extends BroadcastReceiver {
    	public void onReceive(Context c, Intent intent) {
    		
    			if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_ENABLED)==WifiManager.WIFI_STATE_ENABLED){//wifi abilitato
    			/*butON.setClickable(false);
        		butON.setEnabled(false);
        		butOFF.setClickable(true);
        		butOFF.setEnabled(true);
        		butScan.setClickable(true);
        		butScan.setEnabled(true);*/
        		waitWifiEna.dismiss();
    		}
    		if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED)==WifiManager.WIFI_STATE_DISABLED){//wifi disabilitato
    			
    			waitWifiEna.dismiss();}
    	}
    }


    private static final ArrayList<HashMap<String,String>> ListviewContent = new ArrayList<HashMap<String,String>>();
    private static HashMap<String,String> WFNet;
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(ListviewContent.get(arg2).get("SSID").indexOf("Alice")>-1){
			Toast.makeText(arg1.getContext(), "Tring on "+ ListviewContent.get(arg2).get("SSID"), Toast.LENGTH_LONG).show();
			AliceWPA a = new AliceWPA(ListviewContent.get(arg2).get("BSSID"),ListviewContent.get(arg2).get("SSID"), wifi,arg1.getContext());

		}else if(ListviewContent.get(arg2).get("SSID").indexOf("FASTWEB")>-1){
			Toast.makeText(arg1.getContext(), "Tring on "+ ListviewContent.get(arg2).get("SSID"), Toast.LENGTH_LONG).show();
			FastwebWPA a = new FastwebWPA(ListviewContent.get(arg2).get("BSSID"),ListviewContent.get(arg2).get("SSID"), wifi,arg1.getContext());
		}else{
			Toast.makeText(arg1.getContext(),"Net you are tying to connect"+ ListviewContent.get(arg2).get("SSID")+" is NOT Supported", Toast.LENGTH_LONG).show();

		}
		
		
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  menu.add(Menu.NONE,1,Menu.NONE,R.string.contextMenuConnect);
	  menu.add(Menu.NONE,2,Menu.NONE,R.string.contextMenuShowPwd);
	 
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  
	  switch (item.getItemId()) {
	  case 1:
		//placeholder for action
		System.out.println("Selezionato Connect da "+ListviewContent.get((int)info.id).get("SSID"));
	    return true;
	  case 2:
		  //placeholder for new activity
	  	System.out.println("Selezionato Show Password da "+ListviewContent.get((int)info.id).get("SSID"));
	    return true;
	  default:
	    return super.onContextItemSelected(item);
	  }
	}
}