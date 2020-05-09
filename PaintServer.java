/*
 * Michelle Li
 * Period 8
 * This class is the server that multiple clients can connect to in order to paint together.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;

//accepts all connections from clients
//reads in messages from clients and broadcasts those messages to all other clients
public class PaintServer
{
	
	private ArrayList<ClientHandler> allClients;  // used to broadcast messages to all connected clients
	private ArrayList<Point> allPoints;
	
	// creates the serverSocket on port 4242.
	// continously attempts to listen for new clients
	// builds a clientHandler thread off the socket and starts that thread.
	// this constructor never ends.

	public  PaintServer() {

		allClients = new ArrayList<ClientHandler>();
		allPoints = new ArrayList<Point>();
		
		//creates new server socket and deploys new thread every time a new client enters 
		try {
			ServerSocket server = new ServerSocket(4242);
			
			while(true) {

				ClientHandler sock = new ClientHandler(server.accept());
				Thread thread = new Thread(sock);
				thread.start();

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}

	// writes the message to every socket in the ArrayList instance variable.
	private void tellEveryone(String message) {

		for(int i = 0; i < allClients.size(); i++) {

			PrintWriter out = allClients.get(i).theWriter;
			out.println(message);
			out.flush();
		}
	}

	//interacts with a specific client
	public class ClientHandler implements Runnable {

		private Scanner reader;
		private Socket sock;
		private PrintWriter theWriter;
		private String name;				//client's name
		private Color color;				//client color draws with

		// initializes all instance variables
		public ClientHandler(Socket clientSocket) {

			try {
				reader = new Scanner(clientSocket.getInputStream());
				theWriter = new PrintWriter(clientSocket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sock = clientSocket;

			String[] info = reader.nextLine().split(" ");
			name = info[0];

			color = new Color(Integer.parseInt(info[1]), Integer.parseInt(info[2]), Integer.parseInt(info[3]));

			//sends information about all the other clients to the newly connected client
			for(int i = 0; i < allClients.size(); i++) {
				theWriter.println("joined:"+allClients.get(i));
				theWriter.flush();
			}
			
			//sends information about all current points to newly connected client
			for(int i = 0; i < allPoints.size(); i++) {
				theWriter.println(allPoints.get(i));
				theWriter.flush();
			}
			
			//adds new client and tells everyone about the newly connected client
			tellEveryone("joined:"+this);
			allClients.add(this);
		}


		public String toString() {
			return name +" "+ color.getRed() + " "+color.getGreen()+ " "+color.getBlue();
		}

		public boolean equals(Object o) {
			ClientHandler other = (ClientHandler)o;
			return name.equals(other.name);
		}


		//continuously checks to see if there is an available message from the client
		// if so broadcasts received message to all other clients
		// via the outer helper method tellEveryone.
		public void run() {

			String message = "";
			
			//tells all clients in ArrayList the message it received
			 do{

				message = reader.nextLine();				
				
				//if received a Point, add to allPoints
				if(message.split(" ").length == 6) {
					
					String info[] = message.split(" ");
					Color color = new Color(Integer.parseInt(info[2]), Integer.parseInt(info[3]), Integer.parseInt(info[4]));
					allPoints.add(new Point(Integer.parseInt(info[0]), Integer.parseInt(info[1]), color, Integer.parseInt(info[5])));
				}
				
				tellEveryone(message);


			}while(reader.hasNextLine() && !message.contains("logoff"));

			closeConnections();
		}

		private void closeConnections(){

			try{
				synchronized(allClients){

					reader.close();
					theWriter.close();
					sock.close();
					allClients.remove(this);
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	public class Point{

		private int xLoc;
		private int yLoc;
		private Color ptColor;
		private int ptSize;

		public Point(int x, int y, Color color, int size){
			xLoc = x;
			yLoc = y;
			ptColor = color;
			ptSize = size;
		}

		public String toString() {
			return xLoc+" "+yLoc+" "+ptColor.getRed() +" "+ptColor.getGreen()+" "+ptColor.getBlue()+ " "+ptSize+" ";
		}
	}

	public static void main(String[] args) {
		new PaintServer();
	}
}