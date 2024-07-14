package me.choicore.sample.user.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    @QueryHints(
            {
                    @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"),
            }
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UserEntity> findTop10ByLastLoggedInBeforeAndUserStatusEqualsOrderByLastLoggedInDesc(Instant lastLoggedIn, UserStatus userStatus);

    @Modifying
    @Query("update UserEntity u set u.userStatus = :userStatus where u.id in :ids")
    void updateStatusByIds(@Param("userStatus") UserStatus userStatus, @Param("ids") Iterable<Long> ids);
}
