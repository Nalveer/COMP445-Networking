


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileServer {
         
	
	//method to process /../ from the url
	public static String processUri(String path) {

        String [] arr = path.split("/");
        ArrayList<String> arrL = new ArrayList<String>();
        
        for(int i=0;i<arr.length;i++) {
        	arrL.add(arr[i]);
        
        }
        String uri = "";
        int j=0;
        int k=0;
        for(int i=0;i<arr.length;i++) {
        	j=i-k;
        	if(arr[i].equals("..") && i==0) {
        		arrL.remove(j);
        		k++;
        	}
        	else if(arr[i].equals("..") && i!=0) {
        		
        		if(j>-1) {
        		  arrL.remove(j);
        		  k++;
        		  j=i-k;
        		  
        		}
        		if(j>-1) {
        		 arrL.remove(j);
                 k++;
        		 j=i-k;
        		}
        	}	
        }
		
        for(int i=0;i<arrL.size();i++) {
        	if(i>0) {	
        	uri+= "/"+arrL.get(i);
        	}
        	else {
        		uri+=arrL.get(i);
        	}
        }
        
        return uri;
	}
	
	//display help
	public static void help() {
		
		
		System.out.println("httpfs is a simple file server.\n" + 
				"usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]\n" + 
				"-v Prints debugging messages.\n" + 
				"-p Specifies the port number that the server will listen and serve at.\n" + 
				"Default is 8080."+
				"-d Specifies the directory that the server will use to read/write\n" + 
				"requested files. Default is the current directory when launching thenapplication.");
		
	}
	
	//responding to get request
	public static String get(String path) {
		
		 String body = "";
		
	
	    try {
	        File myObj = new File(path);
	        
	        if(!myObj.exists()) {
	        
	        	 return "404 Not Found";
	        }
	        else if(!myObj.canRead()) { //if file not readable            
	        	
	        	return "403 Forbidden"; 
	        }
	        else if(myObj.isFile()) {  //read file
	         
	          Scanner myReader = new Scanner(myObj);
	          while (myReader.hasNextLine()) {
	             String data = myReader.nextLine();
	             body+=data+"\n";
	          }
	          myReader.close();
	        }
	        else if(myObj.isDirectory()){  //if path is a directory, list files contained inside it
	          	 
	        	File[] listOfFiles = myObj.listFiles();

	        	for (int i = 0; i < listOfFiles.length; i++) {
	        	  if (listOfFiles[i].isFile()) {
	        	    body += ("File " + listOfFiles[i].getName()) + "\n";
	        	  } else if (listOfFiles[i].isDirectory()) {
	        	    body += ("Directory " + listOfFiles[i].getName()) + "\n";
	        	  }
	        	}
	        }
	        else {
	        	
	        	 return "404 Not Found";
	        }
	        
	       
	        return body;
	        
	      } catch (FileNotFoundException e) {
	          e.printStackTrace();
	          return "404 Not Found";
	    	  
	      }
		
	}
	
	//responding to post request
	public static String post(String path, String body) {
		
			
	    String arr[] = path.split("/");
	    String fileName=arr[arr.length-1];
	    
	    try {
		      File myObj = new File(path);
		      
		    
		      if(myObj.exists()) { //file already exists
		   
                  if(!myObj.canWrite()) {     //if file not writable
		    		  
		    		  return "403 Forbidden";
		    	  }
		    	  try {
		    	      FileWriter myWriter = new FileWriter(path,false);    //write to file
		    	      myWriter.write(body);
		    	      myWriter.close();
		    	      
		    	      return body+"\n Wrote to "+fileName;
		    	      
		    	    } catch (IOException e) {
		    	      System.out.println("An error.");
		    	      e.printStackTrace();
		    	    }   
		      }
		      else {
		    	  File parent = myObj.getParentFile();  //create parent directory 
		    	  if(!parent.exists()) {
		    		  parent.mkdirs();
		    	  }
		          myObj.createNewFile();
		          
		    	  //create and write to file
		    	  try {
		    	      FileWriter myWriter = new FileWriter(path,true);
		    	      myWriter.write(body);
		    	      myWriter.close();
		    	      
		    	      return body+"\n Wrote to "+fileName;
		    	      
		    	    } catch (IOException e) {
		    	      System.out.println("An error");
		    	      e.printStackTrace();
		    	    }  
		      }
		    
	        } catch (IOException e) {
		      System.out.println("An error occured.");
		    }
	        
		return "400 Bad Request";
	}
	
	
	
	
	
	
	
	
	
	
	
}
