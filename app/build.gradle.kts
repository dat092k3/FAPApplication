plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.fapapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fapapplication"
        minSdk = 36
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.github.bumptech.glide:glide:5.0.5")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Firebase services
    implementation("com.google.firebase:firebase-firestore") // Không cần version vì dùng BOM
    implementation("com.google.firebase:firebase-database") // Nếu bạn dùng Realtime Database

    // Material Design
    implementation("com.google.android.material:material:1.11.0") // Update version mới hơn

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // MPAndroidChart for graphs
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "com.android.support")
    }


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    



}
