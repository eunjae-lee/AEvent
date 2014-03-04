package net.eunjae.android.aevent.manager;

import android.util.Log;
import net.eunjae.android.aevent.annotation.ASticky;
import net.eunjae.android.aevent.util.CompareUtil;
import net.eunjae.android.aevent.event.StickyEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AStickyEventManager {

	private static volatile AStickyEventManager instance = null;

	private EventMap eventMap = new EventMap();
	private HashMap<Class<?>, ArrayList<Method>> annotatedMethodsByClass = new HashMap<Class<?>, ArrayList<Method>>();

	public static AStickyEventManager getInstance() {
		if (instance == null) {
			synchronized (AStickyEventManager.class) {
				if (instance == null) {
					instance = new AStickyEventManager();
				}
			}
		}
		return instance;
	}

	private AStickyEventManager() {
	}

	public void post(StickyEvent event) {
		boolean willAddEvent;
		if (event._isEventAllowDuplicates()) {
			willAddEvent = true;
		} else {
			willAddEvent = !eventMap.hasEvent(event);
		}

		if (willAddEvent) {
			eventMap.add(event._getEventName(), event);
		}
	}

	public void cancelAll(String eventName) {
		eventMap.remove(eventName);
	}

	public void firePendingEvents(Object subscriber) {
		ArrayList<Method> methods = getAnnotatedMethodsRecursively(subscriber.getClass());
		eventMap.fireEvents(subscriber, methods);
	}

	private ArrayList<Method> getAnnotatedMethodsRecursively(Class<?> klass) {
		if (klass == null || klass.equals(Class.class)) {
			annotatedMethodsByClass.put(klass, null);
			return null;
		}
		if (annotatedMethodsByClass.containsKey(klass)) {
			return annotatedMethodsByClass.get(klass);
		}
		ArrayList<Method> result = new ArrayList<Method>();
		Method[] methods = klass.getDeclaredMethods();
		if (methods == null) {
			annotatedMethodsByClass.put(klass, null);
			return null;
		}
		for (Method method : methods) {
			ASticky annotation = method.getAnnotation(ASticky.class);
			if (annotation != null) {
				method.setAccessible(true);
				result.add(method);
			}
		}
		ArrayList<Method> methodsFromSuperClass = getAnnotatedMethodsRecursively(klass.getSuperclass());
		if (methodsFromSuperClass != null) {
			result.addAll(methodsFromSuperClass);
		}
		annotatedMethodsByClass.put(klass, result);
		return result;
	}

	private static class EventMap {
		private HashMap<String, ArrayList<StickyEvent>> map = new HashMap<String, ArrayList<StickyEvent>>();

		public void add(String eventName, StickyEvent event) {
			if (!map.containsKey(eventName)) {
				map.put(eventName, new ArrayList<StickyEvent>());
			}
			map.get(eventName).add(event);
		}

		public void fireEvents(Object subscriber, ArrayList<Method> methods) {
			ArrayList<StickyEvent> eventsToFire = new ArrayList<StickyEvent>();
			HashMap<String, Method> methodMap = new HashMap<String, Method>();

			// collect events to fire
			for (Method method : methods) {
				ASticky annotation = method.getAnnotation(ASticky.class);
				if (annotation == null) {
					continue;
				}
				String eventName = annotation.value();
				if (!map.containsKey(eventName)) {
					continue;
				}

				for (StickyEvent event : map.get(eventName)) {
					if (event._getEventTarget() == null || subscriber.getClass().equals(event._getEventTarget())) {
						eventsToFire.add(event);
					}
				}
				methodMap.put(eventName, method);
			}

			// sort by timestamp
			Collections.sort(eventsToFire, comparator);

			// fire all events
			for (StickyEvent event : eventsToFire) {
				Method method = methodMap.get(event._getEventName());
				fireEvent(event, subscriber, method);
				remove(event);
			}
		}

		private void fireEvent(StickyEvent event, Object subscriber, Method method) {
			Object[] eventData = event._getEventData();
			Class<?>[] parameterTypes = method.getParameterTypes();

			try {
				if ((parameterTypes == null || parameterTypes.length == 0) && (eventData == null || eventData.length == 0)) {
					method.invoke(subscriber);
				} else if (eventData != null && parameterTypes.length == eventData.length) {
					method.invoke(subscriber, eventData);
				} else {
					throw new IllegalArgumentException(String.format("Method %s requires %d parameters, but you posted %s parameter! So method hasn't been excuted."
							, method.toString()
							, parameterTypes.length
							, eventData == null ? 0 : eventData.length));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		public void remove(String eventName) {
			map.remove(eventName);
		}

		private void remove(StickyEvent event) {
			map.get(event._getEventName()).remove(event);
			if (map.get(event._getEventName()).size() == 0) {
				map.remove(event._getEventName());
			}
		}

		public boolean hasEvent(StickyEvent event) {
			if (map.containsKey(event._getEventName())) {
				ArrayList<StickyEvent> events = map.get(event._getEventName());
				for (StickyEvent item : events) {
					if (CompareUtil.isSame(item._getEventTarget(), event._getEventTarget()) &&
							CompareUtil.isSame(item._getEventData(), event._getEventData())) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private static Comparator<StickyEvent> comparator = new Comparator<StickyEvent>() {

		@Override
		public int compare(StickyEvent lhs, StickyEvent rhs) {
			if (lhs._getTimestamp() > rhs._getTimestamp()) {
				return 1;
			} else if (lhs._getTimestamp() < rhs._getTimestamp()) {
				return -1;
			} else {
				return 0;
			}
		}
	};
}
