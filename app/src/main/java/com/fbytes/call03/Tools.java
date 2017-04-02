package com.fbytes.call03;

import java.util.HashMap;

public class Tools {
	
	private Tools() {};
	
	static{
		HashMap<String,String> addrToShortMap=new HashMap<String,String>();
		addrToShortMap.put("","");
	}
	
	public static String AddressToShortForm(String pAddress){
		String tResult=pAddress.replaceAll("корпус ","к.");
		tResult=tResult.replaceAll("улица","ул.");
		tResult=tResult.replaceAll("(\\d+)(к)(\\d+)","$1 $2.$3");
		tResult=tResult.replaceAll("(,)(\\S)",", $2");
		return(tResult);		
	}


	public static String AddressToLongForm(String pAddress){
		String tResult=pAddress.replaceAll("(\\d+)( к.)(\\d+)","$1 корпус $3");
		tResult=tResult.replaceAll("ул.","улица ");
		return(tResult);
	}
}
