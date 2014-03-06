AEvent
======

AEvent is an Android library to handle events using Publish/Subscribe pattern. With AEvent, you can decouple dependencies between views and objects. This is like [EventBus](https://github.com/greenrobot/EventBus) or [Otto](http://square.github.io/otto/) but there's no need to create classes for events in contrast because AEvent distinguishes events by its name(String value). And AEvent also provides sticky events(This will be explained later).

# Download

## Maven

```xml
  <dependency>
    <groupId>net.eunjae.android.aevent</groupId>
    <artifactId>AEvent</artifactId>
    <version>1.0.3</version>
  </dependency>
```

## Gradle

```
  compile 'net.eunjae.android.aevent:AEvent:1.0.3'
```

# Usage

The following is a simple comparison.

* Without AEvent

```java
  class MyActivity extends Activity {
    void onClick() {
      myCustomView.setText("hello world");
    }
  }
  
  class MyCustomView extends View {
    public void setText(String text) {
      myInnerView.setText(text);
    }
  }
  
  class MyInnerView extends View {
    public void setText(String text) {
      textView.setText(text);
    }
  }
```

* With AEvent

```java
  class MyActivity extends Activity {
    void onClick() {
      new Event("SET TEXT").data("hello world").post();
    }
  }
  
  class MyCustomView extends View {
  }
  
  class MyInnerView extends View {
    @AEvent("SET TEXT")
    void setText(String text) {
      textView.setText(text);
    }
  }
```  
See? There's no need to declare recursive functions to pass data or action through hierarchy.

## Normal Event

### Publish
Any object can publish events.
AEvent uses method chaining when creating event object.

The following is the simplest usage. Events are distinguished by its name.

```java
  new Event("event name").post();
```

We can attach event datas as many as you want.

```java
  new Event("event name").data("arg1", 1024, true, ...).post();
```
  
We can specify a certain target class among many subscribers.
Although many objects have subscribed "say it" event, the event will onlybe dispatched to only MyCustomView class.

```java
  new Event("say it").data("hello world").target(MyCustomView.class).post();
```
  
And posting event can have delay.

```java
  new Event("event name").data("hello world").postDelayed(300);   // post event after 300ms delay.
```

Any event gets removed from queue when it gets consumed by a subscriber. So if you want the same events to be consumed by several subscribers, you should create several event objects targeting its each subscriber class.

### Subscribe

Any object can subscribe events but need to register first. In case of activity, it's convenient putting register/unregister codes to BaseActivity.

```java
  class BaseActivity extends Activity {
    
    @Override
    protected void onResume() {
      super.onResume();
      
      AEventManager.getInstance().register(this);
    }
    
    @Override
    protected void onPause() {
      super.onPause();
      
      AEventManager.getInstance().unregister(this);
    }
  }
```

Don't forget to unregister before the subscriber object gets destroyed.

## Sticky Event

AEvent also provides sticky events so that the subscribers can receive events although they weren't created at the time when the event was fired. This pattern can be used effectively when delivering data or action between activities.

### Publish

```java
  class UserInfoEditActivity extends Activity {
    void save() {
      new StickyEvent("Update User Info").data(userObject).post();
    }
  }

  class UserInfoViewActivity extends Activity {
    
    @ASticky("Update User Info")
    void updateUserInfo(UserInfo userInfo) {
      // update stuff..
    }
  }
```

It works fine. You can also specify target class the same way we did with normal events above.
A sticky event is removed from queue when it gets consumed once like the normal events.
When you publish several events with same event name, all of them except the first one will be ignored by default.
If you really want the events to be executed several times on subscriber, you can specify the option like this:

```java
  new StickyEvent("Increase the number").allowDuplicates().post();
```

This option is only available with sticky events.

### Subscribe

Subscribers don't have to register/unregister sticky events. It just requests to fire pending events if exists.

```java

  class BaseActivity extends Activity {
    
    @Override
    protected void onResume() {
      AStickyEventManager.getInstance().firePendingEvents(this);
    }
  }
```

# License
[MIT](http://opensource.org/licenses/mit-license.html)

# Release History

## 1.0.3 (2014/3/6)

* Deploying again due to branch merge issue.

## 1.0.2 (2014/3/4)

* Methods don't have to be public now. (now using "method.setAccessible(true)")

## 1.0.0 (2014/2/18)

* First release.


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/eunjae-lee/aevent/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

