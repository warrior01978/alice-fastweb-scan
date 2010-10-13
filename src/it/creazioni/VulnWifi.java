package it.creazioni;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public abstract class VulnWifi {
String essid_router;
String bssid_router;
String seriale;
WifiManager wifiManager;
Vector<String> le_pass;
Context con;
WifiConfiguration wifiConfig;
recive reciveWifierrorAuth;
int netId;
int counterpass;
ProgressDialog dialog;
	public void setManager(WifiManager wifiManager){
		this.wifiManager=wifiManager;
	}
	public void setContext(Context con){
		this.con=con;
	}
	public void setESSID(String essid){
		essid_router=essid;
	}
	public void setBSSID(String bssid){
		bssid_router=bssid;
	}
/*	public void connectMe(String bssid, String essid, WifiManager wifiManager){
		setManager(wifiManager);
		setESSID(essid);
		setBSSID(bssid);
		le_pass=crackMe();
		configNet();
	}*/
	public void configNet(){
		if (le_pass.size()==0){
			return;
		}
		dialog = new ProgressDialog(con);
		dialog.setCancelable(true);
        dialog.setMessage("Loading...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(le_pass.size());
        dialog.show();
		counterpass=-1;
		reciveWifierrorAuth =new recive();//broadcaster receiver
		wifiConfig= new WifiConfiguration();
		wifiConfig.BSSID=bssid_router;
		wifiConfig.SSID="\""+essid_router+"\""; //lo vuole tra virgolette
		
		wifiConfig.preSharedKey=giveMePass();//already quoted.
		wifiConfig.hiddenSSID = true;  
		wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		//the following two are for 802.1x Enterprise wifi.
		wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		IntentFilter mWifiStateFilter;

		mWifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    mWifiStateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
	    mWifiStateFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
	    mWifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	    con.registerReceiver(reciveWifierrorAuth, mWifiStateFilter);

	    //con.registerReceiver(reciveWifierrorAuth, mWifiStateFilter);
		wifiConfig.status = WifiConfiguration.Status.ENABLED;
		netId=wifiManager.addNetwork(wifiConfig);
		wifiManager.enableNetwork(netId,true);
	}
	class recive extends BroadcastReceiver {
    	public void onReceive(Context c, Intent intent) {
    		 //Toast.makeText(con, ((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)).toString(), Toast.LENGTH_LONG).show();
    		  if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
    			  Toast.makeText(con, ((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)).toString(), Toast.LENGTH_SHORT).show();
    		      handleErrorSuplicant((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE),
    		          intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR),
    		          intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0));
    		      
    		    } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
    		      handleNetworkStateChanged((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
    		    } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
    		      final ConnectivityManager connn = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
    		      final NetworkInfo[] s = connn.getAllNetworkInfo();
    		      for (final NetworkInfo i : s){
    		        if (i.getTypeName().contentEquals("WIFI")){
    		          final NetworkInfo.State state = i.getState();
    		          final String ssid = wifiManager.getConnectionInfo().getSSID();
    		          if (state == NetworkInfo.State.CONNECTED){
    		        	  Toast.makeText(con, "Connected to:\n"+ssid+"with IP:\n"+intToIp(wifiManager.getConnectionInfo().getIpAddress()), Toast.LENGTH_LONG).show();
    		        	  con.unregisterReceiver(reciveWifierrorAuth);
    		        	  dialog.cancel();
    		          }
    		          if (state == NetworkInfo.State.DISCONNECTED){
    		        	  //Toast.makeText(con, "Network not reachable", Toast.LENGTH_LONG).show();
    		        	  //dialog.cancel();
    		          }
    		        }
    		      }
    		    }
    		}
	}
	public static String intToIp(Integer i) {
		String output;
		output = ((Integer)(i & 0xFF)).toString();
		output += "." + ((Integer)((i >> 8) & 0xFF)).toString();
		output += "." + ((Integer)((i >> 16) & 0xFF)).toString();
		output += "." + ((Integer)((i >> 24) & 0xFF)).toString();
		return output;
	}
	private void handleNetworkStateChanged(NetworkInfo networkInfo) {
		final NetworkInfo.DetailedState state = networkInfo.getDetailedState();
    	if (state == NetworkInfo.DetailedState.FAILED){
    		Toast.makeText(con, "Network Error", Toast.LENGTH_LONG).show();
    	}
	}
    private void handleErrorSuplicant(SupplicantState state, boolean hasError, int error) {
    	if (hasError || state == SupplicantState.INACTIVE){
    		if (counterpass+1<le_pass.size()){//until keys are consumed
    			
    			//wifiManager.disableNetwork(netId);
    			wifiConfig.preSharedKey=giveMePass();
    			wifiManager.updateNetwork(wifiConfig);
    			//wifiManager.enableNetwork(netId,true);
    		}
    		else{
    			dialog.cancel();
    			wifiManager.disableNetwork(netId);
    			con.unregisterReceiver(reciveWifierrorAuth);
    			//clear net config
    		}
    		//Toast.makeText(con, "Errore ", Toast.LENGTH_LONG).show();
    	}
    }


	public String giveMePass(){
		counterpass++;
		dialog.incrementProgressBy(1);
		dialog.setMessage("Trying Key:"+le_pass.elementAt(counterpass));
		//Toast.makeText(con, "Trying Key:"+le_pass.elementAt(counterpass), Toast.LENGTH_SHORT).show();
		return "\""+le_pass.elementAt(counterpass)+"\"";
		
	}
	public abstract Vector<String> crackMe();
	public String convertHexToString(String hex){
		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
	 
		  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for( int i=0; i<hex.length()-1; i+=2 ){
	 
		      //grab the hex in pairs
		    String output = hex.substring(i, (i + 2));
		      //convert hex to decimal
		    int decimal = Integer.parseInt(output, 16);
		      //convert the decimal to character
		    sb.append((char)decimal);
	 
		    temp.append(decimal);
		  }
		  
	 
		  return sb.toString();
	  }
	public String hash(String s,String algo) {  
		try {  
			//Create MD5 Hash  
			MessageDigest digest = java.security.MessageDigest.getInstance(algo); 
			//convert to bytes
			digest.update(hexToByte(s));  
			byte messageDigest[] = digest.digest();  
			// Create Hex String  
			StringBuffer hexString = new StringBuffer();  
			for (int i=0; i<messageDigest.length; i++) { 
				String h =Integer.toHexString(0xFF & messageDigest[i]);  
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();  
		} catch (NoSuchAlgorithmException e) {  
			e.printStackTrace();  
		}  
		return "";  
	}  
	public byte[] hexToByte(String s){
		byte[] bts = new byte[s.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(s.substring(2*i, 2*i+2), 16);
		}
	return bts;
	}
		
	public String stringToHex(String base){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<base.length(); i++){
			int ch=(int)base.charAt( i );
			sb.append(Integer.toHexString( ch ));	
		}
	return sb.toString();
	}
		
}
