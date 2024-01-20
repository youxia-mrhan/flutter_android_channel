
import 'dart:async';

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

/// EventChannel
/// 使用方式：单向通信
class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  late EventChannel channel;

  // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
  static const String CHANNEL_NAME = 'flutter.mix.android/single_event_channel';

  StreamSubscription? streamSubscription;

  String msgState = "默认"; // 消息传递状态

  @override
  initState() {
    super.initState();
    initChannel();
  }

  /// 初始化消息通道
  initChannel() {
    channel = const EventChannel(CHANNEL_NAME); // 创建 Flutter端和Android端的，相互通信的通道

    // 监听来自 Android端 的消息通道
    // Android端调用了函数，这个handler函数就会被触发
    streamSubscription = channel
        .receiveBroadcastStream()
        .listen(onData, onError: onError, onDone: onDone);
  }

  /// 监听来自 Android端 的消息通道
  /// 这几个函数就会根据情况被触发

  /// 响应数据
  onData(dynamic data) {
    msgState = data;
    setState(() {});
  }

  /// 发生异常
  onError(dynamic error) {
    msgState = error;
    setState(() {});
  }

  /// 流被关闭
  onDone() {
    msgState = "流被关闭";
    setState(() {});
  }

  @override
  void dispose() {
    super.dispose();
    streamSubscription?.cancel();
  }

  @override
  Widget build(BuildContext context) {
    const defaultStyle = TextStyle(
      fontSize: 50,
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
                    child: Text(msgState,
                        textAlign: TextAlign.center, style: defaultStyle)),
              ],
            ),
          ),
        ));
  }
}
