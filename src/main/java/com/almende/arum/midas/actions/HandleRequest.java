package com.almende.arum.midas.actions;

import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.soa.esb.actions.annotation.Process;
import org.jboss.soa.esb.lifecycle.annotation.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.transform.rpc.RpcTransform;
import com.almende.eve.transform.rpc.RpcTransformFactory;
import com.almende.eve.transform.rpc.formats.JSONMessage;
import com.almende.eve.transform.rpc.formats.JSONRequest;
import com.almende.eve.transform.rpc.formats.JSONResponse;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.certicon.arum.core.messaging.ARUMMessage;
import cz.certicon.arum.core.messaging.ARUMMessageFactory;
import cz.certicon.arum.core.messaging.ARUMMessageLanguage;
import cz.certicon.arum.core.messaging.ARUMMessagePerformative;
import cz.certicon.arum.core.messaging.ARUMMessageType;
import cz.certicon.arum.core.messaging.client.ARUMServiceInvoker;
import cz.certicon.arum.core.messaging.client.ARUMServiceInvokerFactory;
import cz.certicon.arum.core.messaging.exceptions.ARUMMessageDeliveryException;
import cz.certicon.arum.core.messaging.exceptions.ARUMProtocolException;
import cz.certicon.arum.core.messaging.exceptions.ARUMUnexpectedMessageProcessingException;

public class HandleRequest {

	private static Logger logger = LoggerFactory.getLogger(HandleRequest.class);

	private RpcTransform rpc = RpcTransformFactory.get(new SimpleHandler<Object>(this));
	
	private static final ScheduledExecutorService worker = 
			  Executors.newSingleThreadScheduledExecutor();

	private Transport transport;
	//private RpcTransform rpc = RpcTransformFactory.get(new SimpleHandler<Object>(this));
	private URI proxyAgentURI = URI.create("http://localhost:3000/agents/esbProxy");
	
	Random randomNumber = new Random();
	
	protected void generateBusMessage(String content) {
		try {
			ARUMServiceInvoker serviceInvoker = 
					ARUMServiceInvokerFactory.createARUMServiceInvoker("MIDAS", "MidasService");
		
			ARUMMessage message = ARUMMessageFactory.createMessage(ARUMMessageType.FIPA_REQUEST_REQUEST,
				ARUMMessageLanguage.APPLICATION_XML, "testID11223");
			message.setContent(content);
			//message.setFrom(endpoint); //hrmmm what the hell is this now, why necessary?
			//serviceInvoker.setDefaultFromHeader("MidasService", "MIDAS");
			//NOTE: This is not FIPA compliant!! FIPA only has async..
			ARUMMessage response = serviceInvoker.deliverSync(message, 5000);
			//the following is FIPA compliant; I guess the answer then gets delivered to the .._reply queue
				//TODO: check that out
			//serviceInvoker.deliverAsync(message);
			logger.debug("got reply!" + response.getContent());
			System.out.println("system reply " + response.getContent());
		
		} 
		catch (ARUMMessageDeliveryException e) { e.printStackTrace(); } 
		catch (ARUMProtocolException e) {	e.printStackTrace(); } 
		catch (ARUMUnexpectedMessageProcessingException e) { e.printStackTrace(); }
		
		return;
	}
	
	 public class GenerateJob implements Runnable {
		  private String workerName;
		  private double time;
		  public GenerateJob(String _workerName, double _time) {
		    this.workerName = _workerName;
		    this.time = _time; // in seconds
		  }
		  public void run() {
			  // equivalent JS code: 
			  // var message = {id:0, method:"routeEvent", params:{worker:worker, type: "jobStarted", timestamp: date.getTime() }};
			  // eve.useServiceFunction("send", "local://esbProxy", message, function(answer){ console.log(answer); }); 
			  
			  generateBusMessage("{\"worker\":\"" + workerName + "\",\"type\":\"jobStarted\", \"timestamp\":" + String.valueOf(System.currentTimeMillis()) + "}");
			  
			  // now decide how long the job will take
			  // eq. JS code:
			  //var realTime = randgen.rnorm(jobTimes[worker ], 10);
			  //if (realTime < 2) realTime = 2; // making sure we dont have negative or ultrashort times
				
			  double duration = time + randomNumber.nextGaussian() * 10;
			  if (duration < 2) duration = 2;
			  
			  // finally, make sure that at that time the job finished stuff is run
			  FinishJob fin = fR;
			  if (workerName.equals("Ludo")) fin = fL;
			  if (workerName.equals("Giovanni")) fin = fG;
			  
			  worker.schedule(fin, (long) duration, TimeUnit.SECONDS);
		  }
	}
	 
	GenerateJob gR = new GenerateJob("Remco", 40);
	GenerateJob gG = new GenerateJob("Giovanni", 30);
	GenerateJob gL = new GenerateJob("Ludo", 30);
	
	 public class FinishJob implements Runnable {
		  private String workerName;
		  private double time;
		  public FinishJob(String _workerName) {
		    this.workerName = _workerName;
		  }
		  public void run() {
			  // equivalent JS code: 
			  // var message = {id:0, method:"routeEvent", params:{worker:worker, type: "jobStarted", timestamp: date.getTime() }};
			  // eve.useServiceFunction("send", "local://esbProxy", message, function(answer){ console.log(answer); }); 
			  
			  generateBusMessage("{\"worker\":\"" + workerName + "\",\"type\":\"jobFinished\", \"timestamp\":" + String.valueOf(System.currentTimeMillis()) + "}");
			  
			  // finally, make sure that at that time the job finished stuff is run
			  GenerateJob gen = gR;
			  if (workerName.equals("Ludo")) gen = gL;
			  if (workerName.equals("Giovanni")) gen = gG;
			  
			  worker.schedule(gen, 2, TimeUnit.SECONDS);
		  }
	}
	
	FinishJob fR = new FinishJob("Remco");
	FinishJob fG = new FinishJob("Giovanni");
	FinishJob fL = new FinishJob("Ludo");
	 
	@Initialize
	public void checkConfig() {
		
		logger.debug("initializing!");
		//System.out.println("system initializing");
	
		//generate test incoming message
		//worker.schedule(task, 5, TimeUnit.SECONDS);
		worker.schedule(gG, 5, TimeUnit.SECONDS);
		worker.schedule(gR, 6, TimeUnit.SECONDS);
		worker.schedule(gL, 7, TimeUnit.SECONDS);
		
		
		final HttpTransportConfig config = new HttpTransportConfig();
		config.setServletUrl("http://localhost:8080/MIDASGateway/agents/");
		config.setId("gatewayAgent");
		
		//final Transport	transport = TransportFactory.getTransport(config, new SimpleHandler<Receiver>(new Receiver(){
		transport = TransportFactory.getTransport(config, new SimpleHandler<Receiver>(new Receiver(){
			
			@Override
			public void receive(Object msg, URI senderUrl, String tag) {
				
				// incoming message from MIDAS agents
				JSONMessage incoming = RpcTransform.jsonConvert(msg);
				
				// check if we have a reply or a request; 
				// reply either goes to
				
				// TODO; this is never called because when doing a POST request we run into the error: 
				//   The server encountered an internal error (Couldn't load transport) that prevented it from fulfilling this request.
				// which is generated in EveServlet.java / DebugServlet.java for some reason
				
				// answers on requests that were send out are coming through though!
				
				if (incoming instanceof JSONRequest) {
					
					// either generate event
					
					
					// or forward request to ARUM service

					
					// and send a synchronous answer
					
					JSONResponse response = new JSONResponse();					
					response.setId(incoming.getId());
					String result = "success";
					response.setResult(result);
										
					try {
						//transport.send(senderUrl, response.toString(), tag );
						transport.send(proxyAgentURI, response.toString(), tag );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}   
					
				} else if (incoming instanceof JSONResponse) {
					// we can either keep track of reponses ourselves, or let Eve take care of that
					// note that we may have to keep track of responses ourselves anyway, for the ARUM-side of affairs
					
					// we dont need to do anything with the response for now 
					
				}
				
				
				
				logger.info("Received msg:"+msg +" from:"+ senderUrl);
			}
		}));
		//reqSocket.connect("tcp://localhost:5556");
		 
		
	}

	/** Note from Tomas:
	 * Processes the messages received from the ESB. Only ARUM compliant
	 * messages are accepted. FIPA_REQUEST protocol is expected for standard
	 * messages. FIPA_QUERY protocol is expected for requesting API
	 * specification (WSDL file).
	 * 
	 * Remco:
	 * FIPA_REQUEST => forward request to MIDAS
	 * FIPA_QUERY => return wsdl. For now, just ignore it...
	 * 
	 * Also include a listener for events, and forward them to MIDAS
	 */
	
	@Process
	public ARUMMessage handleIncomingRequest(ARUMMessage message) {
		
		//TODO: see how we can make this an async action
		
		logger.debug("got a message! " + message.getContent());
		System.out.println("system got a message "+ message.getContent());
				
		//do something with the message

		// forward it over http 
		
		JSONRequest request = new JSONRequest();					
		//request.setId(0); //ID can stay null
		request.setMethod("incomingEvent");
		request.putParam("Content", message.getContent());
		
		System.out.println(request.toString());
		
		try {
			transport.send(proxyAgentURI, request.toString(), null );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
				
		// send the answer as soon as we got it..
	
		ARUMMessage arumResponse = null;
		ARUMMessagePerformative responsePerformative = ARUMMessagePerformative.INFORM_RESULT;
		
		arumResponse = ARUMMessageFactory.createResponseMessage(message, responsePerformative);
		arumResponse.setContent("Got it");
		
		logger.debug("replying... " + arumResponse.getType());
		System.out.println("replying... "+ arumResponse.getType());
			
		
		return arumResponse;
	}

}
