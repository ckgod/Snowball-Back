# 1. 자바 17 환경 준비
FROM openjdk:17-jdk-slim

# 2. 작업 폴더 생성
WORKDIR /app

# 3. 시간대 설정 (KST) - 로그 시간이 한국 시간으로 찍히게 함
ENV TZ=Asia/Seoul

# 4. 빌드된 Jar 파일 복사 (이름을 app.jar로 통일)
COPY build/libs/*-all.jar app.jar

# 5. 서버 실행 (메모리 제한 옵션 추가)
# -Xmx384m: 램을 최대 384MB까지만 쓰라고 제한 (서버 다운 방지)
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]