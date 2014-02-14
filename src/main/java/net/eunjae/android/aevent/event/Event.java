package net.eunjae.android.aevent.event;

import android.os.Handler;
import net.eunjae.android.aevent.manager.AEventManager;
import net.eunjae.android.aevent.util.CompareUtil;

public class Event {
	private final String eventName;
	private Object[] eventData;
	private Class<?> eventTarget;
	private long timestamp;

	public Event(String eventName) {
		this.eventName = eventName;
		this.eventData = null;
		this.eventTarget = null;
		this.timestamp = System.currentTimeMillis();
	}

	public Event data(Object... eventData) {
		this.eventData = eventData;
		return this;
	}

	public Event target(Class<?> eventTarget) {
		this.eventTarget = eventTarget;
		return this;
	}

	public void post() {
		AEventManager.getInstance().post(this);
	}

	public void postDelayed(long delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				post();
			}
		}, delay);
	}

	public String _getEventName() {
		return eventName;
	}

	public Object[] _getEventData() {
		return eventData;
	}

	public Class<?> _getEventTarget() {
		return eventTarget;
	}

	public long _getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		Event dest = (Event) o;
		if (CompareUtil.isDifferent(_getEventName(), dest._getEventName())) {
			return false;
		}
		if (_getEventData() != null && dest._getEventData() != null) {
			if (_getEventData().length != dest._getEventData().length) {
				return false;
			} else {
				for (int i = 0; i < _getEventData().length; i++) {
					if (!_getEventData()[i].equals(dest._getEventData()[i])) {
						return false;
					}
				}
				return true;
			}
		} else if (_getEventData() == null && dest._getEventData() == null) {
			return true;
		} else {
			return false;
		}
	}
}
