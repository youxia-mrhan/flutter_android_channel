import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

/// BasicMessageChannel + MethodChannel 一起配合使用
class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  late BasicMessageChannel mBasicMessageChannel;
  late MethodChannel mMethodChannel;

  // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
  static const String MIX_BASIC_MESSAGE_CHANNEL_NAME = 'flutter.mix.android/mix_json_basic_message_channel';
  static const String MIX_METHOD_CHANNEL_NAME = 'flutter.mix.android/mix_method_channel';
  static const String ANDROID_SEND_FLUTTER_DATA_NOTICE = 'androidSendFlutterDataNotice'; // Android端 向 Flutter端 发送数据

  String msgState = "默认"; // 消息传递状态

  @override
  initState() {
    super.initState();
    initChannel();
  }

  /// 初始化消息通道
  initChannel() {

    // 创建 Flutter端和Android端的，相互通信的通道
    mBasicMessageChannel = const BasicMessageChannel(MIX_BASIC_MESSAGE_CHANNEL_NAME,JSONMessageCodec());
    mMethodChannel = const MethodChannel(MIX_METHOD_CHANNEL_NAME);

    // 监听来自 Android端 的消息通道
    // Android端调用了函数，这个handler函数就会被触发
    mBasicMessageChannel.setMessageHandler(messageHandler);
    mMethodChannel.setMethodCallHandler(methodHandler);

  }

  /// 监听来自 Android端 的 BasicMessageChannel 消息通道
  /// Android端调用了函数，这个handler函数就会被触发
  Future<dynamic> messageHandler(dynamic message) async {}

  /// 监听来自 Android端 的 MethodChannel 消息通道
  /// Android端调用了函数，这个handler函数就会被触发
  Future<dynamic> methodHandler(MethodCall call) async {
    // 获取调用函数的名称
    final String methodName = call.method;
    switch (methodName) {
      case ANDROID_SEND_FLUTTER_DATA_NOTICE:
        {
          int androidCount = call.arguments['androidNum'];
          msgState = 'Flutter端接收Android端请求成功，数据：$androidCount';
          setState(() {});

          return '$ANDROID_SEND_FLUTTER_DATA_NOTICE ---> success'; // 返回给Android端
        }
      default:
        {
          return PlatformException(code: '-1', message: '未找到Flutter端具体实现函数', details: '具体描述'); // 返回给Android端
        }
    }
  }

  /// Flutter端 向 Android端 发送数据，PUT 操作
  flutterSendAndroidData() {
    var randomV = getRandomV();
    Map<String, int> map = {'flutterNum': randomV};

    // Android端调用Reply相关回调函数后，then、catchError 会接收到

    mBasicMessageChannel.send(map).then((value) {
      var flutterNum = value['flutterNum'];
      msgState = 'Android端接收Flutter端请求成功，数据：$flutterNum ----> 5秒后，Android端会向Flutter端发送请求';
      setState(() {});
    }).catchError((e) {
      if (e is MissingPluginException) {
        debugPrint('flutterSendAndroidDataNotice --- Error：notImplemented --- 未找到Android端具体实现函数');
      } else {
        debugPrint('flutterSendAndroidDataNotice --- Error：$e');
      }
    });

  }

  /// 获取随机数
  int getRandomV() {
    return Random().nextInt(100); // 随机数范围(0-99)
  }

  @override
  Widget build(BuildContext context) {
    const defaultStyle = TextStyle(
        fontSize: 16,
        color: Colors.orangeAccent,
        fontWeight: FontWeight.bold,
    );
    return Scaffold(
        backgroundColor: Colors.blueGrey,
        body: SafeArea(
          top: true,
          child: SizedBox(
            width: MediaQuery.of(context).size.width,
            height: MediaQuery.of(context).size.height,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                SizedBox(
                    width: 300,
                    child: Text(
                      msgState,
                      textAlign: TextAlign.center,
                      style: defaultStyle)
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  child: ElevatedButton(
                    onPressed: flutterSendAndroidData,
                    child: const Text('Flutter端向Android端发送数据'),
                  ),
                )
              ],
            ),
          ),
        ));
  }

}
