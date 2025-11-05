plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tasks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.todolist"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Android基础库
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.fragment:fragment:1.6.1")
    
    // Material Design组件
    implementation("com.google.android.material:material:1.10.0")
    
    // 约束布局
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // 设置页面
    implementation("androidx.preference:preference:1.2.1")
    
    // 生命周期组件
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    
    // Room数据库 (Java版本)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // JSON解析
    implementation("com.google.code.gson:gson:2.10.1")
    
    // 网络请求
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}