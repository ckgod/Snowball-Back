# Snowball

## 1. 프로젝트 개요
**Snowball**은 **Kotlin**과 **Ktor** 프레임워크를 기반으로 구축된 웹 API 서버입니다.
**한국투자증권(KIS) Open API**와 연동하여 실시간 주식 시장 데이터를 조회하고 관리하는 기능을 제공합니다.

이 프로젝트는 **클린 아키텍처(Clean Architecture)** 원칙을 따르며, 도메인 로직과 인프라스트럭처의 관심사를 분리하기 위해 멀티 모듈 구조로 설계되었습니다.

## 2. 아키텍처 및 모듈 구조
프로젝트는 크게 4개의 Gradle 모듈로 구성되어 있습니다:

*   **`application`**: 웹 서버의 진입점입니다. Ktor 서버 설정, API 라우팅, 의존성 주입을 담당합니다.
*   **`domain`**: 비즈니스 로직의 핵심 계층입니다. 외부 프레임워크에 의존하지 않는 순수 Kotlin 코드로 구성되며, 도메인 엔티티와 리포지토리 인터페이스를 정의합니다.
*   **`infrastructure`**: 도메인 계층의 인터페이스를 구현하고 외부 시스템(KIS API, 데이터베이스 등)과의 통신을 담당합니다.
*   **`common`**: 프로젝트 전반에서 사용되는 공통 설정 및 유틸리티를 포함합니다.

## 3. 주요 기술 스택
*   **Language**: Kotlin
*   **Web Framework**: Ktor (Server & Client)
*   **Build System**: Gradle (Kotlin DSL)
*   **Database/ORM**: JetBrains Exposed
*   **Serialization**: kotlinx.serialization
*   **External API**: 한국투자증권(KIS) Open API

## 4. 빌드 및 실행
프로젝트 루트에서 다음 명령어들을 사용하여 빌드 및 실행할 수 있습니다:

*   **서버 실행**:
    ```bash
    ./gradlew :application:run
    ```
*   **프로젝트 빌드**:
    ```bash
    ./gradlew build
    ```
*   **테스트 실행**:
    ```bash
    ./gradlew test
    ```

## 5. 주요 기능
*   **주식 시세 조회**: 실시간 주식 현재가를 조회하는 REST API를 제공합니다.
*   **주식 종목 코드 동기화**: 주식 마스터 파일을 파싱하여 종목 코드를 동기화합니다.
*   **인증 관리**: 외부 API 사용을 위한 인증 토큰을 관리합니다.

## 6. 개발 상태
현재 **활발히 개발 진행 중**인 프로젝트입니다. 데이터베이스 저장 로직 등 일부 기능은 구현 중이거나 변경될 수 있습니다.