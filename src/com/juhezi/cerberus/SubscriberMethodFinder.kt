package com.juhezi.cerberus

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

/**
 * Created by qiao1 on 2017/2/24.
 */
internal class SubscriberMethodFinder {

    companion object {
        /**
         * 在新的类文件里，编译器可能添加一些方法，他们被称为 bridge 或 synthetic 方法。
         * 要忽略这些方法
         */
        private val BRIDGE = 0x40
        private val SYNTHETIC = 0x1000
        private val METHOD_CACHE =
                HashMap<Class<*>, MutableList<SubscriberMethod>>()
        private val MODIFIERS_IGNORE = Modifier.ABSTRACT or Modifier.STATIC
        private val POOL_SIZE = 4
    }

    fun findSubscriberMethods(subscriberClass: Class<*>): MutableList<SubscriberMethod> {
        var subscriberMethods = METHOD_CACHE[subscriberClass]
        if (subscriberMethods != null)
            return subscriberMethods
        subscriberMethods = LinkedList<SubscriberMethod>()
        //通过注解找到对应的方法
        var methods = subscriberClass.declaredMethods
        methods.forEach {
            var modifiers = it.modifiers
            //如果方法shipublic并且不是abstract、static、beidge以及synthetic类型的
            //才能调用这些方法
            if ((modifiers and Modifier.PUBLIC) != 0 &&
                    (modifiers and MODIFIERS_IGNORE) == 0) {
                var parameterTypes = it.parameterTypes  //获得该方法的参数类型
                if (parameterTypes.size == 1) {  //只允许这个方法有一个参数，即被发送的消息
                    var subscriberAnnotation = it.getAnnotation(Subscribe::class.java)
                    if (subscriberAnnotation != null) {
                        var eventType = parameterTypes[0]   //获取事件类型
                        //构造一个SubscriberMethod对象
                        var subscriberMethod = SubscriberMethod(it, eventType)
                        //把这个对象放入缓存中
                        subscriberMethods!!.add(subscriberMethod)
                    }
                }
            }
        }
        //添加到缓存中
        METHOD_CACHE.put(subscriberClass, subscriberMethods)
        return subscriberMethods
    }

}