# دليل النشر على Google Play — تطبيق صنعة

## 1) إعداد Firebase (إلزامي)
1. ادخل [Firebase Console](https://console.firebase.google.com) → **Add project**.
2. أضف تطبيق أندرويد بالمعرّف: `com.san3a.app`.
3. نزّل `google-services.json` وضعه في `app/google-services.json` (استبدل الملف النموذجي الحالي).
4. من **Authentication → Sign-in method**: فعّل **Phone**.
5. أنشئ قاعدة **Firestore Database** (وضع الإنتاج).
6. أضف بصمات التطبيق في **Project settings → Your apps** (SHA-1 و SHA-256):
   - بصمة الـ Debug:
     ```
     gradlew.bat signingReport
     ```
     أو عبر keytool:
     ```
     keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
     ```
   - بصمة الإصدار (Release): من الـ keystore الخاص بك (انظر القسم 2).
   - ملاحظة: عند تفعيل **Play App Signing** أضف أيضًا بصمة SHA من Play Console.

> بدون إضافة SHA-1، قد يفشل التحقق التلقائي لرقم الجوال (reCAPTCHA).

### قواعد Firestore المقترحة
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      // الجميع (المسجّلون) يقرؤون ملفات الحرفيين
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.auth.uid == uid;
      // صاحب الملف يعدّل كل شيء، أو أي زبون يعدّل حقلي التقييم فقط
      allow update: if request.auth != null && (
        request.auth.uid == uid ||
        request.resource.data.diff(resource.data)
          .affectedKeys().hasOnly(['ratingSum', 'ratingCount'])
      );
      allow delete: if request.auth != null && request.auth.uid == uid;

      match /reviews/{reviewerId} {
        allow read: if request.auth != null;
        allow create, update, delete: if request.auth != null
          && request.auth.uid == reviewerId;
      }
    }
  }
}
```
> قاعدة `hasOnly(['ratingSum','ratingCount'])` تسمح للزبون بتحديث المتوسط فقط
> عبر Transaction، دون السماح له بتعديل بقية بيانات الحرفي.

## 1.b) تفعيل الدخول بحساب Google (الطريقة الموصى بها)
1. Firebase Console → **Authentication** → **Sign-in method** → فعّل **Google**.
2. أضف بصمات **SHA-1 / SHA-256** (Debug وRelease) في Firebase → Project settings →
   Your apps → Add fingerprint. (بدونها يفشل الدخول بGoogle.)
3. احصل على **Web client ID**:
   - Google Cloud Console → **APIs & Services** → **Credentials** →
     بند باسم **Web client (auto created by Google Service)** → انسخ **Client ID**
     (ينتهي بـ `.apps.googleusercontent.com`).
   - أو من `google-services.json` عبر أول عنصر في `oauth_client` يحمل `"client_type": 3`.
4. ضع المعرّف في `app/build.gradle.kts`:
```
defaultConfig {
    buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"1234567890-xxxxxxxx.apps.googleusercontent.com\"")
}
```
5. أعد البناء. زر «المتابعة بحساب Google» سيصبح فعّالًا.
> ملاحظة: يبقى الدخول برقم الجوال متاحًا كخيار ثانوي، ولا يمنع رفضه استخدام
> التطبيق بحساب Google.
> التزم بمعايير Google: استخدم شعار/اسم Google المعتمدين، ولا تضع زر Google تحت
> خانات موافقة مفعّلة تلقائيًا.

### حل مشكلة «زر Google لا يفعل شيئًا»
- تأكد أن **GOOGLE_WEB_CLIENT_ID** ليس فارغًا؛ التطبيق يعرض الآن رسالة خطأ واضحة
  (Snackbar) عند غيابه أو عند أي فشل.
- **SHA-1/SHA-256**: يجب تسجيل بصمة **بناء الـ debug أيضًا** (من مفتاح الـ debug)،
  لا بصمة الإصدار فقط. لأنك تختبر نسخة debug.
  لعرض بصمة الـ debug:
```
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
- أعد تنزيل `google-services.json` **بعد** إضافة البصمة.
- تأكد من تفعيل **Google** في Authentication، ومن ضبط **شاشة الموافقة (OAuth consent screen)**
  في Google Cloud، وإضافة بريدك كـ **Test user** إن كانت الحالة "Testing".
- حدّث **Google Play services** على الجهاز/المحاكي، وسجّل حساب Google فعليًا فيه.
- امتلك اسم الحزمة نفسه: `com.san3a.app` (يجب أن يطابق ما في Firebase).

## 2) مفتاح التوقيع (Upload Key)
> ملاحظة: يوجد keystore نموذجي في `keystore/` للبناء فقط. **أنشئ مفتاحك الخاص** واحفظه بأمان.
```
keytool -genkeypair -v -keystore keystore/san3a-upload.jks ^
  -alias san3a -keyalg RSA -keysize 2048 -validity 10000
```
ثم عدّل `keystore.properties`:
```
storeFile=keystore/san3a-upload.jks
storePassword=كلمة_سر_قوية
keyAlias=san3a
keyPassword=كلمة_سر_قوية
```
> لا ترفع `keystore.properties` ولا ملف `.jks` إلى Git. احتفظ بنسخة احتياطية آمنة؛
> فقدانها يمنعك من تحديث التطبيق مستقبلًا.

## 3) البناء
```
gradlew.bat bundleRelease     # ينتج app/build/outputs/bundle/release/app-release.aab
gradlew.bat assembleRelease   # APK موقّع للاختبار
```

## 4) Google Play Console — خطوة بخطوة
1. **إنشاء التطبيق**: الاسم «صنعة»، اللغة الافتراضية: العربية.
2. **قائمة المتجر (Store listing)**:
   - وصف قصير + وصف كامل.
   - أيقونة التطبيق 512×512، صورة بارزة 1024×500، لقطات شاشة (2 على الأقل).
   - التصنيف: Tools / Business.
3. **App content**:
   - **Privacy policy**: ضع رابطًا عامًا للسياسة (من `docs/PRIVACY_POLICY.md`).
   - **Data safety**: صرّح بأنك تجمع الاسم/الهاتف/واتساب/الحرفة/المنطقة ورمز FCM،
     و**الموقع التقريبي** (لخاصية «قريب مني»)، لأغراض «وظائف التطبيق»، وبيانات
     مشفّرة أثناء النقل، ويمكن للمستخدم طلب الحذف.
   - **Data deletion**: وفّر طريقة لطلب حذف الحساب (بريد دعم).
   - **Content rating**: أجب على الاستبيان (التطبيق غير مخصص للأطفال، 18+).
   - **Target audience**: 18+.
   - **Ads**: لا (إلا إذا أضفت إعلانات).
   - **Government apps / Financial / Health**: لا.
4. **App integrity**: فعّل **Play App Signing** (موصى به).
5. **Release**: أنشئ Release جديدًا في **Production** وارفع ملف `app-release.aab`.
6. **اختبار**: يُفضّل البدء بـ **Internal testing** ثم **Closed → Production**.

## 5) قائمة تحقق قبل الإرسال
- [ ] `google-services.json` الحقيقي مضاف و`appId` صحيح.
- [ ] تمكين **Google** (و**Phone** اختياريًا) وإضافة **SHA-1/SHA-256** (Debug + Release).
- [ ] ضبط **GOOGLE_WEB_CLIENT_ID** في `app/build.gradle.kts`.
- [ ] سياسة خصوصية منشورة ولها رابط عام.
- [ ] استبيان **Data safety** مكتمل ومطابق للبيانات المجمّعة فعلًا (مع الموقع).
- [ ] **Content rating** و**Target audience** محدّدان (18+).
- [ ] أيقونة 512×512 + صورة بارزة 1024×500 + لقطات شاشة (2 على الأقل) — جاهزة في `store-assets/`.
- [ ] `targetSdk = 35` (مطلوب من Google Play للتطبيقات الجديدة).
- [ ] التطبيق موقّع بمفتاحك الخاص (لا بمفتاح debug) في الإصدار النهائي.
- [ ] نسخة احتياطية من `keystore` محفوظة بأمان.
- [ ] اختبار التسجيل والاتصال وواتساب على جهاز حقيقي.

## 6) ملاحظات مهمة
- مصادقة الجوال في Firebase تتطلب حصة SMS؛ راجع **Usage and billing**.
- لأغراض الاختبار، أضف أرقامًا تجريبية في Firebase:
  **Authentication → Sign-in method → Phone → Phone numbers for testing**.
- بعد قبول Play App Signing، استخدم بصمة SHA من Play Console عند الحاجة.
