package ru.netology.tablead.model

data class Ad(
    val country: String? = null,
    val city: String? = null,
    val index: String? = null,
    val telephone: String? = null,
    val withSend: Boolean = false,
    val category: String? = null,
    val title: String? = null,
    val price: String? = null,
    val description: String? = null,
    val email: String? = null,
    val mainImage: String? = null,
    val image2: String? = null,
    val image3: String? = null,
    val key: String? = null,
    val uid: String? = null,
    val time: String = "0",
    var favCounter: String = "0",
    var isFav: Boolean = false,

    var viewCounter: String = "0",
    var emailsCounter: String = "0",
    var callsCounter: String = "0",
): java.io.Serializable
