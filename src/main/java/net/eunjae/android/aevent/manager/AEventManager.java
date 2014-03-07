package net.eunjae.android.aevent.manager;

import android.os.Handler;
import android.util.Log;
import net.eunjae.android.aevent.annotation.AEvent;
import net.eunjae.android.aevent.event.Event;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class AEventManager {
	private static volatile AEventManager instance = null;
	private ArrayList<WeakReference<Object>> subscribers;
	private HashMap<Class<?>, ArrayList<Method>> annotatedMethodsByClass = new HashMap<Class<?>, ArrayList<Method>>();
	private Handler handler = new Handler();

	public static AEventManager getInstance() {
		if (instance == null) {
			synchronized (AEventManager.class) {
				if (instance == null) {
					instance = new AEventManager();
				}
			}
		}
		return instance;
	}

	private AEventManager() {
		subscribers = new ArrayList<WeakReference<Object>>();
	}

	public void register(Object subscriber) {
		if (subscriber == null) {
			return;
		}
		for (WeakReference<Object> item : subscribers) {
			if (subscriber.equals(item.get())) {
				return;
			}
		}
		subscribers.add(new WeakReference<Object>(subscriber));
	}

	public void unregister(Object subscriber) {
		if (subscriber == null) {
			return;
		}
		Iterator<WeakReference<Object>> iterator = subscribers.iterator();
		while (iterator.hasNext()) {
			WeakReference<Object> reference = iterator.next();
			if (subscriber.equals(reference.get())) {
				iterator.remove();
			}
		}
	}

	public void post(Event event) {
		ArrayList<WeakReference<Object>> copiedSubscribers = new ArrayList<WeakReference<Object>>(subscribers);
		for (WeakReference<Object> item : copiedSubscribers) {
			Object subscriber = item.get();
			if (subscriber != null) {
				ArrayList<Method> methods = getAnnotatedMethodsRecursively(subscriber.getClass());
				fireEvents(subscriber, event, methods);
			}
		}
	}

	public void postOnUiThread(final Event event) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				post(event);
			}
		});
	}

	private void fireEvents(Object subscriber, Event event, ArrayList<Method> methods) {
		if (event._getEventTarget() != null && !event._getEventTarget().equals(subscriber.getClass())) {
			return;
		}

		ArrayList<Method> methodsToFire = new ArrayList<Method>();
		for (Method method : methods) {
			AEvent annotation = method.getAnnotation(AEvent.class);
			if (annotation == null) {
				continue;
			}
			String eventName = annotation.value();
			if (!event._getEventName().equals(eventName)) {
				continue;
			}
			methodsToFire.add(method);
		}

		Collections.sort(methodsToFire, comparator);

		Object[] eventData = event._getEventData();
		for (Method method : methodsToFire) {
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
//					Log.e("AEVENT", String.format("Method %s requires %d parameters, but you posted %s parameter! So method hasn't been excuted."
//							, method.toString()
//							, parameterTypes.length
//							, eventData == null ? 0 : eventData.length));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
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
			AEvent annotation = method.getAnnotation(AEvent.class);
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

	private static Comparator<Method> comparator = new Comparator<Method>() {
		@Override
		public int compare(Method lhs, Method rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
	};
}
