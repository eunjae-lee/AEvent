package net.eunjae.android.aevent.event;

import net.eunjae.android.aevent.manager.AStickyEventManager;

public class StickyEvent extends Event {
	private boolean eventAllowDuplicates;

	public StickyEvent(String eventName) {
		super(eventName);
		this.eventAllowDuplicates = false;
	}

	@Override
	public void post() {
		AStickyEventManager.getInstance().post(this);
	}

	@Override
	public void postOnUiThread() {
		AStickyEventManager.getInstance().postOnUiThread(this);
	}

	public Event allowDuplicates() {
		this.eventAllowDuplicates = true;
		return this;
	}

	public boolean _isEventAllowDuplicates() {
		return eventAllowDuplicates;
	}

	@Override
	public boolean equals(Object o) {
		StickyEvent dest = (StickyEvent) o;
		if (_isEventAllowDuplicates() != dest._isEventAllowDuplicates()) {
			return false;
		}
		return super.equals(o);
	}
}