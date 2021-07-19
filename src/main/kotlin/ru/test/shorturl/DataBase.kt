package ru.test.shorturl

import org.springframework.beans.factory.annotation.Autowired

//class InMemoryDataBase : DataBaseChecking {
//    @Autowired
//    var keyCreater: KeyCreater? = null
//    fun urlAndKeyCheck(url: String): String {
//        if (inMemoryDataBase.containsValue(url)) {
//            for ((key, value) in inMemoryDataBase) {
//                if (value == url) {
//                    return key
//                }
//            }
//        }
//        var key: String = keyCreater.generateKey()
//        if (inMemoryDataBase.containsKey(key)) {
//            while (inMemoryDataBase.containsKey(key)) {
//                key = keyCreater.generateKey()
//            }
//        }
//        inMemoryDataBase[key] = url
//        return key
//    }
//
//    fun seeDB() {
//        for (entry in inMemoryDataBase.entries) {
//            println(entry)
//        }
//    }
//
//    fun returnUrl(key: String): String? {
//        return inMemoryDataBase[key]
//    }
//
//    companion object {
//        private val inMemoryDataBase: MutableMap<String, String> = HashMap()
//    }
//}
