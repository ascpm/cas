# Project
A CAS Example Project

# Tomcat Setting
```
* 키 생성
* ${key.path} : 키 생성 패스
keytool -genkey -alias tomcat -keyalg RSA -keystore ${key.path}/tomcat

* server.xml 설정
* ${key.path} : 키 생성 패스
* keystorePass : 키 생성시 입력했던 패스워드
<!-- Define a SSL Coyote HTTP/1.1 Connector on port 8443 -->
<Connector
   protocol="org.apache.coyote.http11.Http11NioProtocol"
   port="8443" maxThreads="200"
   scheme="https" secure="true" SSLEnabled="true"
   keystoreFile="${key.path}/tomcat" keystorePass="tomcat"
   clientAuth="false" sslProtocol="TLS"/>
```

# JVM Setting
```
* 액티브 프로파일 설정 (local, development, production)
-Dspring.profiles.acvite=local

* 로깅 패스 설정
* ${project.path} : CAS 프로젝트 패스로 설정
-Dlogging.path=${project.path}/logging
```

# Database Setting (H2)
```
* 데이터베이스 파일 위치 설정
* src > main > resources > application-{local|development|production}.yml
* cas.jdbc.query[0].url 항목 수정
- ex) url: jdbc:h2:~/Databases/h2/cas;AUTO_SERVER=TRUE


* CAS Application 실행 > 아이디, 패스워드 입력
- 아이디, 패스워드를 입력하여 한번이라도 실행해야 디폰트 파일디비가 생성됨.

* etc > doc > database > schema.sql 실행 필요
```
