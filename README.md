![Expense Manager Android](docs/images/splash.png)

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.naveenapps.expensemanager" target="_blank">
    <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="320" />
  </a>
</p>

Expense Manager
==================
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Build](https://github.com/nkuppan/expensemanager/actions/workflows/build.yml/badge.svg)](https://github.com/nkuppan/expensemanager/actions/workflows/build.yml)

**Expense Manager** is a Work In-progress Android app. Which is used to manage your finances. It's fully offline based application.

**Features**
* Users can create a multiple accounts to group their transactions under the specific accounts
* Wants to create a budgets for your month and more customisable options are available for budgets
* Analyse and know the trends of your transactions on a day, week and monthly wise.
* Interactive category grouping pie chart to understand where you mostly spending your money.
* Multiple currency switching in UI(Currency conversion is yet to support)


|                 Home Screen                 |              Analysis Screen               |             Transaction Screen              |            Category Chart Screen               |
|:-------------------------------------------:|:------------------------------------------:|:-------------------------------------------:|:-------------------------------------------:|
|   <img src="docs/images/image1.png" width="250px"/>    | <img src="docs/images/image2.png" width="250px"/> | <img src="docs/images/image3.png" width="250px"/> | <img src="docs/images/image4.png" width="250px"/> |

|                  Transaction Create                   |             Account Create             |                 Budget Create                  |                 Dark Theme                  |
|:-------------------------------------------:|:------------------------------------------:|:-------------------------------------------:|:-------------------------------------------:|
| <img src="docs/images/image5.png" width="250px"/> | <img src="docs/images/image6.png" width="250px"/>  |    <img src="docs/images/image7.png" width="250px"/>    |    <img src="docs/images/image8.png" width="250px"/>    | 


## Android development

Rugby Ranker attempts to make use of the latest Android libraries and best practices:
* Completely written in compose [Jetpack Compose](https://developer.android.com/jetpack/compose)
* Entirely written in [Kotlin](https://kotlinlang.org/) (including [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html)) with [Spotless](https://github.com/diffplug/spotless) for code style
* Makes use of [Android Jetpack](https://developer.android.com/jetpack/):
  * [Architecture Components](https://developer.android.com/jetpack/arch/) including **ViewModel**,**Room**, **Navigation**, **WorkManager** and **DataStore**
  * [Android KTX](https://developer.android.com/kotlin/ktx) for more fluent use of Android APIs
* [Hilt](https://dagger.dev/hilt/) for dependency injection
* Designed and built using Material 3 Design [components](https://m3.material.io/) and [theming](https://m3.material.io/theme-builder)
* Full [dark theme](https://m3.material.io/styles/color/choosing-a-scheme) support

## Contributions

Please feel free to file an issue for errors, suggestions or feature requests. Pull requests are also encouraged.

# License

```xml
Copyright 2023 Naveen Kumar Kuppan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
