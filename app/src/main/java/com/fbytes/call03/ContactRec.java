package com.fbytes.call03;

public class ContactRec {
	public int conId;
	String Name;
	String Phone;
	String Email;
	public boolean ContactOptions[];
	

	ContactRec(int pId,String pName, String pPhone,String pEmail){
		this.conId=pId;
		this.Name=pName;
		this.Phone=pPhone;
		this.Email=pEmail;
		CreateProc();
		ContactOptions[0]=false;
		ContactOptions[1]=true;
		ContactOptions[2]=false;
	}
	
	ContactRec(int pId,String pName, String pPhone,boolean pCanCall,boolean pCanSMS,boolean pCanEmail){
		this.conId=pId;	
		this.Name=pName;
		this.Phone=pPhone;		
		CreateProc();
		ContactOptions[0]=pCanCall;
		ContactOptions[1]=pCanSMS;
		ContactOptions[2]=pCanEmail;
	}
	
	void CreateProc(){
		ContactOptions=new boolean[3];	
	}
	
	int getId(){return conId;};
	String getName(){return Name;};
	String getPhone(){return Phone;};
	String getEmail(){return Email;};
	
	boolean getOption(int pOption){return ContactOptions[pOption];};
	
	void setOption(int pOption,boolean pValue){ContactOptions[pOption]=pValue;};
}
