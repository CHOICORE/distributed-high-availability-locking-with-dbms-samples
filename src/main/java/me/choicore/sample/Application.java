package me.choicore.sample;

import lombok.extern.slf4j.Slf4j;
import me.choicore.sample.user.batch.DormantAccountBatchProcessor;
import me.choicore.sample.user.domain.UserEntity;
import me.choicore.sample.user.domain.UserJpaRepository;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableAsync
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener
    public void onApplicationReadyEvent(ApplicationReadyEvent event) throws InterruptedException {
        ConfigurableApplicationContext context = event.getApplicationContext();

        UserJpaRepository userJpaRepository = context.getBean(UserJpaRepository.class);

        int numberOfSize = 50;

        Faker faker = new Faker(Locale.KOREA);

        Clock clock = Clock.fixed(Instant.now().minus(365, ChronoUnit.DAYS), ZoneOffset.UTC);

        for (int i = 0; i < numberOfSize; i++) {
            UserEntity userEntity = new UserEntity(
                    faker.name().fullName(),
                    faker.internet().emailAddress(),
                    faker.phoneNumber().cellPhone(),
                    clock
            );
            userJpaRepository.save(userEntity);
        }

        DormantAccountBatchProcessor bean = context.getBean(DormantAccountBatchProcessor.class);
        bean.process();
        TimeUnit.SECONDS.sleep(5);
        userJpaRepository.findAll().forEach(userEntity -> log.info("User: {}", userEntity));
    }
}