package com.hexabeast.sandbox;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.hexabeast.hexboxserver.HServer;
import com.hexabeast.hexboxserver.NCompressedLayer;
import com.hexabeast.hexboxserver.NInputUpdate;
import com.hexabeast.hexboxserver.NPlayer;
import com.hexabeast.hexboxserver.NPlayerUpdate;
import com.hexabeast.sandbox.mobs.PNJ;

public class NetworkManager {
	
	public static NetworkManager instance;

	public Client client;
	
	public NetworkRequestList modifications;
	
	String name = "John";
	
	boolean online = false;
	
	boolean mapReady = false;
	
	public Timer playerTimer;
	public Timer playerUpdateTimer;
	
	public NetworkManager()
	{
		instance = this;
		modifications = new NetworkRequestList();
		client = new Client(1048576, 1048576);
		HServer.initKryoClasses(client.getKryo());
		playerTimer = new Timer(0.33f);
		playerUpdateTimer = new Timer(0.1f);
	}
	
	public void addListener()
	{
		client.addListener(new Listener() 
		{
	       public void received (Connection c, Object object) 
	       {
	    	   if(object instanceof String)
	    	   {
	    		   String str = (String)object;
	    		   System.out.println(str);
	    		   if(str.equals("Connected"))
	    		   {
	    			   online = true;
	    		   }
	    		   
	    	   }
	    	   else if (object instanceof NCompressedLayer)
		        {
					System.out.println("layer");
					NCompressedLayer n = (NCompressedLayer)object;
					if(n.isMain)
					{
						Main.loading.NmainLayer = n;
						Main.loading.NmainLoaded = true;  
					}
					else
					{
						Main.loading.NbackLayer = n;
						Main.loading.NbackLoaded = true;
						NetworkManager.instance.sendTCP("GetMainLayer");
					}    
		        }
	    	   else
	    	   modifications.add(object);
	    	   
	    	   
	       }
	       
	       public void connected(Connection c)
	       {
	    	   online = true;
	    	   System.out.println("online");
           }
           public void disconnected(Connection c)
           {
        	   online = false;
           }
           
		});
	}
	
	public void connectLocal()
	{
		connect("127.0.0.1");
	}
	
	public void connect(String IP)
	{
		//client.start();
		new Thread(client).start();
		try {
			addListener();
			
			client.connect(10000, IP, 43321, 45322);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendTCP(Object object)
	{
		client.sendTCP(object);
	}
	
	public void sendUDP(Object object)
	{
		client.sendUDP(object);
	}
	
	public void update()
	{
		if(online)
		{
			if(playerUpdateTimer.check())
			{
				NPlayerUpdate npu = new NPlayerUpdate(GameScreen.player.PNJ.x, GameScreen.player.PNJ.y, GameScreen.player.PNJ.vx, GameScreen.player.PNJ.vy, GameScreen.player.PNJ.larmRotation,GameScreen.player.PNJ.currentItem);
				sendUDP(npu);
			}
			if(playerTimer.check())
			{
				PNJ n = GameScreen.player.PNJ;
				
				NPlayer npc = new NPlayer();
				
				npc.x = n.x;
				npc.y = n.y;
				
				npc.armId = (byte) n.currentArm;
				npc.legId = (byte) n.currentLegs;
				npc.headId = (byte) n.currentHead;
				npc.bodyId = (byte) n.currentBody;
				npc.currentItem = n.currentItem;

				npc.helmetId = (byte) n.currentHelmet.id;
				
				npc.armorId = (byte) n.currentArmor.id;
				
				npc.legginsId = (byte) n.currentLeggins.id;
				
				npc.gloveId = (byte) n.currentGlove.id;
				
				npc.hookId = (byte) n.currentHook.id;

				sendTCP(npc);
				
				
				
				NInputUpdate nin = new NInputUpdate();
				
				nin.Q = Inputs.instance.Q;
				nin.S = Inputs.instance.S;
				nin.Z = Inputs.instance.Z;
				nin.D = Inputs.instance.D;
				
				nin.Left = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
				nin.Right = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
				
				nin.mousePos = Tools.getAbsoluteMouse();
				nin.Space = Inputs.instance.spacePressed;
				nin.A = Inputs.instance.middleOrAPressed;
				
				sendTCP(nin);
			}
		}
	}
	
}