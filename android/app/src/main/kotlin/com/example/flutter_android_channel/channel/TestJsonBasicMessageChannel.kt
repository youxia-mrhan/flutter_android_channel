package com.example.flutter_android_channel.channel

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryCodec
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.JSONMessageCodec
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.common.StringCodec
import org.json.JSONException
import org.json.JSONObject

/**
 * BasicMessageChannel
 *
 * 使用 JSONObject类型，对应 Flutter端的 Map类型
 */
class TestJsonBasicMessageChannel(messenger: BinaryMessenger) :
    BasicMessageChannel.MessageHandler<Any> {

    private lateinit var mChannel: BasicMessageChannel<Any>

    companion object {
        // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
        private const val CHANNEL_NAME = "flutter.mix.android/json_basic_message_channel"
    }

    init {
        initChannel(messenger)
    }

    /**
     * 初始化消息通道
     */
    private fun initChannel(messenger: BinaryMessenger) {
        // 创建 Android端和Flutter端的，相互通信的通道
        // 通道名称，两端必须一致
        mChannel = BasicMessageChannel(messenger, CHANNEL_NAME, JSONMessageCodec.INSTANCE)

        // 监听来自 Flutter端 的消息通道
        // Flutter端调用了函数，这个handler函数就会被触发
        mChannel.setMessageHandler(this)
    }

    // ========================== PUT 操作 ==========================

    /**
     * 监听来自 Flutter端 的消息通道
     *
     * message： Android端 接收到 Flutter端 发来的 数据对象
     * reply：Android端 给 Flutter端 执行回调的接口对象
     */
    override fun onMessage(message: Any?, reply: BasicMessageChannel.Reply<Any>) {
        // 回调结果对象
        // 获取Flutter端传过来的数据
        val flutterCount = getMap(message.toString())?.get("flutterNum")
        Log.d("TAG", "flutterCount：$flutterCount")

        // 回调状态接口对象，里面只有一个回调方法
        // reply.reply(@Nullable T reply)
        reply.reply(message) // 返回给Flutter端

        Handler(Looper.getMainLooper()).postDelayed({
            androidSendFlutterData()
        }, 5000)
    }

    /**
     * Android端 向 Flutter端 发送数据
     */
    private fun androidSendFlutterData() {
        val map: MutableMap<String, Int> = mutableMapOf<String, Int>()
        map["androidNum"] = getRandomV() // 随机数范围(0-99)

        mChannel.send(map, object : BasicMessageChannel.Reply<Any> {

            override fun reply(reply: Any?) {
                // 获取Flutter端传过来的数据
                Log.d("TAG", "reply：$reply")
            }

        })
    }

    // ========================== GET 操作 ==========================

//    /**
//     * 监听来自 Flutter端 的消息通道
//     *
//     * message： Android端 接收到 Flutter端 发来的 数据对象
//     * reply：Android端 给 Flutter端 执行回调的接口对象
//     */
//    override fun onMessage(message: Any?, reply: BasicMessageChannel.Reply<Any>) {
//        val map: MutableMap<String, Int> = mutableMapOf<String, Int>()
//        map["androidNum"] = getRandomV() // 随机数范围(0-99)
//        reply.reply(map) // 返回给Flutter端
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            androidGetFlutterData()
//        }, 5000)
//    }
//
//    /**
//     * Android端 获取 Flutter端 数据
//     */
//    private fun androidGetFlutterData() {
//        mChannel.send(null, object : BasicMessageChannel.Reply<Any> {
//
//            override fun reply(reply: Any?) {
//                // 获取Flutter端传过来的数据
//                val flutterCount = getMap(reply.toString())?.get("flutterNum")
//                Log.d("TAG", "flutterCount：$flutterCount")
//            }
//
//        })
//    }

    /**
     * 获取随机数
     */
    private fun getRandomV() = (0..100).random()

    /**
     * 解除绑定
     */
    fun closeChannel() {
        mChannel.setMessageHandler(null)
    }

    /**
     * Json 转 Map
     */
    private fun getMap(jsonString: String?): HashMap<String, Any>? {
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(jsonString)
            val keyIter: Iterator<String> = jsonObject.keys()
            var key: String
            var value: Any
            var valueMap = HashMap<String, Any>()
            while (keyIter.hasNext()) {
                key = keyIter.next()
                value = jsonObject[key] as Any
                valueMap[key] = value
            }
            return valueMap
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }
}