# File Manager Pro

**File Manager Pro**는 JavaFX 기반의 로컬 파일 관리 데스크탑 애플리케이션입니다. 파일 정리, 미사용 파일 삭제, 고급 검색, 사용 이력 시각화, 파일 암호화/복호화, 도움말, 개발자 소개 기능을 제공합니다.

## 기능 요약

1. **확장자별 파일 정리**

    * 선택한 폴더 내 파일을 확장자별로 자동 분류
    * 실행 후 바로 복원(취소) 기능 지원
2. **미사용 파일 삭제**

    * 지정 기간(일) 동안 사용하지 않은 파일 검색 및 한 번에 삭제
3. **디스크 용량 확인**

    * 프로그램 시작 시 루트 드라이브의 사용/전체 용량 표시
4. **고급 검색**

    * 이름 키워드, 크기(KB), 수정일 범위 필터를 통한 파일 검색
5. **사용 이력 시각화**

    * 최근 N일간 파일 수정 건수를 BarChart로 시각화
6. **파일 암호화/복호화**

    * AES-256/CBC 방식, PBKDF2 키 파생
    * 진행률 및 로그 표시
7. **도움말 & 개발자 소개**

    * 내장된 도움말과 개발자 경력 소개 화면

## 설치 및 실행

### 요구 사항

* **Java Development Kit (JDK)**: 버전 17 이상
* **JavaFX SDK**: 버전 24

### 실행 파일 형태

* Fat JAR(`MyFileManager.jar`): 모든 라이브러리를 포함한 실행 파일

### 실행 방법

```bash
# JavaFX SDK lib 경로를 module-path에 지정해야 합니다.
java \
  --module-path /path/to/javafx-sdk-24/lib \
  --add-modules javafx.controls,javafx.fxml \
  -jar MyFileManager.jar
```

> **Tip**: IntelliJ IDEA에서 Artifacts → Build를 통해 Fat JAR을 생성할 수 있습니다.

## 프로젝트 구조

```
MyFileManager/
├ src/                     // Java 소스 코드
│  └ filemanager/
│     ├ controller/        // FXML 컨트롤러
│     └ model/             // 데이터 모델 클래스
├ resources/               // FXML, 텍스트 리소스(help.txt, about.txt)
└ README.md                // 이 파일
```

##  개발 환경

* **IDE**: IntelliJ IDEA
* **JDK**: OpenJDK 17
* **JavaFX**: SDK 24

## ✉ 문의

* 이메일: gangminchan5@gmail.com


---

감사합니다!
