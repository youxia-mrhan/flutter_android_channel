package com.example.flutter_android_channel.channel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlin.random.Random

/**
 * MethodChannel
 */
class TestMethodChannel(messenger: BinaryMessenger) : MethodChannel.MethodCallHandler {

    private lateinit var mChannel: MethodChannel

    companion object {
        // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
        private const val CHANNEL_NAME = "flutter.mix.android/method_channel"
        private const val ANDROID_SEND_FLUTTER_DATA_NOTICE: String = "androidSendFlutterDataNotice" // Android端 向 Flutter端 发送数据
        private const val ANDROID_GET_FLUTTER_DATA_NOTICE: String = "androidGetFlutterDataNotice" // Android端 获取 Flutter端 数据
        private const val FLUTTER_SEND_ANDROID_DATA_NOTICE: String = "flutterSendAndroidDataNotice" // Flutter端 向 Android端 发送数据
        private const val FLUTTER_GET_ANDROID_DATA_NOTICE: String = "flutterGetAndroidDataNotice" // Flutter端 获取 Android端 数据
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
        mChannel = MethodChannel(messenger, CHANNEL_NAME)

        // 监听来自 Flutter端 的消息通道
        // Flutter端调用了函数，这个handler函数就会被触发
        mChannel.setMethodCallHandler(this)
    }

    /**
     * 监听来自 Flutter端 的消息通道
     *
     * call： Android端 接收到 Flutter端 发来的 数据对象
     * result：Android端 给 Flutter端 执行回调的接口对象
     */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        // 获取调用函数的名称
        val methodName: String = call.method
        when (methodName) {
            FLUTTER_SEND_ANDROID_DATA_NOTICE -> {
                // 回调结果对象
                // 获取Flutter端传过来的数据
                val flutterCount: Int? = call.argument<Int>("flutterNum")
                result.success("Android端接收Flutter端PUT请求成功，数据：$flutterCount ----> 5秒后，Android端会向Flutter端发送PUT请求")

                Handler(Looper.getMainLooper()).postDelayed({
                    androidSendFlutterData()
                }, 5000)

                // 回调状态接口对象，里面有三个回调方法，都可以给Flutter端返回消息
                // result.success(result: Any?)
                // result.error(errorCode: String, errorMessage: String?, errorDetails: Any?)
                // result.notImplemented()
            }

            FLUTTER_GET_ANDROID_DATA_NOTICE -> {
                result.success("Android端接收Flutter端GET请求成功，返回数据：${getRandomV()} ----> 5秒后，Android端会向Flutter端发送GET请求")

                Handler(Looper.getMainLooper()).postDelayed({
                    androidGetFlutterData()
                }, 5000)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    /**
     * Android端 向 Flutter端 发送数据，相当于 PUT 操作
     */
    private fun androidSendFlutterData() {
        val map: MutableMap<String, Int> = mutableMapOf<String, Int>()
        map["androidNum"] = getRandomV() // 随机数范围(0-99)

        mChannel.invokeMethod(ANDROID_SEND_FLUTTER_DATA_NOTICE, map, object : MethodChannel.Result {
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
     * Android端 获取 Flutter端 数据，相当于 GET 操作
     */
    private fun androidGetFlutterData() {
        // 说一个坑，不传参数可以写null，
        // 但不能这样写，目前它没有这个重载方法，invokeMethod第二个参数是Object类型，所以编译器不会提示错误
        // mChannel.invokeMethod(ANDROID_GET_FLUTTER_DATA_NOTICE, object : MethodChannel.Result {

        // public void invokeMethod(@NonNull String method, @Nullable Object arguments)

        mChannel.invokeMethod(ANDROID_GET_FLUTTER_DATA_NOTICE, null, object : MethodChannel.Result {
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
     * 获取随机数
     */
    private fun getRandomV() = (0..100).random()

}