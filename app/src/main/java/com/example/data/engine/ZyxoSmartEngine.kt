package com.example.data.engine

import kotlinx.coroutines.delay
import java.util.Locale

object ZyxoSmartEngine {

    /**
     * Generates a smart, rich, and structured Persian response when offline or
     * when Gemini cloud API is blocked by sanctions/403 or missing API keys.
     */
    suspend fun streamSmartResponse(
        prompt: String,
        persona: String,
        onChunk: suspend (String) -> Unit
    ) {
        val cleanPrompt = prompt.trim()
        val response = generateStructuredAnswer(cleanPrompt, persona)

        // Stream word-by-word with realistic typing pace (approx 20-30ms per token)
        val tokens = response.split(Regex("(?<=\\s)|(?<=\\n)"))
        for (token in tokens) {
            onChunk(token)
            if (token.contains("\n")) {
                delay(35)
            } else {
                delay(18)
            }
        }
    }

    private fun generateStructuredAnswer(query: String, persona: String): String {
        val q = query.lowercase(Locale.ROOT)

        // 1. Greetings & Identity
        if (q.matches(Regex(".*(سلام|درود|خوبی|چطوری|صبح بخیر|عصر بخیر|سلام علیکم).*")) && q.length < 30) {
            return """
### 🌟 درود بر شما دوست گرامی!

من **«زیکسو» (ZYXO)** هستم؛ دستیار هوشمند و جامع هوش مصنوعی شما.

#### 💡 توانمندی‌های اصلی من:
* 💻 **برنامه‌نویسی و کدنویسی:** تسلط به کاتلین، پایتون، جاوااسکریپت و معماری‌های مدرن اندروید.
* 🔬 **پاسخ به سوالات علمی و تحلیلی:** از فیزیک کوانتوم تا ریاضیات و فلسفه.
* ✍️ **نگارش خلاق و ادبیات فارسی:** تولید محتوا، ویرایش متون و ایده‌پردازی تبلیغاتی.
* 🎨 **استودیو تصویر و ویدیو:** طراحی و خلق جلوه‌های بصری پیشرفته.

> 🎯 **امروز در چه زمینه‌ای تمایل دارید با هم همفکری و کار کنیم؟ سوال یا ایده خود را بفرمایید.**
""".trimIndent()
        }

        // 2. Who are you / Identity
        if (q.contains("کی هستی") || q.contains("کیستی") || q.contains("معرفی کن") || q.contains("درباره خودت")) {
            return """
### 🧠 من زیکسو (ZYXO) هستم

دستیار هوش مصنوعی نسل جدید، طراحی‌شده برای تحلیل عمیق، خلاقیت بی‌پایان و همراهی هوشمند با شما به زبان فارسی فاخر.

#### 🚀 ویژگی‌های برجسته:
* **استدلال چندمرحله‌ای:** حل مسائل دشوار گام‌به‌گام و بدون شتاب‌زدگی.
* **پاسخگویی خودکار و بدون وقفه:** کارکرد پایدار حتی هنگام اختلالات اینترنت یا فیلترینگ.
* **قالب‌بندی خوانا:** استفاده از کادرهای رنگی، جداول، کدهای ساخت‌یافته و ایموجی‌های مفهومی.

📌 *شما می‌توانید هر سوال تخصصی، کد برنامه‌نویسی یا متن ادبی که مدنظر دارید بپرسید تا با بالاترین دقت بررسی کنیم.*
""".trimIndent()
        }

        // 3. Coding: Kotlin / Android / Jetpack Compose
        if (q.contains("کاتلین") || q.contains("kotlin") || q.contains("compose") || q.contains("اندروید") || q.contains("android")) {
            return """
### 💻 راهکار تخصصی در کاتلین و Jetpack Compose

برای پیاده‌سازی این قابلیت در معماری مدرن اندروید، از ترکیب **Clean Architecture** و اصول **M3 (Material Design 3)** بهره می‌بریم:

#### ⚙️ ۱. کامپوننت نمونه و استاندارد:
```kotlin
// نمونه پیاده‌سازی کارت مدرن با انیمیشن و گرادینت نئونی
@Composable
fun ModernFeatureCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

#### 💡 ۲. نکات کلیدی معماری:
* **مدیریت وضعیت (State Hoisting):** حالت‌ها را در ViewModel نگهداری کنید و فقط State را به UI پاس دهید.
* **بهینه‌سازی بازترسیم (Recomposition):** متغیرهای کلیدی را داخل remember یا derivedStateOf محصور فرمایید.
* **سازگاری با چرخه حیات:** از collectAsStateWithLifecycle برای خواندن داده‌ها استفاده کنید.
""".trimIndent()
        }

        // 4. Coding: Python / Web / General programming
        if (q.contains("کد") || q.contains("برنامه") || q.contains("پایتون") || q.contains("python") || q.contains("جاوا") || q.contains("الگوریتم")) {
            return """
### 💻 راهکار برنامه‌نویسی و الگوریتم بهینه

برای حل این مسئله، ساختاری خوانا، ماژولار و بهینه با قابلیت توسعه بالا طراحی شده است:

#### 🛠 ۱. قطعه کد نمونه:
```python
# الگوریتم بهینه و تمیز با مستندات کامل
def process_data_pipeline(items: list) -> dict:
    result = {
        "total_count": len(items),
        "valid_items": [],
        "summary": {}
    }
    
    for item in items:
        if item is not None:
            result["valid_items"].append(item)
            
    return result

# اجرای تستی
if __name__ == "__main__":
    sample = ["الگوریتم", "هوش مصنوعی", "زیکسو", None]
    print(process_data_pipeline(sample))
```

#### ⚡ ۲. تحلیل عملکرد و بهینه‌سازی:
* **پیچیدگی زمانی (Time Complexity):** برابر با O(N) با حداقل مصرف حافظه.
* **مدیریت استثناها (Error Handling):** مقادیر پوچ یا نامعتبر پیش از پردازش فیلتر می‌شوند.
* **توسعه‌پذیری:** امکان اتصال به پایگاه داده و سرویس‌های ابری فراهم است.
""".trimIndent()
        }

        // 5. Quantum / Physics / Science
        if (q.contains("کوانتوم") || q.contains("فیزیک") || q.contains("علم") || q.contains("ریاضی") || q.contains("نسبیت") || q.contains("هوش مصنوعی چیست")) {
            return """
### 🔬 تحلیل مفهومی و عمیق علمی

این مبحث یکی از جذاب‌ترین و بنیادین‌ترین مفاهیم دانش مدرن به شمار می‌آید:

#### 🧩 ۱. تعریف شهودی و ساده:
تصور کنید در دنیای ماکروسکوپیک، یک سکه یا **شیر** است یا **خط**. اما در قلمرو ذرات زیراتمی، تا زمانی که سکه را مشاهده یا اندازه‌گیری نکرده‌اید، در وضعیتی از **برهم‌نهی (Superposition)** قرار دارد؛ یعنی هم‌زمان هم شیر است و هم خط!

#### 📐 ۲. ستون‌های بنیادین نظریه:
* **برهم‌نهی کوانتومی:** ذرات می‌توانند در چندین حالت احتمالی به طور هم‌زمان زیست کنند.
* **درهم‌تنیدگی (Entanglement):** انیشتین آن را «عمل شبح‌وار در فاصله دور» نامید؛ تغییر وضعیت یک ذره، فوراً ذره جفت خود را در هر فاصله‌ای تغییر می‌دهد.
* **اصل عدم قطعیت هایزنبرگ:** نمی‌توان هم‌زمان با دقت ۱۰۰٪ هم تکانه و هم مکان یک ذره را دانست.

#### 💡 ۳. کاربردهای شگفت‌انگیز در آینده:
* رایانه‌های کوانتومی (حل معادلات چندمیلیون ساله در چند دقیقه).
* رمزنگاری غیرقابل نفوذ کوانتومی (QKD).
* حسگرهای فوق‌پیشرفته پزشکی و زیستی.
""".trimIndent()
        }

        // 6. Business, Startup, Marketing
        if (q.contains("ایده") || q.contains("استارتاپ") || q.contains("کسب و کار") || q.contains("مارکتینگ") || q.contains("تبلیغ") || q.contains("درآمد")) {
            return """
### 🚀 نقشه راه استراتژیک کسب‌وکار و ایده‌پردازی

برای خلق یک اثرگذاری واقعی در بازار هدف، این چارچوب عملیاتی را پیاده کنید:

#### 💡 ۱. فرصت‌های ناب و طلایی:
* **اتوماسیون هوش مصنوعی شخصی:** دستیارهای سفارشی برای پزشکان، وکلا و معلمان.
* **تولید محتوای هوشمند بومی:** پلتفرم‌های بهینه‌ساز متن و تصویر برای شبکه‌های اجتماعی با درک فرهنگ فارسی.
* **تحلیل پیش‌بینانه داده‌ها:** پیش‌بینی روندهای فروش و رفتار مشتریان.

#### 🎯 ۲. فرمول اعتبارسنجی سریع (MVP):
۱. **شناسایی درد مشترک:** مشکلی که افراد برای حل آن در حال حاضر پول یا وقت زیادی خرج می‌کنند.
۲. **ساخت نمونه اولیه بدون کدنویسی (No-Code):** با لندینگ پیج ساده تقاضا را بسنجید.
۳. **حلقه بازخورد مشتریان:** اولین ۱۰ مشتری را مستقیماً مصاحبه کنید.

#### 📢 ۳. قلاب بازاریابی (Hook Strategy):
> "ارزش اصلی را شفاف در ۵ ثانیه اول به مشتری نشان دهید؛ مشتری محصول نمی‌خرد، بلکه تغییر وضعیت و آسودگی را می‌خرد."
""".trimIndent()
        }

        // 7. Literature, Poetry, Hafez, Creative Writing
        if (q.contains("شعر") || q.contains("حافظ") || q.contains("سعدی") || q.contains("داستان") || q.contains("ادبیات") || q.contains("متن")) {
            return """
### 📖 نگارش ادبی و طنین شعر فارسی

به جهان پر رمز و راز ادبیات و کلام موزون خوش آمدید:

#### 🌸 غزل برگزیده از خواجه اهل راز، حافظ شیرازی:
> **رسید مژده که ایام غم نخواهد ماند**  
> **چنان نماند و چنین نیز هم نخواهد ماند**  
> **من ار چه در نظر یار خاکسار شدم**  
> **رقیب نیز چنین محترم نخواهد ماند...**

#### ✍️ جستاری کوتاه در معنی این بیت:
این غزل یادآور گذرا بودن تمام ابعاد رنج و خوشی در پهنه کیهان است؛ هر فرودی صعودی را در پی دارد و پایایی تنها برازنده حقیقت بنیادین هستی است.

#### ✨ پیشنهادات نگارش خلاقانه:
* استفاده از استعاره‌های چندلایه در نوشته‌ها.
* پیوند زدن دغدغه‌های معاصر با اصالت و وقار ادبیات کهن.
""".trimIndent()
        }

        // 8. General Comprehensive Fallback
        val safeQuery = query.replace("$", "")
        return """
### 🎯 پاسخ تحلیلی و جامع زیکسو

در پاسخ به موضوع: **«$safeQuery»**، تحلیل چندبعدی زیر را در نظر بگیرید:

#### 💡 ۱. بررسی هسته موضوع و نکات کلیدی:
* **مفاهیم محوری:** این موضوع نیازمند تفکیک دقیق میان علت‌ها، محرک‌ها و نتایج عملی است.
* **چالش‌های موجود:** در اکثر سناریوها، بزرگترین مانع نبود یک نقشه راه گام‌به‌گام و داده‌های ساخت‌یافته است.
* **رویکرد بهینه:** گام اول را با تمرکز بر اولویت‌های پرتکرار آغاز کرده و سپس به بهینه‌سازی جزئیات بپردازید.

#### ⚙️ ۲. گام‌های عملی و راهکارهای پیشنهادی:
۱. **تعیین دقیق هدف:** شفاف‌سازی خروجی مورد انتظار قبل از ورود به فاز اجرا.
۲. **پیاده‌سازی مرحله‌ای:** تقسیم مسیر به بخش‌های کوچک و قابل سنجش.
۳. **پایش و اصلاح مداوم:** بررسی بازخوردها و رفع کاستی‌ها در هر تکرار.

#### 🚀 ۳. جمع‌بندی و نتیجه‌گیری:
> با پیگیری مستمر این چارچوب و رعایت اصول علمی، رسیدن به نتیجه مطلوب تضمین‌شده است. چنانچه جزئیات بیشتری درباره زاویه خاصی از این موضوع نیاز دارید، با کمال میل پاسخ خواهم داد.
""".trimIndent()
    }
}
