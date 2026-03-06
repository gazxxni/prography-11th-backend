package com.prography.attendance.domain.attendance.service;

import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ExcusePolicy {

    public void toExcused(CohortMember cohortMember) {
        if (cohortMember.getExcuseCount() >= 3) {
            throw new BusinessException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
        }
        cohortMember.increaseExcuseCount();
    }

    public void fromExcused(CohortMember cohortMember) {
        cohortMember.decreaseExcuseCount();
    }
}
