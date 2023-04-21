

//Author Nalveer Moocheet 40072605
//COMP 445

import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


import java.io.*;


public class HttpClient {
    
	
	
	public static void get(String server,String path,boolean verbose, boolean toFile,String fileName ,ArrayList<String> headers,int port) {
		
		//server name and port 
        String serverName = server;
        
        
        //method,URL and version of request
        String request = "GET "+path+" HTTP/1.0\r\n";
        
        //headers of request -h
        int size = headers.size();
        
        for(int i=0;i<size;i++) {
        	
        	request = request + headers.get(i)+"\r\n";
        }
        
        //host
        request = request + "HOST: "+server+"\r\n\r\n";
        //System.out.println(request);
        
        //starting connection and sending request
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            
            //reliable udp
            UDP client = new UDP(9080,7080);
            System.out.println(request);
            boolean sent = client.sendMessage(request, serverName, port);
            
            if(sent) {
            System.out.println("waiting");
            String reply = client.receiveMessage(6080);
            System.out.println("received");
            
            //printing all info for verbose
            if(verbose && !toFile) {
                System.out.println(reply);
            }
            //displaying output only
            else if(!verbose && !toFile){
                
            	String arr[] = reply.split("\r\n");
            	boolean body=false;
            	
            	for(int i=0;i<arr.length;i++) {
            		
            		 String line = arr[i];
            		 if(body) {
            			 System.out.println(line);
            		 }
            		 
            		 if(line.length() == 0) {
            			 body = true;
            		 }
            		
            	}
            	
            }
            else if(verbose) { //output to file with verbose
            	
            	try {
            	      FileWriter myWriter = new FileWriter(fileName);
            	      
            	      
            	      myWriter.write(reply);
                      
            	      
            	      myWriter.close();
            	      System.out.println("Output wrote to the file.");
            	    } catch (IOException e) {
            	      System.out.println("An error occurred.");
            	      e.printStackTrace();
            	    }
            }
            else {   //output to file, no verbose
            	
            	
            	try {
          	      FileWriter myWriter = new FileWriter(fileName);
          	    	 
          	    	
              	  String arr[] = reply.split("\r\n");
              	  boolean body=false;
              	
              	  for(int i=0;i<arr.length;i++) {
              		
              		  String line = arr[i];
              		  if(body) {
              			 myWriter.write(line+"\n");
              		  }
              		 
              		  if(line.length() == 0) {
              			 body = true;
              		  }
              	  }
              	  
              	  myWriter.close();
          	      System.out.println("Output wrote to the file.");
              	
            	}          	      
          	    catch (IOException e) {
          	      System.out.println("An error occurred.");
          	      e.printStackTrace();
          	    }
            	
          }

         }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
		
	
	
	
	public static void post(String server,String path,boolean verbose,boolean toFile,String fileName, ArrayList<String> headers, ArrayList<String> data,int port) {
		
        String serverName = server;
        
        
        //method,path,version 
        String request = "POST "+path+" HTTP/1.0\r\n";
       
        int size = headers.size();
        
        //headers
        for(int i=0;i<size;i++) {
        	//discard content length to override user entry
        	if(!(headers.get(i).substring(0, headers.get(i).indexOf(":"))).equals("Content-Length"))
        	request = request + headers.get(i)+"\r\n";
        }
        request = request + "HOST: "+server+"\r\n";
        
       
        //entity body
        int sizeData = data.size();
        String dataString = "";
        for(int i=0;i<sizeData;i++) {
        	
        	if(i==sizeData-1 || sizeData==1) {
        	dataString = dataString + data.get(i);
        	
        	}
        	else {
        	dataString = dataString + data.get(i)+",";
        	}
        }
        int stringL = dataString.length();
        
        //System.out.println(stringL);
        
        //overiding content length
        request= request + "Content-Length:"+stringL+" \r\n\r\n";
        
        //adding request body
        request= request + dataString;
        
        //System.out.println(request);
        
        String reply = null;
        
        //sending request
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);

            UDP client = new UDP(9080,7080);
            System.out.println(request);
            boolean sent = client.sendMessage(request, serverName, port);
            
            if(sent) {
            System.out.println("waiting");
            reply = client.receiveMessage(6080);
            System.out.println("received");
            
            
            
            //printing all info for verbose
            if(verbose && !toFile) {
                
             	System.out.println(reply);
            }
            //displaying output only
            }else if(!verbose && !toFile){
                
               	String arr[] = reply.split("\r\n");
            	boolean body=false;
            	
            	for(int i=0;i<arr.length;i++) {
            		
            		 String line = arr[0];
            		 if(body) {
            			 System.out.println(line);
            		 }
            		 
            		 if(line.length() == 0) {
            			 body = true;
            		 }
            		
            	}
            }
            else if(verbose) {  //writing to file with verbose
            	
            	try {
            	      FileWriter myWriter = new FileWriter(fileName,false);
            	      
            	    	  myWriter.write(reply);
                      
            	      
            	      myWriter.close();
            	      System.out.println("Output wrote to the file.");
            	    } catch (IOException e) {
            	      System.out.println("An error occurred.");
            	      e.printStackTrace();
            	    }
            }
            else {    //writing to file no verbose
            	
            	int print=0;
            	
            	try {
          	      FileWriter myWriter = new FileWriter(fileName,false);
          	    	 
                 	String arr[] = reply.split("\r\n");
                	boolean body=false;
                	
                	for(int i=0;i<arr.length;i++) {
                		
                		 String line = arr[0];
                		 if(body) {
                			 myWriter.write(line);
                		 }
                		 
                		 if(line.length() == 0) {
                			 body = true;
                		 }
                		
                	}
          	      
          	      myWriter.close();
          	      System.out.println("Output wrote to the file.");
          	    } catch (IOException e) {
          	      System.out.println("An error occurred.");
          	      e.printStackTrace();
          	    }
            	
          }

   

        } catch (IOException e) {
            e.printStackTrace();
        }
	
		
		
	}
	
	//usage help
	public static void help(String help) {
		
		if(help.equalsIgnoreCase("general")) {
			System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n"
					+ "Usage:\n"
					+ "httpc command [argument]\n"
					+ "The commands are:\n"
					+ "get   executes HTTP GET regquest and prints the response.\n"
					+ "post  executes HTTP POST request and prints the response.\n"
					+ "help  prints this screen.\n"
					+ "Use \" httpc help [command]\" for more information about a command.");
		}
		else if(help.equalsIgnoreCase("post")) {
			
			System.out.println("Usage: http post [-v] [-h \"key:value\"] [-d \"inline-data\"] [-f file] URL\n"
					+ "\n"
					+ "Get executes a HTTP GET request for a given URL.\n"
					+ "\n"
					+ "-v             prints the detail of the reponse such as protocol, status, and headers.\n"
					+ "-h key:value   Associates headers to the HTTP request with the format \'key:value\'.\n"
					+ "-d string      Associates an inline data to the body HTTP POST request.\n"
					+ "-f file        Associates the content of a file to the body HTTP POST request.\n"
					+ "-o file        Write output to file.\n"
					+ "Either [-d] or [-f] can be used but not both.");
						
		}
		else if(help.equalsIgnoreCase("get")) {
			
			System.out.println("Usage: http post [-v] [-h \"key:value\"] [-d \"inline-data\"] [-f file] URL\n"
					+ "\n"
					+ "Get executes a HTTP GET request for a given URL.\n"
					+ "\n"
					+ "-v             prints the detail of the reponse such as protocol, status, and headers.\n"
					+ "-h key:value   Associates headers to the HTTP request with the format \'key:value\'.\n"
					+ "-o file        Write output to file.\n");
						
		}
		else{
			System.out.println("Wrong input");
		}
	}
	
	//getting data from file for -f
	public static ArrayList<String> getFileData(String name) {
		
		ArrayList<String> data = new ArrayList<String>();
		
	    try {
	        File myObj = new File(name);
	        Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	          data.add(myReader.nextLine());
	        }
	        myReader.close();
	      } catch (FileNotFoundException e) {
	        System.out.println("Could not open File.");
	        help("post");
	        System.exit(0);
	      }
		
		return data;
		
	}
	
}
