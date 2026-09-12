## Fix duplicate `mozilla/public-suffix-list.txt` packaging error

### Root cause
The build fails with "2 files found with path 'mozilla/public-suffix-list.txt'" because two dependencies both ship an identical copy of that resource:
- `com.google.firebase:firebase-crashlytics-buildtools:3.0.8` (declared directly in the app)
- `org.apache.httpcomponents:httpclient:4.5.14` (pulled in transitively — OkHttp's `HttpUrl` uses the IANA public suffix list for domain parsing)

Gradle's resource merger rejects the duplicate because `mozilla/public-suffix-list.txt` is not currently whitelisted in the packaging config.

### Change (single file: `app/build.gradle.kts`)
In the existing `packaging { resources { ... } }` block (lines 42–50), add a `resources.excludes` entry for `mozilla/public-suffix-list.txt`.

Targeted edit:
```kotlin
packaging {
    resources {
        excludes += "mozilla/public-suffix-list.txt"
        pickFirsts += listOf(
            "META-INF/INDEX.LIST",
            "META-INF/io.netty.versions.properties",
            "META-INF/DEPENDENCIES"
        )
    }
}
```

### Why `excludes` (not `pickFirsts`)
The two copies are byte-identical, and nothing in the app reads this file at runtime (it's only used by OkHttp when the CJVM queries public suffixes, and even then the library loads its own bundled copy internally). Excluding it is cleaner than picking one arbitrarily and keeps the output lean.

### Verification
Run a Gradle assemble/debug build to confirm the duplicate-file error is gone:
`./gradlew :app:assembleDebug` (or the build command you normally use).

### Out of scope
No dependency changes, no model/viewmodel changes. This is a pure build-config fix.