package org.example.hexagonspring

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronizationManager


@Suppress("LoggingSimilarMessage")
@SpringBootTest
class Test0TransactionalBlockingConnectionPool(@Autowired private val test0Service: Test0Service) {

    private val logger = LoggerFactory.getLogger(Test1TransactionalBlockingConnectionPool::class.java)

    /**
     *  Prerequisites: Check connection pool is 10
     */
    @Test
    fun `test transactional`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        test0Service.doWorkTransactional(number = 1)
        test0Service.doWorkTransactional(number = 2)
        test0Service.doWorkTransactional(number = 3)

        logger.info("Test done")
    }

    /**
     *  Prerequisites: Check connection pool is 10
     */
    @Test
    fun `test transactional function from inside this class - check in debug the difference between those two`() {
        logger.info("Is some tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")

        doWorkTransactionFromWithinClass(number = 1)
        doWorkTransactionFromWithinClass(number = 2)
        doWorkTransactionFromWithinClass(number = 3)

        logger.info("Test done")
    }

    @Transactional
    fun doWorkTransactionFromWithinClass(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }
}

@Service
class Test0Service {

    private val logger = LoggerFactory.getLogger(Test0Service::class.java)

    @Transactional
    fun doWorkTransactional(number: Int) {
        logger.info("Number $number is starting... Tx active: ${TransactionSynchronizationManager.isActualTransactionActive()}")
        Thread.sleep(1000)
        logger.info("Number $number done!")
    }
}
