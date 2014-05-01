import java.util.HashMap;
import java.util.Random;

class DispatcherVer2 implements PacketWorker {
	int totalPackets;
	final PacketGenerator source;
	final ParallelAccessControl accessController;
	WaitFreeQueue<Packet>[] queueBank;
	PaddedPrimitiveNonVolatile<Boolean> done;
	Random rand = new Random();
	// for now use Java HashMap to store fingerprint values
	public DispatcherVer2(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketGenerator source,
			ParallelAccessControl accessController,
			WaitFreeQueue<Packet>[] queueBank
			) {
		totalPackets = 0;
		this.done = done;
		this.source = source;
		this.accessController = accessController;
		this.queueBank = queueBank;
	}
	
	public void run() {
		while (!done.value) {
			Packet pkt = source.getPacket();
			if (pkt.type == Packet.MessageType.ConfigPacket) {
				accessController.updatePermission(pkt);
			} else {
//				System.out.println("Processed DataPacket...");				
				// process data, NOT YET CONSIDER IF THIS IS THE LAST IN TRAIN
				if (accessController.isPermittedTransfer(pkt.header.source, pkt.header.dest)) {
					int i = rand.nextInt(queueBank.length);
			        boolean delivered = false;
			        while( !delivered ) {
			        	if (!done.value) {
				          try {
				            queueBank[i].enq(pkt);
				            delivered = true;
				          } catch (FullException e) {;}
				        } else {
				        	break;
				        }
			        }					
				}
			}
			totalPackets++;			
		}
	}
}



class ParallelPacketWorkerVer2 implements PacketWorker {
	final WaitFreeQueue<Packet> queue;
	private HashMap<Long, Integer> fingerprintsHash = new HashMap<Long, Integer>();
	PaddedPrimitiveNonVolatile<Boolean> done;
	final Fingerprint residue = new Fingerprint();
	
	public ParallelPacketWorkerVer2(
			PaddedPrimitiveNonVolatile<Boolean> done,
			WaitFreeQueue<Packet> queue
			) {
		this.queue = queue;
		this.done = done;
	}
	
	
	@Override
	public void run() {
		boolean doneFlag = false;
		while (!doneFlag) {
	          while( ProcessQueue() ) {;}
	          doneFlag = done.value;			
		}
	}
	
	public boolean ProcessQueue() {
		if (!done.value) {
			Packet pkt;
			try {
				pkt = queue.deq();
					if (pkt != null) {
					Long currFingerprint = residue.getFingerprint(pkt.body.iterations, pkt.body.seed);
			        try {
			        	fingerprintsHash.put(currFingerprint, fingerprintsHash.get(currFingerprint)+1);
			        } catch (NullPointerException e) {
			        	fingerprintsHash.put(currFingerprint, 1);
			        }
				}
			} catch (EmptyQueueException e) {
				return false;
			}
			return true;
		}
		return false;		
	}
}


//// VER 3
class DispatcherVer3 implements PacketWorker {
	int totalPackets;
	final PacketGenerator source;
	final ParallelAccessControlVer3 accessController;
	WaitFreeQueue<Packet>[] queueBank;
	PaddedPrimitiveNonVolatile<Boolean> done;
	Random rand = new Random();
	// for now use Java HashMap to store fingerprint values
	public DispatcherVer3(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketGenerator source,
			ParallelAccessControlVer3 accessController,
			WaitFreeQueue<Packet>[] queueBank
			) {
		totalPackets = 0;
		this.done = done;
		this.source = source;
		this.accessController = accessController;
		this.queueBank = queueBank;
	}
	
	public void run() {
		while (!done.value) {
			Packet pkt = source.getPacket();
			if (pkt.type == Packet.MessageType.ConfigPacket) {
				accessController.updatePermission(pkt);
			} else {
//				System.out.println("Processed DataPacket...");				
				// process data, NOT YET CONSIDER IF THIS IS THE LAST IN TRAIN
				if (accessController.isPermittedTransfer(pkt.header.source, pkt.header.dest)) {
					int i = rand.nextInt(queueBank.length);
			        boolean delivered = false;
			        while( !delivered ) {
			        	if (!done.value) {
				          try {
				            queueBank[i].enq(pkt);
				            delivered = true;
				          } catch (FullException e) {;}
				        } else {
				        	break;
				        }
			        }					
				}
			}
			totalPackets++;			
		}
	}
}



class ParallelPacketWorkerVer3 implements PacketWorker {
	final WaitFreeQueue<Packet> queue;
	private HashMap<Long, Integer> fingerprintsHash = new HashMap<Long, Integer>();
	PaddedPrimitiveNonVolatile<Boolean> done;
	final Fingerprint residue = new Fingerprint();
	
	public ParallelPacketWorkerVer3(
			PaddedPrimitiveNonVolatile<Boolean> done,
			WaitFreeQueue<Packet> queue
			) {
		this.queue = queue;
		this.done = done;
	}
	
	
	@Override
	public void run() {
		boolean doneFlag = false;
		while (!doneFlag) {
	          while( ProcessQueue() ) {;}
	          doneFlag = done.value;			
		}
	}
	
	public boolean ProcessQueue() {
		if (!done.value) {
			Packet pkt;
			try {
				pkt = queue.deq();
					if (pkt != null) {
					Long currFingerprint = residue.getFingerprint(pkt.body.iterations, pkt.body.seed);
			        try {
			        	fingerprintsHash.put(currFingerprint, fingerprintsHash.get(currFingerprint)+1);
			        } catch (NullPointerException e) {
			        	fingerprintsHash.put(currFingerprint, 1);
			        }
				}
			} catch (EmptyQueueException e) {
				return false;
			}
			return true;
		}
		return false;		
	}
}


////// VER 4
class SerialPacketWorkerVer4 implements PacketWorker {
	int totalPackets;
	final PacketGenerator source;
	final ParallelAccessControlVer3 accessController;
	final Fingerprint residue = new Fingerprint();
	
	// for now use Java HashMap to store fingerprint values
	private HashMap<Long, Integer> fingerprintsHash = new HashMap<Long, Integer>();
	PaddedPrimitiveNonVolatile<Boolean> done;

	public SerialPacketWorkerVer4(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketGenerator source,
			ParallelAccessControlVer3 accessController
			) {
		totalPackets = 0;
		this.done = done;
		this.source = source;
		this.accessController = accessController;
	}
	
	public void run() {
		while (!done.value) {
			Packet pkt = source.getPacket();
			if (pkt.type == Packet.MessageType.ConfigPacket) {
				accessController.updatePermission(pkt);
			} else {
//				System.out.println("Processed DataPacket...");				
				// process data, NOT YET CONSIDER IF THIS IS THE LAST IN TRAIN
				if (accessController.isPermittedTransfer(pkt.header.source, pkt.header.dest)) {
					// should take a histogram of fingerprints?
			        // fingerprint += residue.getFingerprint(pkt.body.iterations, pkt.body.seed);  
					Long currFingerprint = residue.getFingerprint(pkt.body.iterations, pkt.body.seed);
			        try {
			        	fingerprintsHash.put(currFingerprint, fingerprintsHash.get(currFingerprint)+1);
			        } catch (NullPointerException e) {
			        	fingerprintsHash.put(currFingerprint, 1);
			        }
				}
			}
			totalPackets++;			
		}
	}
}

//class DispatcherVer4 implements PacketWorker {
//	int totalPackets;
//	final PacketGenerator source;
//	final ParallelAccessControlVer3 accessController;
//	WaitFreeQueue<Packet>[] queueBank;
//	PaddedPrimitiveNonVolatile<Boolean> done;
//	Random rand = new Random();
//	// for now use Java HashMap to store fingerprint values
//	public DispatcherVer4(PaddedPrimitiveNonVolatile<Boolean> done,
//			PacketGenerator source,
//			ParallelAccessControlVer3 accessController,
//			WaitFreeQueue<Packet>[] queueBank
//			) {
//		totalPackets = 0;
//		this.done = done;
//		this.source = source;
//		this.accessController = accessController;
//		this.queueBank = queueBank;
//	}
//	
//	public void run() {
//		while (!done.value) {
//			Packet pkt = source.getPacket();
//			if (pkt.type == Packet.MessageType.ConfigPacket) {
//				accessController.updatePermission(pkt);
//			} else {
////				System.out.println("Processed DataPacket...");				
//				// process data, NOT YET CONSIDER IF THIS IS THE LAST IN TRAIN
//				if (accessController.isPermittedTransfer(pkt.header.source, pkt.header.dest)) {
//					int i = rand.nextInt(queueBank.length);
//			        boolean delivered = false;
//			        while( !delivered ) {
//			        	if (!done.value) {
//				          try {
//				            queueBank[i].enq(pkt);
//				            delivered = true;
//				          } catch (FullException e) {;}
//				        } else {
//				        	break;
//				        }
//			        }					
//				}
//			}
//			totalPackets++;			
//		}
//	}
//}
//
//
//
//class ParallelPacketWorkerVer4 implements PacketWorker {
//	final WaitFreeQueue<Packet> queue;
//	private HashMap<Long, Integer> fingerprintsHash = new HashMap<Long, Integer>();
//	PaddedPrimitiveNonVolatile<Boolean> done;
//	final Fingerprint residue = new Fingerprint();
//	
//	public ParallelPacketWorkerVer4(
//			PaddedPrimitiveNonVolatile<Boolean> done,
//			WaitFreeQueue<Packet> queue
//			) {
//		this.queue = queue;
//		this.done = done;
//	}
//	
//	
//	@Override
//	public void run() {
//		boolean doneFlag = false;
//		while (!doneFlag) {
//	          while( ProcessQueue() ) {;}
//	          doneFlag = done.value;			
//		}
//	}
//	
//	public boolean ProcessQueue() {
//		if (!done.value) {
//			Packet pkt;
//			try {
//				pkt = queue.deq();
//					if (pkt != null) {
//					Long currFingerprint = residue.getFingerprint(pkt.body.iterations, pkt.body.seed);
//			        try {
//			        	fingerprintsHash.put(currFingerprint, fingerprintsHash.get(currFingerprint)+1);
//			        } catch (NullPointerException e) {
//			        	fingerprintsHash.put(currFingerprint, 1);
//			        }
//				}
//			} catch (EmptyQueueException e) {
//				return false;
//			}
//			return true;
//		}
//		return false;		
//	}
//}