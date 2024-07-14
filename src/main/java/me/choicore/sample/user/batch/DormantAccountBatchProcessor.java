package me.choicore.sample.user.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.choicore.sample.user.domain.UserEntity;
import me.choicore.sample.user.domain.UserJpaRepository;
import me.choicore.sample.user.domain.UserStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantAccountBatchProcessor {
    private final UserJpaRepository userJpaRepository;
    private final TransactionTemplate transactionTemplate;

    public void process() {
        boolean isContinue = true;
        while (isContinue) {
            isContinue = TRUE.equals(
                    transactionTemplate.execute(status -> {
                                var candidates = findDormantAccountCandidates(getDormantThreshold());
                                if (candidates.isEmpty()) {
                                    return false;
                                }

                                userJpaRepository.updateStatusByIds(UserStatus.DORMANT, getCandidateIds(candidates));
                                return true;
                            }
                    )
            );
        }
    }

    private Iterable<Long> getCandidateIds(List<UserEntity> candidates) {
        return candidates.stream().map(UserEntity::getId).collect(Collectors.toUnmodifiableSet());
    }

    private Instant getDormantThreshold() {
        return Instant.now().minus(365, ChronoUnit.DAYS);
    }

    private List<UserEntity> findDormantAccountCandidates(Instant threshold) {
        return userJpaRepository.findTop10ByLastLoggedInBeforeAndUserStatusEqualsOrderByLastLoggedInDesc(
                threshold, UserStatus.ACTIVE);
    }
}
