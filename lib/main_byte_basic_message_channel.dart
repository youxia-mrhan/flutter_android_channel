
import 'package:just_audio/just_audio.dart';
import 'dart:typed_data';

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
/// 使用ByteData类型，对应 Android端的 ByteBuffer类型
class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  late BasicMessageChannel channel;

  // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
  static const String CHANNEL_NAME = 'flutter.mix.android/byte_basic_message_channel';

  String msgState = "默认"; // 消息传递状态

  @override
  initState() {
    super.initState();
    initChannel();
  }

  /// 初始化消息通道
  initChannel() {
    channel = const BasicMessageChannel(CHANNEL_NAME,BinaryCodec()); // 创建 Flutter端和Android端的，相互通信的通道

    // 监听来自 Android端 的消息通道
    // Android端调用了函数，这个handler函数就会被触发
    channel.setMessageHandler(handler);
  }

  /// 监听来自 Android端 的消息通道
  /// Android端调用了函数，这个handler函数就会被触发
  Future<dynamic> handler(dynamic message) async {
    // PUT
    var data = message as ByteData;
    loadMusic(data);
    msgState = 'Flutter端接收Android端PUT请求成功，音频加载完毕，开始播放';
    setState(() {});
    return ByteData.view(Uint8List(0).buffer); // 返回给Android端

    // GET，这里模拟在Android端播放音乐
    // final data = await rootBundle.load('assets/music/di_jia_a.mp3');
    // loadMusic(data);
    // msgState = 'Flutter端接收Android端GET请求成功，音频加载完毕，开始播放';
    // setState(() {});
    // return data; // 返回给Android端
  }

  /// Flutter端 向 Android端 发送数据，PUT 操作
  flutterSendAndroidData() async {
    final byteData = await rootBundle.load('assets/music/di_jia_a.mp3');

    // Android端调用Reply相关回调函数后，then、catchError 会接收到

    channel.send(byteData).then((value) {
      loadMusic(value);
      msgState = 'Android端接收Flutter端PUT请求成功，音频加载完毕，开始播放 --- 5秒钟后 Android端会向Flutter端发送PUT请求';
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
      loadMusic(value);
      msgState = 'Android端接收Flutter端GET请求成功，音频加载完毕，开始播放 --- 5秒钟后 Android端会向Flutter端发送GET请求';
      setState(() {});
    }).catchError((e) {
      if (e is MissingPluginException) {
        debugPrint('flutterGetAndroidDataNotice --- Error：notImplemented --- 未找到Android端具体实现函数');
      } else {
        debugPrint('flutterGetAndroidDataNotice --- Error：$e');
      }
    });

  }

  final player = AudioPlayer();

  /// 加载音频
  loadMusic(ByteData data) async {
    var buffer = data.buffer;
    var uint8list = buffer.asUint8List(data.offsetInBytes,data.lengthInBytes);
    var audioSource = AudioSource.uri(Uri.dataFromBytes(uint8list));
    await player.setAudioSource(audioSource);
    player.play(); // 播放音乐
  }

  /// 播放或暂停
  palsyOrPause() {
    if(player.playing) {
      player.pause();
    }
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
                Padding(
                  padding: const EdgeInsets.only(bottom: 16),
                  child: ElevatedButton(
                    onPressed: flutterGetAndroidData,
                    child: const Text('Flutter端获取Android端数据'),
                  ),
                ),
                ElevatedButton(
                  onPressed: palsyOrPause,
                  child: Text('暂停'),
                ),
              ],
            ),
          ),
        ));
  }

}
