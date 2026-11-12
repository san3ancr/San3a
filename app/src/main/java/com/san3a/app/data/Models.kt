package com.san3a.app.data

enum class UserRole(val value: String) {
    CRAFTSMAN("craftsman"),
    CUSTOMER("customer");

    companion object {
        fun from(value: String?): UserRole =
            entries.firstOrNull { it.value == value } ?: CUSTOMER
    }
}

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val role: String = UserRole.CUSTOMER.value,
    val craft: String = "",
    val region: String = "",
    val available: Boolean = true,
    val createdAt: Long = 0L,
    val ratingSum: Long = 0L,
    val ratingCount: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    val isCraftsman: Boolean get() = role == UserRole.CRAFTSMAN.value

    val ratingAvg: Double
        get() = if (ratingCount > 0) ratingSum.toDouble() / ratingCount.toDouble() else 0.0

    val hasLocation: Boolean get() = latitude != 0.0 || longitude != 0.0
}

data class Review(
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L
)

object Crafts {
    val list = listOf(
        "سبّاك",
        "كهربائي",
        "نجّار",
        "حداد",
        "خيّاط",
        "حلاق",
        "ميكانيكي سيارات",
        "دهّان",
        "بنّاء",
        "فني تكييف وتبريد",
        "فني أجهزة كهربائية",
        "تنظيف منازل",
        "نقل أثاث",
        "جبس وديكور",
        "تركيب كاميرات مراقبة",
        "فني كمبيوتر وشبكات"
    )
}

object Regions {
    val list = listOf(
        "الرياض",
        "جدة",
        "مكة المكرمة",
        "المدينة المنورة",
        "الدمام",
        "الخبر",
        "الظهران",
        "الطائف",
        "تبوك",
        "أبها",
        "خميس مشيط",
        "بريدة",
        "عنيزة",
        "حائل",
        "الجبيل",
        "ينبع",
        "نجران",
        "جيزان"
    )
}
