//package com.example.zhanyuzhen.assistnet;

//import android.util.Log;

import org.json.JSONObject;
import org.json.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanyuzhen on 2016/6/16.
 */
public class Server extends Thread {
    private boolean OutServer = false;
    private ServerSocket server;
    private final int ServerPort = 8765;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String input;
    private JSONObject json;
    private JSONObject jsonAccount;
    ArrayList<JSONObject> list = new ArrayList<JSONObject>();
    private Socket socket = new Socket();
    ArrayList<JSONObject> user_info = new ArrayList<JSONObject>();
    Map<String, ArrayList<String>> mails = new HashMap<String, ArrayList<String>>(); 

    public Server(){
        try{
            server = new ServerSocket(ServerPort);
            server.setSoTimeout(20000);
        } catch(IOException e){
            System.out.println("Server fault!");
            System.out.println("IOException: " + e.toString());
        }
        String ip = getLocalIpAddress();
        System.out.println("ip = " + ip);

        //try put in list
        JSONObject first = new JSONObject();
        first.put("Title", "pot");
        first.put("Content", "need a pot for performance.");
        first.put("author", "Doris");
        first.put("date", "2016/06/19");
        ArrayList<String> comment = new ArrayList<String>();
        String c1 = "Jacky pot 1";
        String c2 = "Jamess pot 1";
        comment.add(c1);
        comment.add(c2);
        JSONArray jsonArray = new JSONArray(comment);
        first.put("comment", jsonArray);
        System.out.println("1 : " + first);
        list.add(first); 
        ArrayList<String> show = new ArrayList<String>();
        JSONArray showJson = first.getJSONArray("comment");
        for(int i = 0; i < showJson.length(); i ++){
            show.add(showJson.get(i).toString());
            System.out.println("c1: " + show.get(i));
        }
    }
    public void run(){
        System.out.println("Server start!");
        while(!OutServer){
            socket = null;
            try{
                synchronized (server){
                    socket = server.accept();
                }
               // socket = server.accept();
                System.out.println("Connected, InetAddress = " + socket.getInetAddress());
                socket.setSoTimeout(80000);
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());

                while(!((input = inputStream.readUTF()).equals("Bye"))){
                    //client request for data
                    if(input.equals("Data")){
                        System.out.println("client request for data");
                        for(int i = 0; i < list.size(); i ++){
                            outputStream.writeUTF(list.get(i).toString());
                        }
                        outputStream.writeUTF("Data End");
                    }
                    else if (input.equals("New")){
                        System.out.println("client request new data");
                        input = inputStream.readUTF();
                        System.out.println("input: " + input);
                        json = new JSONObject(input);
                        list.add(json);
                        System.out.println("new request success!");
                        outputStream.writeUTF("add success");
                    }
                    else if (input.equals("Edit")){
                        System.out.println("client request edit data");
                        input = inputStream.readUTF();
                        System.out.println("input: " + input);
                        json = new JSONObject(input);
                        for(int i = 0; i < list.size(); i ++){
                           if((json.getString("author")).equals(list.get(i).getString("author")) && 
                                 (json.getString("date")).equals(list.get(i).getString("date"))){
                                 list.set(i, json);
                                 System.out.println("i = " + i);
                                 break;
                           }
                        }
                        System.out.println("edit request success");
                        outputStream.writeUTF("edit success");
                    }
                    else if(input.equals("Delete")){
                        System.out.println("client request delete data");
                        input = inputStream.readUTF();
                        System.out.println("input: " + input);
                        json = new JSONObject(input);
                        for(int i = 0 ; i < list.size(); i ++){
                        
                           if((json.getString("author")).equals(list.get(i).getString("author")) && 
                                 (json.getString("date")).equals(list.get(i).getString("date"))){
                                 list.remove(i);
                                 System.out.println("i = " + i);
                                 break;
                           }
                        }
                        outputStream.writeUTF("delete success");
                    }
                    else if(input.equals("Support")){
                        System.out.println("client request add support");
                        input = inputStream.readUTF();
                        json = new JSONObject(input);
                        System.out.println("input: " + input);
                        if(mails.get(json.getString("author"))!= null){
                           mails.get(json.getString("author")).add(json.getString("date"));
                           System.out.println("support for " + json.getString("author") + ":" + mails.get(json.getString("author")).get(0));
                        } else {
                           ArrayList<String> user_support = new ArrayList<String>();
                           user_support.add(json.getString("date"));
                           mails.put(json.getString("author"), user_support);
                           System.out.println("support for " + json.getString("author") + ":" + mails.get(json.getString("author")).get(0));
                        }
                        for(int i = 0 ; i < list.size(); i ++){
                        
                           if((json.getString("author")).equals(list.get(i).getString("author")) && 
                                 (json.getString("date")).equals(list.get(i).getString("date"))){
                                 System.out.println("i = " + i);
                                 JSONArray jsonArray = list.get(i).getJSONArray("support");
                                 System.out.println("jsonArray = " + jsonArray);
                                 ArrayList<HashMap<String, String>> tmp_list = new ArrayList<HashMap<String, String>>();
                                 for(int j = 0; j < jsonArray.length(); j ++){
                                       System.out.println("hashing map");
                                       JSONObject tmp = new JSONObject(jsonArray.get(j).toString());
                                       HashMap<String, String> map_tmp = new HashMap<String, String>();
                                       map_tmp.put("name", tmp.getString("name"));
                                       map_tmp.put("num", tmp.getString("num"));
                                       map_tmp.put("pic", tmp.getString("pic"));
                                       tmp_list.add(map_tmp);
                                 }
                                 String Name = inputStream.readUTF();
                                 String Num = inputStream.readUTF();
                                 String Pic = inputStream.readUTF();
                                 HashMap<String, String> map_tmp = new HashMap<String, String>();
                                 map_tmp.put("name", Name);
                                 map_tmp.put("num", Num);
                                 map_tmp.put("pic", Pic);
                                 tmp_list.add(map_tmp);
                                 JSONArray support = new JSONArray(tmp_list);
                                 list.get(i).put("support", support);
                                 outputStream.writeUTF(list.get(i).toString());
                                 System.out.println("new support" + support);
                                 break;
                           }
                        }
                        outputStream.writeUTF("support success");
                    }
                    else if (input.equals("LogIn")){
                        String account, passwd;
                        account = inputStream.readUTF();
                        passwd = inputStream.readUTF();
                        boolean NoAccount = true;
                        for(int i = 0; i < user_info.size(); i ++){
                           try{
                              if(user_info.get(i).getString("account").equals(account)){
                                 NoAccount = false;
                                 if(user_info.get(i).getString("password").equals(passwd)){
                                    //login success
                                    outputStream.writeUTF("LogIn_Success");
                                    //get mailbox
                                    outputStream.writeUTF("mail");
                                    if(mails.get(account)!=null){
                                        for(int j = 0; j < mails.get(account).size(); j ++){
                                           outputStream.writeUTF(mails.get(account).get(j));
                                        }                                        
                                    }
                                    outputStream.writeUTF("mail end");
                                 }
                                 else{
                                    outputStream.writeUTF("LogIn_WrongPassWord");
                                 }
                              }
                           } catch (IOException e){
                              System.out.println("Login fault!");
                              System.out.println("IOException: " + e.toString());
                           }
                        }
                        if(NoAccount){
                           outputStream.writeUTF("LogIn_NoAccount");
                        }
                    }
                    else if (input.equals("Register")){
                        String AccountInfo = inputStream.readUTF();
                        boolean repeat = false;
                        try{
                           System.out.println("AccountInfo string: " + AccountInfo);
                           jsonAccount = new JSONObject(AccountInfo);
                        } catch(JSONException e){
                           System.out.println("Register Fault!");
                           System.out.println("JSONException: " + e.toString());
                           outputStream.writeUTF("Register_Fail");
                        }        
                        for(int i = 0; i < user_info.size(); i ++){
                           try{
                              if(user_info.get(i).getString("account").equals(jsonAccount.getString("account"))){
                                 repeat = true;
                              }
                           } catch (JSONException e){
                              System.out.println("Register Fault!");
                              System.out.println("JSONException: " + e.toString());
                           }
                        }
                        if(repeat == true){
                           outputStream.writeUTF("Register_SameAccount");
                        }
                        else {
                           user_info.add(jsonAccount);
                           outputStream.writeUTF("Register_Success");
                           System.out.println("Register Success");
                        }
                    }
                    else if(input.equals("Delete_Mail")){
                        System.out.println("Deleting");
                        String name = new String(inputStream.readUTF());
                        String target = new String(inputStream.readUTF());
                        for(int i=0;i<mails.get(name).size();i++){
                            if(mails.get(name).get(i).equals(target)){
                                mails.get(name).remove(i);
                                System.out.println("Delete Mail "+ target);                                
                            }
                        }
                        for(int i=0;i<mails.get(name).size();i++){
                            outputStream.writeUTF(mails.get(name).get(i));
                        }   
                        outputStream.writeUTF("mails_end");
                        System.out.println("Delete Done");
                    }
                }
                outputStream.close();
                outputStream = null;
                inputStream.close();
                inputStream = null;
                socket.close();
            } catch(IOException e){
                System.out.println("Socket fault!");
                System.out.println("IOException: " + e.toString());
            }
        }
    }
    public static void main(String args[]){
        (new Server()).start();
    }
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println("cannot get ip");
        }
        return null;
    }
}
