@echo off

rem 启动项目
echo 正在启动分布式系统项目...
echo ================================

rem 使用Maven运行Spring Boot应用
"apache-maven-3.8.8\bin\mvn.cmd" spring-boot:run

rem 暂停以查看输出
pause