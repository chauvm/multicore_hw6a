import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import org.deuce.Atomic;

public class ParallelAccessControl {
	private HashMap<Integer, HashSet<Integer>> R;
	private boolean[] PNG;
	private boolean[] cache;
//	ReentrantLock lock;
	
	public ParallelAccessControl (int numAddressesLog) {
		R = new HashMap<Integer, HashSet<Integer>>();
		PNG = new boolean[(int) Math.pow(2, numAddressesLog)];
		for (int i = 0; i < PNG.length; i++) {
			PNG[i] = true;
		}
//		lock = new ReentrantLock();
	}
	
//	@Atomic
//	public boolean isForbiddenSource(int source) {
////		lock.lock();
//		boolean ans = PNG[source];
////		lock.unlock();
//		return ans;
//	}

	@Atomic
	public boolean isPermittedTransfer(int source, int destination) {
//		lock.lock();
		if (PNG[source])
			return false;
		HashSet<Integer> destinationPermissions = R.get(destination);
		boolean ans = (destinationPermissions == null) ? false : destinationPermissions.contains(source);		
//		lock.unlock();
		return ans;
	}

	@Atomic
	public void updatePermission(Packet configPacket) {
//		lock.lock();
		Config config = configPacket.config;
		PNG[config.address] = config.personaNonGrata;
		
		if (!R.containsKey(config.address)) {
			R.put(config.address, new HashSet<Integer>());
		}
		HashSet<Integer> addressPermissions = R.get(config.address);
		
		if (config.acceptingRange) {
			for (int i = config.addressBegin; i < config.addressEnd; i++) {
				if (!addressPermissions.contains(i))
					addressPermissions.add(i);
			}
		} else {
			for (int i = config.addressBegin; i < config.addressEnd; i++) {
				if (addressPermissions.contains(i))
					addressPermissions.remove(i);
			}			
		}
//		lock.unlock();
	}

}