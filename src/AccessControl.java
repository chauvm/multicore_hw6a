import java.util.HashMap;
import java.util.HashSet;

public class AccessControl {
	private HashMap<Integer, HashSet<Integer>> R;
	private boolean[] PNG;
//	private HashMap<Integer, Boolean> PNG;
	private boolean[] cache;
	
	public AccessControl (int numAddressesLog) {
		R = new HashMap<Integer, HashSet<Integer>>();
		PNG = new boolean[(int) Math.pow(2, numAddressesLog)];
		for (int i = 0; i < PNG.length; i++) {
			PNG[i] = true;
		}
//		PNG = new HashMap<Integer, Boolean>();
	}
	
	/**
	 * MAYBE DONT NEED THIS METHOD
	 * @param source
	 * @return
	 */
	public boolean isForbiddenSource(int source) {
		return PNG[source];
//		boolean ans = (PNG.get(source) == null) ? false : PNG.get(source);
//		return ans;
	}

	public boolean isPermittedTransfer(int source, int destination) {
		if (isForbiddenSource(source))
			return false;
		HashSet<Integer> destinationPermissions = R.get(destination);
		boolean ans = (destinationPermissions == null) ? false : destinationPermissions.contains(source);		
		return ans;
	}

	public void updatePermission(Packet configPacket) {
		Config config = configPacket.config;
		PNG[config.address] = config.personaNonGrata;
//		PNG.put(config.address, config.personaNonGrata);
		
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
	}

}