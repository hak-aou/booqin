package fr.uge.booqin.domain.model.cart;

import java.util.Optional;

public interface TransactionWorkflowResolver {

    Optional<TransactionStepType> nextAsBorrower(TransactionStepType currentStep);
    Optional<TransactionStepType> nextAsLender(TransactionStepType currentStep);

    static TransactionWorkflowResolver defaultResolver() {
        return new TransactionWorkflowResolver() {

            @Override
            public Optional<TransactionStepType> nextAsBorrower(TransactionStepType currentStep) {
                var nextStep = switch (currentStep) {
                        case SENT -> TransactionStepType.RECEIVED;
                        case RECEIVED -> TransactionStepType.RETURNED;
                        default -> null;
                    };
                return Optional.ofNullable(nextStep);
            }

            @Override
            public Optional<TransactionStepType> nextAsLender(TransactionStepType currentStep) {
                var nextstep = switch (currentStep) {
                        case TO_BE_SENT -> TransactionStepType.SENT;
                        case RETURNED -> TransactionStepType.RECEIVED_BACK;
                        default -> null;
                    };
                return Optional.ofNullable(nextstep);
            }
        };
    }
}
