



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import java.util.Timer;
import java.util.TimerTask;




public class UDP {
  
	//0-data
	//1-syn, 2-Ack, 3- SYN-ACK, 4-data, 5-fin, 6-fin ack
	int routerPort;
    String routerIP;
    int rPort;
    Packet[] buffer;
    int base;
    int windowSize;
    char [] ack;
    Timer timer;
    int serverPort;
    int listeningPort;
    public long delay;
    public static boolean stop=false;
    
    public UDP(int port2, int port) {
    	
    	routerPort = 3000;
        routerIP = "localhost";
        buffer=null;
    	base=0;
    	windowSize=0;
    	ack =null;
    	rPort=port;
    	serverPort=0;
    	listeningPort = port2;
    	delay = 500;
    }
	
	public boolean sendMessage(String message,String serverIP, int serverPort) throws UnknownHostException {
		
		 int portNum=0;
		 int ackNum=0;
		
		 this.serverPort=serverPort;
		 
		 
		 Packet synPacket = new Packet.Builder()
                 .setType(1)
                 .setSequenceNumber(0)
                 .setPortNumber(serverPort)
                 .setPeerAddress(InetAddress.getByName(serverIP))
                 .setPayload(("9080").getBytes())
                 .create();
		
		 Packet ackPacket = new Packet.Builder()
                 .setType(2)
                 .setSequenceNumber(1)
                 .setPortNumber(serverPort)
                 .setPeerAddress(InetAddress.getByName(serverIP))
                 .setPayload("".getBytes()) 
                 .create();
		 
		 //send SYN
		 Packet reply = sendR(synPacket);
		 
		 System.out.println("SYN sent SYN ACK received");
		 //receive SYN-ACK
		 if(reply.getType()==3) {
			 
			String res = reply.getPayload().toString();  
			
			//portNum  = Integer.valueOf(res.split(" ")[0]); //assigned port for this client by server
			//ackNum = Integer.valueOf(res.split(" ")[1]); 
			
			//send ACK
			send(ackPacket);
			 System.out.println("ACK sent");
		 }
		 else {
			 return false;
		 }
		
	  //handshake completed
	  
	  //converting message to list of packets
	  Queue<Packet> que = new LinkedList<Packet>();
	  
	  byte[] payload= new byte[1013];
	  byte[] payloadTotal = message.getBytes();
  
	  int j=0;
	  int p=0;
	  for(int i=0;i<payloadTotal.length;i++) {
		  
		  if(j==1012) {
			payload[j] = payloadTotal[i];
		    Packet packet = new Packet.Builder()
		                 .setType(4)
		                 .setSequenceNumber(p)
		                 .setPortNumber(serverPort)
		                 .setPeerAddress(InetAddress.getByName(serverIP))
		                 .setPayload(payload) 
		                 .create();  
			  
		    que.add(packet);
		    p++;
		    j=0;
		    payload = new byte[1013];
		  
		  }
		  else if(i==payloadTotal.length-1) {
			
			 payload[j] = payloadTotal[i]; 
			  
			 Packet packet = new Packet.Builder()
		                 .setType(4)
		                 .setSequenceNumber(p)
		                 .setPortNumber(serverPort)
		                 .setPeerAddress(InetAddress.getByName(serverIP))
		                 .setPayload(payload) 
		                 .create();  
			  
		      que.add(packet);
			  p++;
			  
		  }
		  else {
			  
			  payload[j] = payloadTotal[i];  
			  j++;
		  }
		 
	  }
	  
 
	  int bufferSize = que.size();
	  
	  buffer = new Packet[bufferSize];
	  ack = new char[bufferSize];
	  
	  for(int i=0;i<bufferSize;i++) {
		  
		  buffer[i] = que.remove();
		  ack[i] = 'N';
	  }
	  
	  windowSize = Math.min(bufferSize/2, 5);
	  
	  boolean done=false;
      Timer timer = new Timer(true);
      
      //thread that receives acknowledgement packets
		Runnable task = () -> {
			
			
			while(!stop) {
			   
				Packet rep = receive(listeningPort);
				if(rep!=null) {
				handleReply(rep);
				}
			} 
			
			
		};

	
		Thread thread = new Thread(task);

		thread.start();
        
		System.out.println("Buffer Size: "+bufferSize);
		
        
        long startTime = System.nanoTime();
        
        //sending packets
        while(!done) {
	  
		//send untransmitted packets of window
		for(int i=base;i<=base+windowSize;i++) {
	         		
				if(i < bufferSize && (ack[i]=='N')) {
		    	ack[i] = 'S';
		    	
		    	//System.out.println("dest: "+buffer[i].toString());
				send(buffer[i]);
				scheduleRetransmit(buffer[i]);
 
		       }
	    }
		if(base < bufferSize && ack[base]=='A') {
			base++;
		}
	    
		if(base == bufferSize && (ack[bufferSize-1]=='A')) {
			done=true;
		}
        
		System.out.print("");
		
      }
      
      stop=true;
      System.out.println("All Packets Sent");
      
      
      
	  Packet finPacket = new Packet.Builder()
              .setType(5)
              .setSequenceNumber(p)
              .setPortNumber(serverPort)
              .setPeerAddress(InetAddress.getByName(serverIP))
              .setPayload("9080".getBytes()) 
              .create(); 
	  p++;
	  
	  Packet ackPacket2 = new Packet.Builder()
            .setType(2)
            .setSequenceNumber(p)
            .setPortNumber(serverPort)
            .setPeerAddress(InetAddress.getByName(serverIP))
            .setPayload("".getBytes()) 
            .create();
      
      //send Fin
      Packet r = sendR(finPacket);
      //FIN-ACK
      if(r.getType()==6) {
    	  
    	  sendR(ackPacket2); //ACK
    	  
      }
	
      long endTime = System.nanoTime();
      
      delay = (long) (delay*(1-0.125) + (1-0.125)*((endTime-startTime)/1000000));
      stop=false;
      
	  return true;
	
	}
	
    private void handleReply(Packet rep) {
    	
    	int seq = (int)rep.getSequenceNumber();
    	//System.out.println(seq+ "received ack");
    	if(rep.getType()==2) {
    		ack[seq] = 'A';
    	}
    	
    	
    }

    
    
    public String receiveMessage(int replyPort) {

		String mes="";
		HashMap<Integer,Packet> map = new HashMap<Integer,Packet>();
		
		boolean done = false;
		boolean lastAck = false;
		DatagramSocket aSocket = null;
		   int p=0; 
			try {
				aSocket = new DatagramSocket(rPort); 
				
				byte[] buffer = new byte[1024];
				
                int port=0;
				
				
				
				while(!done) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					
					aSocket.receive(request);
			
					byte[] req = request.getData();
					
					Packet pak = Packet.fromBytes(req);
					
					
					System.out.println("received.."+pak.getSequenceNumber());
					
					//System.out.println("dest: "+pak.toString());

					int seq=(int) pak.getSequenceNumber() ;
					int type = pak.getType();
					
					Packet replyPacket = null;
					
					Packet ackPacket = new Packet.Builder()
			                 .setType(2)
			                 .setSequenceNumber(seq)
			                 .setPortNumber(replyPort)
			                 .setPeerAddress(pak.getPeerAddress())
			                 .setPayload("".getBytes()) 
			                 .create();
					
					
					Packet finAckPacket = new Packet.Builder()
			                 .setType(6)
			                 .setSequenceNumber(seq)
			                 .setPortNumber(pak.getPeerPort())
			                 .setPeerAddress(pak.getPeerAddress())
			                 .setPayload("".getBytes()) 
			                 .create();
					
					Packet synAckPacket = new Packet.Builder()
			                 .setType(3)
			                 .setSequenceNumber(seq)
			                 .setPortNumber(pak.getPeerPort())
			                 .setPeerAddress(pak.getPeerAddress())
			                 .setPayload("".getBytes()) 
			                 .create();
					
					
					port = request.getPort();
					
					//syn req
					if(type==1) {
						replyPacket = synAckPacket;
						String d = new String(pak.getPayload());
						//System.out.println(d);
						
					}
					//fin req
					else if(type==5) {
                    	replyPacket = finAckPacket;
						lastAck=true;
                    	
                    }
					//ack
                    else if(type==2 && lastAck==true) {
                    	done=true;
                    	replyPacket = ackPacket;
                    }
					//data 
                    else if(type==4) {
						port= routerPort;
						
						if(!map.containsKey(seq)) {
						map.put(seq, pak);
						}
					  
					   replyPacket = ackPacket;
                    }
			
					
					if(replyPacket!=null) {
					
					byte[] message = replyPacket.toBytes();
					DatagramPacket reply = new DatagramPacket(message, message.length, request.getAddress(),
							port);
					
					aSocket.send(reply);
					
					}	
				}
			} 
			catch(SocketTimeoutException e) {
                 
			}
			catch (SocketException e) {
				System.out.println("Socket: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			} finally {
				if (aSocket != null)
					aSocket.close();
			}
			

		int len = map.size();
	    
		for(int i=0;i<len;i++) {
			
			byte[] pay = map.get(i).getPayload();
			String a = new String(pay);
			
			mes+=a;
			
			
		}
	    
	    return(mes);
	
	}
    
    
    
    
	//receive 
	private static Packet receive(int port) {
			
		   Packet reply = null;
		   DatagramSocket aSocket = null;
			
			try {
				aSocket = new DatagramSocket(port); 
				
				byte[] buffer = new byte[1024];
				

					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					byte [] message = request.getData();

					reply =  Packet.fromBytes(message);
					
				
			} 
			catch(SocketTimeoutException e) {
                 return reply;
			}
			catch (SocketException e) {
				System.out.println("Socket: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			} finally {
				if (aSocket != null)
					aSocket.close();
			}
			
			return reply;

		}
	

	private  Packet sendR(Packet req) {

			
			Packet res = null;
			DatagramSocket aSocket = null; 	
			
			
			
			try{
				System.out.println("Client Started........");
				aSocket = new DatagramSocket(rPort); 
				byte [] message = req.toBytes(); 

				
				InetAddress aHost = InetAddress.getByName(routerIP); 
					
				DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);//request packet ready
				aSocket.send(request);//request sent out
				//System.out.println("packet sent to port: "+serverPort);
				
				byte [] buffer = new byte[1024];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				
				//aSocket.setSoTimeout(230);
				aSocket.receive(reply);
				
				res = Packet.fromBytes(reply.getData());
				
				
			}
			catch(SocketException e){
				System.out.println("Socket: "+e.getMessage());
				
			}
			catch(SocketTimeoutException e) {
				//resend packet after time out
				//res = sendR(req);
			}
			catch(IOException e){
				e.printStackTrace();
				System.out.println("IO: "+e.getMessage());
			}
			
			finally{
				if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
													//resource leakage, therefore, close the socket after it's use is completed to release resources.
			}
			
			return res;
		}
	
	private void send(Packet req) {

		
		Packet res = null;
		DatagramSocket aSocket = null; 	
		
		
		
		try{
			System.out.println("Client Started........");
			aSocket = new DatagramSocket(rPort); 
			
			byte [] message = req.toBytes(); 


			
			
			
			InetAddress aHost = InetAddress.getByName(routerIP); 
				
			DatagramPacket request = new DatagramPacket(message, message.length, aHost, routerPort);//request packet ready
			aSocket.send(request);//request sent out
			//System.out.println("packet sent.");
			

			
			
			
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
			
		}
		catch(SocketTimeoutException e) {
			//resend packet after time out
			//res = sendR(req);
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
		}
		
		finally{
			if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
												//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		
		
	}
	
	
		public synchronized void scheduleRetransmit(Packet p) {
		    
		    Timer timer = new Timer();

		    TimerTask action = new TimerTask() {
		        public void run() {
		            
		        	if(!(ack[(int) p.getSequenceNumber()]=='A')) {
		        		System.out.println("retransmiting");
		        		send(p);
		        		//System.out.println("dest: "+p.getPeerPort());
		        		scheduleRetransmit(p);
		        	}
		        	
		        	 
		        }

		    };

		    timer.schedule(action, 500); 
		}
	
	
	
	
}
