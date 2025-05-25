package fr.uge.booqin.app.service;

import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.dto.pagination.RequestWithPagination;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.function.Function;
import java.util.function.Supplier;


public interface ServiceUtils {

    static void optimisticRetry(Runnable supplier){
        var retry=true;
        while(retry) {
            retry=false;
            try {
                supplier.run();
            } catch (ObjectOptimisticLockingFailureException e) {
                retry=true;
            }
        }
    }

    static <T> T optimisticRetry(Supplier<T> supplier){
        var retry=true;
        while(retry) {
            retry=false;
            try {
                return supplier.get();
            } catch (ObjectOptimisticLockingFailureException e) {
                retry=true;
            }
        }
        throw new Error("Unreachable code reached");
    }

    static void checkRequest(Validator validator, Object request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new TheirFaultException("in request",
                    violations.stream().map(violation -> violation.getPropertyPath() + " " + violation.getMessage()).toList());
        }
    }

    static PageRequest getSpringPageRequest(Validator validator, RequestWithPagination request) {
        ServiceUtils.checkRequest(validator, request);
        return getSpringPageRequest(validator, request.pageRequest());
    }

    static PageRequest getSpringPageRequest(Validator validator, fr.uge.booqin.app.dto.pagination.PageRequest request) {
        ServiceUtils.checkRequest(validator, request);
        var offset = request.offset();
        var limit = request.limit();
        int pageNumber = offset / limit;
        return PageRequest.of(pageNumber, limit);
    }

    static <T> PaginatedResult<T> paginatedRequest(Validator validator, RequestWithPagination request, Function<PageRequest, Page<T>> requestProcessor) {
        return paginatedRequest(
                validator,
                request.pageRequest(),
                requestProcessor
        );
    }

    static <T> PaginatedResult<T> paginatedRequest(Validator validator, fr.uge.booqin.app.dto.pagination.PageRequest request, Function<PageRequest, Page<T>> requestProcessor) {
        var page = ServiceUtils.getSpringPageRequest(validator, request);
        var pageResult = requestProcessor.apply(page);
        var content = pageResult.getContent();
        return new PaginatedResult<>(
                content,
                pageResult.getTotalElements(),
                content.size(),
                request.offset(),
                request.limit());
    }
}

