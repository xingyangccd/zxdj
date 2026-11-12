package com.demo.bff.client;

import com.demo.bff.common.BizException;
import com.demo.bff.common.ErrorCode;
import com.demo.bff.common.R;
import com.demo.bff.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClientWrapper {

    private final OrderClient orderClient;

    public OrderDTO getOrder(Long id) {

        // 最终的远程feign调用
        R<OrderDTO> result = orderClient.getOrder(id);

        // 额外添加一些其他的代码，这些代码就是装饰的作用。
        // 下游统一返回 200，这里统一校验业务码
        // 下游系统虽然返回了数据，但是上游在进行数据反序列化的可能失败，这个时候result为null
        if (result == null) {
            log.error("下游返回 null, orderId={}", id);
            throw new BizException(ErrorCode.DOWNSTREAM_ERROR);
        }
        if (result.getCode() != 0) {
            log.warn("下游业务异常, orderId={}, code={}, msg={}",
                    id, result.getCode(), result.getMessage());
            throw new BizException(result.getCode(), result.getMessage());
        }
        // 正常将result中的数据返回即可。
        return result.getData();
    }
}