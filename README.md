# 웹 동영상 컨텐츠 아카이빙 및 공유 앱 만들기
## 프로젝트 개요
* 웹의 동영상 컨텐츠의 URL을 아카이빙 하여 손쉽게 다시 볼 수 있다.
* 아키이빙 한 동영상 컨텐츠를 다른 사람에게 공유 할 수 있다.
* 아키이빙 한 동영상 컨텐츠의 특정 구간을 지정하여 쉽게 다시 볼 수 있다.
* 아카이빙 한 동영상 컨텐츠의 특정 구간을 지정하여 다른 사람에게 공유 할 수 있다.

## 사용 아키텍처
* MVC(Model-View-Controller)

## 사용 라이브러리
* [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [RxKotlin](https://github.com/ReactiveX/RxKotlin)
* [Exoplayer](https://github.com/google/ExoPlayer)
* [Firebase DynamicLink](https://github.com/firebase/)

### 1.2. 구현 목표
* 기본적인 WebView 기능들을 활용 할 수 있다.
* Animator 를 활용하여 기본적인 애니메이션을 구현 할 수 있다.
* Exoplayer 를 활용하여 동영상 플레이를 할 수 있다.
* Exoplayer 의 Custom 기능들을 활용 할 수 있다.
* DeepLink 를 구현 할 수 있다.

## 2.개발환경
* 언어 : 주 - Kotlin, 부 - Java
* MinSDK : 19

## 3.구현내용
### 3.1. 기본 기능
#### 3.1.1. 웹 브라우저
* 기본적인 웹브라우저 동작 구현
  - 새로고침, 뒤로가기(back press)
  - 주소입력 창 
  - 웹에서 동영상 재생 시 수집 버튼 생성
  - 웹 동영상 URL을 네이티브 디비에 저장
  - WebViewClient, WebChromeClient 활용
#### 3.1.2. 동영상 컨텐츠 리스트
  - 수집된 동영상 리스트를 보여준다.
  - 리스트 자체에서 동영상 플레이 가능
#### 3.1.3. 동영상 컨텐츠
  - 동영상 1개에 대한 상세 정보 및 재생
### 3.2. 추가 구현 내용
#### 3.2.1. 동영상 공유
  - 동영상 정보를 url scheme 을 통해 타인에게 공유할 수 있다.
  - 공유 받은 url scheme 정보를 이용해 앱에서 동영상을 재생 할 수 있다.
#### 3.2.2. 동영상 태그
  - 동영상의 특정 지점에 태깅을 할 수 있다.
  - 태그를 클릭하면 특정 지점으로 자동으로 넘어갈 수 있다.
  - 태그 정보와 함께 타인에게 공유할 수 있다.
#### 3.2.4. 자동재생
  - 동영상 리스트에서 자동재생이 된다. ( ex> facebook )

### 4.1. Main 화면
* 두개의 탭으로 구성
#### 4.1.1. Browser 탭
![Browser](https://github.com/jiyunn/WebVideoArchiving/blob/master/screenshot/home.jpg)
* 간단한 웹탐색 및 동영상 URL 수집을 위한 화면
##### 1. 브라우저
 - 웹뷰를 이용한 브라우저 화면 구성
 - Scroll-Up 시에 주소창 숨김 / Scroll-Down 시 주소창 노출
##### 2. 브라우저 네이게이터
 - 뒤로가기, 앞으로가기 구현
 - Scroll-Up 시에 주소창 숨김 / Scroll-Down 시 주소창 노출
##### 3. 비디오 수집
![Collect](https://github.com/Jiyunn/WebVideoArchiving/blob/master/screenshot/collect.jpg)
 - 웹브라우저에서 비디오 컨텐츠를 플레이 했을 때 버튼이 노출
 - 비디오 컨텐츠 플레이 종료시 버튼 숨김
 - 클릭 시 비디오 컨텐츠 URL 을 DB 에 저장
#### 4.1.2. VideoList 탭
 * 수집한 동영상 URL 을 플레이어 리스트 형태로 보여주는 화면
 ##### 1. 플레이어
 - 리스트에는 수집된 URL 로 플레이어가 영상을 재생
 - 현재 재생되는 컨텐츠가 없고 플레이어가 사용자에게 100% 노출 시 자동 재생 / 재생되는  플레이어가 90% 노출시 pause
 - 클릭 시 전체화면으로 넘어감
 ##### 2. 태그 
 - 사용자가 등록한 태그 리스트를 노출
 - 태그에는 태그명과 재생시점 정보를 포함
 - 각 태그 클릭 시 해당 재생 시점으로 이동
 - 롱클릭 시 공유 url scheme 클립보드에 복사
### 4.2. Video 화면
* 한개의 동영상 플레이를 위한 화면
* 진입경로 : 동영상 리스트, url scheme  
 ##### 1. 플레이어
 - 리스트에서 클릭하여 들어온 컨텐츠를 재생
 - 공유 받은 url sheme 을 통하여 들어온 영상을 재생
 ##### 2. 태그
 ![Tag](https://github.com/jiyunn/WebVideoArchiving/blob/master/screenshot/share.jpg)
 - 사용자가 등록한 태그 리스트를 노출
 - 태그에는 태그명과 재생시점 정보를 포함
 - 각 태그 클릭 시 해당 재생 시점으로 이동
 - 롱클릭 시 공유 url scheme 클립보드에 복사
 ##### 3. 태그추가
 - 클릭 시 동영상 pause 현 재생 시점 정보를 가져와서 태그 생성
 - 태그명 입력 다이얼로그 노출
