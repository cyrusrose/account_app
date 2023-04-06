# Marketplace App

A sample view-based project for handling transactions.

<p align="center">
  <img src="/images/chart.png"/>
</p>

Account App is a project that shows how to process transactions. 
This project uses API provided by **[Account Server](https://github.com/cyrusrose/account_app_server.git)** 
and configured to be launched in emulator for Android 10 and above.

In this branch you'll find:
* User Interface built with **Views** and **[Data Binding](https://developer.android.com/topic/libraries/data-binding)**.
* A single-activity architecture with **Navigation** API used to navigate between fragments 
  and **[Safe Args](https://developer.android.com/guide/navigation/navigation-pass-data)**
  to share fragments' data in a safe way.
* Dependency injection with **[Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android)**.
* Intents' reply processing using **[Result](https://developer.android.com/training/basics/intents/result)** API callbacks.
* **MVVM** Pattern with **View**, **[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)**.
* Modal **[BottomSheets](https://github.com/material-components/material-components-android/blob/master/docs/components/BottomSheet.md)** 
  to interact with a user.
* Reactive UIs using **[Flow](https://developer.android.com/kotlin/flow)** and **[Coroutine](https://kotlinlang.org/docs/coroutines-overview.html)** 
  components for asynchronous operations.
* A **data layer** with repositories and **Retrofit** client.
* Design based on **Material 3** and systemized with the help 
  of **[design tokens](https://m3.material.io/foundations/design-tokens/overview)**.
* Selection tracking implementing `RadioButton` behavior with **[SelectionTracker](https://developer.android.com/reference/kotlin/androidx/recyclerview/selection/SelectionTracker)**.

## Screenshots

`Home` panel:

<p align="center">
  <video src="/images/home.mp4" width="25%" autoplay loop/>
</p>

`History` panel:

<p align="center">
  <video src="/images/history.mp4" width="25%" autoplay loop/>
</p>

`Payment` panel:

<p align="center">
  <video src="/images/payment.mp4" width="25%" autoplay loop/>
</p>

`Shop-window` panel:

<p align="center">
  <video src="/images/shopwindow.mp4" width="25%" autoplay loop/>
</p>