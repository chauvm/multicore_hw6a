
class SerialFirewall {
  public static void main(String[] argsMain) {
   
    final int paramNum = Integer.parseInt(argsMain[0]);
    final int numMilliseconds = Integer.parseInt(argsMain[1]);
    
    String[] args = ParameterMix.getParamsFromID(paramNum).split(" ");
    final int numAddressesLog = Integer.parseInt(args[0]);     
    final int numTrainsLog = Integer.parseInt(args[1]);
    final int meanTrainSize = Integer.parseInt(args[2]);
    final int meanTrainsPerComm = Integer.parseInt(args[3]);
    final int meanWindow = Integer.parseInt(args[4]);
    final int meanCommsPerAddress = Integer.parseInt(args[5]);
    final int meanWork = Integer.parseInt(args[6]);

    final float configFraction = Float.parseFloat(args[7]);
    final float pngFraction = Float.parseFloat(args[8]);
    final float acceptingFraction = Float.parseFloat(args[9]);    
    
    // create the PacketGenerator class
    PacketGenerator source = new PacketGenerator(numAddressesLog,
    	    numTrainsLog,
    	    meanTrainSize,
    	    meanTrainsPerComm,
    	    meanWindow,
    	    meanCommsPerAddress,
    	    meanWork,
    	    configFraction,
    	    pngFraction,
    	    acceptingFraction);
    StopWatch timer = new StopWatch();

    AccessControl accessController = new AccessControl(numAddressesLog);
//    for (int i = 0; i < Math.pow(2,  3*numAddressesLog/2); i++) {
    for (int i = 0; i< 3; i++) {
    	Packet initConfigPacket = source.getConfigPacket();
    	accessController.updatePermission(initConfigPacket);
    }
	PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
	PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
    
    SerialPacketWorker workerData = new SerialPacketWorker(done, source, accessController);
    Thread workerThread = new Thread(workerData);    
    
    workerThread.start();
    timer.startTimer();
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    done.value = true;
    memFence.value = true;
    try {
      workerThread.join();
    } catch (InterruptedException ignore) {;}      
    timer.stopTimer();

    final long totalCount = workerData.totalPackets;
    
    System.out.println("Serial FireWall");
    System.out.println("count: " + totalCount);
    System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
    
    System.out.println("Parameter Mix: " + paramNum);
  }
}
class ParallelFirewall {
	public static void createHistogram() {
		
	}
	
	public static void main(String[] argsMain) {
	    final int paramNum = Integer.parseInt(argsMain[0]);
	    final int numMilliseconds = Integer.parseInt(argsMain[1]);
	    final int numWorkers = Integer.parseInt(argsMain[2]);
	    
	    String[] args = ParameterMix.getParamsFromID(paramNum).split(" ");
	    final int numAddressesLog = Integer.parseInt(args[0]);     
	    final int numTrainsLog = Integer.parseInt(args[1]);
	    final int meanTrainSize = Integer.parseInt(args[2]);
	    final int meanTrainsPerComm = Integer.parseInt(args[3]);
	    final int meanWindow = Integer.parseInt(args[4]);
	    final int meanCommsPerAddress = Integer.parseInt(args[5]);
	    final int meanWork = Integer.parseInt(args[6]);
	
	    final float configFraction = Float.parseFloat(args[7]);
	    final float pngFraction = Float.parseFloat(args[8]);
	    final float acceptingFraction = Float.parseFloat(args[9]);
	    
	    final int queueDepth = 8;
	    // create the PacketGenerator class
	    PacketGenerator source = new PacketGenerator(numAddressesLog,
	    	    numTrainsLog,
	    	    meanTrainSize,
	    	    meanTrainsPerComm,
	    	    meanWindow,
	    	    meanCommsPerAddress,
	    	    meanWork,
	    	    configFraction,
	    	    pngFraction,
	    	    acceptingFraction);
	    
	    StopWatch timer = new StopWatch();
	
	    AccessControl accessController = new AccessControl(numAddressesLog);
	    for (int i = 0; i < Math.pow(2,  3*numAddressesLog/2); i++) {
	    	Packet initConfigPacket = source.getConfigPacket();
	    	accessController.updatePermission(initConfigPacket);
	    }
	    @SuppressWarnings("unchecked")
		WaitFreeQueue<Packet>[] queueBank = new WaitFreeQueue[numWorkers];
	    for (int i = 0; i < numWorkers; i++)
	        queueBank[i] = new WaitFreeQueue<Packet>(queueDepth);
		PaddedPrimitiveNonVolatile<Boolean> doneDispatch = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitiveNonVolatile<Boolean> doneWorker = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
		
	    ParallelDispatcherPacketWorker dispatchData = new ParallelDispatcherPacketWorker(doneDispatch, source, accessController, queueBank);
	    Thread dispatchThread = new Thread(dispatchData);
	    
	    ParallelPacketWorker[] workers = new ParallelPacketWorker[numWorkers];
	    Thread[] workerThread = new Thread[numWorkers];
	    for( int i = 0; i < numWorkers; i++ ) {	    
	    	workers[i] = new ParallelPacketWorker(doneWorker, queueBank[i]);
	    	workerThread[i] = new Thread(workers[i]);
	    }
	    
	    for( int i = 0; i < numWorkers; i++ ) {
	        workerThread[i].start();
	    }
	    
	    timer.startTimer();
	    
	    dispatchThread.start();
	    
	    try {
	      Thread.sleep(numMilliseconds);
	    } catch (InterruptedException ignore) {;}
	    
	    doneDispatch.value = true;
	    memFence.value = true;
	    
	    try {
//	    	System.out.println("Joining...");
	        dispatchThread.join(); // cant get out
//	    	System.out.println("Done Joining...");
	    } catch (InterruptedException ignore) {;}
	    
	    doneWorker.value = true;
	    memFence.value = false;
	    
	    for( int i = 0; i < numWorkers; i++ ) {
	        try {
//	        	System.out.println("Workers joining...");
	        	workerThread[i].join();
//	      		System.out.println("Done Workers joining...");
	        } catch (InterruptedException ignore) {;}      
	      }
	    
	    timer.stopTimer();    	    
	
	    final long totalCount = dispatchData.totalPackets;
	    System.out.println("Parallel FireWall");
	    System.out.println("count: " + totalCount);
	    System.out.println("time: " + timer.getElapsedTime());
	    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
	    System.out.println("Parameter Mix: " + paramNum);
	    System.out.println("Number of worker threads: " + numWorkers);
	  }
}


class ParallelFirewallVer2 {
	public static void createHistogram() {
		
	}
	
	public static void main(String[] argsMain) {
	    final int paramNum = Integer.parseInt(argsMain[0]);
	    final int numMilliseconds = Integer.parseInt(argsMain[1]);
//	    final int numWorkers = Integer.parseInt(argsMain[2])/2;
//	    final int numDispatchers = Integer.parseInt(argsMain[2]) - numWorkers;
	    final int numWorkers = Integer.parseInt(argsMain[2]);
	    final int numDispatchers = 2;
	    
	    String[] args = ParameterMix.getParamsFromID(paramNum).split(" ");
	    final int numAddressesLog = Integer.parseInt(args[0]);     
	    final int numTrainsLog = Integer.parseInt(args[1]);
	    final int meanTrainSize = Integer.parseInt(args[2]);
	    final int meanTrainsPerComm = Integer.parseInt(args[3]);
	    final int meanWindow = Integer.parseInt(args[4]);
	    final int meanCommsPerAddress = Integer.parseInt(args[5]);
	    final int meanWork = Integer.parseInt(args[6]);
	
	    final float configFraction = Float.parseFloat(args[7]);
	    final float pngFraction = Float.parseFloat(args[8]);
	    final float acceptingFraction = Float.parseFloat(args[9]);
	    
	    final int queueDepth = 128;   // increase queuedepth
	    // create the PacketGenerator class
	    PacketGenerator source = new PacketGenerator(numAddressesLog,
	    	    numTrainsLog,
	    	    meanTrainSize,
	    	    meanTrainsPerComm,
	    	    meanWindow,
	    	    meanCommsPerAddress,
	    	    meanWork,
	    	    configFraction,
	    	    pngFraction,
	    	    acceptingFraction);
	    
	    StopWatch timer = new StopWatch();
	
	    ParallelAccessControl accessController = new ParallelAccessControl(numAddressesLog);
	    for (int i = 0; i < Math.pow(2,  3*numAddressesLog/2); i++) {
	    	Packet initConfigPacket = source.getConfigPacket();
	    	accessController.updatePermission(initConfigPacket);
	    }
	    @SuppressWarnings("unchecked")
		WaitFreeQueue<Packet>[] queueBank = new WaitFreeQueue[numWorkers];
	    for (int i = 0; i < numWorkers; i++)
	        queueBank[i] = new WaitFreeQueue<Packet>(queueDepth);
		PaddedPrimitiveNonVolatile<Boolean> doneDispatch = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitiveNonVolatile<Boolean> doneWorker = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
		
//		DispatcherVer2 dispatchData = new DispatcherVer2(doneDispatch, source, accessController, queueBank);
//	    Thread dispatchThread = new Thread(dispatchData);
	    
		DispatcherVer2[] dispatchers = new DispatcherVer2[numDispatchers];
		Thread[] dispatcherThread = new Thread[numDispatchers];
	    for( int i = 0; i < numDispatchers; i++ ) {	    
	    	dispatchers[i] = new DispatcherVer2(doneDispatch, source, accessController, queueBank);
	    	dispatcherThread[i] = new Thread(dispatchers[i]);
	    }		
		
	    ParallelPacketWorker[] workers = new ParallelPacketWorker[numWorkers];
	    Thread[] workerThread = new Thread[numWorkers];
	    for( int i = 0; i < numWorkers; i++ ) {	    
	    	workers[i] = new ParallelPacketWorker(doneWorker, queueBank[i]);
	    	workerThread[i] = new Thread(workers[i]);
	    }
	    
	    for( int i = 0; i < numWorkers; i++ ) {
	        workerThread[i].start();
	    }
	    
	    timer.startTimer();
	    
//	    dispatchThread.start();
	    for( int i = 0; i < numDispatchers; i++ ) {
	    	dispatcherThread[i].start();
	    }
	    
	    try {
	      Thread.sleep(numMilliseconds);
	    } catch (InterruptedException ignore) {;}
	    
	    doneDispatch.value = true;
	    memFence.value = true;
	    
//	    try {
//	        dispatchThread.join(); // cant get out
//	    } catch (InterruptedException ignore) {;}
	    for( int i = 0; i < numDispatchers; i++ ) {
	        try {
	        	dispatcherThread[i].join();
	        } catch (InterruptedException ignore) {;}      
	      }
	    
	    doneWorker.value = true;
	    memFence.value = false;
	    
	    for( int i = 0; i < numWorkers; i++ ) {
	        try {
	        	workerThread[i].join();
	        } catch (InterruptedException ignore) {;}      
	      }
	    
	    timer.stopTimer();    	    
	
//	    final long totalCount = dispatchData.totalPackets;
	    long totalCount = 0;
	    for( int i = 0; i < numDispatchers; i++ ) {
	        	totalCount += dispatchers[i].totalPackets;
	      }	    
	    System.out.println("Parallel FireWall");
	    System.out.println("count: " + totalCount);
	    System.out.println("time: " + timer.getElapsedTime());
	    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
	    System.out.println("Parameter Mix: " + paramNum);
	    System.out.println("Number of worker threads: " + numWorkers);
	    System.out.println("Number of dispatcher threads: " + numDispatchers);
	  }
}

class ParallelFirewallVer3 {
	public static void createHistogram() {
		
	}
	
	public static void main(String[] argsMain) {
	    final int paramNum = Integer.parseInt(argsMain[0]);
	    final int numMilliseconds = Integer.parseInt(argsMain[1]);
//	    final int numWorkers = Integer.parseInt(argsMain[2])/2;
//	    final int numDispatchers = Integer.parseInt(argsMain[2]) - numWorkers;
	    final int numWorkers = Integer.parseInt(argsMain[2]);
	    final int numDispatchers = 1;
	    
	    String[] args = ParameterMix.getParamsFromID(paramNum).split(" ");
	    final int numAddressesLog = Integer.parseInt(args[0]);     
	    final int numTrainsLog = Integer.parseInt(args[1]);
	    final int meanTrainSize = Integer.parseInt(args[2]);
	    final int meanTrainsPerComm = Integer.parseInt(args[3]);
	    final int meanWindow = Integer.parseInt(args[4]);
	    final int meanCommsPerAddress = Integer.parseInt(args[5]);
	    final int meanWork = Integer.parseInt(args[6]);
	
	    final float configFraction = Float.parseFloat(args[7]);
	    final float pngFraction = Float.parseFloat(args[8]);
	    final float acceptingFraction = Float.parseFloat(args[9]);
	    
	    final int queueDepth = 128;   // increase queuedepth
	    // create the PacketGenerator class
	    PacketGenerator source = new PacketGenerator(numAddressesLog,
	    	    numTrainsLog,
	    	    meanTrainSize,
	    	    meanTrainsPerComm,
	    	    meanWindow,
	    	    meanCommsPerAddress,
	    	    meanWork,
	    	    configFraction,
	    	    pngFraction,
	    	    acceptingFraction);
	    
	    StopWatch timer = new StopWatch();
	
	    ParallelAccessControlVer3 accessController = new ParallelAccessControlVer3(numAddressesLog);
	    for (int i = 0; i < Math.pow(2,  3*numAddressesLog/2); i++) {
	    	Packet initConfigPacket = source.getConfigPacket();
	    	accessController.updatePermission(initConfigPacket);
	    }
	    @SuppressWarnings("unchecked")
		WaitFreeQueue<Packet>[] queueBank = new WaitFreeQueue[numWorkers];
	    for (int i = 0; i < numWorkers; i++)
	        queueBank[i] = new WaitFreeQueue<Packet>(queueDepth);
		PaddedPrimitiveNonVolatile<Boolean> doneDispatch = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitiveNonVolatile<Boolean> doneWorker = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
		
//		DispatcherVer2 dispatchData = new DispatcherVer2(doneDispatch, source, accessController, queueBank);
//	    Thread dispatchThread = new Thread(dispatchData);
	    
		DispatcherVer3[] dispatchers = new DispatcherVer3[numDispatchers];
		Thread[] dispatcherThread = new Thread[numDispatchers];
	    for( int i = 0; i < numDispatchers; i++ ) {	    
	    	dispatchers[i] = new DispatcherVer3(doneDispatch, source, accessController, queueBank);
	    	dispatcherThread[i] = new Thread(dispatchers[i]);
	    }		
		
	    ParallelPacketWorker[] workers = new ParallelPacketWorker[numWorkers];
	    Thread[] workerThread = new Thread[numWorkers];
	    for( int i = 0; i < numWorkers; i++ ) {	    
	    	workers[i] = new ParallelPacketWorker(doneWorker, queueBank[i]);
	    	workerThread[i] = new Thread(workers[i]);
	    }
	    
	    for( int i = 0; i < numWorkers; i++ ) {
	        workerThread[i].start();
	    }
	    
	    timer.startTimer();
	    
//	    dispatchThread.start();
	    for( int i = 0; i < numDispatchers; i++ ) {
	    	dispatcherThread[i].start();
	    }
	    
	    try {
	      Thread.sleep(numMilliseconds);
	    } catch (InterruptedException ignore) {;}
	    
	    doneDispatch.value = true;
	    memFence.value = true;
	    
//	    try {
//	        dispatchThread.join(); // cant get out
//	    } catch (InterruptedException ignore) {;}
	    for( int i = 0; i < numDispatchers; i++ ) {
	        try {
	        	dispatcherThread[i].join();
	        } catch (InterruptedException ignore) {;}      
	      }
	    
	    doneWorker.value = true;
	    memFence.value = false;
	    
	    for( int i = 0; i < numWorkers; i++ ) {
	        try {
	        	workerThread[i].join();
	        } catch (InterruptedException ignore) {;}      
	      }
	    
	    timer.stopTimer();    	    
	
//	    final long totalCount = dispatchData.totalPackets;
	    long totalCount = 0;
	    for( int i = 0; i < numDispatchers; i++ ) {
	        	totalCount += dispatchers[i].totalPackets;
	      }	    
	    System.out.println("Parallel FireWall Ver 3");
	    System.out.println("count: " + totalCount);
	    System.out.println("time: " + timer.getElapsedTime());
	    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
	    System.out.println("Parameter Mix: " + paramNum);
	    System.out.println("Number of worker threads: " + numWorkers);
	    System.out.println("Number of dispatcher threads: " + numDispatchers);
	  }
}


class ParallelFirewallVer4 {
	public static void createHistogram() {
		
	}
	
	public static void main(String[] argsMain) {
	    final int paramNum = Integer.parseInt(argsMain[0]);
	    final int numMilliseconds = Integer.parseInt(argsMain[1]);
//	    final int numWorkers = Integer.parseInt(argsMain[2])/2;
//	    final int numDispatchers = Integer.parseInt(argsMain[2]) - numWorkers;
	    final int numWorkers = Integer.parseInt(argsMain[2]);
	    
	    String[] args = ParameterMix.getParamsFromID(paramNum).split(" ");
	    final int numAddressesLog = Integer.parseInt(args[0]);     
	    final int numTrainsLog = Integer.parseInt(args[1]);
	    final int meanTrainSize = Integer.parseInt(args[2]);
	    final int meanTrainsPerComm = Integer.parseInt(args[3]);
	    final int meanWindow = Integer.parseInt(args[4]);
	    final int meanCommsPerAddress = Integer.parseInt(args[5]);
	    final int meanWork = Integer.parseInt(args[6]);
	
	    final float configFraction = Float.parseFloat(args[7]);
	    final float pngFraction = Float.parseFloat(args[8]);
	    final float acceptingFraction = Float.parseFloat(args[9]);
	    
	    // create the PacketGenerator class
	    PacketGenerator source = new PacketGenerator(numAddressesLog,
	    	    numTrainsLog,
	    	    meanTrainSize,
	    	    meanTrainsPerComm,
	    	    meanWindow,
	    	    meanCommsPerAddress,
	    	    meanWork,
	    	    configFraction,
	    	    pngFraction,
	    	    acceptingFraction);
	    
	    StopWatch timer = new StopWatch();
	
	    ParallelAccessControlVer3 accessController = new ParallelAccessControlVer3(numAddressesLog);
	    for (int i = 0; i < Math.pow(2,  3*numAddressesLog/2); i++) {
	    	Packet initConfigPacket = source.getConfigPacket();
	    	accessController.updatePermission(initConfigPacket);
	    }
		PaddedPrimitiveNonVolatile<Boolean> doneWorker = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
		
		
		SerialPacketWorkerVer4[] workers = new SerialPacketWorkerVer4[numWorkers];
	    Thread[] workerThread = new Thread[numWorkers];
	    for( int i = 0; i < numWorkers; i++ ) {	    
	    	workers[i] = new SerialPacketWorkerVer4(doneWorker, source, accessController);
	    	workerThread[i] = new Thread(workers[i]);
	    }
	    timer.startTimer();
 
	    for( int i = 0; i < numWorkers; i++ ) {
	        workerThread[i].start();
	    }
	    
	    
	    
	    try {
	      Thread.sleep(numMilliseconds);
	    } catch (InterruptedException ignore) {;}
	    
	    
	    doneWorker.value = true;
	    memFence.value = true;
	    
	    for( int i = 0; i < numWorkers; i++ ) {
	        try {
	        	workerThread[i].join();
	        } catch (InterruptedException ignore) {;}      
	      }
	    
	    timer.stopTimer();    	    
	
//	    final long totalCount = dispatchData.totalPackets;
	    long totalCount = 0;
	    for( int i = 0; i < numWorkers; i++ ) {
	        	totalCount +=workers[i].totalPackets;
	      }	    
	    System.out.println("Parallel FireWall Ver 4");
	    System.out.println("count: " + totalCount);
	    System.out.println("time: " + timer.getElapsedTime());
	    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
	    System.out.println("Parameter Mix: " + paramNum);
	    System.out.println("Number of worker threads: " + numWorkers);
	  }
}