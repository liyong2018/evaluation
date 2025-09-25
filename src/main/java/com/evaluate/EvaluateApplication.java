package com.evaluate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 减灾能力评估系统启动类
 * 
 * @author System
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.evaluate.mapper")
public class EvaluateApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvaluateApplication.class, args);
        System.out.println("\n" +
                "  ____  _                 _              ____          _            _   _             \n" +
                " |  _ \\(_)___  __ _ ___| |_ ___ _ __  |  _ \\ ___  __| |_   _  ___| |_(_) ___  _ __  \n" +
                " | | | | / __|/ _` / __| __/ _ \\ '__| | |_) / _ \\/ _` | | | |/ __| __| |/ _ \\| '_ \\ \n" +
                " | |_| | \\__ \\ (_| \\__ \\ ||  __/ |    |  _ <  __/ (_| | |_| | (__| |_| | (_) | | | |\n" +
                " |____/|_|___/\\__,_|___/\\__\\___|_|    |_| \\_\\___|\\__,_|\\__,_|\\___|\\__|_|\\___/|_| |_|\n" +
                "                                                                                    \n" +
                " :: 减灾能力评估系统启动成功 ::                                                        \n");
    }
}