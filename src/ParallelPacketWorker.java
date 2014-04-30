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