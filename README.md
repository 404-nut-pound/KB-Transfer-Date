# KB-Transfer-Date

## 개발 환경

- Java - 8
- Spring Boot - 2.7.5
- Gradle - 7.5.1
- Querydsl - 5.0.0
- IDE - VSCode
  - Java 코드 포맷 - [Java Prettier Formatter](https://marketplace.visualstudio.com/items?itemName=mwpb.java-prettier-formatter)
- DB
  - MariaDB
    - Host - 125.138.183.144
    - Port - 3306
    - Database - kb_db
    - Username - kb
    - Password - kb!!pass
  - MongoDB - 사내 VPN 연결 필요
    - Host - 125.138.183.62
    - Port - 40022
    - Authentication-database - admin
    - Username - admin
    - Password - elqtlrmsjfadmin1!
    - Database - deep_signal_dataextraction

## 빌드 방법

- 아래 명령어 실행 후 `프로젝트 디렉토리/build/libs/kb-transfer-date-SNAPSHOT.jar` 확인

  ```sh
  chmod u+x gradlew
  ./gradlew bootJar # 기본 버전 'SNAPSHOT'
  ```

- 버전을 입력하려면 다음 명렁어 추가 후 `프로젝트 디렉토리/build/libs/kb-transfer-date-'버전 값'.jar` 확인

  ```sh
  chmod u+x gradlew
  ./gradlew bootJar -Pversion="버전 값"
  ```

## 실행 방법

- 실행 변수

  - 날짜 - yyyyMMdd 양식
  - AgentId - 숫자 6자리, 필수 값 아님

- 실행 구조

  - 입력 받은 날짜를 기준으로 그 날짜에 생성된 MongoDB 데이터를 조회해 각 기관 코드, 카테고리 코드 별로 결과 json 파일을 생성
  - AgentId를 입력했을 경우 해당 AgentId에 대해서만 동작
  - 실행 환경의 가용한 Thread 수에 맞춰 다중 스레드로 동작
  - 출력 디렉토리 예시

    ```sh
    # /data/kb_guest/makeup/file/json/yyyyMM/ddHHmm/siteCode/categoryCode/json/
    /data/kb_guest/makeup/file/json/202210/120005/002/C10227/json/

    # yyyy-MM-dd_HH-mm_siteCode_categoryCode_result.json
    2022-10-21_00-05_002_C10227_result.json
    ```

## 유의 사항

- VSCode에서 개발 시 IDE와 Querydsl의 호환 문제로 classpath 오류가 발생할 수 있음.
- 다음 링크 참조
  - [Inflearn Q&A](https://www.inflearn.com/questions/35226)
- 혹은 다음과 같이 조치

  - VSCode 설정 - java.import.generatesMetadataFilesAtProjectRoot 검색 - 검색 결과 체크
  - VSCode 설정 - file exclude 검색 - 패턴 중 `**/.classpath` 삭제
  - 프로젝트 디렉토리에 나타난 `.classpath` 파일 중 다음 내용 수정 후 저장

    ```xml
    <classpathentry kind="src" output="bin/querydsl" path="build/generated/querydsl">
      <attributes>
        <attribute name="gradle_scope" value="main" /> <!--querydsl을 main으로 변경-->
        <attribute name="gradle_used_by_scope" value="main" /> <!--querydsl을 main으로 변경-->
      </attributes>
    </classpathentry>
    ```

  - 위 방법은 `Gradle Reload`를 실행하거나 `build.gradle` 파일을 저장할 때마다 수행해야 함
