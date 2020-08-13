# HBluetooth
Android蓝牙工具，可以更便捷的操作蓝牙

## 快速开始
### 1.添加依赖
  ```sh
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	} 
  ```
  
  ```sh
  dependencies {
        implementation 'com.github.lianghuihui-plus:HBluetooth:v1.0.2'
  }
  ```
### 2.配置HBApplication
您需要在清单文件中配置HBApplication
```sh
  <manifest>
      <application
          android:name="com.lhh.hbluetooth.HBApplication"
          ...
      >
          ...
      </application>
  </manifest>
```
您也可以使用自定义的Application文件
```sh
  <manifest>
      <application
          android:name="com.example.MyOwnApplication"
          ...
      >
          ...
      </application>
  </manifest>
```
这样您就需要在自定义的Application文件中做如下配置
```sh
  public class MyOwnApplication extends Application {

      @Override
      public void onCreate() {
          super.onCreate();
          HBUtil.initialize(this);
      }
      ...
  }
```
## 如何使用
### 1.初始化
在使用之前，您需要先初始化
```sh
    HBUtil.getInstance().init(new HBUtil.HBInitCallback() {
        @Override
        public void onSuccess() {
            if (!HBUtil.getInstance().isAdapterEnabled()) {
                HBUtil.getInstance().requestEnableAdapter(MainActivity.this);
            } else {
                ...
            }
        }

        @Override
        public void onError(int code) {
            ...
        }
    });
```
处理申请打开蓝牙的结果
```sh
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (requestCode) {
          case HBConstant.REQ_CODE_ACTION_REQUEST_ENABLE:
              if (resultCode != RESULT_OK) {
                  // 用户拒绝了请求
              } else {
                  // 用户同意了请求
              }
              break;
      }
  }
```
### 2.连接设备
通过调用HBUtil.getInstance().connectDevice()方法连接目标设备，该方法需要三个参数
1.目标设备的蓝牙地址
2.UUID
3.连接回调
在设备成功连接后，您应该向该连接注册一个观察者，以便处理该连接的事务
```sh
  HBUtil.getInstance().connectDevice(
          DeviceAddress,
          YourUUID,
          new HBConnectThread.HBConnectCallback() {
              @Override
              public void onConnected(HBConnection connection) {
                  //您应该向该连接注册一个观察者
                  connection.registerListener(
                          Tag,  //观察者标签，建议直接使用当前类名
                          new HBConnectionListener() {
                              @Override
                              public void onDisconnected(String address) {
                                ...
                              }

                              @Override
                              public void onRead(byte[] cache) {
                                 ...
                              }

                              @Override
                              public void onError(int code) {
                                ...
                              }
                          }
                  );
                  //您需要手动调用该方法让连接实例开始接收数据
                  connection.startRead();
              }

              @Override
              public void onError(int code) {
              
                ...
              }
          }
  );
```
您可以在回调中保存连接实例，也可以在需要的时候调用HBUtil.getInstance.getConnection()方法获取连接实例，该方法接收蓝牙地址作为参数。
您可以对一个连接实例注册多个观察者，但在不需要的时候请及时卸载观察者。
您也可以通过调用HBUtil.getInstance().unregisterAll()方法一次卸载所有连接实例中的所有同名观察者，如果您使用类名注册观察者，在销毁页面的时候可以调用该方法卸载在该页面注册的所有观察者。
```sh
  @Override
  protected void onDestroy() {
      super.onDestroy();
      HBUtil.getInstance().unregisterAll(MainActivity.class.getName());
      
      ...
  }
```
### 3.等待设备的连接
首先您需要先让设备处于可被发现的状态，这是一个向用户询问的操作
```sh
  HBUtil.getInstance().requestDiscoverable(this, 120);
```
您需要在Activity中对结果进行处理
```sh
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (requestCode) {
          case HBConstant.REQ_CODE_ACTION_REQUEST_ENABLE:
              if (resultCode != RESULT_OK) {
                  // 用户拒绝了请求
              } else {
                  // 用户同意了请求
              }
              break;
          case HBConstant.REQ_CODE_ACTION_REQUEST_DISCOVERABLE:
              if (resultCode == RESULT_CANCELED) {
                  // 用户拒绝了请求
              }
              break;
      }
  }
```
然后您就可以开始监听设备的连接了
```sh
  HBUtil.getInstance().startAccept(
          YourServerName,
          YourUUID,
          new HBAcceptThread.AcceptCallback() {
              @Override
              public void onClientConnected(HBConnection connection) {
                  //如果你的设备不需要被多个设备连接，就应该在连接成功后停止监听
                  HBUtil.getInstance().cancelAccept();
                  //您同样需要对该连接注册观察者
                  connection.registerListener(
                          Tag,
                          new HBConnectionListener() {
                              @Override
                              public void onDisconnected(String address) {
                                  
                                  ...
                              }

                              @Override
                              public void onRead(byte[] cache) {

                                  ...
                              }

                              @Override
                              public void onError(int code) {
                                  
                                  ...
                              }
                          }
                  );
              }

              @Override
              public void onFailed(int code) {
                  
                  ...
              }
          }
  );
```
### 4.断开连接
您可以主动断开连接
```sh
  HBUtil.getInstance().disconnectDevice(DeviceAddress);
```
如果你持有连接实例，也可以直接断开该连接，连接将通知所有观察者连接已断开，然后卸载所有观察者
```sh
  connection.die();
```
如果是目标设备断开连接，只要您注册了观察者，就会在一段时间后收到连接断开的通知
```sh
  connection.registerListener(
          Tag,
          new HBConnectionListener() {
              @Override
              public void onDisconnected(String address) {
                  //连接已断开
                  ...
              }

              @Override
              public void onRead(byte[] cache) {

                  ...
              }

              @Override
              public void onError(int code) {

                  ...
              }
          }
  );
```
### 5.读写操作
您可以通过注册观察者来获取收到的数据，在此之前您需要先让连接实例开始接收数据
```sh
  connection.startRead();
```
您仍需要通过连接实例来发送数据
```sh
  connection.write(bytes);
```
### 6.发现问题
如果您在使用过程中发现任何问题，可以留言，或者发送邮件到我的邮箱979180936@qq.com
