@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  calendly-apis startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and CALENDLY_APIS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\calendly-apis-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\vertx-web-client-4.5.10.jar;%APP_HOME%\lib\vertx-auth-jwt-4.5.10.jar;%APP_HOME%\lib\vertx-service-proxy-4.5.10.jar;%APP_HOME%\lib\vertx-health-check-4.5.10.jar;%APP_HOME%\lib\vertx-web-openapi-router-4.5.10.jar;%APP_HOME%\lib\vertx-web-graphql-4.5.10.jar;%APP_HOME%\lib\vertx-web-4.5.10.jar;%APP_HOME%\lib\vertx-service-discovery-4.5.10.jar;%APP_HOME%\lib\vertx-micrometer-metrics-4.5.10.jar;%APP_HOME%\lib\vertx-jdbc-client-4.5.10.jar;%APP_HOME%\lib\vertx-config-4.5.10.jar;%APP_HOME%\lib\vertx-rx-java3-4.5.10.jar;%APP_HOME%\lib\vertx-service-factory-4.5.10.jar;%APP_HOME%\lib\vertx-pg-client-4.5.10.jar;%APP_HOME%\lib\vertx-redis-client-4.5.10.jar;%APP_HOME%\lib\vertx-circuit-breaker-4.5.10.jar;%APP_HOME%\lib\vertx-lang-kotlin-coroutines-4.5.10.jar;%APP_HOME%\lib\vertx-lang-kotlin-4.5.10.jar;%APP_HOME%\lib\firebase-admin-9.3.0.jar;%APP_HOME%\lib\google-cloud-storage-2.38.0.jar;%APP_HOME%\lib\google-cloud-firestore-3.21.1.jar;%APP_HOME%\lib\google-api-client-gson-2.4.0.jar;%APP_HOME%\lib\google-api-client-2.4.0.jar;%APP_HOME%\lib\google-auth-library-oauth2-http-1.23.0.jar;%APP_HOME%\lib\google-oauth-client-1.35.0.jar;%APP_HOME%\lib\google-http-client-gson-1.44.1.jar;%APP_HOME%\lib\gson-2.10.1.jar;%APP_HOME%\lib\vertx-mail-client-4.5.10.jar;%APP_HOME%\lib\vertx-jooq-rx-reactive-6.3.0.jar;%APP_HOME%\lib\vertx-jooq-generate-6.3.0.jar;%APP_HOME%\lib\postgresql-42.5.4.jar;%APP_HOME%\lib\vertx-jooq-rx-6.3.0.jar;%APP_HOME%\lib\vertx-jooq-shared-reactive-6.3.0.jar;%APP_HOME%\lib\jooq-codegen-3.14.9.jar;%APP_HOME%\lib\vertx-jooq-shared-6.3.0.jar;%APP_HOME%\lib\jooq-meta-3.14.9.jar;%APP_HOME%\lib\jooq-3.14.9.jar;%APP_HOME%\lib\commons-io-2.11.0.jar;%APP_HOME%\lib\slf4j-simple-2.0.7.jar;%APP_HOME%\lib\logback-classic-1.4.5.jar;%APP_HOME%\lib\graphql-java-20.7.jar;%APP_HOME%\lib\java-dataloader-3.2.0.jar;%APP_HOME%\lib\slf4j-api-2.0.13.jar;%APP_HOME%\lib\vertx-sql-client-4.5.10.jar;%APP_HOME%\lib\vertx-auth-common-4.5.10.jar;%APP_HOME%\lib\vertx-rx-java3-gen-4.5.10.jar;%APP_HOME%\lib\vertx-rx-java2-4.5.10.jar;%APP_HOME%\lib\vertx-rx-java2-gen-4.5.10.jar;%APP_HOME%\lib\vertx-rx-gen-4.5.10.jar;%APP_HOME%\lib\vertx-web-common-4.5.10.jar;%APP_HOME%\lib\vertx-bridge-common-4.5.10.jar;%APP_HOME%\lib\vertx-uri-template-4.5.10.jar;%APP_HOME%\lib\vertx-openapi-4.5.10.jar;%APP_HOME%\lib\vertx-json-schema-4.5.10.jar;%APP_HOME%\lib\vertx-core-4.5.10.jar;%APP_HOME%\lib\vertx-codegen-4.5.10.jar;%APP_HOME%\lib\jackson-databind-2.17.0.jar;%APP_HOME%\lib\jackson-core-2.17.0.jar;%APP_HOME%\lib\jackson-annotations-2.17.0.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.17.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-2.0.20.jar;%APP_HOME%\lib\kotlinx-coroutines-core-jvm-1.6.4.jar;%APP_HOME%\lib\kotlin-stdlib-2.0.20.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-2.0.20.jar;%APP_HOME%\lib\client-2.1.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.111.Final.jar;%APP_HOME%\lib\netty-codec-http2-4.1.111.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.111.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.1.111.Final.jar;%APP_HOME%\lib\netty-handler-4.1.111.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.111.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.1.111.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.1.111.Final.jar;%APP_HOME%\lib\netty-codec-4.1.111.Final.jar;%APP_HOME%\lib\netty-transport-4.1.111.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.111.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.111.Final.jar;%APP_HOME%\lib\netty-common-4.1.111.Final.jar;%APP_HOME%\lib\google-http-client-apache-v2-1.44.1.jar;%APP_HOME%\lib\google-http-client-1.44.1.jar;%APP_HOME%\lib\proto-google-cloud-firestore-bundle-v1-3.21.1.jar;%APP_HOME%\lib\api-common-2.31.0.jar;%APP_HOME%\lib\opencensus-contrib-http-util-0.31.1.jar;%APP_HOME%\lib\guava-33.1.0-jre.jar;%APP_HOME%\lib\snakeyaml-2.0.jar;%APP_HOME%\lib\proto-google-cloud-firestore-v1-3.21.1.jar;%APP_HOME%\lib\checker-qual-3.42.0.jar;%APP_HOME%\lib\logback-core-1.4.5.jar;%APP_HOME%\lib\common-2.1.jar;%APP_HOME%\lib\c3p0-0.9.5.5.jar;%APP_HOME%\lib\micrometer-core-1.12.4.jar;%APP_HOME%\lib\HdrHistogram-2.1.12.jar;%APP_HOME%\lib\rxjava-3.0.13.jar;%APP_HOME%\lib\rxjava-2.2.21.jar;%APP_HOME%\lib\reactive-streams-1.0.3.jar;%APP_HOME%\lib\jaxb-api-2.3.1.jar;%APP_HOME%\lib\httpclient-4.5.14.jar;%APP_HOME%\lib\commons-codec-1.16.1.jar;%APP_HOME%\lib\google-auth-library-credentials-1.23.0.jar;%APP_HOME%\lib\httpcore-4.4.16.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\error_prone_annotations-2.26.1.jar;%APP_HOME%\lib\j2objc-annotations-3.0.0.jar;%APP_HOME%\lib\opencensus-api-0.31.1.jar;%APP_HOME%\lib\grpc-context-1.62.2.jar;%APP_HOME%\lib\auto-value-annotations-1.10.4.jar;%APP_HOME%\lib\javax.annotation-api-1.3.2.jar;%APP_HOME%\lib\failureaccess-1.0.2.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\google-http-client-jackson2-1.44.1.jar;%APP_HOME%\lib\google-api-services-storage-v1-rev20240319-2.0.0.jar;%APP_HOME%\lib\google-cloud-core-2.38.0.jar;%APP_HOME%\lib\google-cloud-core-http-2.38.0.jar;%APP_HOME%\lib\google-http-client-appengine-1.44.1.jar;%APP_HOME%\lib\gax-httpjson-2.48.0.jar;%APP_HOME%\lib\google-cloud-core-grpc-2.38.0.jar;%APP_HOME%\lib\gax-2.48.0.jar;%APP_HOME%\lib\gax-grpc-2.48.0.jar;%APP_HOME%\lib\grpc-inprocess-1.62.2.jar;%APP_HOME%\lib\grpc-alts-1.62.2.jar;%APP_HOME%\lib\grpc-grpclb-1.62.2.jar;%APP_HOME%\lib\conscrypt-openjdk-uber-2.5.2.jar;%APP_HOME%\lib\grpc-auth-1.62.2.jar;%APP_HOME%\lib\proto-google-iam-v1-1.34.0.jar;%APP_HOME%\lib\protobuf-java-3.25.3.jar;%APP_HOME%\lib\protobuf-java-util-3.25.3.jar;%APP_HOME%\lib\grpc-core-1.62.2.jar;%APP_HOME%\lib\annotations-4.1.1.4.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.23.jar;%APP_HOME%\lib\perfmark-api-0.27.0.jar;%APP_HOME%\lib\grpc-protobuf-1.62.2.jar;%APP_HOME%\lib\grpc-protobuf-lite-1.62.2.jar;%APP_HOME%\lib\proto-google-common-protos-2.39.0.jar;%APP_HOME%\lib\threetenbp-1.6.9.jar;%APP_HOME%\lib\proto-google-cloud-storage-v2-2.38.0-alpha.jar;%APP_HOME%\lib\grpc-google-cloud-storage-v2-2.38.0-alpha.jar;%APP_HOME%\lib\gapic-google-cloud-storage-v2-2.38.0-alpha.jar;%APP_HOME%\lib\grpc-api-1.62.2.jar;%APP_HOME%\lib\grpc-netty-shaded-1.62.2.jar;%APP_HOME%\lib\grpc-util-1.62.2.jar;%APP_HOME%\lib\grpc-stub-1.62.2.jar;%APP_HOME%\lib\grpc-googleapis-1.62.2.jar;%APP_HOME%\lib\grpc-xds-1.62.2.jar;%APP_HOME%\lib\opencensus-proto-0.2.0.jar;%APP_HOME%\lib\grpc-services-1.62.2.jar;%APP_HOME%\lib\re2j-1.7.jar;%APP_HOME%\lib\grpc-rls-1.62.2.jar;%APP_HOME%\lib\opencensus-contrib-grpc-util-0.31.1.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\saslprep-1.1.jar;%APP_HOME%\lib\mchange-commons-java-0.2.19.jar;%APP_HOME%\lib\micrometer-observation-1.12.4.jar;%APP_HOME%\lib\micrometer-commons-1.12.4.jar;%APP_HOME%\lib\LatencyUtils-2.0.3.jar;%APP_HOME%\lib\javax.activation-api-1.2.0.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\stringprep-1.1.jar;%APP_HOME%\lib\byte-buddy-1.14.9.jar


@rem Execute calendly-apis
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %CALENDLY_APIS_OPTS%  -classpath "%CLASSPATH%" io.vertx.core.Launcher %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable CALENDLY_APIS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%CALENDLY_APIS_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
