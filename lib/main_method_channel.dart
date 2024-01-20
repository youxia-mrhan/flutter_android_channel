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

/// MethodChannel
class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  late MethodChannel channel;

  // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
  static const String CHANNEL_NAME = 'flutter.mix.android/method_channel';
  static const String FLUTTER_SEND_ANDROID_DATA_NOTICE = 'flutterSendAndroidDataNotice'; // Flutter端 向 Android端 发送数据
  static const String FLUTTER_GET_ANDROID_DATA_NOTICE = 'flutterGetAndroidDataNotice'; // Flutter端 获取 Android端 数据
  static const String ANDROID_SEND_FLUTTER_DATA_NOTICE = 'androidSendFlutterDataNotice'; // Android端 向 Flutter端 发送数据
  static const String ANDROID_GET_FLUTTER_DATA_NOTICE = 'androidGetFlutterDataNotice'; // Android端 获取 Flutter端 数据

  String msgState = "默认"; // 消息传递状态

  @override
  initState() {
    super.initState();
    initChannel();
  }

  /// 初始化消息通道
  initChannel() {
    channel = const MethodChannel(CHANNEL_NAME); // 创建 Flutter端和Android端的，相互通信的通道

    // 监听来自 Android端 的消息通道
    // Android端调用了函数，这个handler函数就会被触发
    channel.setMethodCallHandler(handler);
  }

  /// 监听来自 Android端 的消息通道
  /// Android端调用了函数，这个handler函数就会被触发
  Future<dynamic> handler(MethodCall call) async {
    // 获取调用函数的名称
    final String methodName = call.method;
    switch (methodName) {
      case ANDROID_SEND_FLUTTER_DATA_NOTICE:
        {
          int androidCount = call.arguments['androidNum'];
          msgState = 'Flutter端接收Android端PUT请求成功，数据：$androidCount';
          setState(() {});

          return '$ANDROID_SEND_FLUTTER_DATA_NOTICE ---> success'; // 返回给Android端
        }
      case ANDROID_GET_FLUTTER_DATA_NOTICE:
      {
        msgState = 'Flutter端接收Android端GET请求成功，返回数据：${getRandomV()}';
        setState(() {});

        return '$ANDROID_GET_FLUTTER_DATA_NOTICE ---> success：${getRandomV()}'; // 返回给Android端
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

    // Android端调用Result相关回调函数后，then、catchError 会接收到

    channel.invokeMethod(FLUTTER_SEND_ANDROID_DATA_NOTICE, map).then((value) {
      msgState = value;
      setState(() {});
    }).catchError((e) {
      if (e is MissingPluginException) {
        debugPrint('$FLUTTER_SEND_ANDROID_DATA_NOTICE --- Error：notImplemented --- 未找到Android端具体实现函数');
      } else {
        debugPrint('$FLUTTER_SEND_ANDROID_DATA_NOTICE --- Error：$e');
      }
    });

  }

  ///  Flutter端 获取 Android端 数据，GET 操作
  flutterGetAndroidData() {

    // Android端调用Result相关回调函数后，then、catchError 会接收到

    channel.invokeMethod(FLUTTER_GET_ANDROID_DATA_NOTICE).then((value) {
      msgState = value;
      setState(() {});
    }).catchError((e) {
      if (e is MissingPluginException) {
        debugPrint('$FLUTTER_GET_ANDROID_DATA_NOTICE --- Error：notImplemented --- 未找到Android端具体实现函数');
      } else {
        debugPrint('$FLUTTER_GET_ANDROID_DATA_NOTICE --- Error：$e');
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
                ),
                ElevatedButton(
                  onPressed: flutterGetAndroidData,
                  child: const Text('Flutter端获取Android端数据'),
                ),
              ],
            ),
          ),
        ));
  }

}
