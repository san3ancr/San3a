# صنعة — San3a

تطبيق أندرويد (Kotlin + Jetpack Compose + Firebase) لتسجيل أصحاب الحرف ومقدّمي الخدمات،
ليتواصل معهم الزبائن مباشرة عبر **الاتصال الهاتفي** و**واتساب**.

التسجيل بسيط في البداية: **الاسم + رقم الهاتف + رقم واتساب** (بدون صور).

## المزايا
- تسجيل دخول سهل بحساب **Google** عبر **Credential Manager** (الطريقة الحديثة)،
  مع خيار ثانوي برقم الجوال + OTP.
- اختيار نوع الحساب: حرفي / زبون.
- الحرفي: الاسم، واتساب، الحرفة، منطقة العمل، وحالة (متاح/مشغول).
- الرئيسية: بحث + فلترة بالحرفة والمنطقة + ترتيب حسب الأعلى تقييمًا + بطاقات الحرفيين.
- **قريب مني**: زر لجلب موقع المستخدم وترتيب الحرفيين حسب المسافة.
- صفحة الحرفي: اتصال مباشر + محادثة واتساب + **تقييم ومراجعات** (نجوم 1-5 + تعليق).
- **متوسط التقييم** يظهر على البطاقة وفي الملف والحساب.
- حسابي: تعديل حالة الاستقبال + تسجيل الخروج.
- إشعارات FCM (بنية جاهزة).

## التقنيات
- Kotlin + Jetpack Compose (Material 3)
- Firebase Auth / Firestore / Messaging
- Navigation Compose
- Gradle 8.13، AGP 8.13، Kotlin 2.2
- compileSdk 36 / targetSdk 35 / minSdk 24

## هيكل المشروع
```
san3a/
├─ app/
│  ├─ google-services.json          ← استبدله بملف مشروعك من Firebase
│  ├─ build.gradle.kts
│  └─ src/main/
│     ├─ AndroidManifest.xml
│     ├─ java/com/san3a/app/
│     │  ├─ MainActivity.kt / San3aApp.kt
│     │  ├─ data/        (Models, AuthRepository, UserRepository)
│     │  ├─ ui/          (AppRoot, ContactActions, WhatsAppIcon, theme/, screens/, viewmodel/)
│     │  └─ notifications/ (San3aMessagingService)
│     └─ res/            (أيقونة، ألوان، ثيم عربي RTL)
├─ gradle/libs.versions.toml
├─ keystore.properties               ← بيانات التوقيع (لا ترفعه!)
└─ docs/ (سياسة الخصوصية ودليل النشر)
```

## التشغيل
1. استبدل `app/google-services.json` بملفك من Firebase Console.
2. في Firebase: فعّل **Google** (و**Phone** اختياريًا) في Authentication، وأنشئ قاعدة **Firestore**.
3. أضف بصمة SHA-1/SHA-256 (Debug وRelease) في إعدادات المشروع.
4. اضبط معرّف عميل Google في `app/build.gradle.kts`:
   `buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"...apps.googleusercontent.com\"")`
   (التفاصيل في `docs/PLAY_STORE_GUIDE.md`).
5. البناء:
```
gradlew.bat assembleDebug      # APK للتجربة
gradlew.bat bundleRelease      # AAB للنشر على Google Play
gradlew.bat assembleRelease    # APK موقّع
```

## قواعد Firestore المقترحة
انظر `docs/PLAY_STORE_GUIDE.md` لنسخة القواعد المقترحة (كل مستخدم يقرأ/يكتب ملفه،
والجميع يقرأ ملفات الحرفيين).

## النشر
اتبع `docs/PLAY_STORE_GUIDE.md`، وسياسة الخصوصية في `docs/PRIVACY_POLICY.md`.
