package com.coop.payments.messaging;

import com.coop.payments.config.RabbitConfig;
import com.coop.payments.util.MaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransferEventConsumer {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handle(TransferCompletedEvent event) {
        log.info("NOTIFICATION [mock SMS] ref={} {} {} moved from {} to {}",
                event.reference(), event.currency(), event.amount(),
                MaskUtil.mask(event.fromAccount()), MaskUtil.mask(event.toAccount()));
    }
}
