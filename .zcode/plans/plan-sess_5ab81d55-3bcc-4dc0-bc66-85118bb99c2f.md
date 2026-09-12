## 你选的方案 B：改用本地已缓存的 `volc-sdk-java:1.0.138`

我核实过 Maven Central 上该 artifact 存在（1.0.138 ~ 最新 1.0.284），且你本地 Gradle 缓存里 `volc-sdk-java:1.0.138` 的 jar 已经下载好了，所以这一步不需要联网也能过。

注意：这是**旧版 SDK**，API 风格和代码里现在写的完全不同。要一并把 import 和调用方式全部重写。

---

## 改动清单

### 1. `app/build.gradle.kts` — 换成真实存在的坐标
第 56 行：
```kotlin
// 旧
implementation("com.volcengine:volcengine-java-sdk-translate:0.2.16")
// 新
implementation("com.volcengine:volc-sdk-java:1.0.138")
```

### 2. `app/build.gradle.kts` — 修掉 KSP/Kotlin 版本不匹配
第 5 行 KSP 是 `2.0.21-1.0.27`，但 `libs.versions.toml` 里 `kotlin = "2.0.20"`。KSP 版本必须与 Kotlin 编译器严格一致，否则构建直接失败（和翻译无关，但会挡路）：
```kotlin
id("com.google.devtools.ksp") version "2.0.20-1.0.25"
```

### 3. `PostViewModel.kt` — 修正 import（第 27-30 行）
```kotlin
// 删掉这四行（都不存在）
import com.volcengine.ApiClient
import com.volcengine.sign.Credentials
import com.volcengine.translate.TranslateApi
import com.volcengine.translate.model.TranslateTextRequest

// 换成真实存在的
import com.volcengine.service.translate.ITranslateService
import com.volcengine.service.translate.impl.TranslateServiceImpl
import com.volcengine.model.request.translate.TranslateTextRequest
```

### 4. `PostViewModel.kt` — 重写 `translateText()`（第 798-819 行）
旧版 SDK 是**单例 + setter**，不是 builder 链。真实签名我已用 javap 核实：
```kotlin
private val translateService: ITranslateService by lazy {
    TranslateServiceImpl.getInstance().apply {
        setAccessKey(AK)
        setSecretKey(SK)
        setRegion("cn-north-1")
    }
}

suspend fun translateText(text: String, target: String = "zh"): String = withContext(Dispatchers.IO) {
    try {
        val req = TranslateTextRequest().apply {
            targetLanguage = target
            textList = listOf(text)
        }
        translateService.translateText(req)
            .translationList
            .firstOrNull()?.translation ?: ""
    } catch (e: Exception) {
        Log.e("PostViewModel", "translate failed: ${e.message}")
        ""
    }
}
```
- `TranslateServiceImpl.getInstance()` 是唯一入口，没有公开构造函数。
- 密钥常量集中到一处（建议放 `ApiClient` 里或单独的 Config），**先说明占位符必须替换成真实 AK/SK**，否则鉴权必失败。

### 5. `DetailScreen.kt` — 修掉"挂起函数放进 clickable"的编译错误
第 634、728 行在 `Modifier.clickable(onClick = { ... translateText(...) })` 里调挂起函数，这里不是协程作用域。加一个 `rememberCoroutineScope()`，在 `scope.launch { }` 里调。

### 6. `DetailScreen.kt` — 修掉第 728 行的作用域 bug
嵌套回复里写的是 `textContent.value = ...`，但 `textContent` 是**外层评论**的变量；回复自己用的是 `replyContent`（第 704 行）。现在这行会把译文写进外层评论，而且是从 `forEach` 里改外部可变状态。应改成 `replyContent.value = ...`。

### 7. `AndroidManifest.xml` — 修正 ML Kit 的 model key（第 28 行）
```xml
<!-- 旧：vision 用的 key，language-id 不认 -->
android:name="com.google.mlkit.vision.DEPENDENCIES" android:value="langid"
<!-- 新：language-id 的 key 把 langid 打进包，否则首次识别可能失败/走网络下载 -->
<meta-data android:name="com.google.mlkit.nl.languageid" android:value="true" />
```

---

## 验证
- `./gradlew :app:assembleDebug` 应能通过（第 4 步前 AK/SK 占位符不影响编译）。
- 运行后进详情页，评论里的"翻译"应替换为译文；非中文评论才显示"翻译"入口（`detectLanguage` 返回 true 时）。

## 需要你确认
- **AK/SK**：占位符我不会编造真实值；实现时留成常量并在代码注释里标注替换位置。真实的火山引擎 AK/SK 需要你自己填（或告诉我让我从某处读取）。
- **目标语言**：现在固定目标 `zh`（和原代码一致）。`sourceLanguage` 不传时由服务端自动识别。
- 只修现有代码使其能编译+运行，**不新增**独立翻译页或正文翻译按钮。

如果你还想先把密钥放安全（不硬编码进 APK），这需要另做后端代理，属于额外范围，本次不含。