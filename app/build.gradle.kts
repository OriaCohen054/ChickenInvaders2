plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("com.google.gms.google-services")
    id("com.google.gms.google-services") version "4.4.2" apply false
}

android {
    namespace = "com.example.chickeninvaders"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chickeninvaders"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.media)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.play.services.location)
    implementation(libs.firebase.crashlytics.buildtools)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("androidx.core:core-ktx:1.9.0" )
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.fragment:fragment-ktx:1.5.5")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1" )

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")


    // RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.2.1")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    implementation ("com.google.android.gms:play-services-location:4.4.2")
    // Google Play Services – Maps:
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")


    //Glide:
    implementation(libs.glide)

    //Lottie:
    implementation (libs.lottie)

//    implementation ("com.google.firebase:firebase-firestore-ktx")

//    implementation ("com.google.android.gms:play-services-location:21.0.1")
//    implementation ("com.google.firebase:firebase-firestore-ktx:24.5.2")      // אם אתה עובד עם Firestore
//    implementation ("com.google.firebase:firebase-auth-ktx:22.1.0")           // אם אתה משתמש ב־Auth
}
apply(plugin = "com.google.gms.google-services")