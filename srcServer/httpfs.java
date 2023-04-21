


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;


public class httpfs {

	public static void main(String[] args) throws IOException{
		
		//default path
		String rootDirectory = "C:/Users/nalve/Desktop/Java.progr/445A3Server/445Directory";
		boolean verbose = true;
		int port = 2000;
		
		
		//getting arguments
		for(int i=0;i<args.length;i++) {
			
			if(args[i].equalsIgnoreCase("-v")) {
				verbose = true;
			}
			else if(args[i].equalsIgnoreCase("-p")) {
				port = Integer.valueOf(args[i+1]);
			}
			else if(args[i].equalsIgnoreCase("-d")) {
				rootDirectory = (args[i+1]);
			}
			else if(args[i].equalsIgnoreCase("help")) {
				FileServer.help();
				System.exit(0);
			}
		}
		
		
		
		//infinite loop to keep answering requests
		while(true) {
			
			//creating server sockets
            UDP server = new UDP(6080,port);
			
			System.out.println("Server Started");
		    
			String msg = server.receiveMessage(9080);

			
		    if(verbose) {
		      System.out.println("Request Received:");
		      System.out.println(msg);
		    }
		    
			String [] arr = msg.split("\r\n");
			
			String [] methodLine = arr[0].split(" ");
			String method = methodLine[0];
			String uri = methodLine[1];
			
			//process /../ from uri
			uri = FileServer.processUri(uri);
			
			String path = rootDirectory+uri;
			String statusCode = "200 OK";
			String body = null;
			
					
			if(verbose) {
				System.out.println("Path:");
				System.out.println(path);	
			}
			int contentLength = 0; 
			String requestBody = "";
						    
			//POST Request
			
			if(method.equalsIgnoreCase("POST")) {
				
				int sep=0;
				//reading body
				for(int i=0;i<arr.length;i++) {
					if(arr[i].contains("Content-Length")) {
						String len = arr[i].substring(arr[i].indexOf(':')+1, arr[i].length());
						len = len.split(" ")[0];
						len = len.trim();
						contentLength = Integer.valueOf(len);
					}
					
					if(arr[i].length()==0) {
						sep = i;
						System.out.println("break:" +i);
					}
					
					
				}
				
				String fullBody="";
				
				for(int i=sep+1;i<arr.length;i++) {
                    
					fullBody+=arr[i]+"\n";
					
				}

				
				for(int j=0;j<contentLength;j++) {
					
					requestBody+= fullBody.charAt(j);
				}
				
				if(verbose) {
				System.out.println(requestBody);
				}
				
				String response = FileServer.post(path, requestBody); //execute post request
				
				
                if(response.equalsIgnoreCase("400 Bad Request") || response.equalsIgnoreCase("403 Forbidden")) {
					
					statusCode = response;
					body = "";
				}
				else {
					
					body = response;
				}
				//send response
				String responseHeader = "HTTP/1.0 "+statusCode+"\r\nContent-Length:"+contentLength+"\r\nUser-Agent:nal\r\n\r\n";
			
				server.sendMessage(responseHeader+body,"localhost",7080);
				
				System.out.println("DONE");
				
				if(verbose) {
				System.out.println("Responce Sent: "+responseHeader+body);
				}
			}
			
			// GET Request
			else if(method.equalsIgnoreCase("GET")) {
				
				String response = FileServer.get(path); //execute get request
				
				File file = new File(path);
				String type =null;
				
				try {
				type = Files.probeContentType(file.toPath());
				}catch(IOException e) {
					System.out.println("cannot determine type");
				}
				
				if(response.equalsIgnoreCase("404 Not Found") || response.equalsIgnoreCase("403 Forbidden")) {
					
					statusCode = response;
					body = "";
				}
				else {
					
					body = response;
				}
				
				String responseHeader=null;
				
				
				//content type and disposition
				if(file.exists() && file.isFile() && (type.contains("zip")||type.contains("pdf"))){
					responseHeader = "HTTP/1.0 "+statusCode+"\r\nContent-Type:"+type+"\r\nContent-Disposition:attachment ; filename = \""+file.getName()+"\"\r\nUser-Agent:nal\r\n\r\n";
				}
				else if(file.isFile() && type!=null) {
					responseHeader = "HTTP/1.0 "+statusCode+"\r\nContent-Type:"+type+"\r\nContent-Disposition: inline\r\nUser-Agent:nal\r\n\r\n";
				}else {
				    responseHeader = "HTTP/1.0 "+statusCode+"\r\nContent-Type:Text\r\nContent-Disposition: inline\r\nUser-Agent:nal\r\n\r\n";
				}
			    
				server.sendMessage(responseHeader+body, "localhost",7080);
				
		
				
				if(verbose) {
				System.out.println("Responce Sent: "+responseHeader+body);
				System.out.println("DONE");
				}
            }
			else {
				
				server.sendMessage("HTTP/1.0 400 Bad Request\r\nContent-Type:Text\r\nUser-Agent:nal\r\n\r\n","localhost",7080);
			    
				if(verbose) {
				System.out.println("Response Sent : HTTP/1.0 400 Bad Request\r\nContent-Type:Text\r\nUser-Agent:nal\r\n\r\n");
				}
				
				System.out.println("DONE");
			}
			
		   
		      
		 }
        
	
	}

}
