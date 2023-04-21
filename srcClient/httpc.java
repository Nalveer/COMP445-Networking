



import java.util.ArrayList;


public class httpc {


	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        
		String server = null;
		String path = null;
		int port=80;
	    String method=null;
	    boolean verbose=false;
	    ArrayList<String> headers = new ArrayList<String>();
	    ArrayList<String> data = new ArrayList<String>();
	    
	    
	    //display help if no input
		if(args.length==0) {
			System.out.println("error");
			HttpClient.help("General");
			System.exit(0);
		}
		  
		  //method get/post/help
		  if(args[0].equalsIgnoreCase("get")) {
			  method = "get";
		  }
		  else if(args[0].equalsIgnoreCase("post")) {
			  method = "post";
		  }
		  else if(args[0].equalsIgnoreCase("help")) {
			  
			  if(args.length>1 && args[1].equalsIgnoreCase("get")) {
				  HttpClient.help("get");
				  System.exit(0);
			  }
			  else if(args.length>1 && args[1].equalsIgnoreCase("post")) {
				  HttpClient.help("post");
				  System.exit(0);
			  }
			  else {
				  HttpClient.help("general");
				  System.exit(0);
			  }
		  }
		  else {
			  System.out.println("method \'"+args[0]+"\' is not recognized");
			  HttpClient.help("general");
			  System.exit(0);
		  }
		 
		  //getting Server and path
		  String fullUrl = args[args.length-1];
		  System.out.println("url: "+ fullUrl);
		  System.out.println("---------");
		  //splitting URL
		  
		  if(fullUrl.contains("http://")) {
			  
			  fullUrl=fullUrl.replace("http://", "");
			  
		  }
		  
		  if(fullUrl.contains("localhost")) {
			  
			  server = "localhost";
			  port = Integer.valueOf(fullUrl.substring(fullUrl.indexOf(':')+1, fullUrl.indexOf('/')));
			  path = fullUrl.substring(fullUrl.indexOf('/'));
		  }
		  else {
		  
		  int index = fullUrl.indexOf('/');
		  server= fullUrl.substring(0,index);
		  path = fullUrl.substring(index);
		 
		  }
		 
		  
		  //getting headers -h, data -d, file -f, verbose -v
			  boolean h =false;
			  boolean d =false;
			  boolean f =false;
			  boolean toFile = false;
			  String fileName=null;
			  String fileNameW = null;
			  
			  //searching for -h,-f,-d, -O commands and adding to arrayList
		      for(int i = 0;i<args.length;i++) {
			  
		         if(args[i].equalsIgnoreCase("-h")) {
			      h = true;
			      headers.add(args[i+1]);
			    
			     }
		         else if(args[i].equalsIgnoreCase("-d")){
		          d = true;
		          data.add(args[i+1]);
		         }
		         else if(args[i].equalsIgnoreCase("-f")) {
				  f = true;
				  fileName = args[i+1];
		         }
		         else if(args[i].equalsIgnoreCase("-v")) {
					  verbose = true;
				 }
		         else if(args[i].equalsIgnoreCase("-o")) {
		        	 toFile = true;
		        	 fileNameW = args[i+1];
		         }
		      
		      }
		      
		      //checking for wrong use of -h,-d,-f
		      if((method.equals("get")&&(d==true||f==true)) || (d==true&&f==true)) {
		    	  System.out.println("Invalid Command");
		    	  HttpClient.help(method);
		    	  System.exit(0);
		      }
          
		  //setting data arrayList based on -d or -f
		  
		  if(f) {
			  
			  data = HttpClient.getFileData(fileName);
			  
		  }
		  		 
		
		  //Sending request
		  //if get
		  if(method.equalsIgnoreCase("get")) {
			  HttpClient.get(server,path, verbose, toFile, fileNameW ,headers,port);
		  }
		  //if post
		  else if(method.equalsIgnoreCase("post")) {
			  HttpClient.post(server,path,verbose, toFile,fileNameW,headers,data,port);  
	       		  
		  }
	      
          System.out.println("finished");
		  System.exit(0);
	}
}
