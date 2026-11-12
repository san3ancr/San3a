# أصول متجر Google Play — تطبيق صنعة

جميع الصور جاهزة واحترافية، وبالقياسات التي يطلبها Google Play بالضبط.
مكانها: `D:\new\san3a\store-assets\`

| الملف | القياس | الاستخدام في Play Console |
|---|---|---|
| `icon-512.png` | 512×512 | أيقونة التطبيق (App icon) — Store listing |
| `feature-1024x500.png` | 1024×500 | الصورة البارزة (Feature graphic) — إلزامية |
| `screenshot-1.png` | 540×960 | لقطة: تسجيل الدخول |
| `screenshot-2.png` | 540×960 | لقطة: البحث وفلترة الحرفيين |
| `screenshot-3.png` | 540×960 | لقطة: ملف الحرفي والمراجعات |
| `screenshot-4.png` | 540×960 | لقطة: تقييم الخدمة |

## أين تُرفع في Play Console
- **أيقونة التطبيق**: Store listing → App icon → ارفع `icon-512.png`.
- **الصورة البارزة**: Store listing → Feature graphic → ارفع `feature-1024x500.png`.
- **لقطات الشاشة**: Store listing → Phone screenshots → ارفع `screenshot-1..4.png` (الحد الأدنى لقطتان).
- **صور المتجر** (اختياري): يمكن استخدام `screenshot-*.png` نفسها في قسم «صور المتجر».

## نفس الصور على الموقع
الصور منشورة أيضًا على موقع التطبيق:
- اللقطات: `https://san3ancr.github.io/San3a/screenshot-1.png` … `-4.png`
- الأيقونة: `https://san3ancr.github.io/San3a/icon-512.png`
- الصورة البارزة: `https://san3ancr.github.io/San3a/feature-1024x500.png`
(تُستخدم أيضًا في `og:image` لمشاركة الرابط.)

## إعادة التوليد / التعديل
مصدر التصميم (HTML + CSS) موجود في `D:\new\san3a\_render\`:
- `base.css` — الأنماط المشتركة (ألوان العلامة، الخطوط، مكوّنات الواجهة).
- `icon.html` — الأيقونة.
- `feature.html` — الصورة البارزة.
- `shot1.html` … `shot4.html` — لقطات الشاشة.

لتعديل أي صورة: عدّل ملف HTML المقابل، ثم شغّل خادمًا محليًا وافتح الصفحة
وصوّر العنصر بقياسه (الأصول مُصمّمة بأبعاد ثابتة تسهّل التصوير بدقة البكسل).

## الملاحظات
- الخط المستخدم: Tajawal (من Google Fonts) مع بديل Tahoma/Segoe UI.
- الألوان: الأخضر `#0F7B6C` / `#0A5749`، الكهرماني `#F5A623`.
- لا حاجة لتعديل أي قياس: كل ملف مطابق لمتطلبات Play.
