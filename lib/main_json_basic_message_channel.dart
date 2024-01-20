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

/// BasicMessageChannel
/// 使用Map类型，对应 Android端的 JSONObject类型
class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  late BasicMessageChannel channel;

  // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
  static const String CHANNEL_NAME = 'flutter.mix.android/json_basic_message_channel';

  String msgState = "默认"; // 消息传递状态

  @override
  initState() {
    super.initState();
    initChannel();
  }

  /// 初始化消息通道
  initChannel() {
    channel = const BasicMessageChannel(CHANNEL_NAME,JSONMessageCodec()); // 创建 Flutter端和Android端的，相互通信的通道

    // 监听来自 Android端 的消息通道
    // Android端调用了函数，这个handler函数就会被触发
    channel.setMessageHandler(handler);
  }

  /// 监听来自 Android端 的消息通道
  /// Android端调用了函数，这个handler函数就会被触发
  Future<dynamic> handler(dynamic message) async {
    // PUT
    var androidCount = message['androidNum'];
    msgState = 'Flutter端接收Android端PUT请求成功，数据：$androidCount';
    setState(() {});
    return 0; // 返回给Android端

    // GET，这里模拟在Android端显示
    // var randomV = getRandomV();
    // Map<String, int> map = {'flutterNum': randomV};
    // msgState = 'Flutter端接收Android端GET请求成功：$randomV';
    // setState(() {});
    // return map; // 返回给Android端
  }

  /// Flutter端 向 Android端 发送数据，PUT 操作
  flutterSendAndroidData() {
    var randomV = getRandomV();
    Map<String, int> map = {'flutterNum': randomV};

    // Android端调用Reply相关回调函数后，then、catchError 会接收到

    channel.send(map).then((value) {
      var flutterNum = value['flutterNum'];
      msgState = 'Android端接收Flutter端PUT请求成功，数据：$flutterNum ----> 5秒后，Android端会向Flutter端发送PUT请求';
      setState(() {});
    }).catchError((e) {
      if (e is MissingPluginException) {
        debugPrint('flutterSendAndroidDataNotice --- Error：notImplemented --- 未找到Android端具体实现函数');
      } else {
        debugPrint('flutterSendAndroidDataNotice --- Error：$e');
      }
    });

  }

  ///  Flutter端 获取 Android端 数据，GET 操作
  flutterGetAndroidData() {

    // Android端调用Reply相关回调函数后，then、catchError 会接收到

    channel.send(null).then((value) {
      var androidCount = value['androidNum'];
      msgState = 'Android端接收Flutter端GET请求成功，数据：$androidCount ----> 5秒后，Android端会向Flutter端发送GET请求';
      setState(() {});
    }).catchError((e) {
      if (e is MissingPluginException) {
        debugPrint('flutterGetAndroidDataNotice --- Error：notImplemented --- 未找到Android端具体实现函数');
      } else {
        debugPrint('flutterGetAndroidDataNotice --- Error：$e');
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
