package com.ssafy.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NoHome Backend의 실행 진입점이다. 이 package 아래의 Controller, service, adapter와 설정을
 * component scan하고 Spring Boot 자동 설정으로 HTTP 서버와 DB 연결을 구성한다.
 */
@SpringBootApplication
public class HomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeApplication.class, args);
    }
}
