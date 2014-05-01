import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.deuce.Atomic;

public class ParallelAccessControlVer3 {
	private ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> R;
	private boolean[] PNG;
	private boolean[] cache;
//	ReentrantLock lock;
	
	public ParallelAccessControlVer3 (int numAddressesLog) {
		R = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>>();
		PNG = new boolean[(int) Math.pow(2, numAddressesLog)];
		for (int i = 0; i < PNG.length; i++) {
			PNG[i] = true;
		}
	}
	

	@Atomic
	public boolean isPermittedTransfer(int source, int destination) {
		if (PNG[source])
			return false;
		ConcurrentSkipListSet<Integer> destinationPermissions = R.get(destination);
		boolean ans = (destinationPermissions == null) ? false : destinationPermissions.contains(source);		
		return ans;
	}

	@Atomic
	public void updatePermission(Packet configPacket) {
		Config config = configPacket.config;
		PNG[config.address] = config.personaNonGrata;
		
		if (!R.containsKey(config.address)) {
			R.put(config.address, new ConcurrentSkipListSet<Integer>());
		}
		ConcurrentSkipListSet<Integer> addressPermissions = R.get(config.address);
		
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
	}

}