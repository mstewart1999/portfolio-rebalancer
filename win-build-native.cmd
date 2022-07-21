
REM https://medium.com/graalvm/using-graalvm-and-native-image-on-windows-10-9954dc071311
REM https://docs.gluonhq.com/#platforms_windows

set GRAALVM_HOME=C:\tools\graalvm-svm-java17-windows-gluon-22.1.0.1-Final
set JAVA_HOME=C:\tools\graalvm-svm-java17-windows-gluon-22.1.0.1-Final
set PATH=%JAVA_HOME%\bin;%PATH%

if not defined VSCMD_VER (
  call C:\tools\MicrosoftVisualStudio\2022\Community\VC\Auxiliary\Build\vcvars64.bat
)


REM call .\gradlew nativeBuild nativePackage --stacktrace
call .\gradlew nativeBuild --stacktrace

REM editbin /SUBSYSTEM:CONSOLE .\build\gluonfx\x86_64-windows\pbalancer.exe

dir .\build\gluonfx\x86_64-windows\*.exe
