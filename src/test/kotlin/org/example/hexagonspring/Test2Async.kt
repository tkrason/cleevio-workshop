package org.example.hexagonspring

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronizationManager

@Suppress("LoggingSimilarMessage")
@SpringBootTest
class Test2Async(@Autowired private val test2Service: Test2Service) {

    private val logger = LoggerFactory.getLogger(Test2Async::class.java)

    @Test
    fun `test async - wrong?`() {
        test2Service.doWorkAsync(number = 1)
        test2Service.doWorkAsync(number = 2)
        test2Service.doWorkAsync(number = 3)

        logger.info("Invocations done")
    }

    @Test
    fun `test async - now properly`() {
        test2Service.doWorkAsync(number = 1)
        test2Service.doWorkAsync(number = 2)
        test2Service.doWorkAsync(number = 3)

        asyncButActuallyNot()

        logger.info("Invocations done")
        Thread.sleep(1500)
    }

    @Async
    fun asyncButActuallyNot() {
        logger.info("Im on main, am I not?")
    }
}

@Service
class Test2Service {

    private val logger = LoggerFactory.getLogger(Test2Service::class.java)

    @Async
    fun doWorkAsync(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }
}
