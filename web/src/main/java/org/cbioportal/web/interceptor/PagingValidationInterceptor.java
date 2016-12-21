package org.cbioportal.web.interceptor;

import org.cbioportal.web.exception.*;
import org.cbioportal.web.parameter.PagingConstants;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PagingValidationInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String pageSize = request.getParameter("pageSize");
        String pageNumber = request.getParameter("pageNumber");

        if (pageSize != null && !pageSize.isEmpty()) {

            try {
                int intPageSize = Integer.parseInt(pageSize);

                if (intPageSize > PagingConstants.MAX_PAGE_SIZE) {
                    throw new PageSizeTooBigException(intPageSize);
                } else if (intPageSize < PagingConstants.MIN_PAGE_SIZE) {
                    throw new PageSizeTooSmallException(intPageSize);
                }

            } catch (NumberFormatException ex) {
                throw new PageSizeInvalidFormatException(pageSize);
            }
        }

        if (pageNumber != null && !pageNumber.isEmpty()) {

            try {
                int intPageNumber = Integer.parseInt(pageNumber);

                if (intPageNumber < PagingConstants.MIN_PAGE_NUMBER) {
                    throw new PageNumberTooSmallException(intPageNumber);
                }
            } catch (NumberFormatException ex) {
                throw new PageNumberInvalidFormatException(pageNumber);
            }
        }

        return true;
    }
}
