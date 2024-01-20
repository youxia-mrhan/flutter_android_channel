package com.example.flutter_android_channel.channel

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.JSONMessageCodec
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONException
import org.json.JSONObject

/**
 * BasicMessageChannel + MethodChannel 一起配合使用
 *
 */
class TestMixUseChannel(messenger: BinaryMessenger) : BasicMessageChannel.MessageHandler<Any>,MethodChannel.MethodCallHandler {

    private lateinit var mBasicMessageChannel: BasicMessageChannel<Any>
    private lateinit var mMethodChannel: MethodChannel

    companion object {
        // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
        private const val MIX_BASIC_MESSAGE_CHANNEL_NAME = "flutter.mix.android/mix_json_basic_message_channel"
        private const val MIX_METHOD_CHANNEL_NAME = "flutter.mix.android/mix_method_channel"
        private const val ANDROID_SEND_FLUTTER_DATA_NOTICE: String = "androidSendFlutterDataNotice" // Android端 向 Flutter端 发送数据
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
        mBasicMessageChannel = BasicMessageChannel(messenger, MIX_BASIC_MESSAGE_CHANNEL_NAME, JSONMessageCodec.INSTANCE)
        mMethodChannel = MethodChannel(messenger, MIX_METHOD_CHANNEL_NAME)

        // 监听来自 Flutter端 的消息通道
        // Flutter端调用了函数，这个handler函数就会被触发
        mBasicMessageChannel.setMessageHandler(this)
        mMethodChannel.setMethodCallHandler(this)

    }

    /**
     * 监听来自 Flutter端 的 BasicMessageChannel 消息通道
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
     * 监听来自 Flutter端 的 MethodChannel 消息通道
     *
     * call： Android端 接收到 Flutter端 发来的 数据对象
     * result：Android端 给 Flutter端 执行回调的接口对象
     */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {}

    /**
     * Android端 向 Flutter端 发送数据，相当于 PUT 操作
     */
    private fun androidSendFlutterData() {
        val map: MutableMap<String, Int> = mutableMapOf<String, Int>()
        map["androidNum"] = getRandomV() // 随机数范围(0-99)

        mMethodChannel.invokeMethod(ANDROID_SEND_FLUTTER_DATA_NOTICE, map, object : MethodChannel.Result {
            override fun success(result: Any?) {
                Log.d("TAG", "$result")
            }

            override fun error(
                errorCode: String,
                errorMessage: String?,
                errorDetails: Any?
            ) {
                Log.d(
                    "TAG", "errorCode：$errorCode --- errorMessage：$errorMessage --- errorDetails：$errorDetails"
                )
            }

            /**
             * Flutter端 未实现 Android端 定义的接口方法
             */
            override fun notImplemented() {
                Log.d("TAG", "notImplemented")
            }
        })
    }

    /**
     * 解除绑定
     */
    fun closeChannel() {
        mBasicMessageChannel.setMessageHandler(null)
        mMethodChannel.setMethodCallHandler(null)
    }

    /**
     * 获取随机数
     */
    private fun getRandomV() = (0..100).random()


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