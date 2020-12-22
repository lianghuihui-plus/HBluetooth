# HBluetooth
Android蓝牙工具，仅封装蓝牙连接相关操作，需用户自行完成蓝牙适配器基础操作部分，如打开蓝牙，搜索设备等等。

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
        implementation 'com.github.lianghuihui-plus:HBluetooth:v2.0.1'
  }
  ```
## 如何使用
### 1.初始化
在使用之前，您需要先初始化
```sh
    HBluetooth.getInstance().init();
```
### 2.连接设备
通过调用HBluetooth.getInstance().connect()方法连接目标设备，该方法需要三个参数
1.BluetoothDevice对象
2.UUID
3.连接回调
在设备成功连接后，您应该向该连接注册一个观察者，以便处理该连接的事务
```sh
	HBluetooth.getInstance().connect(
		bluetoothDevice,
		YOUR_UUID,
		new ConnectDeviceCallback() {
		    @Override
		    public void onConnected(HBConnection connection) {
			// 成功连接后将返回HBlueooth连接实例
			// 通过该实例进行读写操作
			...
		    }

		    @Override
		    public void onError(Exception e) {
			...
		    }
		}
	);
```
您可以在回调中保存连接实例，也可以在需要的时候调用HBluetooth.getInstance.getConnection()方法获取连接实例，该方法接收蓝牙地址作为参数。
您可以对一个连接实例注册多个观察者，但在不需要的时候请及时卸载观察者。
您也可以通过调用HBluetooth.getInstance().unregisterAll()方法一次卸载所有连接实例中的所有同名观察者，如果您使用类名注册观察者，在销毁页面的时候可以调用该方法卸载在该页面注册的所有观察者。
```sh
  @Override
  protected void onDestroy() {
      super.onDestroy();
      HBluetooth.getInstance().unregisterAll(getClass.getName());
      
      ...
  }
```
### 3.等待设备的连接
首先您需要先让设备处于可被发现的状态，然后使用如下代码开始监听设备的连接了
```sh
	HBluetooth.getInstance().startAccept(
		bluetoothAdapter,
		"serverName",
		YOUR_UUID,
		acceptListener
	);
	
	...
	private HBAcceptDeviceListener acceptListener = new HBAcceptDeviceListener() {
		@Override
		public void onAccepted(HBConnection connection) {
			// 有设备成功连接同样返回连接实例
		}

		@Override
		public void onError(Exception e) {

		}
    	};
```
### 4.断开连接
您可以主动断开连接
```sh
  HBUtil.getInstance().disconnectDevice(DeviceAddress);
```
如果你持有连接实例，可以通过调用disconnect()方法断开该连接，连接将通知所有观察者连接已断开，然后卸载所有观察者。因此如果当前对象在该连接上注册了监听，建议先取消注册再断开连接，否则在断开连接后，当前运行对象将会受到该连接断开的通知，一般来说这是没有必要的。
```sh
	connection.unregister(listener);
  	connection.disconnect();
```
如果是目标设备断开连接，只要您注册了观察者，就会在连接断开后收到通知
```sh
    connection.registerListener(listener);
    ...

    private HBConnectionListener listener = new HBConnectionListener() {
        @Override
        public void onRead(byte[] bytes) {
            
        }

        @Override
        public void onDisconnected(String address) {

        }

        @Override
        public void onError(Exception e) {

        }
    };
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
## 发现问题
如果您在使用过程中发现任何问题，可以留言，或者发送邮件到我的邮箱979180936@qq.com
