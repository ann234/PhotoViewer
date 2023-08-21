# PhotoViewer
<img src="https://github.com/ann234/PhotoViewer/assets/19405117/43d2fce6-c345-4944-b590-a1a4ece88563" width="50%" height="50%" alt="thumbnail">

## 프로젝트 목표
- RecyclerView를 효율적으로 활용해본다
- Hilt를 활용해 DI를 적용해본다
- Unit test부터 Android test까지 테스트코드를 작성해본다

## 빌드 환경
### Android Studio
- Android Studio Giraffe | 2022.3.1
- Build #AI-223.8836.35.2231.10406996, built on June 29, 2023
### Android SDK
- Android SDK Build-Tools 34
- Android SDK Platform-Tools 34.0.4
### Java
- JetBrains Runtime version 17.0.6
### 기타
- Android Gradle Plugin 8.1.0
- Kotlin 1.9.0

## 프로젝트 구성
프로젝트는 메인 소스코드와 두 개의 테스트(Unit, Android) 코드로 구성됩니다.
```bash
├── androidTest
├── main
└── test
```

### 1. 메인 프로젝트
프로젝트 소스코드는 클린 아키텍처의 가장 기본적인 구조를 본따 구성했습니다.
```bash
├── PhotoViewerApplication.kt
├── data
│   ├── entity
│   ├── module
│   └── network
├── domain
│   ├── model
│   └── repository
└── presentation
    ├── view
    └── viewmodel
```

#### 1.1. Data
```bash
├── PhotoViewerApplication.kt
├── data
│   ├── entity
│   │   └── PhotoDataEntity.kt
│   ├── module
│   │   └── NetworkModule.kt
│   └── network
│       └── PhotoService.kt
```
Data에는 서버로부터 데이터를 요청하고 받기 위해 필요한 함수 및 프로퍼티로 구성된 `PhotoService` 서비스와, 서버 데이터를 본따 만든 entity가 있습니다.

#### 1.2 Domain
```bash
├── domain
│   ├── model
│   │   └── PhotoDataModel.kt
│   └── repository
│       └── PhotoRepository.kt
```
Domain에는 Data 레이어에서 받은 데이터를 앱에서 사용할 수 있도록 가공하기 위한 코드가 있습니다. 
<br>
Entity를 앱에서 사용할 형식으로 변경한 model과, 서버에 데이터를 요청하고 model로 변환해 전달하거나, 에러가 발생할 경우 적절히 처리하는 코드가 포함된 `PhotoRepository` 리포지터리가 있습니다.

#### 1.3 Presentation
```bash
└── presentation
    ├── view
    │   ├── activities
    │   ├── adapters
    │   └── component
    └── viewmodel
        └── MainViewModel.kt
```
Presentation에는 View에 필요한 여러 컴포넌트들이 있고, ViewModel에는 UI 비지니스 로직이 구현돼있는 ViewModel이 있습니다.

### 2. 유닛 테스트
```bash
└── java.com.alcherainc.app.photoviewer
    └── PhotoServiceTest.kt
```
`PhotoService`에서 서버로부터 데이터를 읽어와 성공/실패하는 시나리오에 대해 테스트 합니다. 테스트는 더미데이터 및 [mock-android](https://github.com/mockito/mockito-kotlin)를 사용해 진행했습니다. <br>

### 3. 안드로이드 테스트
```bash
└── java.com.alcherainc.app.photoviewer
	├── CustomTestRunner.kt
	└── mainactivity
		├── BaseTest.kt
		├── SortButtonTest.kt
		├── ClearButtonTest.kt
		└── readbutton
			├── RecyclerViewUpdateTest.kt
			└── ThrottleTest.kt
```
안드로이드 테스트에서는 각 기능들을 instrumented test를 사용해 테스트를 진행합니다.
테스트에는 [mockk](https://github.com/mockk/mockk) 및 [espresso](https://developer.android.com/training/testing/espresso?hl=ko)를 사용했습니다.
- `BaseTest.kt`
  - `MainActivity`를 inflate한 후 기본 UI가 올바르게 구성돼있는지 테스트합니다.
- ReadButton
  - `RecyclerViewUpdateTest.kt`
	- Read 버튼을 클릭 후 데이터를 불러온 뒤, `RecyclerView`가 올바르게 업데이트 되는지 테스트합니다. 
  - `ThrottleTest.kt`
	- Read 버튼을 짧은 주기로 여러번 클릭하여 throttle 기능이 정상 동작하는지 테스트합니다.
- `ClearButtonTest.kt`
  - Clear 버튼을 클릭할 시. 데이터 삭제, `RecyclerView` 업데이트, Read 버튼 일시. 비활성화 등의 기능이 정상. 동작하는지 테스트합니다.
- `SortButtonTest.kt`
  - 정렬되지 않은 더미데이터로 `RecyclerView`를 채운 후, Sort 버튼을 클릭 시 더미데이터의 정렬 순서와 `RecyclerView`의 아이템 순서를 비교하여 올바르게 정렬됐는지 테스트합니다.


## 기능 목록

### 1. 데이터 불러오기
- Read 버튼을 누르면, 서버에서 데이터를 받아 저장 후 RecyclerView를 사용해 보여줍니다.<br>
- Throttle을 적용해 버튼 클릭 후 0.5초 안에 발생한 복수 클릭은 무시합니다.<br>
![readbutton](https://github.com/ann234/PhotoViewer/assets/19405117/adbdcbba-68e4-43e5-b6cb-fb728685a6f3)

### 2. 데이터 삭제
- Clear 버튼을 누르면, 그 동안 불러왔던 데이터를 모두 삭제한 후 RecyclerView도 업데이트 합니다.<br>
- Read 버튼을 3초간 비활성화 합니다.<br>
- 만약 데이터를 불러오는 도중이었다면, 해당 작업을 중단합니다.<br>
![clearbutton](https://github.com/ann234/PhotoViewer/assets/19405117/a20e6317-1fd2-459d-a99e-2787b4230b04)

### 3. 데이터 정렬하기
- Sort 버튼을 누르면, 현재 저장된 데이터들을 제목 기준 사전순서로 오름차순으로 정렬합니다.<br>
![sortbutton](https://github.com/ann234/PhotoViewer/assets/19405117/99bab083-6b09-4fb3-b52e-2e115871401e)
