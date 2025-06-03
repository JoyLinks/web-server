package com.joyzl.webserver.webdav.elements;

import java.util.ArrayList;
import java.util.List;

public class LockDiscovery extends Property {

	// <!ELEMENT lockdiscovery (activelock)* >

	private List<ActiveLock> actives;

	public List<ActiveLock> getActives() {
		return actives;
	}

	public void setActives(List<ActiveLock> values) {
		if (actives != values) {
			if (actives == null) {
				actives = values;
			} else {
				actives.clear();
				actives.addAll(values);
			}
		}
	}

	public void setActives(ActiveLock... values) {
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				actives().add(values[i]);
			}
		}
	}

	public List<ActiveLock> actives() {
		if (actives == null) {
			actives = new ArrayList<>();
		}
		return actives;
	}

	public boolean hasActive() {
		return actives != null && actives.size() > 0;
	}
}